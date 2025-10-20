import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Handles the submission of the edit expense form and updates the database.
 */
@WebServlet("/UpdateExpenseServlet")
public class UpdateExpenseServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        Integer userId = (session != null) ? (Integer) session.getAttribute("id") : null;

        if (userId == null) {
            response.sendRedirect("login.html");
            return;
        }

        try {
            // Retrieve all form fields, including the hidden expense ID.
            int expenseId = Integer.parseInt(request.getParameter("expenseId"));
            String dateStr = request.getParameter("date");
            String desc = request.getParameter("description");
            double amount = Double.parseDouble(request.getParameter("amount"));
            String[] modes = request.getParameterValues("paymentMethod");
            String paymentMethod = (modes != null) ? String.join(",", modes) : "";

            Date sqlDate = Date.valueOf(dateStr);

            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/kunal_kitab", "root", "")) {
                
                // SQL query to update the specified expense record.
                String sql = "UPDATE addexpense SET date = ?, descri = ?, amt = ?, paytmd = ? WHERE expense_id = ? AND user_id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setDate(1, sqlDate);
                ps.setString(2, desc);
                ps.setDouble(3, amount);
                ps.setString(4, paymentMethod);
                ps.setInt(5, expenseId);
                ps.setInt(6, userId);

                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    response.sendRedirect("dashboard");
                } else {
                    response.getWriter().println("Error: Update failed. Record not found or you do not have permission.");
                }
            }
        } catch (NumberFormatException | ClassNotFoundException | SQLException e) {
            response.getWriter().println("Error processing your request: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
