import java.sql.*;
import java.util.*;

public class check_integrity {
    static final String URL = "jdbc:mysql://10.0.181.204:13306/bs_exp_vue?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai";
    static final String USER = "root";
    static final String PASS = "ZHTKFc5ta7tPTeyx";

    static final String[] REQUIRED = {
        "mb_task", "mb_task_submission", "mb_work", "mb_work_file",
        "mb_quiz_record", "mb_quiz_daily", "mb_parent_child"
    };

    static final String[] FEATURE = {
        "mb_badge_def", "mb_badge_progress", "mb_comment", "mb_user_reaction"
    };

    static final String[] FORBIDDEN = {"mb_user_history", "mb_notice_read", "mb_user_settings"};

    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection c = DriverManager.getConnection(URL, USER, PASS)) {
            System.out.println("=== DATABASE INTEGRITY CHECK: bs_exp_vue ===\n");
            checkTables(c);
            checkFeatureTables(c);
            checkColumns(c, "mb_parent_child", new String[]{
                "bind_id", "parent_user_id", "child_user_id", "bind_status",
                "confirm_user_id", "confirm_time", "reject_reason"
            });
            checkColumns(c, "mb_comment", new String[]{
                "comment_id", "target_type", "target_id", "user_id", "content",
                "status", "deleted_by", "deleted_time"
            });
            checkColumns(c, "mb_badge_def", new String[]{
                "badge_id", "icon", "title", "criteria_type", "criteria_value", "status"
            });
            checkColumns(c, "mb_badge_progress", new String[]{
                "user_id", "badge_id", "earned", "progress_current", "progress_target"
            });
            checkColumns(c, "mb_user_reaction", new String[]{
                "user_id", "target_id", "target_type", "reaction_type"
            });
            checkColumns(c, "mb_quiz_daily", new String[]{"daily_id", "quiz_date", "question_id", "bonus_points", "status"});
            checkColumns(c, "mb_work", new String[]{"work_id", "student_user_id", "submission_id", "review_status"});
            checkColumns(c, "mb_task", new String[]{"task_id", "title", "task_type", "video_id", "class_org_id"});
            checkFeatureIndexes(c);
            checkIndexes(c);
            checkDemoUserIds(c);
            checkRowCounts(c);
            checkBaseline(c);
            checkForbidden(c);
            checkExtraMb(c);
        }
    }

    static void checkTables(Connection c) throws SQLException {
        System.out.println("--- 1. Required tables (Tier 1) ---");
        int missing = 0;
        for (String t : REQUIRED) {
            boolean ok = tableExists(c, t);
            if (!ok) missing++;
            System.out.println((ok ? "[OK]" : "[MISSING]") + " " + t);
        }
        System.out.println("Result: " + (missing == 0 ? "ALL 7 PRESENT" : missing + " MISSING") + "\n");
    }

    static void checkFeatureTables(Connection c) throws SQLException {
        System.out.println("--- 2. Feature tables (Tier 1.5) ---");
        int missing = 0;
        for (String t : FEATURE) {
            boolean ok = tableExists(c, t);
            if (!ok) missing++;
            System.out.println((ok ? "[OK]" : "[MISSING]") + " " + t);
        }
        System.out.println("Result: " + (missing == 0 ? "ALL 4 PRESENT" : missing + " MISSING") + "\n");
    }

    static void checkFeatureIndexes(Connection c) throws SQLException {
        System.out.println("--- Feature unique indexes ---");
        String[][] idx = {
            {"mb_badge_progress", "uk_mb_badge_user"},
            {"mb_user_reaction", "uk_user_target_reaction"},
            {"mb_badge_def", "PRIMARY"}
        };
        for (String[] pair : idx) {
            if (!tableExists(c, pair[0])) continue;
            boolean ok = "PRIMARY".equals(pair[1])
                ? primaryKeyExists(c, pair[0])
                : indexExists(c, pair[0], pair[1]);
            System.out.println((ok ? "[OK]" : "[MISSING]") + " " + pair[0] + "." + pair[1]);
        }
        System.out.println();
    }

    static boolean primaryKeyExists(Connection c, String table) throws SQLException {
        PreparedStatement ps = c.prepareStatement(
            "SELECT 1 FROM information_schema.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA='bs_exp_vue' AND TABLE_NAME=? AND CONSTRAINT_TYPE='PRIMARY KEY'");
        ps.setString(1, table);
        ResultSet rs = ps.executeQuery();
        boolean ok = rs.next();
        rs.close();
        ps.close();
        return ok;
    }

    static void checkDemoUserIds(Connection c) throws SQLException {
        System.out.println("--- Demo user_id scan (should be 0 rows) ---");
        String[] tables = {"mb_task", "mb_task_submission", "mb_work", "mb_quiz_record",
            "mb_badge_progress", "mb_parent_child"};
        int total = 0;
        for (String t : tables) {
            if (!tableExists(c, t)) continue;
            int n = countLikeDemo(c, t);
            if (n > 0) {
                System.out.println("[WARN] " + t + ": " + n + " rows with demo-* id");
                total += n;
            } else {
                System.out.println("[OK] " + t + ": no demo-* ids");
            }
        }
        System.out.println("Result: " + (total == 0 ? "CLEAN" : total + " demo rows remain") + "\n");
    }

    static int countLikeDemo(Connection c, String table) throws SQLException {
        String[] cols = {"user_id", "student_user_id", "teacher_user_id", "parent_user_id", "child_user_id"};
        Set<String> existing = getColumns(c, table);
        int sum = 0;
        for (String col : cols) {
            if (!existing.contains(col)) continue;
            Statement st = c.createStatement();
            ResultSet rs = st.executeQuery(
                "SELECT COUNT(*) FROM `" + table + "` WHERE `" + col + "` LIKE 'demo-%'");
            rs.next();
            sum += rs.getInt(1);
            rs.close();
            st.close();
        }
        return sum;
    }

    static void checkColumns(Connection c, String table, String[] cols) throws SQLException {
        System.out.println("--- Columns: " + table + " ---");
        if (!tableExists(c, table)) {
            System.out.println("[SKIP] table missing\n");
            return;
        }
        Set<String> existing = getColumns(c, table);
        for (String col : cols) {
            System.out.println((existing.contains(col) ? "[OK]" : "[MISSING COL]") + " " + col);
        }
        System.out.println();
    }

    static void checkIndexes(Connection c) throws SQLException {
        System.out.println("--- 3. Required unique indexes ---");
        String[][] idx = {
            {"mb_task_submission", "uk_mb_task_student"},
            {"mb_quiz_record", "uk_mb_quiz_user_date"},
            {"mb_quiz_daily", "uk_mb_quiz_daily_date"},
            {"mb_parent_child", "uk_mb_parent_child"}
        };
        for (String[] pair : idx) {
            if (!tableExists(c, pair[0])) continue;
            boolean ok = indexExists(c, pair[0], pair[1]);
            System.out.println((ok ? "[OK]" : "[MISSING]") + " " + pair[0] + "." + pair[1]);
        }
        System.out.println();
    }

    static void checkRowCounts(Connection c) throws SQLException {
        System.out.println("--- Row counts (Tier 1 + 1.5) ---");
        String[] all = concat(REQUIRED, FEATURE);
        for (String t : all) {
            if (!tableExists(c, t)) continue;
            System.out.println(t + ": " + count(c, t));
        }
        System.out.println();
    }

    static String[] concat(String[] a, String[] b) {
        String[] r = new String[a.length + b.length];
        System.arraycopy(a, 0, r, 0, a.length);
        System.arraycopy(b, 0, r, a.length, b.length);
        return r;
    }

    static void checkBaseline(Connection c) throws SQLException {
        System.out.println("--- 5. Layer A baseline ---");
        printCount(c, "sys_user");
        printCount(c, "exp_msg");
        printCount(c, "exp_question");
        printCount(c, "exp_question_select");
        printCount(c, "sys_org");
        printCount(c, "school_notice");
        printCount(c, "sys_msg");
        System.out.println();
    }

    static void printCount(Connection c, String table) throws SQLException {
        if (!tableExists(c, table)) {
            System.out.println("[MISSING] " + table);
            return;
        }
        System.out.println(table + ": " + count(c, table));
    }

    static void checkForbidden(Connection c) throws SQLException {
        System.out.println("--- 6. Forbidden tables ---");
        for (String t : FORBIDDEN) {
            System.out.println((tableExists(c, t) ? "[WARN exists]" : "[OK absent]") + " " + t);
        }
        System.out.println();
    }

    static void checkExtraMb(Connection c) throws SQLException {
        System.out.println("--- 7. All mb_* tables ---");
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT TABLE_NAME, IFNULL(TABLE_ROWS,0), TABLE_COMMENT FROM information_schema.TABLES " +
                 "WHERE TABLE_SCHEMA='bs_exp_vue' AND TABLE_NAME LIKE 'mb_%' ORDER BY TABLE_NAME")) {
            while (rs.next()) {
                System.out.println(rs.getString(1) + " | rows~=" + rs.getString(2) + " | " + rs.getString(3));
            }
        }
    }

    static boolean tableExists(Connection c, String name) throws SQLException {
        PreparedStatement ps = c.prepareStatement(
            "SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA='bs_exp_vue' AND TABLE_NAME=?");
        ps.setString(1, name);
        ResultSet rs = ps.executeQuery();
        boolean ok = rs.next();
        rs.close();
        ps.close();
        return ok;
    }

    static Set<String> getColumns(Connection c, String table) throws SQLException {
        Set<String> set = new HashSet<String>();
        PreparedStatement ps = c.prepareStatement(
            "SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='bs_exp_vue' AND TABLE_NAME=?");
        ps.setString(1, table);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) set.add(rs.getString(1));
        rs.close();
        ps.close();
        return set;
    }

    static boolean indexExists(Connection c, String table, String index) throws SQLException {
        PreparedStatement ps = c.prepareStatement(
            "SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA='bs_exp_vue' AND TABLE_NAME=? AND INDEX_NAME=?");
        ps.setString(1, table);
        ps.setString(2, index);
        ResultSet rs = ps.executeQuery();
        boolean ok = rs.next();
        rs.close();
        ps.close();
        return ok;
    }

    static int count(Connection c, String table) throws SQLException {
        Statement st = c.createStatement();
        ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM `" + table + "`");
        rs.next();
        int n = rs.getInt(1);
        rs.close();
        st.close();
        return n;
    }
}
