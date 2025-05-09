import java.io.*;  
import javax.servlet.*;  
import javax.servlet.http.*;  
import java.sql.*;  
    
public class DisplayPlaces extends HttpServlet  
{    
     public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException 
      {  
         PrintWriter out = res.getWriter();  
         res.setContentType("text/html");  
         out.println("<html><body>");  
         try 
         {  
             Class.forName("com.mysql.jdbc.Driver");  
             Connection con=DriverManager.getConnection(  "jdbc:mysql://localhost:3306/tollgate_db","root","4002");  
             // Here dsnname- mydsn,user id- system(for oracle 10g),password is pintu.  
             Statement stmt = con.createStatement();  
             ResultSet rs = stmt.executeQuery("select * from employee");  
             out.println("<table border=1 width=50% height=50%>");  
             out.println("<tr><th>Id</th><th>SOURCES</th><th>DESTINATION</th><th>NO_OF_TOLGATES</th><tr>");  
             while (rs.next()) 
             {  
                 int a = rs.getInt("id");
                 String n = rs.getString("sources");  
                 String nm = rs.getString("destination");  
                 int s = rs.getInt("no_of_tolgates");   
                 out.println("<tr><td>" + a + "</td><td>" + n + "</td><td>" + nm + "</td><td>" + s + "</td></tr>");   
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