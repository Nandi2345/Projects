
import java.time.LocalDate;

public class Pass {
    private int id;
    private String userId;
    private String vehicleNumber;
    private String passType;
    private LocalDate startDate;
    private LocalDate endDate;
    private double amount;
    private String status;
    
    // Default constructor
    public Pass() {
    }
    
    // Parameterized constructor
    public Pass(int id, String userId, String vehicleNumber, String passType, 
                LocalDate startDate, LocalDate endDate, double amount, String status) {
        this.id = id;
        this.userId = userId;
        this.vehicleNumber = vehicleNumber;
        this.passType = passType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.amount = amount;
        this.status = status;
    }
    
    // Factory pattern method to create different types of passes
    public static Pass createPass(String passType, String userId, String vehicleNumber, LocalDate startDate) {
        Pass pass = new Pass();
        pass.setUserId(userId);
        pass.setVehicleNumber(vehicleNumber);
        pass.setPassType(passType);
        pass.setStartDate(startDate);
        pass.setStatus("ACTIVE");
        
        // Calculate end date and amount based on pass type
        double baseAmount = 0;
        
        switch (passType) {
            case "DAILY":
                pass.setEndDate(startDate.plusDays(1));
                baseAmount = 100; // Example base amount for daily pass
                break;
            case "MONTHLY":
                pass.setEndDate(startDate.plusMonths(1));
                baseAmount = 2000; // Example base amount for monthly pass
                break;
            case "QUARTERLY":
                pass.setEndDate(startDate.plusMonths(3));
                baseAmount = 5500; // Example base amount for quarterly pass
                break;
            case "YEARLY":
                pass.setEndDate(startDate.plusYears(1));
                baseAmount = 20000; // Example base amount for yearly pass
                break;
            default:
                pass.setEndDate(startDate.plusDays(1));
                baseAmount = 100;
        }
        
        pass.setAmount(baseAmount);
        return pass;
    }
    
    // Check if pass is valid
    public boolean isValid() {
        return status.equals("ACTIVE") && !LocalDate.now().isAfter(endDate);
    }
    
    // Getters and setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getVehicleNumber() {
        return vehicleNumber;
    }
    
    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }
    
    public String getPassType() {
        return passType;
    }
    
    public void setPassType(String passType) {
        this.passType = passType;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}