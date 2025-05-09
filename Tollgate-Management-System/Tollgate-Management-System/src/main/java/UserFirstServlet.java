import java.io.IOException;  
import java.io.PrintWriter;  
  
import javax.servlet.RequestDispatcher;  
import javax.servlet.ServletException;  
import javax.servlet.http.HttpServlet;  
import javax.servlet.http.HttpServletRequest;  
import javax.servlet.http.HttpServletResponse;  
  
  
public class UserFirstServlet extends HttpServlet {  
public void doPost(HttpServletRequest request, HttpServletResponse response)  
        throws ServletException, IOException {  
  
    response.setContentType("text/html");  
    PrintWriter out = response.getWriter();  
          
    String n=request.getParameter("name");  
    String p=request.getParameter("password");  
          
    if(UserLoginDoa.validate(n, p)){  
        request.getSession().setAttribute("username",n);
        RequestDispatcher rd=request.getRequestDispatcher("UserWelcomeServlet");  
        rd.forward(request,response);  
    }  
    else{  
        out.print("Sorry username or password error");  
        RequestDispatcher rd=request.getRequestDispatcher("userlogin.html");  
        rd.include(request,response);  
    }  
          
    out.close();  
    }  
}  