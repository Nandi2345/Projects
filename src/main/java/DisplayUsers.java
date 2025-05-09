import java.io.*;  
import javax.servlet.*;  
import javax.servlet.http.*;  
import java.sql.*;  
    
public class DisplayUsers extends HttpServlet  
{    
     public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException 
      {  
         PrintWriter out = res.getWriter();  
         res.setContentType("text/html");  
         out.println("<html><body>");  
         try 
         {  
             Class.forName("com.mysql.jdbc.Driver");  
             Connection con=DriverManager.getConnection( "jdbc:mysql://localhost:3306/tollgate_db","root","4002");  
             // Here dsnname- mydsn,user id- system(for oracle 10g),password is pintu.  
             Statement stmt = con.createStatement();  
             ResultSet rs = stmt.executeQuery("select * from `users`");  
             out.println("<table border=1 width=50% height=50%>");  
             out.println("<tr><th>SNO</th><th>NAME</th><th>EMAIL</th><th>PHONE</th><th>PASSWORD</th><tr>");  
             while (rs.next()) 
             {  
                 int p = rs.getInt("sno");
                 String n = rs.getString("name");  
                 String nm = rs.getString("email");  
                 int s = rs.getInt("phone");  
                 String ps = rs.getString("password");
                 out.println("<tr><td>" + p + "</td><td>" + n + "</td><td>" + nm + "</td><td>" + s + "</td><td>" + ps + "</td></tr>");   
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