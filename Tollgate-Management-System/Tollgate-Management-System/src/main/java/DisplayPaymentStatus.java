import java.io.*;  
import javax.servlet.*;  
import javax.servlet.http.*;  
import java.sql.*;  
    
public class DisplayPaymentStatus extends HttpServlet  
{    
     public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException 
      {  
         PrintWriter out = res.getWriter();  
         res.setContentType("text/html");  
         out.println("<html><body>");  
         try 
         {  
            Class.forName("com.mysql.jdbc.Driver");  
Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/annamalai","root","");  
             // Here dsnname- mydsn,user id- system(for oracle 10g),password is pintu.  
             Statement stmt = con.createStatement();  
             ResultSet rs = stmt.executeQuery("select * from payment");  
             out.println("<table border=1 width=50% height=50%>");  
             out.println("<tr>ID</tr><tr>SOURCE_Name</tr><tr>DESTINATION_NAME</tr><tr>TYPE_OF_VEHICLE</tr><tr>DATE</tr><tr>NO_OF_TOLLGATES</tr><tr>AMOUNT</tr><tr>PAYMENT_STATUS</tr><tr>TYPE_OF_PAYMENT</tr>");  
             while (rs.next()) 
             {  
                 int a =rs.getInt("id");
                 String n = rs.getString("sources_location");  
                 String nm = rs.getString("destination_location");
                 String b = rs.getString("type_of_vehicle");
                 Date c = rs.getDate("date");
                 int d =rs.getInt("no of tolgate");
                 int e = rs.getInt("amount");
                 String f = rs.getString("payment status");
                 String g = rs.getString("type of payment");
                 out.println("<tr><td>" + a + "</td><td>" + n + "</td><td>" + nm + "</td><td>" + b + "</td><td>" + c + "</td><td>" + d + "</td><td>" + e + "</td><td>" + f + "</td><td>" + g + "</td></tr>");   
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