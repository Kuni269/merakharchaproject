import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

/**
 * Servlet to handle adding a new expense, including an optional receipt upload.
 * Annotated with @MultipartConfig to support file uploads.
 */
@MultipartConfig
public class AddExpenseservlet extends HttpServlet {
    
    // A constant for the directory where uploaded files will be stored.
    private static final String UPLOAD_DIRECTORY = "uploads";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Get the current user's session.
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("id");

        // If there's no user ID in the session, the user is not logged in.
        // Redirect them to the login page.
        if (userId == null) {
            response.sendRedirect("login.html");
            return;
        }

        // Retrieve all form fields.
        String dateStr = request.getParameter("date");
        String desc = request.getParameter("description");
        String amtStr = request.getParameter("amount");
        String[] modes = request.getParameterValues("paymentMethod");

        // --- Start: File Upload Handling ---
        Part filePart = request.getPart("receipt");
        String fileName = (filePart != null) ? Paths.get(filePart.getSubmittedFileName()).getFileName().toString() : ""; // Sanitize filename
        String receiptPath = null;

        if (fileName != null && !fileName.isEmpty()) {
            // Get the absolute path of the web application.
            String applicationPath = request.getServletContext().getRealPath("");
            String uploadFilePath = applicationPath + File.separator + UPLOAD_DIRECTORY;
            
            // Create the upload directory if it doesn't exist.
            File uploadDir = new File(uploadFilePath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Create a unique file name using a timestamp to avoid overwriting existing files.
            String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
            filePart.write(uploadFilePath + File.separator + uniqueFileName);
            
            // Store the relative path to be saved in the database.
            receiptPath = UPLOAD_DIRECTORY + "/" + uniqueFileName;
        }
        // --- End: File Upload Handling ---

        // --- Start: Data Parsing and Validation ---
        double amount;
        try {
            amount = Double.parseDouble(amtStr);
        } catch (NumberFormatException e) {
            response.getWriter().println("Invalid amount format.");
            return;
        }

        java.sql.Date sqlDate;
        try {
            sqlDate = java.sql.Date.valueOf(dateStr);
        } catch (IllegalArgumentException e) {
            response.getWriter().println("Invalid date format.");
            return;
        }
        // --- End: Data Parsing and Validation ---

        // Join the payment methods into a single comma-separated string.
        String paymentMethod = (modes != null) ? String.join(",", modes) : "";
        
        try {
            // Load the MySQL JDBC driver.
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Establish a connection to the database.
            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/kunal_kitab", "root", "")) {
                
                // SQL query to insert the new expense record, including the receipt path.
                String sql = "INSERT INTO addexpense (user_id, date, descri, amt, paytmd, receipt_path) VALUES (?, ?, ?, ?, ?, ?)";
                
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, userId);
                ps.setDate(2, sqlDate);
                ps.setString(3, desc);
                ps.setDouble(4, amount);
                ps.setString(5, paymentMethod);
                ps.setString(6, receiptPath); // Set the receipt path (can be null).

                // Execute the query and check if it was successful.
                if (ps.executeUpdate() > 0) {
                    // If successful, redirect the user back to their dashboard.
                    response.sendRedirect("dashboard");
                } else {
                    response.getWriter().println("Failed to save the expense.");
                }
            }
        } catch (Exception e) {
            // Log the exception and provide a user-friendly error message.
            Logger.getLogger(AddExpenseservlet.class.getName()).log(Level.SEVERE, "Database error", e);
            response.getWriter().println("Error: " + e.getMessage());
        }
    }
}
