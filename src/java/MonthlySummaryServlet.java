import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.time.Month;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/MonthlySummaryServlet")
public class MonthlySummaryServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        Integer userId = (session != null) ? (Integer) session.getAttribute("id") : null;

        if (userId == null) {
            response.sendRedirect("login.html");
            return;
        }

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        try {
            int month = Integer.parseInt(request.getParameter("month"));
            int year = Integer.parseInt(request.getParameter("year"));
            
            String monthName = Month.of(month).name();
            monthName = monthName.charAt(0) + monthName.substring(1).toLowerCase();

            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/kunal_kitab", "root", "")) {
                
                // Corrected SQL to select 'expense_id' instead of 'id'.
                String sql = "SELECT expense_id, date, descri, amt, paytmd FROM addexpense WHERE user_id = ? AND YEAR(date) = ? AND MONTH(date) = ? ORDER BY date ASC";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, userId);
                ps.setInt(2, year);
                ps.setInt(3, month);

                ResultSet rs = ps.executeQuery();
                
                // --- Start of Styled HTML Output ---
                out.println("<!DOCTYPE html><html><head><title>Monthly Summary</title>");
                out.println("<script src='https://cdn.jsdelivr.net/npm/chart.js'></script>");
                out.println("<style>");
                out.println("body { font-family: 'Segoe UI', sans-serif; background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%); margin: 0; padding: 20px; }");
                out.println(".container { max-width: 800px; margin: 40px auto; background: rgba(255,255,255,0.9); padding: 30px; border-radius: 16px; box-shadow: 0 8px 24px rgba(0,0,0,0.15); }");
                out.println("h1, h2 { text-align: center; color: #0093E9; }");
                out.println(".chart-container { width: 60%; margin: 30px auto; }");
                out.println("table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
                out.println("th, td { padding: 12px; border: 1px solid #ddd; text-align: left; }");
                out.println("th { background-color: #0093E9; color: white; }");
                out.println("tr:nth-child(even) { background-color: #f9f9f9; }");
                out.println(".total { font-weight: bold; font-size: 1.2em; text-align: right; margin-top: 20px; color: #d9534f; }");
                out.println(".back-link { display: block; text-align: center; margin-top: 30px; font-weight: 600; color: #0093E9; text-decoration: none; }");
                out.println("</style></head><body>");
                
                out.println("<div class='container'>");
                out.println("<h1>Expense Summary</h1>");
                out.println("<h2>For " + monthName + ", " + year + "</h2>");

                out.println("<div class='chart-container'><canvas id='expenseChart'></canvas></div>");
                
                out.println("<table>");
                out.println("<tr><th>Date</th><th>Description</th><th>Amount (₹)</th><th>Payment Method</th><th>Actions</th></tr>");

                double totalAmount = 0;
                StringBuilder chartLabels = new StringBuilder();
                StringBuilder chartData = new StringBuilder();
                
                if (!rs.isBeforeFirst()) { 
                    out.println("<tr><td colspan='5' style='text-align:center;'>No expenses found for this period.</td></tr>");
                } else {
                    while (rs.next()) {
                        // Corrected to get the integer from the 'expense_id' column.
                        int expenseId = rs.getInt("expense_id");
                        double currentAmount = rs.getDouble("amt");
                        String currentDesc = rs.getString("descri");
                        totalAmount += currentAmount;
                        
                        chartLabels.append("'").append(currentDesc.replace("'", "\\'")).append("',");
                        chartData.append(currentAmount).append(",");

                        out.println("<tr>");
                        out.println("<td>" + rs.getDate("date") + "</td>");
                        out.println("<td>" + currentDesc + "</td>");
                        out.println("<td>" + String.format("%,.2f", rs.getDouble("amt")) + "</td>");
                        out.println("<td>" + rs.getString("paytmd") + "</td>");
                        out.println("<td>" +
                                    "<a href='editexpense.html?id=" + expenseId + "'>Edit</a> | " +
                                    "<a href='DeleteExpenseServlet?id=" + expenseId + "' onclick='return confirm(\"Are you sure you want to delete this expense?\");'>Delete</a>" +
                                    "</td>");
                        out.println("</tr>");
                    }
                }
                
                out.println("</table>");
                out.println("<p class='total'>Total Monthly Expense: ₹" + String.format("%,.2f", totalAmount) + "</p>");
                
                if (chartData.length() > 0) {
                    out.println("<script>");
                    out.println("const ctx = document.getElementById('expenseChart').getContext('2d');");
                    out.println("new Chart(ctx, {");
                    out.println("  type: 'pie',");
                    out.println("  data: {");
                    out.println("    labels: [" + chartLabels.substring(0, chartLabels.length() - 1) + "],");
                    out.println("    datasets: [{");
                    out.println("      label: 'Expense in ₹',");
                    out.println("      data: [" + chartData.substring(0, chartData.length() - 1) + "],");
                    out.println("      backgroundColor: ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40', '#C9CBCF'],");
                    out.println("      hoverOffset: 4");
                    out.println("    }]");
                    out.println("  }");
                    out.println("});");
                    out.println("</script>");
                }

                out.println("<a class='back-link' href='dashboard'>Back to Dashboard</a>");
                out.println("</div></body></html>");

            }
        } catch (NumberFormatException | ClassNotFoundException | SQLException e) {
            out.println("<p>Error: " + e.getMessage() + "</p>");
            e.printStackTrace(out);
        }
    }
}

