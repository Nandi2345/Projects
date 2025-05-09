import java.sql.*;  
  
public class AdminLoginDoa {  
public static boolean validate(String username,String password){  
boolean status=false;  
 Connection con=null;
try{  
Class.forName("com.mysql.jdbc.Driver");  
con=DriverManager.getConnection("jdbc:mysql://localhost:3306/tollgate_db","root","4002");  
              
      
PreparedStatement ps=con.prepareStatement(  
"select * from admin where username=? and password=?");  
ps.setString(1,username);  
ps.setString(2,password);  
      
ResultSet rs=ps.executeQuery();  
status=rs.next();  
          
}catch(Exception e){System.out.println(e);}  
return status;  
}  
}  