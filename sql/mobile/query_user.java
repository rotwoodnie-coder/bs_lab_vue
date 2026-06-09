import java.sql.*;
public class query_user {
  public static void main(String[] a) throws Exception {
    Class.forName("com.mysql.cj.jdbc.Driver");
    String url = "jdbc:mysql://10.0.181.204:13306/bs_exp_vue?useSSL=false&serverTimezone=Asia/Shanghai";
    try (Connection c = DriverManager.getConnection(url, "root", "ZHTKFc5ta7tPTeyx")) {
      q(c, "SELECT user_id, login_name, user_name, user_phone, user_role_id, status, root_org_id, LEFT(login_pwd,20) pwd_prefix FROM sys_user WHERE login_name='18918902651' OR user_phone='18918902651'");
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
