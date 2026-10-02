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

/* Class: RequestingBasket
 * 	Displays the "Requesting your Condor basket" page: explains that
 *  Condor only builds the basket - turning it into an actual data
 *  request happens separately, on Skylark - and links to the two
 *  Skylark forms that cover the two situations a user might be in
 *  (starting a new request, or adding to an already-approved
 *  project). Linked from the OWL dropdown, after Upload Data
 *  Dictionary, at /requestingBasket.
 *
 *  Follows the same minimal, render-only pattern as HelpPresentation
 *  and UploadDictionary - this page has no form of its own to submit,
 *  it only links out to Skylark's own forms.
 */
public class RequestingBasket extends HttpServlet
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger("mrc.user");

	/* Function: doGet
	 	Renders the Help-RequestingBasket Pebble template inside the
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
		log.info(" RequestingBasket: Requesting your Condor basket page requested");
		Page p = new Page("Help-RequestingBasket");
		p.UserPage(out, "Requesting your Condor basket", s);
	}

	/**
	* Returns a short description of the servlet.
	*/
	@Override
	public String getServletInfo() {
		return "Displays the Requesting your Condor basket page";
	}
}
