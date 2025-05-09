import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@WebServlet("/PassManagementServlet")
public class PassManagementServlet extends HttpServlet {
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
        String action = request.getParameter("action");
        
        if (action == null) {
            // Display list of user's passes
            showUserPasses(out, username);
        } else if (action.equals("new")) {
            // Show form to apply for a new pass
            showNewPassForm(out, username);
        } else if (action.equals("view")) {
            // View details of a specific pass
            int passId = Integer.parseInt(request.getParameter("id"));
            showPassDetails(out, passId, username);
        } else if (action.equals("cancel")) {
            // Cancel a pass
            int passId = Integer.parseInt(request.getParameter("id"));
            cancelPass(out, passId, username);
        }
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("userlogin.html");
            return;
        }
        
        String username = (String) session.getAttribute("username");
        String action = request.getParameter("action");
        
        if (action.equals("applyPass")) {
            // Process new pass application
            String vehicleNumber = request.getParameter("vehicleNumber");
            String passType = request.getParameter("passType");
            String startDateStr = request.getParameter("startDate");
            
            LocalDate startDate = LocalDate.parse(startDateStr);
            
            // Check if vehicle already has an active pass
            if (PassDao.hasActivePass(username, vehicleNumber)) {
                out.println("<script>alert('This vehicle already has an active pass.'); window.location='PassManagementServlet';</script>");
                return;
            }
            
            // Create a new pass using factory pattern
            Pass pass = Pass.createPass(passType, username, vehicleNumber, startDate);
            
            int status = PassDao.createPass(pass);
            
            if (status > 0) {
                out.println("<script>alert('Pass application successful!'); window.location='PassManagementServlet';</script>");
            } else {
                out.println("<script>alert('Failed to apply for pass. Please try again.'); window.location='PassManagementServlet?action=new';</script>");
            }
        }
    }
    
    private void showUserPasses(PrintWriter out, String username) {
        List<Pass> passes = PassDao.getAllPassesByUser(username);
        
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>My Passes - Tollgate Management System</title>");
        out.println("<link rel='stylesheet' type='text/css' href='passstyle.css'>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class='container'>");
        out.println("<h1>My Passes</h1>");
        
        out.println("<div class='action-buttons'>");
        out.println("<a href='PassManagementServlet?action=new' class='btn btn-primary'>Apply for New Pass</a>");
        out.println("<a href='UserWelcomeServlet' class='btn btn-secondary'>Back to Dashboard</a>");
        out.println("</div>");
        
        if (passes.isEmpty()) {
            out.println("<p>You don't have any passes yet. Apply for a new pass to get started.</p>");
        } else {
            out.println("<table class='pass-table'>");
            out.println("<thead>");
            out.println("<tr>");
            out.println("<th>Vehicle Number</th>");
            out.println("<th>Pass Type</th>");
            out.println("<th>Valid From</th>");
            out.println("<th>Valid Until</th>");
            out.println("<th>Amount</th>");
            out.println("<th>Status</th>");
            out.println("<th>Actions</th>");
            out.println("</tr>");
            out.println("</thead>");
            out.println("<tbody>");
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            
            for (Pass pass : passes) {
                out.println("<tr>");
                out.println("<td>" + pass.getVehicleNumber() + "</td>");
                out.println("<td>" + pass.getPassType() + "</td>");
                out.println("<td>" + pass.getStartDate().format(formatter) + "</td>");
                out.println("<td>" + pass.getEndDate().format(formatter) + "</td>");
                out.println("<td>₹" + String.format("%.2f", pass.getAmount()) + "</td>");
                out.println("<td>" + pass.getStatus() + "</td>");
                out.println("<td>");
                out.println("<a href='PassManagementServlet?action=view&id=" + pass.getId() + "' class='btn btn-small'>View</a>");
                
                if (pass.getStatus().equals("ACTIVE") && !pass.getEndDate().isBefore(LocalDate.now())) {
                    out.println("<a href='PassManagementServlet?action=cancel&id=" + pass.getId() + "' class='btn btn-small btn-danger'>Cancel</a>");
                }
                
                out.println("</td>");
                out.println("</tr>");
            }
            
            out.println("</tbody>");
            out.println("</table>");
        }
        
        out.println("</div>"); // End container
        out.println("</body>");
        out.println("</html>");
    }
    
    private void showNewPassForm(PrintWriter out, String username) {
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Apply for New Pass - Tollgate Management System</title>");
        out.println("<link rel='stylesheet' type='text/css' href='passstyle.css'>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class='container'>");
        out.println("<h1>Apply for New Pass</h1>");
        
        // Form to apply for a new pass
        out.println("<form action='PassManagementServlet' method='post' class='pass-form'>");
        out.println("<input type='hidden' name='action' value='applyPass'>");
        
        // Vehicle number input (in a real application, you would fetch the user's vehicles from the database)
        out.println("<div class='form-group'>");
        out.println("<label for='vehicleNumber'>Vehicle Number:</label>");
        out.println("<input type='text' id='vehicleNumber' name='vehicleNumber' required placeholder='Enter vehicle registration number'>");
        out.println("</div>");
        
        // Pass type selection
        out.println("<div class='form-group'>");
        out.println("<label for='passType'>Pass Type:</label>");
        out.println("<select id='passType' name='passType' required>");
        out.println("<option value=''>Select Pass Type</option>");
        out.println("<option value='DAILY'>Daily Pass (₹100)</option>");
        out.println("<option value='MONTHLY'>Monthly Pass (₹2,000)</option>");
        out.println("<option value='QUARTERLY'>Quarterly Pass (₹5,500)</option>");
        out.println("<option value='YEARLY'>Yearly Pass (₹20,000)</option>");
        out.println("</select>");
        out.println("</div>");
        
        // Start date
        out.println("<div class='form-group'>");
        out.println("<label for='startDate'>Start Date:</label>");
        out.println("<input type='date' id='startDate' name='startDate' required min='" + LocalDate.now() + "'>");
        out.println("</div>");
        
        // Submit button
        out.println("<div class='form-actions'>");
        out.println("<button type='submit' class='btn btn-primary'>Apply for Pass</button>");
        out.println("<a href='PassManagementServlet' class='btn btn-secondary'>Cancel</a>");
        out.println("</div>");
        
        out.println("</form>");
        
        out.println("</div>"); // End container
        out.println("</body>");
        out.println("</html>");
    }
    
    private void showPassDetails(PrintWriter out, int passId, String username) {
        Pass pass = PassDao.getPassById(passId);
        
        if (pass == null || !pass.getUserId().equals(username)) {
            out.println("<script>alert('Pass not found or access denied.'); window.location='PassManagementServlet';</script>");
            return;
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Pass Details - Tollgate Management System</title>");
        out.println("<link rel='stylesheet' type='text/css' href='passstyle.css'>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class='container'>");
        out.println("<h1>Pass Details</h1>");
        
        out.println("<div class='pass-details'>");
        out.println("<div class='pass-header'>");
        out.println("<h2>" + pass.getPassType() + " Pass</h2>");
        
        // Display validity status
        if (pass.getStatus().equals("ACTIVE") && !pass.getEndDate().isBefore(LocalDate.now())) {
            out.println("<span class='badge badge-success'>VALID</span>");
        } else {
            out.println("<span class='badge badge-danger'>EXPIRED/CANCELLED</span>");
        }
        
        out.println("</div>"); // End pass-header
        
        out.println("<div class='pass-info'>");
        out.println("<p><strong>Vehicle Number:</strong> " + pass.getVehicleNumber() + "</p>");
        out.println("<p><strong>Valid From:</strong> " + pass.getStartDate().format(formatter) + "</p>");
        out.println("<p><strong>Valid Until:</strong> " + pass.getEndDate().format(formatter) + "</p>");
        out.println("<p><strong>Amount Paid:</strong> ₹" + String.format("%.2f", pass.getAmount()) + "</p>");
        out.println("<p><strong>Status:</strong> " + pass.getStatus() + "</p>");
        
        // Calculate remaining days if active
        if (pass.getStatus().equals("ACTIVE") && !pass.getEndDate().isBefore(LocalDate.now())) {
            long daysRemaining = LocalDate.now().until(pass.getEndDate()).getDays();
            out.println("<p><strong>Days Remaining:</strong> " + daysRemaining + "</p>");
        }
        
        out.println("</div>"); // End pass-info
        
        out.println("<div class='pass-actions'>");
        if (pass.getStatus().equals("ACTIVE") && !pass.getEndDate().isBefore(LocalDate.now())) {
            out.println("<a href='PassManagementServlet?action=cancel&id=" + pass.getId() + "' class='btn btn-danger'>Cancel Pass</a>");
        }
        out.println("<a href='PassManagementServlet' class='btn btn-secondary'>Back to My Passes</a>");
        out.println("</div>"); // End pass-actions
        
        out.println("</div>"); // End pass-details
        
        out.println("</div>"); // End container
        out.println("</body>");
        out.println("</html>");
    }
    
    private void cancelPass(PrintWriter out, int passId, String username) {
        Pass pass = PassDao.getPassById(passId);
        
        if (pass == null || !pass.getUserId().equals(username)) {
            out.println("<script>alert('Pass not found or access denied.'); window.location='PassManagementServlet';</script>");
            return;
        }
        
        if (!pass.getStatus().equals("ACTIVE") || pass.getEndDate().isBefore(LocalDate.now())) {
            out.println("<script>alert('This pass is already expired or cancelled.'); window.location='PassManagementServlet';</script>");
            return;
        }
        
        int status = PassDao.updatePassStatus(passId, "CANCELLED");
        
        if (status > 0) {
            out.println("<script>alert('Pass cancelled successfully.'); window.location='PassManagementServlet';</script>");
        } else {
            out.println("<script>alert('Failed to cancel pass. Please try again.'); window.location='PassManagementServlet';</script>");
        }
    }
}