import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class App {
    public static void main(String[] args) {

        System.out.println("Starting Workforce Analytics Backend...");
        
        SimpleWebServer server = new SimpleWebServer();
        server.startServer();

        
        
        // was testing the connection, login, signup as i was moving on.. so didn't want to delete everything that's why i commented them

        
        // XAMPP CONNECTION TESTING

        // String url = "jdbc:mysql://localhost:3306/workforce_analytics";
        // String user = "root";
        // String password = ""; 

        // System.out.println("Attempting to connect to the XAMPP database...");

        // try {
        //     Connection connection = DriverManager.getConnection(url, user, password);
        //     System.out.println("SUCCESS: Connected to the workforce_analytics database!");
            
        //     connection.close();
            
        // } catch (SQLException e) {
        //     System.out.println("FAILED: Could not connect to the database. Check if XAMPP MySQL is running.");
        //     e.printStackTrace();
        // }
         

        
        
        
        // LOGIN TESTING

    //     AuthService auth = new AuthService();

    //     System.out.println("--- Testing Login ---");
        
    //     Employee loggedInUser = auth.login("t.stark@starkindustries.com", "ironman");
        
    //     if(loggedInUser != null){
    //         System.out.println("SUCCESS! Logged in as: " + loggedInUser.getFirstName() + " " + loggedInUser.getLastName());
    //         System.out.println("Role: " + loggedInUser.getRole());
    //     }
    //     else{
    //         System.out.println("FAILED! Invalid email or password.");
    //     }






    // // SIGNUP TESTINGG

    // System.out.println("\n--- Testing Signup ---");

    //     // Creating a brand new employee (Sam Wilson)
    // Employee newHire = new Employee(
    //     21, 
    //     "Sam", 
    //     "Wilson", 
    //     "s.wilson@starkindustries.com", 
    //     "falcon", 
    //     "Aero Engineer", 
    //     "Active", 
    //     1, 
    //     101, 
    //     1
    // );

    // boolean isRegistered = auth.signup(newHire);
    
    // if (isRegistered) {
    //     System.out.println("SUCCESS! Sam Wilson was added to the database.");

    //     Employee verifyLogin = auth.login("s.wilson@starkindustries.com", "falcon");
    //     if (verifyLogin != null) {
    //         System.out.println("VERIFIED: New user can successfully log in!");
    //     }
    //     } else {
    //         System.out.println("FAILED! Could not register the new employee.");
    //     }
    }
}