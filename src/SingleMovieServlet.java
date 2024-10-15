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
import java.sql.PreparedStatement;

@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie") // Correct URL pattern
public class SingleMovieServlet extends HttpServlet {
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

        // Extract movie ID from the request parameter
        String movieId = request.getParameter("id");
        if (movieId == null || movieId.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        System.out.println("Requested movie ID: " + movieId);

        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT m.id, m.title, m.year, m.director, r.rating, " +
                    "GROUP_CONCAT(DISTINCT s.id SEPARATOR ',') AS starIds, " +
                    "GROUP_CONCAT(DISTINCT s.name SEPARATOR ',') AS starNames, " +
                    "GROUP_CONCAT(DISTINCT g.name ORDER BY g.id SEPARATOR ', ') AS genres " +
                    "FROM movies m " +
                    "JOIN ratings r ON m.id = r.movieId " +
                    "LEFT JOIN genres_in_movies gm ON m.id = gm.movieId " +
                    "LEFT JOIN genres g ON gm.genreId = g.id " +
                    "LEFT JOIN stars_in_movies sm ON m.id = sm.movieId " +
                    "LEFT JOIN stars s ON sm.starId = s.id " +
                    "WHERE m.id = ? " +
                    "GROUP BY m.id, m.title, m.year, m.director, r.rating;"; // Include all non-aggregated columns


            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, movieId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", rs.getString("id"));
                jsonObject.addProperty("title", rs.getString("title"));
                jsonObject.addProperty("year", rs.getString("year"));
                jsonObject.addProperty("director", rs.getString("director"));
                jsonObject.addProperty("rating", rs.getFloat("rating"));
                jsonObject.addProperty("genres", rs.getString("genres"));

                // Combine star IDs and names
                String starIds = rs.getString("starIds");
                String starNames = rs.getString("starNames");
                if (starIds != null && starNames != null) {
                    String[] ids = starIds.split(",");
                    String[] names = starNames.split(",");
                    StringBuilder starsBuilder = new StringBuilder();
                    for (int i = 0; i < ids.length; i++) {
                        if (i > 0) {
                            starsBuilder.append(", "); // Add a comma separator
                        }
                        starsBuilder.append(ids[i]).append(":").append(names[i]); // Format as id:name
                    }
                    jsonObject.addProperty("stars", starsBuilder.toString());
                } else {
                    jsonObject.addProperty("stars", "0"); // Default to "0" if no stars found
                }

                out.write(jsonObject.toString());
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write("{\"errorMessage\":\"Movie not found\"}");
            }
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.close();
        }
    }
}