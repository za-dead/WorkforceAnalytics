import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
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
                if (emp != null) {
                    // fetching the relational data
                    String deptName = auth.getDepartmentName(emp.getDepartmentId());
                    String projName = auth.getProjectName(emp.getProjectId());
                    String supName = auth.getSupervisorName(emp.getSupervisorId());
                    String taskDetails = auth.getTaskDetails(emp.getEmployeeId());
                    
                    // supervisor part
                    String supervisorDisplay = (supName == null) ? 
                        "Supervisor: There's no one above you, you are the boss here." : 
                        "Supervisor: " + supName;

                    htmlResponse = "<h2>Login Successful!</h2>" +
                                   "<p>Welcome to the dashboard, " + emp.getFirstName() + " " + emp.getLastName() + ".</p>" +
                                   "<p><b>Employee ID:</b> " + emp.getEmployeeId() + "</p>" +
                                   "<p><b>Department:</b> " + deptName + "</p>" +
                                   "<p><b>Role:</b> " + emp.getRole() + "</p>" +
                                   "<p><b>" + supervisorDisplay + "</b></p>" +
                                   "<p><b>Project:</b> " + projName + " (ID: " + emp.getProjectId() + ")</p>" +
                                   "<p><b>Current Task:</b> " + taskDetails + "</p>";
                }
                else{
                    htmlResponse = "<h2>Login Failed</h2><p>Invalid email or password. Please try again.</p>";
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
                
                // auto assign supervisor and project ids
                int assignedSupervisor;
                int assignedProject;
                switch (deptId) {
                    case 1: assignedSupervisor = 1;  assignedProject = 101; break;  
                    case 2: assignedSupervisor = 5;  assignedProject = 102; break;  
                    case 3: assignedSupervisor = 9;  assignedProject = 103; break;  
                    case 4: assignedSupervisor = 13; assignedProject = 104; break; 
                    case 5: assignedSupervisor = 17; assignedProject = 105; break; 
                    default: assignedSupervisor = 1; assignedProject = 101; break; 
                }
                newEmp.setSupervisorId(assignedSupervisor); 
                newEmp.setProjectId(assignedProject);

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
}