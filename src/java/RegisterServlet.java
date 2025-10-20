
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.util.regex.Pattern;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // --- Input Validation ---

        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";

        if (!Pattern.matches(emailRegex, email)) {
            out.println("<p style='color:red'>Invalid email format. Please enter a valid email.</p>");
            request.getRequestDispatcher("register.html").include(request, response);
            return;
        }

        if (!Pattern.matches(passwordRegex, password)) {
            out.println("<p style='color:red'>Password is too weak. It must be at least 8 characters and include an uppercase letter, a lowercase letter, a number, and a special character.</p>");
            request.getRequestDispatcher("register.html").include(request, response);
            return;
        }

        // --- Password Hashing (Important for Security) ---
        String hashedPassword = PasswordUtil.hashPassword(password);


        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/kunal_kitab", "root", "")) {
                String sql = "INSERT INTO rag (unm, ema, passwd) VALUES (?, ?, ?)";
                PreparedStatement ps = con.prepareStatement(sql);

                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, hashedPassword); // Store the hashed password

                if (ps.executeUpdate() > 0) {
                    out.println("<p style='color:green'>You are successfully registered! Please login.</p>");
                    request.getRequestDispatcher("login.html").include(request, response);
                }
            }
        } catch (ClassNotFoundException | SQLException | ServletException | IOException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("Duplicate entry")) {
                out.println("<p style='color:red'>This email address is already registered.</p>");
                request.getRequestDispatcher("register.html").include(request, response);
            } else {
                out.println("<p style='color:red'>An unexpected error occurred. Please try again.</p>");
            }
        }
    }
}