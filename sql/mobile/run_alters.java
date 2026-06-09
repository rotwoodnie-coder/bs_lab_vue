import java.sql.*;

public class run_alters {
    static final String URL = "jdbc:mysql://10.0.181.204:13306/bs_exp_vue?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai";
    static final String USER = "root";
    static final String PASS = "ZHTKFc5ta7tPTeyx";

    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection c = DriverManager.getConnection(URL, USER, PASS)) {
            System.out.println("MySQL: " + c.getMetaData().getDatabaseProductVersion());
            exec(c, "ALTER TABLE mb_parent_child ADD COLUMN confirm_user_id varchar(32) DEFAULT NULL AFTER bind_status");
            exec(c, "ALTER TABLE mb_parent_child ADD COLUMN confirm_time datetime DEFAULT NULL AFTER confirm_user_id");
            exec(c, "ALTER TABLE mb_parent_child ADD COLUMN reject_reason varchar(500) DEFAULT NULL AFTER confirm_time");
            exec(c, "ALTER TABLE mb_comment ADD COLUMN deleted_by varchar(32) DEFAULT NULL AFTER status");
            exec(c, "ALTER TABLE mb_comment ADD COLUMN deleted_time datetime DEFAULT NULL AFTER deleted_by");
        }
    }

    static void exec(Connection c, String sql) throws SQLException {
        try (Statement st = c.createStatement()) {
            st.execute(sql);
            System.out.println("[OK] " + sql);
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate column")) {
                System.out.println("[SKIP] " + sql);
            } else {
                System.out.println("[ERR] " + e.getMessage());
            }
        }
    }
}
