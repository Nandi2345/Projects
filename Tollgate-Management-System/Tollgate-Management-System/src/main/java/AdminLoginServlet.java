import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class AdminLoginServlet extends HttpServlet {
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Get parameters from the form
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        // Validate using your existing AdminLoginDoa
        if(AdminLoginDoa.validate(username, password)){
            // If login successful, create session and redirect
            HttpSession session = request.getSession();
            session.setAttribute("admin", username);
            response.sendRedirect("AdminWelcomeServlet");
        } else {
            // If login failed, redirect back to login with an error message
            response.sendRedirect("adminlogin.html?error=1");
        }
    }
}