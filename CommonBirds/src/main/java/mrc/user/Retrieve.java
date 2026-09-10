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
import java.sql.ResultSet;

import java.util.HashMap;

import java.util.Map;

import java.util.ArrayList;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import mrc.util.Page;
import mrc.util.StoreXMLBasket;
import mrc.db.ConnectDB;
import mrc.util.HostInfo;

public class Retrieve extends HttpServlet {
	/**
	 * Allows users to download a Basket in XML format from xmlbaskets table
	 * 
	 * @author LHA SST
	 */
	private static final long serialVersionUID = 1L;
	// private static final Logger log = Logger.getLogger(HostInfo.tell() + ":" + Retrieve.class.getName());
	private static final Logger log = Logger.getLogger(Retrieve.class.getName());
	private StoreXMLBasket xmlversion; // In case we have to build it

	/**
	 * Handles the HTTP <code>GET</code> method.
	 * 
	 * @param request  servlet request
	 * @param response servlet response
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		HttpSession s = request.getSession();
		String command = request.getParameter("command");
		String basket = request.getParameter("basketID"); // Remember this could be null
		if (basket == null) {
			log.warning(HostInfo.tell()+"Retrieve: GET called with command " + command + " and NULL Basket");
			listBaskets(out, s);
		} else if (command.equalsIgnoreCase("toXML")) {
			String descrip = request.getParameter("description");
			String username = request.getParameter("username");
			log.fine(HostInfo.tell()+"Retrieve: GET called with command toXML and Basket -> " + basket);
			Page p = new Page("Retrieve-XMLDownloadForm");
			Map<String, Object> ctx = new HashMap<>();
			ctx.put("basketID", basket);
			ctx.put("description", descrip);
			ctx.put("username", username);
			p.extendPageContext(out, "Download XML Basket", s, ctx);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		String projectid = request.getParameter("projectid");
		String basketid = request.getParameter("basketID");
		String description = request.getParameter("description");
		displayXML(request, response, out, basketid, description, projectid);
	}

	private void listBaskets(PrintWriter out, HttpSession s) throws IOException {
		ArrayList<String> results = new ArrayList<String>();
		try {
			String username = (String) s.getAttribute("username");
			String query = "";
			query = "select basketID, description from basketdetails where basketID in (select basketID from shoppingbaskets where username=\'"
					+ username + "\')";
			/*
			 * Field info: 1) Basket id, 2) description
			 */
			getServletContext().log(username + ":" + query);
			ConnectDB c = new ConnectDB();
			c.capture();
			ResultSet rs = c.doQuery(query);
			while (rs.next()) {
				results.add(rs.getString(1));
				results.add(rs.getString(2));
			}
			rs.close();
			c.release();
		} catch (Exception e) {
			log.severe(HostInfo.tell()+" listBaskets: Failed to read from basketdetails.");
			e.printStackTrace();
		}
		Page p = new Page("Retrieve-displayListOfBasketsForDownload");
		p.setnumCols(2);
		p.setResults(results);
		p.UserPage(out, "List of Saved Baskets", s);
	}

	/* Function: displayXML
	 * This version looks to pull the XML form of the basket from the
	 * robin.xmlbaskets table. Of course, it might not be in the table, in which
	 * case we need to push it in first.
	 * 
	 * Parameters:
	 * 	request - HttpServletRequest Tomcat provided
	 * 	response - HttpServletResponse returned to Tomcat
	 * 	basket - Name of basket to be turned into XML
	 * 	description - Label for the basket
	 * 	projectid - A project identifier used in the Condor Wiki to display all approved data sharing projects
	 */
	private void displayXML(HttpServletRequest request, HttpServletResponse response, PrintWriter out, String basket,
			String description, String projectid) {
		try {
			HttpSession s = request.getSession();
			String username = (String) s.getAttribute("username");
			String query = "select name, label from variablelabels where name in (select name from shoppingbaskets where basketID=\'"
					+ basket + "\')";
			String xquery = "select basketId,xmlcontent from xmlbaskets where basketId=\'" + basket + "\'";

			/*
			 * Field list for query is: 1) Variable Name, 2) Variable Label Field list for
			 * xquery is: 1) BasketId, 2) XMl version of basket
			 */
			log.fine(HostInfo.tell()+" Retrieve: displayXML: "+username + ":" + query);
			ConnectDB c = new ConnectDB();
			c.capture();
			// First check if it's in xmlbaskets
			ResultSet xs = c.doQuery(xquery);
			if (xs.next()) {
				// Found it so simply pull it out
				String xmlform = xs.getString(2);
				this.displayXMLonScreen(request, response, out, s, basket, xmlform);
			} else {
				// Not in the table so create it from existing basket
				this.xmlversion = new StoreXMLBasket(username, basket, description, projectid); // Initial work
				ResultSet rs = c.doQuery(query);
				/*
				 * We now have a list of variables; time to instantiate BasketXML objects
				 */
				int numfound = 0;
				while (rs.next()) {
					this.xmlversion.plusVar(rs.getString(1)); // Add a Variable to the list
					numfound += 1;
				}
				rs.close();
				c.release();
				/*
				 * Now serialise the xbasket object to output
				 */
				this.xmlversion.storeDoc(); // to serialise and push into table
				if (numfound > 0) {
					// Now you can start again from the beginning
					this.displayXML(request, response, out, basket, description, projectid);
				} else {
					// The basket's not in xmlbaskets and we can't find it in shoppingbaskets Help!
					log.severe(HostInfo.tell()+" Retrieve: can't find basket -> " + basket);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void displayXMLonScreen(HttpServletRequest request, HttpServletResponse response, PrintWriter out,
			HttpSession s, String basket, String xmlbasket) {
		Page p = new Page("Retrieve-displayXMLonScreen");
		p.XMLPage(out, "Basket " + basket + " in XML format", s, xmlbasket);

	}

	public String returnXML(HttpServletRequest request, HttpServletResponse response, PrintWriter out, String basket) 
	{
		/*
		 * This version looks to pull the XML form of the basket from the
		 * robin.xmlbaskets table. Of course, it might not be in the table, in which
		 * case we need to push it in first.  It assumes the basket exists and has to 
		 * pull out the description from the basketdetails table
		 */
		HttpSession s = request.getSession();
		String username = (String) s.getAttribute("username");
		String query = "select name, label from variablelabels where name in (select name from shoppingbaskets where basketID=\'"
				+ basket + "\')";
		String fquery = "select basketID, Description, username from basketdetails where basketID=\'"+ basket +"\'";
		String xquery = "select basketID,xmlcontent from xmlbaskets where basketID=\'" + basket + "\'";
		String xmlform = "";
		String description = null;
		String projectId = "NYA";
		/*
		 * Field list for query is: 1) Variable Name, 2) Variable Label Field list for
		 * fquery is: 1) BasketId, 2) Description, 3) username
		 * xquery is: 1) BasketId, 2) XMl version of basket
		 */
		log.info(HostInfo.tell()+" Retrieve: returnXML: "+username + ":" + query);
		try {
			ConnectDB c = new ConnectDB();
			c.capture();
			// First check if it's in xmlbaskets
			ResultSet xs = c.doQuery(xquery);
			if (xs.next()) {
				// Found it so simply pull it out
				xmlform = xs.getString(2);
				return xmlform;
			} else {
				// Not in the table so create it from existing basket
				ResultSet rs = c.doQuery(fquery);
				while (rs.next()) {
					description = rs.getString(1);
				}
				rs.close();
				this.xmlversion = new StoreXMLBasket(username, basket, description, projectId); // Initial work
				rs = c.doQuery(query);
				/*
				 * We now have a list of variables; time to instantiate BasketXML objects
				 */
				int numfound = 0;
				while (rs.next()) {
					this.xmlversion.plusVar(rs.getString(1)); // Add a Variable to the list
					numfound += 1;
				}
				rs.close();
				c.release();
				/*
				 * Now serialise the xbasket object to output
				 */
				this.xmlversion.storeDoc(); // to serialise and push into table
				if (numfound > 0) {
					// Now you can have start again from the beginning
					xmlform = this.returnXML(request, response, out, basket);
					return xmlform;
				} else {
					// The basket's not in xmlbaskets and we can't find it in shoppingbaskets Help!
					log.severe(HostInfo.tell()+" Retrieve: can't find basket -> " + basket);
				}

			}
		} catch (Exception e) {
			log.severe(HostInfo.tell()+" Retrieve: returnXML can't find or create basket -> " + basket);
			e.printStackTrace();
		}
		return xmlform;
	}

	/**
	 * Returns a short description of the servlet.
	 */
	@Override
	public String getServletInfo() {
		return "This class implements a Basket download feature that allows users to save an XML representation of their baskets.";
	}// </editor-fold>

	/**
	 * Lists all data sets available for Download.
	 * 
	 * @param out HTML PrintWriter
	 * @param p   HTML SWIFT page
	 * @param s   HTTPSession
	 */

}
