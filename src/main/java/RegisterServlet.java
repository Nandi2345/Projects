import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.mvc.bean.RegisterBean;
import com.mvc.dao.RegisterDao;

public class RegisterServlet extends HttpServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        // Get the form parameters
        String fullName = request.getParameter("fullName"); // Keep reading the form field as is
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");    // Read from userName field but map to phone
        String password = request.getParameter("password");
        
        System.out.println("Form data: " + fullName + ", " + email + ", " + phone + ", " + password);
        
        // Create RegisterBean and set values with new property names
        RegisterBean registerBean = new RegisterBean();
        registerBean.setName(fullName);    // Changed to setName
        registerBean.setEmail(email);
        registerBean.setPhone(phone);      // Changed to setPhone
        registerBean.setPassword(password);
        
        // Register user using RegisterDao
        RegisterDao registerDao = new RegisterDao();
        String userRegistered = registerDao.registerUser(registerBean);
        
        if(userRegistered.equals("SUCCESS")) {
            // If registration is successful, redirect to login page
            RequestDispatcher rd = request.getRequestDispatcher("userlogin.html");
            rd.forward(request, response);
        } else {
            // If registration fails, show error message
            out.print("<p style='color:red'>Registration failed: " + userRegistered + "</p>");
            RequestDispatcher rd = request.getRequestDispatcher("signup.html");
            rd.include(request, response);
        }
        
        out.close();
    }
}