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

/* Class: HelpSearch
 * 	Displays the in-app help page for the Search feature (By Category,
 *  By Variable Name, By Keyword, By Library). First of a planned series
 *  of per-section help pages, following the same pattern as the rest
 *  of the site's Pebble-templated pages.
 */
public class HelpSearch extends HttpServlet
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger("mrc.user");

	/* Function: doGet
	 	Renders the Help-Search Pebble template inside the standard
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
		log.info(" HelpSearch: Help-Search page requested");
		Page p = new Page("Help-Search");
		p.UserPage(out, "Search Help", s);
	}

	/**
	* Returns a short description of the servlet.
	*/
	@Override
	public String getServletInfo() {
		return "Displays the help page for the Search feature";
	}
}
