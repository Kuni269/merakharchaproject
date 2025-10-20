import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class DeleteExpenseServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        Integer userId = (session != null) ? (Integer) session.getAttribute("id") : null;

        if (userId == null) {
            response.sendRedirect("login.html");
            return;
        }

        try {
            int expenseId = Integer.parseInt(request.getParameter("id"));
            
            // Using modern driver and best practices
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // The try-with-resources block ensures that the connection and statement are
            // automatically closed, even if an error occurs. This is the best way to prevent locks.
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/kunal_kitab", "root", "");
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM addexpense WHERE expense_id = ? AND user_id = ?")) {
                
                ps.setInt(1, expenseId);
                ps.setInt(2, userId);

                int result = ps.executeUpdate();

                // It's good practice to check if the delete was successful
                if (result > 0) {
                    // Redirect to the dashboard or a success page
                    response.sendRedirect("dashboard");
                } else {
                    // This could happen if the expense_id was invalid or didn't belong to the user
                    response.getWriter().println("Error: Could not find the expense to delete.");
                }

            } catch (SQLException e) {
                // Log the exception and provide a user-friendly error message
                e.printStackTrace();
                response.getWriter().println("Error processing your request: A database error occurred.");
            }

        } catch (NumberFormatException e) {
            response.getWriter().println("Error: Invalid expense ID format.");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            response.getWriter().println("Error: Database driver not found.");
        }
    }
}

