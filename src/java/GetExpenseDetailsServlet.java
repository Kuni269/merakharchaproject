import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Fetches details for a single expense and returns them as JSON.
 * This acts as a mini-API for the editexpense.html page.
 */
@WebServlet("/GetExpenseDetailsServlet")
public class GetExpenseDetailsServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        Integer userId = (session != null) ? (Integer) session.getAttribute("id") : null;

        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Send 401 Unauthorized
            return;
        }

        String expenseIdStr = request.getParameter("id");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            int expenseId = Integer.parseInt(expenseIdStr);
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/kunal_kitab", "root", "")) {
                String sql = "SELECT date, descri, amt, paytmd FROM addexpense WHERE expense_id = ? AND user_id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, expenseId);
                ps.setInt(2, userId);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    // Manually build a JSON object string with the expense data.
                    String json = String.format("{\"date\":\"%s\", \"descri\":\"%s\", \"amt\":%.2f, \"paytmd\":\"%s\"}",
                            rs.getDate("date"),
                            rs.getString("descri").replace("\"", "\\\""), // Escape quotes
                            rs.getDouble("amt"),
                            rs.getString("paytmd"));
                    out.print(json);
                } else {
                    out.print("{\"error\":\"Expense not found or you do not have permission.\"}");
                }
            }
        } catch (Exception e) {
            out.print("{\"error\":\"Server error: " + e.getMessage().replace("\"", "\\\"") + "\"}");
        }
        out.flush();
    }
}
