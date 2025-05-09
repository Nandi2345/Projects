import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class UserWelcomeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("userlogin.html");
            return;
        }
        
        String username = (String) session.getAttribute("username");
        
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>User Dashboard - Tollgate Management System</title>");
        out.println("<link rel='stylesheet' type='text/css' href='userpage.css'>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class='container'>");
        out.println("<h1>Welcome, " + username + "!</h1>");
        out.println("<div class='dashboard'>");
        
        // Display various options including the new pass management option
        out.println("<div class='dashboard-item'>");
        out.println("<h3>Vehicle Management</h3>");
        out.println("<a href='DisplayUserVehicleDetails'>View My Vehicles</a>");
        out.println("</div>");
        
        // Pass Management Section
        out.println("<div class='dashboard-item'>");
        out.println("<h3>Pass Management</h3>");
        out.println("<a href='PassManagementServlet'>View My Passes</a><br>");
        out.println("<a href='PassManagementServlet?action=new'>Apply for New Pass</a>");
        out.println("</div>");
        
        out.println("<div class='dashboard-item'>");
        out.println("<h3>Travel History</h3>");
        out.println("<a href='DisplayUserTravelHistory'>View Travel History</a>");
        out.println("</div>");
        
        out.println("<div class='dashboard-item'>");
        out.println("<h3>Account</h3>");
        out.println("<a href='DisplayUserProfile'>View Profile</a><br>");
        out.println("<a href='LogoutServlet'>Logout</a>");
        out.println("</div>");
        
        out.println("</div>"); // End dashboard
        out.println("</div>"); // End container
        out.println("</body>");
        out.println("</html>");
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}