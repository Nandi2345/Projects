import java.io.*;  
import javax.servlet.*;  
import javax.servlet.http.*;  
import java.sql.*;  
    
public class DisplayTollgates extends HttpServlet  
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
             ResultSet rs = stmt.executeQuery("select * from `tolgate_detail`");  
             out.println("<table border=1 width=50% height=50%>");  
             out.println("<tr><th>SNO</th><th>TOLGATE NAMES</th><th>VEHICLE TYPE</th><th>AMOUNT</th><th>TOLGATE ADDRESS</th><tr>");  
             while (rs.next()) 
             {  
                 int a = rs.getInt("sno");
                 
                 String n = rs.getString("sources_location");  
                 String nm = rs.getString("destination_location");
                  String b = rs.getString("tolgate name");  
                 String c = rs.getString("tolgate address");
                 String d = rs.getString("vehicle type");
                 int s = rs.getInt("amount");   
                 out.println("<tr><td>" + a + "</td><td>" + n + "</td><td>" + nm + "</td><td>" + b + "</td><td>" + c + "</td><td>" + d + "</td><td>" + s + "</td></tr>");   
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