import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class JDBCTest {
    public static void main(String[] args) {
        // Database credentials and URL
        String jdbcURL = "jdbc:mysql://localhost:3306/moviedb";  // Ensure this matches your actual database name
        String username = "mytestuser";  // Replace with your database username
        String password = "My6$Password"; // Ensure this matches your MySQL root password

        try {
            // Establish the connection
            Connection connection = DriverManager.getConnection(jdbcURL, username, password);

            // Create a statement
            Statement statement = connection.createStatement();

            // Write a simple query to test
            String query = "SELECT * FROM movies LIMIT 5";  // This selects 5 movies as a test

            // Execute the query and get the results
            ResultSet resultSet = statement.executeQuery(query);

            // Loop through and print the results
            while (resultSet.next()) {
                String movieTitle = resultSet.getString("title");
                System.out.println("Movie: " + movieTitle);
            }

            // Close connections
            resultSet.close();
            statement.close();
            connection.close();

            System.out.println("Database connection test successful!");

        } catch (Exception e) {
            // Print any error
            e.printStackTrace();
            System.out.println("Database connection failed!");
        }
    }
}