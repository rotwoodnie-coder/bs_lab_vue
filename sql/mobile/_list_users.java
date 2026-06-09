import java.sql.*;
public class list_users {
  public static void main(String[] a) throws Exception {
    Class.forName("com.mysql.cj.jdbc.Driver");
    try (Connection c = DriverManager.getConnection("jdbc:mysql://10.0.181.204:13306/bs_exp_vue?useSSL=false&serverTimezone=Asia/Shanghai","root","ZHTKFc5ta7tPTeyx")) {
      q(c, "SELECT u.user_id, u.login_name, u.user_name, u.user_nick_name, r.role_name FROM sys_user u LEFT JOIN sys_user_role ur ON ur.user_id=u.user_id LEFT JOIN sys_role r ON r.role_id=ur.role_id ORDER BY u.login_name");
      q(c, "SELECT org_id, org_name, org_type_id, parent_org_id FROM sys_org ORDER BY org_name LIMIT 15");
    }
  }
  static void q(Connection c, String sql) throws SQLException {
    try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
      ResultSetMetaData m = rs.getMetaData(); int n = m.getColumnCount();
      while (rs.next()) { for (int i=1;i<=n;i++) System.out.print(m.getColumnLabel(i)+"="+rs.getString(i)+" | "); System.out.println(); }
    }
    System.out.println("---");
  }
}
