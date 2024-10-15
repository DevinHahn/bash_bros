import com.google.gson.JsonObject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;

@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Retrieve the star ID from the query parameter
        String starId = request.getParameter("id");
        if (starId == null || starId.isEmpty()) {
            response.setStatus(400); // Bad request
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT s.id, s.name, s.birthYear, " +
                    "GROUP_CONCAT(DISTINCT m.id ORDER BY m.id SEPARATOR ', ') AS movieIds, " +
                    "GROUP_CONCAT(DISTINCT m.title ORDER BY m.id SEPARATOR ', ') AS movies " +
                    "FROM stars s " +
                    "LEFT JOIN stars_in_movies sm ON s.id = sm.starId " +
                    "LEFT JOIN movies m ON sm.movieId = m.id " +
                    "WHERE s.id = ? " +
                    "GROUP BY s.id;";

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, starId); // Bind the starId to the prepared statement
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", rs.getString("id"));
                jsonObject.addProperty("name", rs.getString("name"));
                jsonObject.addProperty("birthYear", rs.getInt("birthYear"));
                jsonObject.addProperty("movies", rs.getString("movies"));
                jsonObject.addProperty("movieIds", rs.getString("movieIds")); // Add movie IDs to JSON
                out.write(jsonObject.toString());
                response.setStatus(200);
            } else {
                response.setStatus(404); // Star not found
            }
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500); // Internal server error
        } finally {
            out.close();
        }
    }
}