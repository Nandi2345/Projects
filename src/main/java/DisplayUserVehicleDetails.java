import java.io.*;  
import javax.servlet.*;  
import javax.servlet.http.*;  
import java.sql.*;  
    
public class DisplayUserVehicleDetails extends HttpServlet  
{    
     public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException 
      {  
         PrintWriter out = res.getWriter();  
         res.setContentType("text/html");  
         out.println("<html><body>");  
         try 
         {  
            Class.forName("com.mysql.jdbc.Driver");  
            Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/tollgate_db","root","4002");  
             // Here dsnname- mydsn,user id- system(for oracle 10g),password is pintu.  
             Statement stmt = con.createStatement();  
             ResultSet rs = stmt.executeQuery("select * from vehicle_detail");  
             out.println("<table border=1 width=50% height=50%>");  
             out.println("<tr><th>ID</th><th>VEHICLE_CATAGORY</th><th>VEHICLE_NAME</th><th>VEHICLE_NUMBER</th><tr>");  
             while (rs.next()) 
             {  
                 String p = rs.getString("vehicle number");
                 String n = rs.getString("vehicle name");  
                 String nm = rs.getString("vehicle catagory");  
                 int s = rs.getInt("id");   
                 out.println("<tr><td>" + s + "</td><td>" + nm + "</td><td>" + p + "</td><td>" + n + "</td></tr>");   
             }  
             out.println("</table>");  
             out.println("</html></body>");  
             con.close();  
            }  
             catch (Exception e) 
            {  
             out.println("error");  
         }  
     }  
 }  