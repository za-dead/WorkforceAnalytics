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

    // generates html options for employees in a specific department
    public String getEmployeeDropdownHTML(int departmentId, int supervisorId) {
        StringBuilder html = new StringBuilder();
        String query = "SELECT Employee_ID, First_Name, Last_Name FROM EMPLOYEE WHERE Department_ID = ?";
        
        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)){
             
            stmt.setInt(1, departmentId);
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
        String query = "INSERT INTO PROJECT (Project_Name, Department_ID, Start_Date) VALUES (?, ?, CURDATE())";
        
        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)){
            
            stmt.setString(1, projectName);
            stmt.setInt(2, departmentId);
            
            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
            
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

    public String getOverdueTasksHTML(int supervisorId){
        StringBuilder html = new StringBuilder();
        
        String query = "SELECT p.Project_Name, e.First_Name, e.Last_Name, t.Task_Name, t.Deadline " +
                       "FROM TASK t " +
                       "JOIN EMPLOYEE e ON t.Employee_ID = e.Employee_ID " +
                       "JOIN PROJECT p ON t.Project_ID = p.Project_ID " +
                       "WHERE (e.Supervisor_ID = ? OR e.Employee_ID = ?) AND t.Status != 'Completed' AND t.Deadline < CURDATE() " +
                       "ORDER BY p.Project_Name";
                       
        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)){
             
            stmt.setInt(1, supervisorId);
            stmt.setInt(2, supervisorId);
            ResultSet rs = stmt.executeQuery();
            
            String currentProject = "";
            boolean hasOverdue = false;
            
            while(rs.next()){
                hasOverdue = true;
                String projName = rs.getString("Project_Name");
                
                // if a new project, create the same style header as the other sections
                if(!projName.equals(currentProject)){
                    if(!currentProject.isEmpty()){
                        html.append("</ul>"); 
                    }
                    html.append("<h4 style='color: #856404; margin-bottom: 5px; margin-top: 15px;'>Project: ").append(projName).append("</h4><ul style='margin-top: 5px; color: #dc3545; font-weight: bold;'>");
                    currentProject = projName;
                }
                
                html.append("<li style='margin-bottom: 5px;'>")
                    .append("<b>").append(rs.getString("First_Name")).append(" ").append(rs.getString("Last_Name")).append("</b> - ")
                    .append("<i>").append(rs.getString("Task_Name")).append("</i> ")
                    .append("<span style='font-size: 0.9em;'>(Due: ").append(rs.getString("Deadline")).append(")</span></li>");
            }
            
            if(hasOverdue){
                html.append("</ul>");
            }
            else{
                html.append("<p style='color: #28a745; font-weight: bold;'>All tasks are currently on schedule!</p>");
            }
            
        }
        catch(SQLException e){
            e.printStackTrace();
            return "<p style='color: red;'>Error loading overdue alerts.</p>";
        }
        
        return html.toString();
    }

    public String getCompletedTasksByProjectHTML(int supervisorId) {
        StringBuilder html = new StringBuilder();
        
        String query = "SELECT p.Project_Name, e.First_Name, e.Last_Name, t.Task_Name " +
                       "FROM TASK t " +
                       "JOIN EMPLOYEE e ON t.Employee_ID = e.Employee_ID " +
                       "JOIN PROJECT p ON t.Project_ID = p.Project_ID " +
                       "WHERE (e.Supervisor_ID = ? OR e.Employee_ID = ?) AND t.Status = 'Completed' " +
                       "ORDER BY p.Project_Name"; 
                       
        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)){
             
            stmt.setInt(1, supervisorId);
            stmt.setInt(2, supervisorId); // second parameter for the OR condition
            ResultSet rs = stmt.executeQuery();
            
            String currentProject = "";
            boolean hasCompleted = false;
            
            while(rs.next()){
                hasCompleted = true;
                String projName = rs.getString("Project_Name");
                
                if(!projName.equals(currentProject)){
                    if(!currentProject.isEmpty()){
                        html.append("</ul>"); 
                    }
                    html.append("<h4 style='color: #17a2b8; margin-bottom: 5px; margin-top: 15px;'>Project: ").append(projName).append("</h4><ul style='margin-top: 5px;'>");
                    currentProject = projName;
                }
                
                html.append("<li style='margin-bottom: 5px;'>")
                    .append("<b>").append(rs.getString("First_Name")).append(" ").append(rs.getString("Last_Name")).append("</b> finished ")
                    .append("<i>").append(rs.getString("Task_Name")).append("</i></li>");
            }
            
            if(hasCompleted){
                html.append("</ul>");
            }
            else{
                html.append("<p style='color: #6c757d; font-style: italic;'>No tasks have been completed yet.</p>");
            }
            
        }
        catch(SQLException e){
            e.printStackTrace();
            return "<p style='color: red;'>Error loading completed tasks.</p>";
        }
        
        return html.toString();
    }

    //collectrs active tasks
    public String getRunningTasksByProjectHTML(int supervisorId) {
        StringBuilder html = new StringBuilder();
        
        String query = "SELECT p.Project_Name, e.First_Name, e.Last_Name, t.Task_Name, t.Deadline " +
                       "FROM TASK t " +
                       "JOIN EMPLOYEE e ON t.Employee_ID = e.Employee_ID " +
                       "JOIN PROJECT p ON t.Project_ID = p.Project_ID " +
                       "WHERE (e.Supervisor_ID = ? OR e.Employee_ID = ?) AND t.Status != 'Completed' AND t.Deadline >= CURDATE() " +
                       "ORDER BY p.Project_Name";
                       
        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
             
            stmt.setInt(1, supervisorId);
            stmt.setInt(2, supervisorId); // second parameter for the OR condition
            ResultSet rs = stmt.executeQuery();
            
            String currentProject = "";
            boolean hasRunning = false;
            
            while(rs.next()){
                hasRunning = true;
                String projName = rs.getString("Project_Name");
                
                if(!projName.equals(currentProject)){
                    if(!currentProject.isEmpty()){
                        html.append("</ul>"); 
                    }
                    html.append("<h4 style='color: #0056b3; margin-bottom: 5px; margin-top: 15px;'>Project: ").append(projName).append("</h4><ul style='margin-top: 5px;'>");
                    currentProject = projName;
                }
                
                html.append("<li style='margin-bottom: 5px;'>")
                    .append("<b>").append(rs.getString("First_Name")).append(" ").append(rs.getString("Last_Name")).append("</b> is working on ")
                    .append("<i>").append(rs.getString("Task_Name")).append("</i> ")
                    .append("<span style='color: #6c757d; font-size: 0.9em;'>(Due: ").append(rs.getString("Deadline")).append(")</span></li>");
            }
            
            if(hasRunning){
                html.append("</ul>");
            }
            else{
                html.append("<p style='color: #6c757d; font-style: italic;'>No active tasks currently running.</p>");
            }
            
        }
        catch(SQLException e){
            e.printStackTrace();
            return "<p style='color: red;'>Error loading running tasks.</p>";
        }
        
        return html.toString();
    }

    // progress bars
    public String getProjectProgressHTML(int departmentId){
        StringBuilder html = new StringBuilder();
    
        String query = "SELECT p.Project_ID, p.Project_Name, " +
                       "COUNT(t.Task_ID) AS Total_Tasks, " +
                       "SUM(CASE WHEN t.Status = 'Completed' THEN 1 ELSE 0 END) AS Completed_Tasks " +
                       "FROM PROJECT p " +
                       "LEFT JOIN TASK t ON p.Project_ID = t.Project_ID " +
                       "WHERE p.Department_ID = ? " +
                       "GROUP BY p.Project_ID, p.Project_Name";
                   
        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)){
         
            stmt.setInt(1, departmentId);
            ResultSet rs = stmt.executeQuery();
        
            boolean hasProjects = false;
            
            while(rs.next()){
                hasProjects = true;
                String projName = rs.getString("Project_Name");
                int total = rs.getInt("Total_Tasks");
                int completed = rs.getInt("Completed_Tasks");
            
                int percentage = (total == 0) ? 0 : (int) Math.round(((double) completed / total) * 100);
            
                html.append("<div style='margin-bottom: 15px;'>")
                    .append("<div style='display: flex; justify-content: space-between; margin-bottom: 4px;'>")
                    .append("<b>").append(projName).append("</b>")
                    .append("<span>").append(completed).append("/").append(total).append(" tasks (").append(percentage).append("%)</span>")
                    .append("</div>")
                    // Progress Bar Container
                    .append("<div style='background: #e9ecef; border-radius: 6px; overflow: hidden; height: 18px;'>")
                    // Filled Progress Indicator
                    .append("<div style='width: ").append(percentage).append("%; background: #28a745; height: 100%; transition: width 0.4s;'></div>")
                    .append("</div>")
                    .append("</div>");
            }
        
            if(!hasProjects){
                html.append("<p style='color: #6c757d; font-style: italic;'>No projects registered for this department.</p>");
            }
        
        }
        catch(SQLException e){
            e.printStackTrace();
            return "<p style='color: red;'>Error calculating project progress.</p>";
        }
    
        return html.toString();
    }

    // ets the active task ID so employee doesn't have to manually select it
    public int getActiveTaskId(int employeeId){
        String query = "SELECT Task_ID FROM TASK WHERE Employee_ID = ? AND Status != 'Completed' LIMIT 1";
        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)){
            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) return rs.getInt("Task_ID");
        }
        catch(SQLException e){ 
            e.printStackTrace(); 
        }
        return -1;     //  no active task found
    }

    public String[] getActiveTaskInfo(int employeeId) {
        String query = "SELECT t.Task_Name, p.Project_Name FROM TASK t " +
                       "JOIN PROJECT p ON t.Project_ID = p.Project_ID " +
                       "WHERE t.Employee_ID = ? AND t.Status != 'Completed' LIMIT 1";
                       
        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)){
             
            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                return new String[] { rs.getString("Task_Name"), rs.getString("Project_Name") };
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return new String[] {"Unknown Task", "Unknown Project"};
    }

    // inserts the daily work hours into the database
    public boolean logWorkHours(int employeeId, int taskId, String logDate, double hours) {
        String query = "INSERT INTO TIME_LOG (Employee_ID, Task_ID, Log_Date, Hours_Worked) VALUES (?, ?, ?, ?)";
        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)){
            
            stmt.setInt(1, employeeId);
            stmt.setInt(2, taskId);
            stmt.setString(3, logDate);
            stmt.setDouble(4, hours);
            
            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
            
        }
        catch(SQLException e){
            System.out.println("Database error during logging hours:");
            e.printStackTrace();
            return false;
        }
    }

    // total hours in last 7 days
    public double getWeeklyHours(int employeeId){
        String query = "SELECT COALESCE(SUM(Hours_Worked), 0) AS Weekly_Hours " +
                       "FROM TIME_LOG WHERE Employee_ID = ? AND Log_Date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)){
            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble("Weekly_Hours");
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return 0.0;
    }

    // visuals
    public String getEmployeeWorkSummaryHTML(int employeeId) {
        StringBuilder html = new StringBuilder();
        
        // joins TASK, PROJECT, and TIME_LOG, grouping by task to sum up the hours
        String query = "SELECT t.Task_Name, t.Status, p.Project_Name, " +
                       "COALESCE(SUM(l.Hours_Worked), 0) AS Total_Hours " +
                       "FROM TASK t " +
                       "JOIN PROJECT p ON t.Project_ID = p.Project_ID " +
                       "LEFT JOIN TIME_LOG l ON t.Task_ID = l.Task_ID " +
                       "WHERE t.Employee_ID = ? " +
                       "GROUP BY t.Task_ID, t.Task_Name, t.Status, p.Project_Name " +
                       "ORDER BY CASE WHEN t.Status = 'Completed' THEN 1 ELSE 0 END, t.Task_ID DESC";
                       
        try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)){
             
            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            boolean hasTasks = false;
            
            while(rs.next()){
                hasTasks = true;
                String taskName = rs.getString("Task_Name");
                String projName = rs.getString("Project_Name");
                String status = rs.getString("Status");
                double hours = rs.getDouble("Total_Hours");
                
                if("Completed".equalsIgnoreCase(status)){
                    html.append("<div style='background: #e9ecef; border-left: 5px solid #28a745; padding: 15px; margin-bottom: 15px; border-radius: 4px;'>")
                        .append("<h4 style='margin: 0 0 10px 0; color: #343a40;'>&#10004; Finished Task: ").append(taskName).append("</h4>")
                        .append("<p style='margin: 0; color: #495057;'>Project: <b>").append(projName).append("</b></p>")
                        .append("<p style='margin: 5px 0 0 0; color: #28a745; font-weight: bold;'>Total Time Invested: ").append(hours).append(" hours</p>")
                        .append("</div>");
                }
                else{
                    html.append("<div style='background: #e2eef9; border-left: 5px solid #007bff; padding: 15px; margin-bottom: 15px; border-radius: 4px;'>")
                        .append("<h4 style='margin: 0 0 10px 0; color: #0056b3;'>&#9654; Current Task: ").append(taskName).append("</h4>")
                        .append("<p style='margin: 0; color: #495057;'>Project: <b>").append(projName).append("</b></p>")
                        
                        // flexbox to align textr left righty
                        .append("<div style='display: flex; justify-content: space-between; align-items: flex-end; margin-top: 5px;'>")
                        .append("<p style='margin: 0; color: #007bff; font-weight: bold;'>Time Logged So Far: ").append(hours).append(" hours</p>")
                        
                        // compltee button form
                        .append("<form action='/api/completeTask' method='POST' style='margin: 0;'>")
                        .append("<input type='hidden' name='empId' value='").append(employeeId).append("'>")
                        .append("<button type='submit' style='padding: 8px 15px; background: #28a745; color: white; border: none; cursor: pointer; border-radius: 4px; font-weight: bold; font-size: 13px;'>Mark Task as Completed</button>")
                        .append("</form>")
                        
                        .append("</div>") // close flex container
                        .append("</div>"); // Close blue box
                }
            }
            
            if(!hasTasks){
                html.append("<p style='color: #6c757d; font-style: italic;'>No task history found.</p>");
            }
        }
        catch(SQLException e){
            e.printStackTrace();
            return "<p style='color: red;'>Error generating work summary.</p>";
        }
        return html.toString();
    }

    // calculates the average hours per working day over the last 30 days
    public double getAverageDailyProductivity(int employeeId){
        String query = "SELECT COALESCE(SUM(Hours_Worked) / NULLIF(COUNT(DISTINCT Log_Date), 0), 0) AS Avg_Daily_Hours " +
                       "FROM TIME_LOG WHERE Employee_ID = ? AND Log_Date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)";
                       
        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)){
             
            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            
            if(rs.next()){
                double avg = rs.getDouble("Avg_Daily_Hours");
                return Math.round(avg * 10.0) / 10.0;       // rounded to 1 decimal place
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return 0.0;
    }
    
    public String getCompanyStructureHTML() {
        StringBuilder html = new StringBuilder();
        
        // group employees by department and role and count them
        String query = "SELECT d.Department_Name, e.Role, COUNT(e.Employee_ID) AS Headcount " +
                       "FROM EMPLOYEE e " +
                       "JOIN DEPARTMENT d ON e.Department_ID = d.Department_ID " +
                       "GROUP BY d.Department_Name, e.Role " +
                       "ORDER BY d.Department_Name, e.Role";

        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query);
        ResultSet rs = stmt.executeQuery()){

            String currentDept = "";
            boolean first = true;

            // css grid container
            html.append("<div style='display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 20px;'>");

            while(rs.next()){
                String deptName = rs.getString("Department_Name");
                String role = rs.getString("Role");
                int count = rs.getInt("Headcount");

                //new department new card
                if(!deptName.equals(currentDept)){
                    if(!first){
                        html.append("</ul></div>"); // close previous card
                    }
                    
                    // new dept card
                    html.append("<div style='background: white; border-top: 4px solid #343a40; border-radius: 6px; box-shadow: 0 4px 10px rgba(0,0,0,0.05); padding: 20px;'>")
                        .append("<h3 style='margin: 0 0 15px 0; color: #343a40; border-bottom: 1px solid #eee; padding-bottom: 10px;'>")
                        .append(deptName).append("</h3>")
                        .append("<ul style='list-style-type: none; padding: 0; margin: 0;'>");
                        
                    currentDept = deptName;
                    first = false;
                }

                // specific role and its headcount as a row 
                html.append("<li style='display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; color: #495057; font-size: 15px;'>")
                    .append("<span>").append(role).append("</span>")
                    .append("<span style='background: #e2eef9; color: #0056b3; padding: 4px 10px; border-radius: 12px; font-weight: bold; font-size: 13px;'>").append(count).append("</span>")
                    .append("</li>");
            }

            if(!first){
                html.append("</ul></div>"); // close the last card
            }
            else{
                html.append("<p style='color: #6c757d;'>No department data available.</p>");
            }

            html.append("</div>"); // close grid container

        }
        catch(SQLException e){
            e.printStackTrace();
            return "<p style='color: red;'>Error loading company structure.</p>";
        }

        return html.toString();
    }

    // employee directory
    public String getEmployeeDirectoryHTML(){
        StringBuilder html = new StringBuilder();
        
        String query = "SELECT e.Employee_ID, e.First_Name, e.Last_Name, d.Department_Name, e.Role, " +
                       "COALESCE(e.Status, 'Active') AS Status " + 
                       "FROM EMPLOYEE e " +
                       "JOIN DEPARTMENT d ON e.Department_ID = d.Department_ID " +
                       "ORDER BY e.First_Name, e.Last_Name";

        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query);
        ResultSet rs = stmt.executeQuery()){

            while(rs.next()){
                String name = rs.getString("First_Name") + " " + rs.getString("Last_Name");
                String dept = rs.getString("Department_Name");
                String role = rs.getString("Role");
                String status = rs.getString("Status");
                
                // instant filtyering
                html.append("<tr class='employee-row' data-name='").append(name.toLowerCase())
                    .append("' data-dept='").append(dept.toLowerCase())
                    .append("' data-status='").append(status.toLowerCase()).append("'>")
                    
                    .append("<td style='padding: 12px; border-bottom: 1px solid #dee2e6;'>").append(rs.getInt("Employee_ID")).append("</td>")
                    .append("<td style='padding: 12px; border-bottom: 1px solid #dee2e6; font-weight: bold; color: #343a40;'>").append(name).append("</td>")
                    .append("<td style='padding: 12px; border-bottom: 1px solid #dee2e6; color: #495057;'>").append(dept).append("</td>")
                    .append("<td style='padding: 12px; border-bottom: 1px solid #dee2e6; color: #495057;'>").append(role).append("</td>")
                    .append("<td style='padding: 12px; border-bottom: 1px solid #dee2e6;'>");
                
                // status
                if("Active".equalsIgnoreCase(status)){
                    html.append("<span style='background: #d4edda; color: #155724; padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: bold;'>Active</span>");
                }
                else{
                    html.append("<span style='background: #fff3cd; color: #856404; padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: bold;'>").append(status).append("</span>");
                }
                
                html.append("</td>");
                html.append("<td style='padding: 12px; border-bottom: 1px solid #dee2e6;'>")
                    .append("<form action='/api/viewHistory' method='POST' style='margin: 0;'>")
                    .append("<input type='hidden' name='empId' value='").append(rs.getInt("Employee_ID")).append("'>")
                    .append("<input type='hidden' name='empName' value='").append(name).append("'>")
                    .append("<button type='submit' style='background: #6c757d; color: white; border: none; padding: 6px 12px; border-radius: 4px; cursor: pointer; font-size: 12px; font-weight: bold;'>View History</button>")
                    .append("</form></td>")
                    .append("</tr>");
            }
        }
        catch(SQLException e){
            e.printStackTrace();
            return "<tr><td colspan='5' style='color: red; padding: 12px; text-align: center;'>Database Error: Ensure 'Status' column exists in EMPLOYEE table.</td></tr>";
        }
        return html.toString();
    }

    // logs old role then updates new role
    public boolean processEmployeeTransfer(int employeeId, int newDeptId, String newRole){
        String getOldInfoQuery = "SELECT Department_ID, Role FROM EMPLOYEE WHERE Employee_ID = ?";
        String logHistoryQuery = "INSERT INTO employment_history (Department_ID, Employment_ID, Transfer_Date, Past_Role) VALUES (?, ?, CURDATE(), ?)";
        String updateEmployeeQuery = "UPDATE EMPLOYEE SET Department_ID = ?, Role = ? WHERE Employee_ID = ?";

        try(Connection conn = DatabaseConnection.getConnection()){
            conn.setAutoCommit(false);

            int oldDeptId = 0;
            String oldRole = "";

            try(PreparedStatement getStmt = conn.prepareStatement(getOldInfoQuery)){
                getStmt.setInt(1, employeeId);
                ResultSet rs = getStmt.executeQuery();
                if(rs.next()){
                    oldDeptId = rs.getInt("Department_ID");
                    oldRole = rs.getString("Role");
                }
            }

            try(PreparedStatement logStmt = conn.prepareStatement(logHistoryQuery)) {
                logStmt.setInt(1, oldDeptId);
                logStmt.setInt(2, employeeId);
                logStmt.setString(3, oldRole);
                logStmt.executeUpdate();
            }

            try(PreparedStatement updateStmt = conn.prepareStatement(updateEmployeeQuery)){
                updateStmt.setInt(1, newDeptId);
                updateStmt.setString(2, newRole);
                updateStmt.setInt(3, employeeId);
                updateStmt.executeUpdate();
            }

            conn.commit();
            return true;
        }
        catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    // past role table
    public String getEmploymentHistoryHTML(int employeeId){
        StringBuilder html = new StringBuilder();
        String query = "SELECT d.Department_Name, eh.Transfer_Date, eh.Past_Role " +
                       "FROM employment_history eh " +
                       "JOIN DEPARTMENT d ON eh.Department_ID = d.Department_ID " +
                       "WHERE eh.Employment_ID = ? ORDER BY eh.Transfer_Date DESC";

        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)){
             
            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            
            boolean hasHistory = false;
            while(rs.next()){
                hasHistory = true;
                html.append("<tr>")
                    .append("<td style='padding: 12px; border-bottom: 1px solid #dee2e6;'>").append(rs.getString("Transfer_Date")).append("</td>")
                    .append("<td style='padding: 12px; border-bottom: 1px solid #dee2e6;'>").append(rs.getString("Department_Name")).append("</td>")
                    .append("<td style='padding: 12px; border-bottom: 1px solid #dee2e6; font-weight: bold;'>").append(rs.getString("Past_Role")).append("</td>")
                    .append("</tr>");
            }
            
            if(!hasHistory){
                return "<tr><td colspan='3' style='padding: 15px; text-align: center; color: #6c757d;'>No past employment history found on record.</td></tr>";
            }
        }
        catch(SQLException e){
            e.printStackTrace();
            return "<tr><td colspan='3' style='color: red;'>Error loading history.</td></tr>";
        }
        return html.toString();
    }

    public String getSubordinateDropdownHTML(int departmentId, int supervisorId){
        StringBuilder html = new StringBuilder();
        String query = "SELECT Employee_ID, First_Name, Last_Name FROM EMPLOYEE WHERE Department_ID = ? AND Employee_ID != ?";
        
        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)){
             
            stmt.setInt(1, departmentId);
            stmt.setInt(2, supervisorId);
            ResultSet rs = stmt.executeQuery();
            
            while(rs.next()){
                html.append("<option value='").append(rs.getInt("Employee_ID")).append("'>")
                    .append(rs.getString("First_Name")).append(" ").append(rs.getString("Last_Name"))
                    .append("</option>");
            }
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return html.toString();
    }

    public String getAllDepartmentsDropdownHTML(){
        StringBuilder html = new StringBuilder();
        String query = "SELECT Department_ID, Department_Name FROM DEPARTMENT ORDER BY Department_ID ASC";
        
        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query);
        ResultSet rs = stmt.executeQuery()) {
             
            while(rs.next()){
                html.append("<option value='").append(rs.getInt("Department_ID")).append("'>")
                    .append(rs.getString("Department_Name")).append("</option>");
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return html.toString();
    }   
}
