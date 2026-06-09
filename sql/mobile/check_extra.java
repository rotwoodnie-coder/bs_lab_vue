import java.sql.*;

public class check_extra {
    static final String URL = "jdbc:mysql://10.0.181.204:13306/bs_exp_vue?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai";
    static final String USER = "root";
    static final String PASS = "ZHTKFc5ta7tPTeyx";

    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection c = DriverManager.getConnection(URL, USER, PASS)) {
            System.out.println("--- Parent role in data_role ---");
            q(c, "SELECT role_id, role_name, status FROM data_role WHERE role_id LIKE '%arent%' OR role_name LIKE '%家长%' OR role_id='Parent'");
            System.out.println("\n--- mb_parent_child bind_status ---");
            q(c, "SELECT bind_status, COUNT(*) c FROM mb_parent_child GROUP BY bind_status");
            System.out.println("\n--- quiz_daily JOIN exp_question (collation test) ---");
            try {
                q(c, "SELECT d.daily_id, d.question_id, q.question_id FROM mb_quiz_daily d JOIN exp_question q ON d.question_id = q.question_id LIMIT 3");
                System.out.println("[OK] JOIN without COLLATE works");
            } catch (SQLException e) {
                System.out.println("[WARN] JOIN failed: " + e.getMessage());
                try {
                    q(c, "SELECT d.daily_id FROM mb_quiz_daily d JOIN exp_question q ON d.question_id COLLATE utf8mb4_unicode_ci = q.question_id LIMIT 1");
                    System.out.println("[OK] JOIN with COLLATE utf8mb4_unicode_ci works");
                } catch (SQLException e2) {
                    System.out.println("[ERR] " + e2.getMessage());
                }
            }
            System.out.println("\n--- Table collations ---");
            q(c, "SELECT TABLE_NAME, TABLE_COLLATION FROM information_schema.TABLES WHERE TABLE_SCHEMA='bs_exp_vue' AND TABLE_NAME IN ('mb_quiz_daily','exp_question','mb_comment')");
        }
    }

    static void q(Connection c, String sql) throws SQLException {
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            ResultSetMetaData m = rs.getMetaData();
            int cols = m.getColumnCount();
            while (rs.next()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= cols; i++) {
                    if (i > 1) sb.append(" | ");
                    sb.append(rs.getString(i));
                }
                System.out.println(sb);
            }
        }
    }
}
