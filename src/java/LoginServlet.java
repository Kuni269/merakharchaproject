import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import org.mindrot.jbcrypt.BCrypt;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || password == null ||
            email.trim().isEmpty() || password.trim().isEmpty()) {
            try (PrintWriter out = response.getWriter()) {
                out.println("<p style='color:red'>Please enter both email and password.</p>");
            }
            return;
        }

        try (PrintWriter out = response.getWriter()) {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Updated driver for MySQL 8+

            try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/kunal_kitab", "root", "");
                 PreparedStatement ps = con.prepareStatement("SELECT id, unm, passwd FROM rag WHERE ema=?")) {

                ps.setString(1, email.trim());

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String storedHash = rs.getString("passwd");

                        if (BCrypt.checkpw(password.trim(), storedHash)) {
                            int userId = rs.getInt("id");
                            String userName = rs.getString("unm");

                            HttpSession session = request.getSession();
                            session.setAttribute("id", userId);
                            session.setAttribute("unm", userName);

                            response.sendRedirect("dashboard");
                        } else {
                            out.println("<p style='color:red'>Invalid email or password.</p>");
                            request.getRequestDispatcher("login.html").include(request, response);
                        }
                    } else {
                        out.println("<p style='color:red'>Invalid email or password.</p>");
                        request.getRequestDispatcher("login.html").include(request, response);
                    }
                }
            }
        } catch (Exception ex) {
            try (PrintWriter out = response.getWriter()) {
                out.println("<p style='color:red'>Error: " + ex.getMessage() + "</p>");
            }
        }
    }
}