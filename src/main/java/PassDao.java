
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.mvc.util.DBConnection;

public class PassDao {
    
    // Create a new pass
    public static int createPass(Pass pass) {
        int status = 0;
        Connection con = null;
        PreparedStatement ps = null;
        
        try {
            con = DBConnection.createConnection();
            String sql = "INSERT INTO passes (user_id, vehicle_number, pass_type, start_date, end_date, amount, status) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";
            ps = con.prepareStatement(sql);
            
            ps.setString(1, pass.getUserId());
            ps.setString(2, pass.getVehicleNumber());
            ps.setString(3, pass.getPassType());
            ps.setDate(4, Date.valueOf(pass.getStartDate()));
            ps.setDate(5, Date.valueOf(pass.getEndDate()));
            ps.setDouble(6, pass.getAmount());
            ps.setString(7, pass.getStatus());
            
            status = ps.executeUpdate();
            System.out.println("Pass created with status: " + status);
            
        } catch (Exception e) {
            System.out.println("Error creating pass: " + e);
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e);
            }
        }
        
        return status;
    }
    
    // Get all passes for a user
    public static List<Pass> getAllPassesByUser(String userId) {
        List<Pass> passes = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            con = DBConnection.createConnection();
            String sql = "SELECT * FROM passes WHERE user_id = ? ORDER BY start_date DESC";
            ps = con.prepareStatement(sql);
            ps.setString(1, userId);
            
            rs = ps.executeQuery();
            while (rs.next()) {
                Pass pass = new Pass();
                pass.setId(rs.getInt("id"));
                pass.setUserId(rs.getString("user_id"));
                pass.setVehicleNumber(rs.getString("vehicle_number"));
                pass.setPassType(rs.getString("pass_type"));
                pass.setStartDate(rs.getDate("start_date").toLocalDate());
                pass.setEndDate(rs.getDate("end_date").toLocalDate());
                pass.setAmount(rs.getDouble("amount"));
                pass.setStatus(rs.getString("status"));
                
                passes.add(pass);
            }
            
        } catch (Exception e) {
            System.out.println("Error retrieving passes: " + e);
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e);
            }
        }
        
        return passes;
    }
    
    // Get a pass by ID
    public static Pass getPassById(int passId) {
        Pass pass = null;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            con = DBConnection.createConnection();
            String sql = "SELECT * FROM passes WHERE id = ?";
            ps = con.prepareStatement(sql);
            ps.setInt(1, passId);
            
            rs = ps.executeQuery();
            if (rs.next()) {
                pass = new Pass();
                pass.setId(rs.getInt("id"));
                pass.setUserId(rs.getString("user_id"));
                pass.setVehicleNumber(rs.getString("vehicle_number"));
                pass.setPassType(rs.getString("pass_type"));
                pass.setStartDate(rs.getDate("start_date").toLocalDate());
                pass.setEndDate(rs.getDate("end_date").toLocalDate());
                pass.setAmount(rs.getDouble("amount"));
                pass.setStatus(rs.getString("status"));
            }
            
        } catch (Exception e) {
            System.out.println("Error retrieving pass: " + e);
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e);
            }
        }
        
        return pass;
    }
    
    // Update pass status
    public static int updatePassStatus(int passId, String status) {
        int result = 0;
        Connection con = null;
        PreparedStatement ps = null;
        
        try {
            con = DBConnection.createConnection();
            String sql = "UPDATE passes SET status = ? WHERE id = ?";
            ps = con.prepareStatement(sql);
            
            ps.setString(1, status);
            ps.setInt(2, passId);
            
            result = ps.executeUpdate();
            
        } catch (Exception e) {
            System.out.println("Error updating pass status: " + e);
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e);
            }
        }
        
        return result;
    }
    
    // Check if user has active pass for a vehicle
    public static boolean hasActivePass(String userId, String vehicleNumber) {
        boolean hasActive = false;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            con = DBConnection.createConnection();
            String sql = "SELECT * FROM passes WHERE user_id = ? AND vehicle_number = ? AND status = 'ACTIVE' AND end_date >= ?";
            ps = con.prepareStatement(sql);
            
            ps.setString(1, userId);
            ps.setString(2, vehicleNumber);
            ps.setDate(3, Date.valueOf(LocalDate.now()));
            
            rs = ps.executeQuery();
            hasActive = rs.next();
            
        } catch (Exception e) {
            System.out.println("Error checking active pass: " + e);
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e);
            }
        }
        
        return hasActive;
    }
}