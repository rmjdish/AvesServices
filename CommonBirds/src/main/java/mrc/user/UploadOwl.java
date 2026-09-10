package mrc.user;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mrc.util.Page;

/**
 * Serves the Upload OWL Basket page (templates/uploadOwl.html) through
 * Pebble, the same way every other page in this app is served.
 *
 * Modeled directly on mrc.user.ChangePassword's doGet(): requires a
 * logged-in session, redirects to /login otherwise.
 */
public class UploadOwl extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger("mrc.user");

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        HttpSession s = request.getSession();

        if (s == null || s.getAttribute("username") == null) {
            request.getRequestDispatcher("/login")
                   .forward((ServletRequest) request,
			    (ServletResponse) response);
            return;
        }

        log.fine("Condor: UploadOwl page requested by " + s.getAttribute("username"));

        // "uploadOwl" here resolves to /templates/uploadOwl.html - the
        // real file already on the server, no changes needed to it.
        Page p = new Page("uploadOwl");
        p.UserPage(out, "Upload NSHD Data Dictionary", s);
    }

    public String getServletInfo() {
        return "Displays the Upload OWL Basket page for uploading an NSHD data dictionary spreadsheet.";
    }
}
