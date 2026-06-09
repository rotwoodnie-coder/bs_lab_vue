import java.sql.*;
import java.nio.file.*;
import java.util.*;

public class run_sql_file {
    static final String URL = "jdbc:mysql://10.0.181.204:13306/bs_exp_vue?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowMultiQueries=true";
    static final String USER = "root";
    static final String PASS = "ZHTKFc5ta7tPTeyx";

    public static void main(String[] args) throws Exception {
        String file = args.length > 0 ? args[0] : "truncate_mb_demo_data.sql";
        Path path = Paths.get("D:/dev_program/bs_lab_vue/sql/mobile", file);
        String sql = new String(Files.readAllBytes(path), "UTF-8");
        List<String> stmts = splitStatements(sql);

        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection c = DriverManager.getConnection(URL, USER, PASS)) {
            c.setAutoCommit(true);
            System.out.println("=== Execute: " + path.getFileName() + " ===\n");
            for (String s : stmts) {
                if (s.trim().isEmpty()) continue;
                try (Statement st = c.createStatement()) {
                    st.execute(s);
                    String preview = s.replaceAll("\\s+", " ").trim();
                    if (preview.length() > 70) preview = preview.substring(0, 70) + "...";
                    System.out.println("[OK] " + preview);
                }
            }
            System.out.println("\n=== Post-cleanup row counts ===");
            String[] tables = {
                "mb_task", "mb_task_submission", "mb_work", "mb_work_file",
                "mb_quiz_record", "mb_quiz_daily", "mb_parent_child",
                "mb_badge_def", "mb_badge_progress", "mb_comment", "mb_user_reaction", "mb_growth_event"
            };
            for (String t : tables) {
                try (Statement st = c.createStatement();
                     ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM `" + t + "`")) {
                    rs.next();
                    System.out.println(t + ": " + rs.getInt(1));
                }
            }
            System.out.println("\n=== demo-* id scan ===");
            checkDemo(c);
        }
    }

    static void checkDemo(Connection c) throws SQLException {
        String[][] checks = {
            {"mb_task", "teacher_user_id"},
            {"mb_task_submission", "student_user_id"},
            {"mb_work", "student_user_id"},
            {"mb_quiz_record", "user_id"},
            {"mb_badge_progress", "user_id"},
            {"mb_parent_child", "parent_user_id"},
            {"mb_parent_child", "child_user_id"},
            {"mb_comment", "user_id"}
        };
        int total = 0;
        for (String[] pair : checks) {
            String sql = "SELECT COUNT(*) FROM `" + pair[0] + "` WHERE `" + pair[1] + "` LIKE 'demo-%'";
            try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                rs.next();
                int n = rs.getInt(1);
                if (n > 0) {
                    System.out.println("[WARN] " + pair[0] + "." + pair[1] + ": " + n);
                    total += n;
                }
            }
        }
        System.out.println(total == 0 ? "Result: CLEAN (no demo-* ids)" : "Result: " + total + " demo rows remain");
    }

    static List<String> splitStatements(String sql) {
        List<String> list = new ArrayList<String>();
        StringBuilder cur = new StringBuilder();
        for (String line : sql.split("\n")) {
            String t = line.trim();
            if (t.startsWith("--")) continue;
            cur.append(line).append("\n");
            if (t.endsWith(";")) {
                list.add(cur.toString());
                cur = new StringBuilder();
            }
        }
        if (cur.toString().trim().length() > 0) list.add(cur.toString());
        return list;
    }
}
