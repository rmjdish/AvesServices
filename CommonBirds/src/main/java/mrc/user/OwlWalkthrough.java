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

/* Class: OwlWalkthrough
 * 	Displays the interactive, step-by-step guided walkthrough of the
 *  OWL-to-Condor workflow. Linked from How It Works rather than
 *  given its own top-level menu entry. Step completion is tracked
 *  client-side only (browser localStorage), not server-side.
 */
public class OwlWalkthrough extends HttpServlet
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger("mrc.user");

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		response.setContentType("text/html;charset=UTF-8");
		HttpSession s = request.getSession(true);
		log.info(" OwlWalkthrough: Guided walkthrough page requested");
		Page p = new Page("Owl-Walkthrough");
		p.UserPage(out, "Guided Walkthrough", s);
	}

	@Override
	public String getServletInfo() {
		return "Displays the interactive guided walkthrough of the OWL-Condor workflow";
	}
}
