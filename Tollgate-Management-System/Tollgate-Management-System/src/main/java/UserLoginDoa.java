import java.sql.*;
// Keep this import
import com.mvc.util.DBConnection;
  
public class UserLoginDoa {  
    public static boolean validate(String user, String pass) {  
        boolean status = false;  
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {  
            // Use the connection utility class
            con = DBConnection.createConnection();
            
            if (con != null) {
                System.out.println("Login validation - DB connection successful");
                
                // Change query to match registration field names but use login parameter
                ps = con.prepareStatement(
                    "select * from users where name=? and password=?");  
                ps.setString(1, user);  // Parameter from form is "user" but column is "name"
                ps.setString(2, pass);
                
                System.out.println("Executing login query for username: " + user);
                rs = ps.executeQuery();  
                status = rs.next();
                System.out.println("Login validation result: " + status);
            } else {
                System.out.println("Login validation - DB connection failed");
            }
        } catch(Exception e) {
            System.out.println("Login validation error: " + e);
            e.printStackTrace();
        } finally {
            try {
                if(rs != null) rs.close();
                if(ps != null) ps.close();
                if(con != null) con.close();
                System.out.println("Login validation - resources closed");
            } catch(SQLException e) {
                System.out.println("Error closing connection resources: " + e);
            }
        }
        return status;  
    }  
}