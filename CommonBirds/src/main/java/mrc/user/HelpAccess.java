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
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import mrc.util.Page;

/* Class: HelpAccess
 * 	Displays the in-app help page covering registration and login
 *  (Register, Login, account status, forgot password). Unlike
 *  HelpSearch/HelpBaskets, this page is reached from pre-login pages
 *  (index.html, Login-loginForm.html, Register-registrationForm.html)
 *  rather than from the normal logged-in menu system, since none of
 *  those pages share the standard SKYLARK menu.
 */
public class HelpAccess extends HttpServlet
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger("mrc.user");

	/* Function: doGet
	 	Renders the Help-Access Pebble template inside the standard
	 	site layout.

	  Parameters:
		request - HttpServletRequest object that carries around
				  attributes of the requesting user/session
		response - HttpServletResponse object that carries around
				  attributes of the output channel used to deliver
				  result of request
	*/
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		response.setContentType("text/html;charset=UTF-8");
		HttpSession s = request.getSession(true);
		log.info(" HelpAccess: Help-Access page requested");
		Page p = new Page("Help-Access");
		p.UserPage(out, "Registration & Login Help", s);
	}

	/**
	* Returns a short description of the servlet.
	*/
	@Override
	public String getServletInfo() {
		return "Displays the help page for registration and login";
	}
}
