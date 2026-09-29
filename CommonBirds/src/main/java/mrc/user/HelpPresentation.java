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

/* Class: HelpPresentation
 * 	Displays the "Condor User Guide" page: a short slide presentation
 *  covering Condor's features, embedded inline, with download links
 *  for the PDF and PowerPoint versions (see Condor_Presentation.pdf
 *  and Condor_Presentation.pptx in web/html). Linked from the
 *  logged-out menu's About... dropdown (index.html) and the
 *  logged-in menu's More... dropdown (SKYLARK.html), same label,
 *  same destination, so it is reachable both before and after login.
 *  Follows the same pattern as HelpBaskets/HelpSearch/HelpAccess,
 *  though this one is not itself part of that per-section help
 *  series - it is a single overview page, not one of several.
 */
public class HelpPresentation extends HttpServlet
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger("mrc.user");

	/* Function: doGet
	 	Renders the Help-Presentation Pebble template inside the
	 	standard site layout.

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
		log.info(" HelpPresentation: Condor User Guide page requested");
		Page p = new Page("Help-Presentation");
		p.UserPage(out, "Condor User Guide", s);
	}

	/**
	* Returns a short description of the servlet.
	*/
	@Override
	public String getServletInfo() {
		return "Displays the Condor User Guide presentation page";
	}
}
