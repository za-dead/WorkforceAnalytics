import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class SimpleWebServer{

    public void startServer(){
        try{
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);     // create listening on port 8080
            
            server.createContext("/api/login", new LoginHandler());           
            server.createContext("/api/signup", new SignupHandler());
            server.createContext("/api/assign", new AssignTaskHandler());
            server.createContext("/api/createProject", new CreateProjectHandler());
            server.createContext("/api/completeTask", new CompleteTaskHandler());
            server.createContext("/api/departmentUpdates", new DepartmentUpdatesHandler());
            server.createContext("/api/projectProgress", new ProjectProgressHandler());
            server.createContext("/api/logHours", new LogHoursHandler());
            server.createContext("/api/logHoursPage", new LogHoursPageHandler());
            server.createContext("/api/workSummary", new WorkSummaryHandler());
            
            server.setExecutor(null); 
            server.start();
            System.out.println("Web Server is running! Waiting for browser requests on http://localhost:8080...");
            
        }
        catch (IOException e){
            System.out.println("Failed to start server.");
            e.printStackTrace();
        }
    }

    // The Login Endpoint Logic
    static class LoginHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange exchange) throws IOException{

            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");       // html file will be able to communicate with the server

            if("POST".equals(exchange.getRequestMethod())){

                String formData = new String(exchange.getRequestBody().readAllBytes());        // reading the data
                Map<String, String> inputs = parseFormData(formData);
                
                String email = inputs.get("email");
                String password = inputs.get("password");

                AuthService auth = new AuthService();                // checking data
                Employee emp = auth.login(email, password);

                String htmlResponse;        // building the response
                if(emp != null){
                    String deptName = auth.getDepartmentName(emp.getDepartmentId());
                    String supName = auth.getSupervisorName(emp.getSupervisorId());
                    
                    // checking if this user is a supervisor
                    boolean isSupervisor = (supName == null);
                    String supervisorDisplay = isSupervisor ? 
                        "Supervisor: There's no one above you, you are the boss." : 
                        "Supervisor: " + supName;

                    htmlResponse = "<h2>Login Successful!</h2>" +
                                   "<p>Welcome to the dashboard, " + emp.getFirstName() + " " + emp.getLastName() + ".</p>" +
                                   "<p><b>Employee ID:</b> " + emp.getEmployeeId() + "</p>" +
                                   "<p><b>Department:</b> " + deptName + "</p>" +
                                   "<p><b>Role:</b> " + emp.getRole() + "</p>" +
                                   "<p><b>" + supervisorDisplay + "</b></p>";
                    
                    //dashboard shortcuts
                    htmlResponse += "<hr><h3>Dashboard Shortcuts</h3>" +
                                    "<div style='display: flex; gap: 10px; margin-bottom: 20px;'>" +
                                    
                                    "<form action='/api/workSummary' method='POST' style='margin: 0;'>" +
                                    "<input type='hidden' name='empId' value='" + emp.getEmployeeId() + "'>" +
                                    "<input type='hidden' name='empName' value='" + emp.getFirstName() + " " + emp.getLastName() + "'>" +
                                    "<button type='submit' style='padding: 10px 20px; background: #6f42c1; color: white; border: none; cursor: pointer; border-radius: 4px; font-weight: bold;'>View My Work Summary</button>" +
                                    "</form>" +
                                    
                                    "<form action='/api/logHoursPage' method='POST' style='margin: 0;'>" +
                                    "<input type='hidden' name='empId' value='" + emp.getEmployeeId() + "'>" +
                                    "<input type='hidden' name='empName' value='" + emp.getFirstName() + " " + emp.getLastName() + "'>" +
                                    "<input type='hidden' name='isSupervisor' value='" + isSupervisor + "'>" + // Passing role flag here!
                                    "<button type='submit' style='padding: 10px 20px; background: #17a2b8; color: white; border: none; cursor: pointer; border-radius: 4px; font-weight: bold;'>Log Work Hours</button>" +
                                    "</form>" +
                                    
                                    "</div>";

                    // role based interface
                    if(isSupervisor){
                        String empDropdown = auth.getEmployeeDropdownHTML(emp.getDepartmentId(), emp.getEmployeeId());
                        String projDropdown = auth.getProjectDropdownHTML(emp.getDepartmentId());

                        htmlResponse += "<hr><h3>Supervisor Control Panel</h3>" +

                                        "<div style='display: flex; gap: 10px; margin-bottom: 20px;'>" +
                                        "<form action='/api/departmentUpdates' method='POST' style='margin: 0;'>" +
                                        "<input type='hidden' name='supervisorId' value='" + emp.getEmployeeId() + "'>" +
                                        "<button type='submit' style='padding: 10px 20px; background: #6f42c1; color: white; border: none; cursor: pointer; border-radius: 4px; font-weight: bold; font-size: 14px;'>" +
                                        "View Department Project & Task Updates</button>" +
                                        "</form>" +
                                        
                                        "<form action='/api/projectProgress' method='POST' style='margin: 0;'>" +
                                        "<input type='hidden' name='deptId' value='" + emp.getDepartmentId() + "'>" +
                                        "<input type='hidden' name='deptName' value='" + deptName + "'>" + 
                                        "<button type='submit' style='padding: 10px 20px; background: #17a2b8; color: white; border: none; cursor: pointer; border-radius: 4px; font-weight: bold; font-size: 14px;'>" +
                                        "View Project Completion Progress</button>" +
                                        "</form>" +
                                        "</div>" +

                                        "<div style='background: #f4f4f4; padding: 15px; border-radius: 5px; margin-bottom: 15px;'>" +
                                        "<h4>Assign Employee to Project & Task</h4>" +
                                        "<form action='/api/assign' method='POST'>" +
                                        "<select name='targetEmpId' required style='padding: 8px; margin-right: 5px; width: 180px;'>" +
                                        "<option value='' disabled selected>Select Employee...</option>" +
                                        empDropdown +
                                        "</select>" +
                                        "<select name='targetProjId' required style='padding: 8px; margin-right: 5px; width: 180px;'>" +
                                        "<option value='' disabled selected>Select Project...</option>" +
                                        projDropdown +
                                        "</select>" +
                                        "<input type='text' name='taskDesc' placeholder='Task Description...' required style='padding: 8px; margin-right: 5px; width: 200px;'>" +
                                        "<input type='date' name='deadline' required style='padding: 8px; margin-right: 5px;'>" +
                                        "<button type='submit' style='padding: 9px 15px; background: #007bff; color: white; border: none; cursor: pointer; border-radius: 3px;'>Assign Task</button>" +
                                        "</form>" +
                                        "</div>" +
                                        
                                        "<div style='background: #e9ecef; padding: 15px; border-radius: 5px;'>" +
                                        "<h4>Create New Project</h4>" +
                                        "<form action='/api/createProject' method='POST'>" +
                                        "<input type='hidden' name='deptId' value='" + emp.getDepartmentId() + "'>" +
                                        "<input type='text' name='newProjName' placeholder='New Project Name' required style='padding: 8px; margin-right: 5px; width: 250px;'>" +
                                        "<button type='submit' style='padding: 9px 15px; background: #17a2b8; color: white; border: none; cursor: pointer; border-radius: 3px;'>Create Project</button>" +
                                        "</form>" +
                                        "</div>";
                    }

                    // logout for everyone
                    htmlResponse += "<br><br><button style='padding: 10px; background: #dc3545; color: white; border: none; cursor: pointer;' " +
                                   "onclick='window.history.back()'>Logout</button>";
                }
                else{
                    htmlResponse = "<h2>Login Failed</h2><p>Invalid email or password. Please try again.</p>" +
                                   "<br><button style='padding: 10px; background: #6c757d; color: white; border: none; cursor: pointer;' " +
                                   "onclick='window.history.back()'>Go Back</button>";
                }

                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");

                exchange.sendResponseHeaders(200, htmlResponse.length());          // sending response back to the browser
                OutputStream os = exchange.getResponseBody();
                os.write(htmlResponse.getBytes());
                os.close();
            }
        }
    }

    // The Signup Endpoint Logic
    static class SignupHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange exchange) throws IOException{
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

            if ("POST".equals(exchange.getRequestMethod())){
                String formData = new String(exchange.getRequestBody().readAllBytes());
                Map<String, String> inputs = parseFormData(formData);
                
                AuthService auth = new AuthService();
                Employee newEmp = new Employee();
                
                // auto assing emp id
                newEmp.setEmployeeId(auth.getNextEmployeeId()); 

                newEmp.setFirstName(inputs.get("firstName"));
                newEmp.setLastName(inputs.get("lastName"));
                newEmp.setEmail(inputs.get("email"));
                newEmp.setPassword(inputs.get("password"));
                newEmp.setRole(inputs.get("role"));
                newEmp.setStatus("Active"); 
                
                int deptId = Integer.parseInt(inputs.get("departmentId"));
                newEmp.setDepartmentId(deptId);
                
                // auto assign supervisor
                int assignedSupervisor;
                switch (deptId){
                    case 1: assignedSupervisor = 1; break;  
                    case 2: assignedSupervisor = 5; break;  
                    case 3: assignedSupervisor = 9; break;  
                    case 4: assignedSupervisor = 13; break; 
                    case 5: assignedSupervisor = 17; break; 
                    default: assignedSupervisor = 1; break; 
                }
                newEmp.setSupervisorId(assignedSupervisor); 
                newEmp.setProjectId(0);

                boolean success = auth.signup(newEmp);

                String htmlResponse;
                if (success) {
                    // fetching the department they joined.. and going back 2 pages after clicking go back(from submit to signup to login)
                    String deptName = auth.getDepartmentName(newEmp.getDepartmentId());

                    htmlResponse = "<h2>Signup Successful!</h2>" +
                                   "<p>Welcome to Stark Industries, " + newEmp.getFirstName() + ".</p>" +
                                   "<p><b>Your new Employee ID is " + newEmp.getEmployeeId() + "</b></p>" +
                                   "<p><b>Your department is " + deptName + "</b></p>" +
                                   "<p><i>Hope you enjoy your time here.</i></p>" +
                                   "<br>" +
                                   "<button style='padding: 10px; background: #28a745; color: white; border: none; cursor: pointer;' " +
                                   "onclick='window.history.go(-2)'>Go to Login</button>";
                }
                else{
                    htmlResponse = "<h2>Signup Failed</h2><p>That Email might already be in use. Please try again.</p>" +
                                   "<button style='padding: 10px; background: #dc3545; color: white; border: none; cursor: pointer;' " +
                                   "onclick='window.history.back()'>Go Back</button>";
                }

                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, htmlResponse.length());
                OutputStream os = exchange.getResponseBody();
                os.write(htmlResponse.getBytes());
                os.close();
            }
        }
    }

    private static Map<String, String> parseFormData(String formData) throws IOException{      // decoding the html form data
        Map<String, String> map = new HashMap<>();
        String[] pairs = formData.split("&");
        for(String pair : pairs){
            String[] keyValue = pair.split("=");
            if(keyValue.length == 2){
                String key = URLDecoder.decode(keyValue[0], "UTF-8");
                String value = URLDecoder.decode(keyValue[1], "UTF-8");
                map.put(key, value);
            }
        }
        return map;
    }

    static class AssignTaskHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange exchange) throws IOException{
            if("POST".equalsIgnoreCase(exchange.getRequestMethod())){
                
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String formData = br.readLine();
                
                Map<String, String> inputs = parseFormData(formData);
                int targetEmpId = Integer.parseInt(inputs.get("targetEmpId"));
                int targetProjId = Integer.parseInt(inputs.get("targetProjId"));
                String taskDesc = inputs.get("taskDesc");
                String deadline = inputs.get("deadline");
                
                AuthService auth = new AuthService();
                String htmlResponse;
                
                // deadline checking
                if (auth.hasIncompleteTask(targetEmpId)) {
                    htmlResponse = "<h2>Assignment Blocked</h2>" +
                                   "<p>This employee already has a task that is not complete yet.</p>" +
                                   "<br><button style='padding: 10px; background: #ffc107; color: black; border: none; cursor: pointer;' " +
                                   "onclick='window.history.back()'>Go Back</button>";
                }
                else{
                    // if they are free
                    boolean success = auth.assignEmployeeToProject(targetEmpId, targetProjId, taskDesc, deadline);
                    
                    if(success){
                        htmlResponse = "<h2>Assignment Successful!</h2>" +
                                       "<p>The employee has been successfully assigned to their new project and task.</p>" +
                                       "<br><button style='padding: 10px; background: #28a745; color: white; border: none; cursor: pointer;' " +
                                       "onclick='window.history.back()'>Go Back to Dashboard</button>";
                    }
                    else{
                        htmlResponse = "<h2>Assignment Failed</h2><p>There was a database error. Please check your VS Code terminal.</p>" +
                                       "<br><button style='padding: 10px; background: #6c757d; color: white; border: none; cursor: pointer;' " +
                                       "onclick='window.history.back()'>Go Back</button>";
                    }
                }
                
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, htmlResponse.length());
                OutputStream os = exchange.getResponseBody();
                os.write(htmlResponse.getBytes());
                os.close();
            }
        }
    }

    static class CreateProjectHandler implements HttpHandler {
    @Override
        public void handle(HttpExchange exchange) throws IOException{
            if("POST".equalsIgnoreCase(exchange.getRequestMethod())){
                // read form inputs
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String formData = br.readLine();
            
                Map<String, String> inputs = parseFormData(formData);
                String projectName = inputs.get("newProjName");
                int deptId = Integer.parseInt(inputs.get("deptId"));
                
                // insert project into database
                AuthService auth = new AuthService();
                boolean success = auth.createProject(projectName, deptId);
            
                // build response
                String htmlResponse;
                if(success){
                    htmlResponse = "<h2>Project Created Successfully!</h2>" +
                                   "<p>The project <b>" + projectName + "</b> has been added to your department.</p>" +
                                   "<br><button style='padding: 10px; background: #17a2b8; color: white; border: none; cursor: pointer;' " +
                                   "onclick='window.history.back()'>Go Back to Dashboard</button>";
                }
                else{
                    htmlResponse = "<h2>Failed to Create Project</h2><p>Please check the database connection and try again.</p>" +
                                   "<br><button style='padding: 10px; background: #6c757d; color: white; border: none; cursor: pointer;' " +
                                   "onclick='window.history.back()'>Go Back</button>";
                }
            
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, htmlResponse.length());
                OutputStream os = exchange.getResponseBody();
                os.write(htmlResponse.getBytes());
                os.close();
            }
        }
    }

    static class CompleteTaskHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange exchange) throws IOException{
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())){
                
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String formData = br.readLine();
                
                Map<String, String> inputs = parseFormData(formData);
                int empId = Integer.parseInt(inputs.get("empId"));
                
                AuthService auth = new AuthService();
                boolean success = auth.completeTask(empId);
                
                String htmlResponse;
                if(success){
                    htmlResponse = "<h2>Task Completed!</h2>" +
                                   "<p>Great job! Your supervisor will now see that you have finished your work.</p>" +
                                   "<br><button style='padding: 10px; background: #28a745; color: white; border: none; cursor: pointer;' " +
                                   "onclick='window.history.back()'>Go Back</button>";
                }
                else{
                    htmlResponse = "<h2>Update Failed</h2><p>Could not update the task status. Please try again.</p>" +
                                   "<br><button style='padding: 10px; background: #6c757d; color: white; border: none; cursor: pointer;' " +
                                   "onclick='window.history.back()'>Go Back</button>";
                }
                
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, htmlResponse.length());
                OutputStream os = exchange.getResponseBody();
                os.write(htmlResponse.getBytes());
                os.close();
            }
        }
    }

    static class DepartmentUpdatesHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange exchange) throws IOException{
            if("POST".equalsIgnoreCase(exchange.getRequestMethod())){
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String formData = br.readLine();
                
                Map<String, String> inputs = parseFormData(formData);
                int supervisorId = Integer.parseInt(inputs.get("supervisorId"));
                
                AuthService auth = new AuthService();
                String completedTasksHTML = auth.getCompletedTasksByProjectHTML(supervisorId);
                String runningTasksHTML = auth.getRunningTasksByProjectHTML(supervisorId);
                String overdueTasksHTML = auth.getOverdueTasksHTML(supervisorId);
                
                String htmlResponse = "<h2>Department Project & Task Updates</h2><hr>" +
                                      
                                      // completed Tasks
                                      "<div style='background: #e9ecef; padding: 15px; border-radius: 5px; margin-bottom: 20px;'>" +
                                      "<h3 style='margin-top:0; color: #343a40;'>Completed Tasks</h3>" +
                                      completedTasksHTML +
                                      "</div>" +
                                      
                                      // running Tasks
                                      "<div style='background: #e2eef9; padding: 15px; border-radius: 5px; margin-bottom: 20px; border-left: 5px solid #007bff;'>" +
                                      "<h3 style='margin-top:0; color: #0056b3;'>Active / Running Tasks</h3>" +
                                      runningTasksHTML +
                                      "</div>" +
                                      
                                      // overdue alerts
                                      "<div style='background: #fff3cd; padding: 15px; border-radius: 5px; margin-bottom: 20px; border-left: 5px solid #ffc107;'>" +
                                      "<h3 style='margin-top: 0; color: #856404;'>Overdue Task Alerts</h3>" +
                                      overdueTasksHTML +
                                      "</div>" +
                                      
                                      "<button style='padding: 10px 15px; background: #6c757d; color: white; border: none; cursor: pointer; border-radius: 3px;' " +
                                      "onclick='window.history.back()'>Go Back to Dashboard</button>";
                
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, htmlResponse.length());
                OutputStream os = exchange.getResponseBody();
                os.write(htmlResponse.getBytes());
                os.close();
            }
        }
    }

    static class ProjectProgressHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange exchange) throws IOException{
            if("POST".equalsIgnoreCase(exchange.getRequestMethod())){
                
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String formData = br.readLine();
                
                Map<String, String> inputs = parseFormData(formData);
                int deptId = Integer.parseInt(inputs.get("deptId"));
                String deptName = inputs.get("deptName");
                
                AuthService auth = new AuthService();
                String progressHTML = auth.getProjectProgressHTML(deptId);
                
                // stark industries ui temp
                String htmlResponse = "<html><body style='font-family: Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 40px;'>" +
                                      "<div style='max-width: 800px; margin: 0 auto; background: white; padding: 40px; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.05);'>" +
                                      
                                      // header
                                      "<div style='text-align: center; border-bottom: 2px solid #343a40; padding-bottom: 20px; margin-bottom: 30px;'>" +
                                      "<h1 style='margin: 0; color: #343a40; font-size: 36px; letter-spacing: 3px; text-transform: uppercase;'>Stark Industries</h1>" +
                                      "<h3 style='margin: 10px 0 0 0; color: #6c757d; font-weight: 400; font-size: 20px;'>" + deptName + " - Project Analytics</h3>" +
                                      "</div>" +
                                      
                                      // progress bar injection
                                      "<div style='margin-bottom: 40px;'>" +
                                      progressHTML +
                                      "</div>" +
                                      
                                      // back button
                                      "<div style='text-align: center;'>" +
                                      "<button style='padding: 12px 30px; background: #343a40; color: white; border: none; cursor: pointer; border-radius: 4px; font-weight: bold; text-transform: uppercase; letter-spacing: 1px;' " +
                                      "onclick='window.history.back()'>Return to Dashboard</button>" +
                                      "</div>" +
                                      
                                      "</div></body></html>";
                
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, htmlResponse.length());
                OutputStream os = exchange.getResponseBody();
                os.write(htmlResponse.getBytes());
                os.close();
            }
        }
    }

    static class LogHoursHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException{
            if("POST".equalsIgnoreCase(exchange.getRequestMethod())){
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String formData = br.readLine();
                
                Map<String, String> inputs = parseFormData(formData);
                int empId = Integer.parseInt(inputs.get("empId"));
                String logDate = inputs.get("logDate");
                double hours = Double.parseDouble(inputs.get("hours"));
                
                AuthService auth = new AuthService();
                int taskId = auth.getActiveTaskId(empId);
                
                String htmlResponse;
                if(taskId != -1 && auth.logWorkHours(empId, taskId, logDate, hours)){
                    
                    String[] taskInfo = auth.getActiveTaskInfo(empId);
                    String taskName = taskInfo[0];
                    String projectName = taskInfo[1];
                    
                    // success
                    htmlResponse = "<html><body style='font-family: Arial, sans-serif; background-color: #f4f6f9; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0;'>" +
                                   "<div style='background: #ffffff; padding: 40px; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.1); max-width: 550px; text-align: center; border-top: 5px solid #28a745;'>" +
                                   "<h2 style='color: #28a745; margin-top: 0; margin-bottom: 20px; font-size: 28px;'>Hours Logged Successfully!</h2>" +
                                   "<p style='color: #495057; font-size: 16px; line-height: 1.6; margin-bottom: 30px;'>" +
                                   "Your time of <b>" + hours + " hours</b> on <b>" + logDate + "</b> doing <i>" + taskName + "</i> under the <b>" + projectName + "</b> project has been officially recorded in the system.</p>" +
                                   "<button style='padding: 12px 25px; background: #17a2b8; color: white; border: none; cursor: pointer; border-radius: 4px; font-weight: bold; font-size: 15px;' " +
                                   "onclick='window.history.go(-2)'>Return to Dashboard</button>" +
                                   "</div></body></html>";
                }
                else{
                    // failure
                    htmlResponse = "<html><body style='font-family: Arial, sans-serif; background-color: #f4f6f9; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0;'>" +
                                   "<div style='background: #ffffff; padding: 40px; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.1); max-width: 500px; text-align: center; border-top: 5px solid #dc3545;'>" +
                                   "<h2 style='color: #dc3545; margin-top: 0; margin-bottom: 20px;'>Logging Failed</h2>" +
                                   "<p style='color: #495057; font-size: 16px; margin-bottom: 30px;'>Could not log your hours. Please try again or check the database connection.</p>" +
                                   "<button style='padding: 12px 25px; background: #6c757d; color: white; border: none; cursor: pointer; border-radius: 4px; font-weight: bold; font-size: 15px;' " +
                                   "onclick='window.history.back()'>Go Back</button>" +
                                   "</div></body></html>";
                }
                
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, htmlResponse.length());
                OutputStream os = exchange.getResponseBody();
                os.write(htmlResponse.getBytes());
                os.close();
            }
        }
    }

    static class LogHoursPageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String formData = br.readLine();
                
                Map<String, String> inputs = parseFormData(formData);
                int empId = Integer.parseInt(inputs.get("empId"));
                String empName = inputs.get("empName");
                boolean isSupervisor = Boolean.parseBoolean(inputs.get("isSupervisor")); // read role from the dashboard
                
                AuthService auth = new AuthService();
                String htmlResponse;
                
                if(!auth.hasIncompleteTask(empId)){
                    
                    // message based on role
                    String noTaskMsg = isSupervisor ? 
                                       "You are not currently working on any active tasks." : 
                                       "You are not currently assigned to any active tasks. Time logging is disabled until your supervisor assigns you a new task.";
                    
                    htmlResponse = "<html><body style='font-family: Arial, sans-serif; background-color: #f4f6f9; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0;'>" +
                                   "<div style='background: #ffffff; padding: 40px; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.1); max-width: 550px; text-align: center; border-top: 5px solid #ffc107;'>" +
                                   "<h2 style='color: #856404; margin-top: 0; margin-bottom: 20px; font-size: 28px;'>No Active Assignment</h2>" +
                                   "<p style='color: #495057; font-size: 16px; line-height: 1.6; margin-bottom: 30px;'>" + noTaskMsg + "</p>" +
                                   "<button style='padding: 12px 25px; background: #6c757d; color: white; border: none; cursor: pointer; border-radius: 4px; font-weight: bold; font-size: 15px;' " +
                                   "onclick='window.history.back()'>Return to Dashboard</button>" +
                                   "</div></body></html>";
                }
                else{
                    // time logging form
                    String[] taskInfo = auth.getActiveTaskInfo(empId);
                    String taskName = taskInfo[0];
                    String projectName = taskInfo[1];
                    
                    htmlResponse = "<html><body style='font-family: Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 40px;'>" +
                                   "<div style='max-width: 600px; margin: 0 auto; background: white; padding: 40px; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); border-top: 5px solid #17a2b8;'>" +
                                   
                                   "<div style='text-align: center; border-bottom: 2px solid #343a40; padding-bottom: 20px; margin-bottom: 30px;'>" +
                                   "<h1 style='margin: 0; color: #343a40; font-size: 32px; text-transform: uppercase; letter-spacing: 2px;'>Stark Industries</h1>" +
                                   "<h3 style='margin: 10px 0 0 0; color: #6c757d; font-weight: 400; font-size: 18px;'>" + empName + " - Time Entry</h3>" +
                                   "</div>" +
                                   
                                   "<div style='background: #e2eef9; padding: 20px; border-radius: 5px; margin-bottom: 30px; border-left: 5px solid #007bff;'>" +
                                   "<h4 style='margin: 0 0 10px 0; color: #0056b3;'>Current Assignment</h4>" +
                                   "<p style='margin: 0 0 5px 0; color: #495057;'>Project: <b>" + projectName + "</b></p>" +
                                   "<p style='margin: 0; color: #495057;'>Task: <b>" + taskName + "</b></p>" +
                                   "</div>" +
                                   
                                   "<form action='/api/logHours' method='POST' style='display: flex; flex-direction: column; gap: 15px;'>" +
                                   "<input type='hidden' name='empId' value='" + empId + "'>" +
                                   
                                   "<div>" +
                                   "<label style='display: block; font-weight: bold; margin-bottom: 5px; color: #343a40;'>Date of Work:</label>" +
                                   "<input type='date' name='logDate' required style='padding: 10px; border: 1px solid #ccc; border-radius: 4px; width: 100%; box-sizing: border-box;'>" +
                                   "</div>" +
                                   
                                   "<div>" +
                                   "<label style='display: block; font-weight: bold; margin-bottom: 5px; color: #343a40;'>Hours Logged:</label>" +
                                   "<input type='number' step='0.1' min='0.1' max='24' name='hours' placeholder='e.g., 6.5' required style='padding: 10px; border: 1px solid #ccc; border-radius: 4px; width: 100%; box-sizing: border-box;'>" +
                                   "</div>" +
                                   
                                   "<div style='display: flex; justify-content: space-between; margin-top: 20px;'>" +
                                   "<button type='button' style='padding: 12px 25px; background: #6c757d; color: white; border: none; cursor: pointer; border-radius: 4px; font-weight: bold;' onclick='window.history.back()'>Cancel</button>" +
                                   "<button type='submit' style='padding: 12px 25px; background: #17a2b8; color: white; border: none; cursor: pointer; border-radius: 4px; font-weight: bold;'>Submit Time Entry</button>" +
                                   "</div>" +
                                   
                                   "</form>" +
                                   "</div></body></html>";
                }
                
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, htmlResponse.length());
                OutputStream os = exchange.getResponseBody();
                os.write(htmlResponse.getBytes());
                os.close();
            }
        }
    }

    static class WorkSummaryHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange exchange) throws IOException{
            if("POST".equalsIgnoreCase(exchange.getRequestMethod())){
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String formData = br.readLine();
                
                Map<String, String> inputs = parseFormData(formData);
                int empId = Integer.parseInt(inputs.get("empId"));
                String empName = inputs.get("empName");
                
                AuthService auth = new AuthService();
                double weeklyHours = auth.getWeeklyHours(empId);
                String taskBreakdownHTML = auth.getEmployeeWorkSummaryHTML(empId);
                double avgProductivity = auth.getAverageDailyProductivity(empId);
                
                // color baSED STATUS ON BASELINE 8 HOURS
                String prodColor, prodStatus, prodIcon;
                if(avgProductivity == 0){
                    prodColor = "#6c757d"; prodStatus = "No Data (Last 30 Days)"; prodIcon = "&#9866;";
                }
                else if(avgProductivity < 7.0){
                    prodColor = "#ffc107"; prodStatus = "Under-Utilization (Below 8h baseline)"; prodIcon = "&#9888;";
                }
                else if(avgProductivity > 9.0){
                    prodColor = "#dc3545"; prodStatus = "Overwork Risk (Above 8h baseline)"; prodIcon = "&#9888;";
                }
                else{
                    prodColor = "#28a745"; prodStatus = "Optimal Utilization (~8h baseline)"; prodIcon = "&#10004;";
                }
                
                // layout
                String htmlResponse = "<html><body style='font-family: Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 40px;'>" +
                                      "<div style='max-width: 700px; margin: 0 auto; background: white; padding: 40px; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.05);'>" +
                                      
                                      // corporate header
                                      "<div style='text-align: center; border-bottom: 2px solid #343a40; padding-bottom: 20px; margin-bottom: 30px;'>" +
                                      "<h1 style='margin: 0; color: #343a40; font-size: 32px; text-transform: uppercase; letter-spacing: 2px;'>Stark Industries</h1>" +
                                      "<h3 style='margin: 10px 0 0 0; color: #6c757d; font-weight: 400; font-size: 18px;'>" + empName + " - Personal Work Summary</h3>" +
                                      "</div>" +
                                      
                                      // weekly hour
                                      "<div style='background: #f8f9fa; padding: 25px; border-radius: 8px; margin-bottom: 30px; text-align: center; border: 1px solid #dee2e6;'>" +
                                      "<h3 style='margin: 0 0 10px 0; color: #495057;'>Hours Logged (Last 7 Days)</h3>" +
                                      "<h1 style='margin: 0; color: #6f42c1; font-size: 54px;'>" + weeklyHours + "<span style='font-size: 20px; color: #6c757d;'> hrs</span></h1>" +
                                      "</div>" +
                                      
                                      // task breakdown
                                      "<h3 style='color: #212529; border-bottom: 1px solid #dee2e6; padding-bottom: 10px;'>Task Analytics Breakdown</h3>" +
                                      "<div style='margin-bottom: 10px;'>" +
                                      taskBreakdownHTML +
                                      "</div>" +
                                      
                                      // avg 30 day roductivity
                                      "<h3 style='color: #212529; border-bottom: 1px solid #dee2e6; padding-bottom: 10px; margin-top: 30px;'>30-Day Productivity Analysis</h3>" +
                                      "<div style='background: #fff; border: 1px solid #dee2e6; border-left: 5px solid " + prodColor + "; padding: 20px; border-radius: 4px; display: flex; align-items: center; justify-content: space-between; margin-bottom: 40px;'>" +
                                      "<div>" +
                                      "<h4 style='margin: 0 0 5px 0; color: #343a40; font-size: 18px;'>Average Daily Hours</h4>" +
                                      "<p style='margin: 0; color: " + prodColor + "; font-weight: bold; font-size: 14px;'>" + prodIcon + " " + prodStatus + "</p>" +
                                      "</div>" +
                                      "<h1 style='margin: 0; color: #343a40; font-size: 42px;'>" + avgProductivity + "<span style='font-size: 16px; color: #6c757d; font-weight: normal;'> hrs/day</span></h1>" +
                                      "</div>" +
                                      // ----------------------------------------
                                      
                                      //back
                                      "<div style='text-align: center;'>" +
                                      "<button style='padding: 12px 30px; background: #343a40; color: white; border: none; cursor: pointer; border-radius: 4px; font-weight: bold; text-transform: uppercase; letter-spacing: 1px;' " +
                                      "onclick='window.history.back()'>Return to Dashboard</button>" +
                                      "</div>" +
                                      
                                      "</div></body></html>";
                
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, htmlResponse.length());
                OutputStream os = exchange.getResponseBody();
                os.write(htmlResponse.getBytes());
                os.close();
            }
        }
    }
}