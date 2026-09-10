/*
    This file is part of Jay/Condor/Swift.

    Jay/Condor/Swift is free software: you can redistribute it and/or
    modify it under the terms of the GNU General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    Jay/Condor/Swift is distributed in the hope that it will be
    useful, but WITHOUT ANY WARRANTY; without even the implied
    warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
    PURPOSE. See the GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Jay/Condor/Swift. If not, see
    <https://www.gnu.org/licenses/>.

*/


package mrc.user;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import mrc.util.HostInfo;
import mrc.util.Page;

/* Class: Logout
 * 	Cleans up after user and destroys session object used
 * 
 */
public class Logout extends HttpServlet 
{   
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// private static final Logger log = Logger.getLogger(HostInfo.tell() + ":" + Logout.class.getName());
	private static final Logger log = Logger.getLogger("mrc.user");
	/* Function: doGet
     	HttpServlet based class that handles HTTP <code>GET</code> method. 
		Invalidates the session object at end

      Parameters:
		request - HttpServletRequest object that carries attributes of user
				  session around.
		response - HttpServletResponse object that carries attributes of output
				  channel to be used for communicating results
	
	*/
    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        PrintWriter out = response.getWriter();        
        HttpSession s = request.getSession(false); 
		String go = request.getParameter("go");  // Returns null if "go" does not exist
		if (Objects.nonNull(go) ) {
			switch (go) {
			case "home":
				this.homePage(out, s);
				break;
			}
			return;
		}
        log.info(" Logout Class: Session End");
        Page p=new Page("Logout-logoutPage");
        p.UserPage(out, "Session Ended", s);
        s.invalidate();
    } 
 
	/** 
	 * Home Page
      Parameters:
		out - PrintWriter object that ...
		s - HttpSession object that ...
	
	 */
	private void homePage(PrintWriter out, HttpSession s) {
		if (Objects.nonNull(s)) {
			String menutype = s.getAttribute("menutype").toString();
			Page p = null;
			switch (menutype) {
			case "Null":
				break;
			case "External":
				p = new Page("Login-externalUserPage");
				p.UserPage(out, HostInfo.tell() + " User.", s);
				break;
			case "Internal":
				p = new Page("Login-internalUserPage");
				p.UserPage(out, "Internal User Access", s);
				break;
			case "Admin":
				p = new Page("Login-adminUserPage");
				p.UserPage(out, "Administrator Access.", s);
				break;
			case "DSG":
				p = new Page("Login-dsgUserPage");
				p.UserPage(out, "Repository Manager Access", s);
				break;
			}
		}
	}
   

	/*
	Function: die
		Displays good bye page to user

	Parameter: s
		HttpSession object
	*/
   
    public void die(HttpSession s) throws IOException {
    	PrintWriter out = (PrintWriter) s.getAttribute("outchannel");
    	Page p=new Page("Logout-sessionTimeout");
    	p.UserPage(out,"Session Timeout", s);
        log.info(" Logout Class: Session Timeout");
    }

    /** 
    * Returns a short description of the servlet.
    */
    @Override
	public String getServletInfo() {
        return "This handles the tidying up tasks when logging out of the system";
    }
}
