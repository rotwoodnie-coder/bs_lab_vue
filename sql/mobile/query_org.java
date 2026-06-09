import java.sql.*;
public class query_org {
  public static void main(String[] a) throws Exception {
    Class.forName("com.mysql.cj.jdbc.Driver");
    try (Connection c = DriverManager.getConnection(
        "jdbc:mysql://10.0.181.204:13306/bs_exp_vue?useSSL=false&serverTimezone=Asia/Shanghai",
        "root", "ZHTKFc5ta7tPTeyx")) {
      q(c, "SELECT org_type_id, org_type_name FROM data_org_type");
      q(c, "SELECT org_id, org_name, org_type_id, parent_org_id FROM sys_org WHERE status='y' ORDER BY sort_order, org_name");
      q(c, "SELECT user_id, login_name, user_name, user_role_id, root_org_id, user_org_id FROM sys_user WHERE user_role_id='Student'");
    }
  }
  static void q(Connection c, String sql) throws SQLException {
    System.out.println("=== " + sql);
    try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
      ResultSetMetaData m = rs.getMetaData();
      int n = m.getColumnCount();
      while (rs.next()) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= n; i++) {
          if (i > 1) sb.append(" | ");
          sb.append(m.getColumnLabel(i)).append("=").append(rs.getString(i));
        }
        System.out.println(sb);
      }
    }
    System.out.println();
  }
}
