import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Integer userId = (session != null) ? (Integer) session.getAttribute("id") : null;
        String username = (session != null) ? (String) session.getAttribute("unm") : null;

        if (userId == null) {
            response.sendRedirect("login.html");
            return;
        }

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'><head><meta charset='UTF-8'>");
        out.println("<title>Dashboard - Mera Kharcha</title>");
        
        out.println("<style>");
        out.println("body { font-family: 'Segoe UI', sans-serif; background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%); text-align:center; margin: 0; min-height: 100vh;}");
        out.println("header { background: linear-gradient(90deg, #0093E9 0%, #80D0C7 100%); color:#fff; padding:25px 0; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }");
        out.println("header h1 { margin: 0; font-size: 2.3rem; }");
        
    
        
       
        out.println(".card-container { display: flex; justify-content: center; align-items: stretch; gap: 40px; padding: 40px 20px 20px 20px; flex-wrap: wrap; }");
        
        
        out.println(".card { background: rgba(255,255,255,0.9); border-radius: 22px; padding: 35px 25px; width: 260px; box-shadow: 0 6px 18px rgba(0,0,0,0.1); transition: transform 0.3s ease, box-shadow 0.3s ease; cursor:pointer; display: flex; flex-direction: column; justify-content: center; }");
        
        
        out.println(".card:hover { transform: translateY(-6px); box-shadow: 0 10px 24px rgba(0,0,0,0.15); }");
        out.println(".card h2 { font-size: 1.5rem; color: #2575fc; margin-bottom: 4px; }");
        out.println("a { text-decoration:none; color:inherit; }");
        out.println(".logout a { background: linear-gradient(90deg, #ff6a88 0%, #ff99ac 100%); color: #fff; padding: 12px 28px; border-radius: 30px; font-weight: 600; display: inline-block; margin-top: 30px;}");
        out.println(".logout a:hover { transform: scale(1.05); box-shadow: 0 6px 14px rgba(0,0,0,0.2); }");
        out.println("</style></head><body>");

        out.println("<header><h1>Welcome " + username + " to Mera Kharcha</h1></header>");

        out.println("<div class='card-container'>");
        out.println("  <div class='card'><a href='addexpense.html'><h2>Add Daily Expenses</h2></a></div>");
        out.println("  <div class='card'><a href='monthlysummary.html'><h2>View Monthly Summary</h2></a></div>");
        out.println("</div>");

        out.println("<div class='logout'><a href='index.html'>Logout</a></div>");
        out.println("</body></html>");
    }
}