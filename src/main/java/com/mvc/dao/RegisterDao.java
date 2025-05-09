package com.mvc.dao;
 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.mvc.bean.RegisterBean;
import com.mvc.util.DBConnection;
 
public class RegisterDao { 
    public String registerUser(RegisterBean registerBean)
    {
        String name = registerBean.getName();
        String email = registerBean.getEmail();
        String phone = registerBean.getPhone();
        String password = registerBean.getPassword();
        
        Connection con = null;
        PreparedStatement preparedStatement = null;         
        try
        {
            // Get database connection
            con = DBConnection.createConnection();
            if (con == null) {
                System.out.println("Database connection is null");
                return "Database connection failed";
            }
            
            System.out.println("Database connection established: " + con);
            
            // Create SQL query
            String query = "insert into users(name,email,phone,password) values (?,?,?,?)";
            System.out.println("Preparing SQL query: " + query);
            
            try {
                preparedStatement = con.prepareStatement(query);
                System.out.println("PreparedStatement created: " + preparedStatement);
                
                // Set parameters
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, email);
                preparedStatement.setString(3, phone);
                preparedStatement.setString(4, password);
                
                System.out.println("Parameters set, executing update");
                int i = preparedStatement.executeUpdate();
                System.out.println("Update executed, rows affected: " + i);
                
                if (i!=0)
                    return "SUCCESS"; 
                else
                    return "Insert failed - no rows affected";
            } catch (SQLException e) {
                System.out.println("Error preparing statement: " + e.getMessage());
                throw e; // Re-throw to be caught by outer catch block
            }
        }
        catch(SQLException e)
        {
            System.out.println("SQL Exception: " + e.getMessage());
            e.printStackTrace();
            return "Database error: " + e.getMessage();
        }
        catch(Exception e)
        {
            System.out.println("General Exception: " + e.getMessage());
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
        finally {
            try {
                if (preparedStatement != null) preparedStatement.close();
                if (con != null) con.close();
                System.out.println("Resources closed");
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}