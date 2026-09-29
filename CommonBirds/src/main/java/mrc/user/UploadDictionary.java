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

/* Class: UploadDictionary
 * 	Displays the "Upload Data Dictionary" page: lets a user upload a
 *  data dictionary spreadsheet (either a basket file downloaded
 *  earlier from OWL, or the full Data Dictionary with variables
 *  marked Y in the "Request variable" column) and add every listed
 *  variable to their Condor basket in one go.
 *
 *  This servlet only renders the page - the file is parsed entirely
 *  client-side (see web/js/upload-dictionary.js), and the actual
 *  adding is done by submitting the parsed variable names as a
 *  normal Multiple Add request to Variable's own doPost(), exactly
 *  as if they had been checked on a search results page. That existing
 *  path already handles linked-variable expansion and restricted-
 *  variable checking against the real database, and its own Basket
 *  Management results page already shows the outcome - so nothing
 *  about adding variables is duplicated here.
 *
 *  Linked from the OWL dropdown, both before and after login, at
 *  /uploadDictionary.
 */
public class UploadDictionary extends HttpServlet
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger("mrc.user");

	/* Function: doGet
	 	Renders the Help-UploadDictionary Pebble template inside the
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
		log.info(" UploadDictionary: Upload Data Dictionary page requested");
		Page p = new Page("Help-UploadDictionary");
		p.UserPage(out, "Upload Data Dictionary", s);
	}

	/**
	* Returns a short description of the servlet.
	*/
	@Override
	public String getServletInfo() {
		return "Displays the Upload Data Dictionary page";
	}
}
