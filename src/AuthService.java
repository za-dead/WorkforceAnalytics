import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService{

    public Employee login(String email, String password){

        String query = "SELECT * FROM EMPLOYEE WHERE Email = ? AND Password = ?";            //mail and password checking
        
        try (Connection conn = DatabaseConnection.getConnection();
            
        PreparedStatement stmt = conn.prepareStatement(query)){
            
            stmt.setString(1, email);
            stmt.setString(2, password);
            
            ResultSet rs = stmt.executeQuery();
            
            if(rs.next()){                                                  // setting the employer data
                
                Employee emp = new Employee();
                emp.setEmployeeId(rs.getInt("Employee_ID"));
                emp.setFirstName(rs.getString("First_Name"));
                emp.setLastName(rs.getString("Last_Name"));
                emp.setEmail(rs.getString("Email"));
                emp.setRole(rs.getString("Role"));
                emp.setStatus(rs.getString("Status"));
                emp.setDepartmentId(rs.getInt("Department_ID"));
                emp.setProjectId(rs.getInt("Project_ID"));
                
                int supervisorId = rs.getInt("Supervisor_ID");
                if(!rs.wasNull()){
                    emp.setSupervisorId(supervisorId);
                }
                return emp;                                                     // login success
            }
        }
        catch(SQLException e){
            System.out.println("Database error during login:");
            e.printStackTrace();
        }
        return null;                               // login failed (wrong email or password)
    }


    public boolean signup(Employee newEmp){
        String query = "INSERT INTO EMPLOYEE (Employee_ID, First_Name, Last_Name, Email, Password, Role, Status, Department_ID, Project_ID, Supervisor_ID) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)){
            
            stmt.setInt(1, newEmp.getEmployeeId());
            stmt.setString(2, newEmp.getFirstName());
            stmt.setString(3, newEmp.getLastName());
            stmt.setString(4, newEmp.getEmail());
            stmt.setString(5, newEmp.getPassword());
            stmt.setString(6, newEmp.getRole());
            stmt.setString(7, newEmp.getStatus());
            stmt.setInt(8, newEmp.getDepartmentId());
            
            // project null check
            if(newEmp.getProjectId() == 0){
                stmt.setNull(9, java.sql.Types.INTEGER);       // inserting null into the database
            }
            else{
                stmt.setInt(9, newEmp.getProjectId());
            }
            
            // supervisor null chek
            if(newEmp.getSupervisorId() != null){
                stmt.setInt(10, newEmp.getSupervisorId());
            }
            else{
                stmt.setNull(10, java.sql.Types.INTEGER);
            }
            
            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;                   // true if user successfully saved
            
        }
        catch(SQLException e){
            System.out.println("Database error during signup:");
            e.printStackTrace();
            return false;             // signup ffailed
        }
    }

    // auto generate next emp id
    public int getNextEmployeeId(){
        String query = "SELECT MAX(Employee_ID) as MaxID FROM EMPLOYEE";
        
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery()) {
            
            if(rs.next()){
                return rs.getInt("MaxID") + 1; // Finds latest and returns latest+1
            }
        }
        catch(SQLException e){
            System.out.println("Error fetching next ID:");
            e.printStackTrace();
        }
        return 1; // fallback just in case table is empty
    }


    // helper methods for dashboard details
    public String getDepartmentName(int deptId){
        String query = "SELECT Department_Name FROM DEPARTMENT WHERE Department_ID = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)){
                stmt.setInt(1, deptId);
                ResultSet rs = stmt.executeQuery();
                if(rs.next()) return rs.getString("Department_Name");
            }
        catch (SQLException e) { e.printStackTrace(); }
        return "Unknown Department";
    }

    public String getProjectName(int projectId){
        String query = "SELECT Project_Name FROM PROJECT WHERE Project_ID = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)){
                stmt.setInt(1, projectId);
                ResultSet rs = stmt.executeQuery();
                if(rs.next()) return rs.getString("Project_Name");
            }
        catch(SQLException e) { e.printStackTrace(); }
        return "Unknown Project";
    }

    public String getSupervisorName(Integer supervisorId){
        if(supervisorId == null) return null; // immediately return if they are the boss
        String query = "SELECT First_Name, Last_Name FROM EMPLOYEE WHERE Employee_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)){
                stmt.setInt(1, supervisorId);
                ResultSet rs = stmt.executeQuery();
                if(rs.next()) return rs.getString("First_Name") + " " + rs.getString("Last_Name");
            }
        catch(SQLException e) { e.printStackTrace(); }
        return null;
    }

    public String getTaskDetails(int employeeId){
        // looks for their first incomplete task
        String query = "SELECT Task_ID, Task_Name FROM TASK WHERE Employee_ID = ? AND Status != 'Completed' LIMIT 1";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)){
                stmt.setInt(1, employeeId);
                ResultSet rs = stmt.executeQuery();
                if(rs.next()) return rs.getString("Task_Name") + " (ID: " + rs.getInt("Task_ID") + ")";
            }
        catch(SQLException e) { e.printStackTrace(); }
        return "No pending tasks at the moment.";
    }

    // generates html options for employees in a specific department.. except the boss
    public String getEmployeeDropdownHTML(int departmentId, int supervisorId){
        StringBuilder html = new StringBuilder();
        String query = "SELECT Employee_ID, First_Name, Last_Name FROM EMPLOYEE WHERE Department_ID = ? AND Employee_ID != ?";
        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)){
            stmt.setInt(1, departmentId);
            stmt.setInt(2, supervisorId); // supervisor wont assign tasks to himself
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                html.append("<option value='").append(rs.getInt("Employee_ID")).append("'>")
                    .append(rs.getString("First_Name")).append(" ").append(rs.getString("Last_Name"))
                    .append("</option>");
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return html.toString();
    }

    // generates html options for projects in a specific department
    public String getProjectDropdownHTML(int departmentId){
        StringBuilder html = new StringBuilder();
        String query = "SELECT Project_ID, Project_Name FROM PROJECT WHERE Department_ID = ?";
        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, departmentId);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                html.append("<option value='").append(rs.getInt("Project_ID")).append("'>")
                    .append(rs.getString("Project_Name")).append("</option>");
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return html.toString();
    }

    public boolean assignEmployeeToProject(int employeeId, int projectId, String taskName, String deadline){
        String updateEmpQuery = "UPDATE EMPLOYEE SET Project_ID = ? WHERE Employee_ID = ?";
        
        String upsertTaskQuery = "INSERT INTO TASK (Employee_ID, Project_ID, Task_Name, Status, Deadline) VALUES (?, ?, ?, 'Pending', ?) " +
                                 "ON DUPLICATE KEY UPDATE Project_ID = VALUES(Project_ID), Task_Name = VALUES(Task_Name), Status = 'Pending', Deadline = VALUES(Deadline)";
                                 
        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement empStmt = conn.prepareStatement(updateEmpQuery);
        PreparedStatement taskStmt = conn.prepareStatement(upsertTaskQuery)){
             
            empStmt.setInt(1, projectId);
            empStmt.setInt(2, employeeId);
            empStmt.executeUpdate();
            
            taskStmt.setInt(1, employeeId);
            taskStmt.setInt(2, projectId);
            taskStmt.setString(3, taskName);
            taskStmt.setString(4, deadline);
            taskStmt.executeUpdate();
            
            return true;
        }
        catch(SQLException e){
            System.out.println("Database error during task assignment:");
            e.printStackTrace();
            return false;
        }
    }

    public boolean createProject(String projectName, int departmentId){
        String query = "INSERT INTO PROJECT (Project_Name, Department_ID) VALUES (?, ?)";
        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)){
        
            stmt.setString(1, projectName);
            stmt.setInt(2, departmentId);
        
            int rowsInserted = stmt.executeUpdate();
            return rowsInserted>0;
        }
        catch(SQLException e){
            System.out.println("Database error during project creation:");
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasIncompleteTask(int employeeId){
        String query = "SELECT Status FROM TASK WHERE Employee_ID = ? AND Status != 'Completed'";
        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)){
            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            // true means pending or in progress task
            return rs.next(); 
        }
        catch(SQLException e){
            e.printStackTrace();
            return true; // assume they are busy if the database throws an error
        }
    }

    public boolean completeTask(int employeeId){
        String query = "UPDATE TASK SET Status = 'Completed' WHERE Employee_ID = ? AND Status != 'Completed'";
        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
             
            stmt.setInt(1, employeeId);
            int rowsUpdated = stmt.executeUpdate();
            
            return rowsUpdated > 0; // returns true if a task was actually updated
        }
        catch(SQLException e){
            System.out.println("Database error during task completion:");
            e.printStackTrace();
            return false;
        }
    }
}
