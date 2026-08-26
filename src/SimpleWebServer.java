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
            
            server.createContext("/api/login", new LoginHandler());            // endpoint for login to subm,mit
            server.createContext("/api/signup", new SignupHandler());
            server.createContext("/api/assign", new AssignTaskHandler());
            server.createContext("/api/createProject", new CreateProjectHandler());
            
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
                    
                    // role based dashboard split
                    if (isSupervisor) {
                        // fetching the dynamic html dropdown lists from the database
                        String empDropdown = auth.getEmployeeDropdownHTML(emp.getDepartmentId(), emp.getEmployeeId());
                        String projDropdown = auth.getProjectDropdownHTML(emp.getDepartmentId());

                        // functional supervisor control panel with dynamic <select> menus
                        htmlResponse += "<hr><h3>Supervisor Control Panel</h3>" +
                                        "<div style='background: #f4f4f4; padding: 15px; border-radius: 5px; margin-bottom: 15px;'>" +
                                        "<h4>Assign Employee to Project & Task</h4>" +
                                        "<form action='/api/assign' method='POST'>" +
                                        
                                        // dynamic employee dropdown
                                        "<select name='targetEmpId' required style='padding: 8px; margin-right: 5px; width: 180px;'>" +
                                        "<option value='' disabled selected>Select Employee...</option>" +
                                        empDropdown +
                                        "</select>" +
                                        
                                        // dynamic project dropdown
                                        "<select name='targetProjId' required style='padding: 8px; margin-right: 5px; width: 180px;'>" +
                                        "<option value='' disabled selected>Select Project...</option>" +
                                        projDropdown +
                                        "</select>" +
                                        
                                        "<input type='text' name='taskDesc' placeholder='Task Description...' required style='padding: 8px; margin-right: 5px; width: 200px;'>" +
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
                    else{
                        // regular employee Interface
                        String projName = (emp.getProjectId() == 0) ? "Not Assigned Yet" : auth.getProjectName(emp.getProjectId());
                        String taskDetails = (emp.getProjectId() == 0) ? "Awaiting Assignment" : auth.getTaskDetails(emp.getEmployeeId());
                        
                        htmlResponse += "<hr><h3>Your Work</h3>" +
                                        "<p><b>Project:</b> " + projName + "</p>" +
                                        "<p><b>Current Task:</b> " + taskDetails + "</p>";
                    }

                    // logout Button for everyone
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
                switch (deptId) {
                    case 1: assignedSupervisor = 1; break;  
                    case 2: assignedSupervisor = 5; break;  
                    case 3: assignedSupervisor = 9; break;  
                    case 4: assignedSupervisor = 13; break; 
                    case 5: assignedSupervisor = 17; break; 
                    default: assignedSupervisor = 1; break; 
                }
                newEmp.setSupervisorId(assignedSupervisor); 
                newEmp.setProjectId(0);switch (deptId) {
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
                
                // read the incoming input form data
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String formData = br.readLine();
                
                // parse the dynamic dropdowns and text input
                Map<String, String> inputs = parseFormData(formData);
                int targetEmpId = Integer.parseInt(inputs.get("targetEmpId"));
                int targetProjId = Integer.parseInt(inputs.get("targetProjId"));
                String taskDesc = inputs.get("taskDesc");
                
                // execute database updste
                AuthService auth = new AuthService();
                boolean success = auth.assignEmployeeToProject(targetEmpId, targetProjId, taskDesc);
                
                // building success/failure response screen
                String htmlResponse;
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
                
                // send it back to the browser
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
}