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

// Code to load already existing baskets.
package mrc.user;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.*;
import java.util.logging.Logger;
import java.sql.*;

import mrc.db.*;
import mrc.util.MessageRelay;
import mrc.util.Page;
import mrc.util.SecModel;

public class LoadBasket extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// private static final Logger log = Logger.getLogger(HostInfo.tell() + ":" + LoadBasket.class.getName());
	private static final Logger log = Logger.getLogger("mrc.user");
	private static SecModel smod = new SecModel();
	private static MessageRelay msg2u = new MessageRelay();
	private ArrayList<String> varResults = new ArrayList<String>();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession s = request.getSession();
		PrintWriter out = response.getWriter();
		response.setContentType("text/html;charset=UTF-8");
		// The following prints to the Tomcat Error Log

		log.fine("Condor LoadBasket Class: doGet Called.");
		if (request.getParameter("basketID") != null) {
			String basket = request.getParameter("basketID");
			getVars(out, s, basket);
		}
	}

	/**
	 * Load variables in a saved shopping Basket.
	 * 
	 * @param out    HTML PrintWriter
	 * @param p      HTML SWIFT page
	 * @param s      HttpSession
	 * @param Basket Basket ID
	 */
	private void getVars(PrintWriter out, HttpSession s, String basket)  {

		ArrayList<String> v = new ArrayList<String>();
		v = (ArrayList<String>) s.getAttribute("items");
		String username = s.getAttribute("username").toString();
		int seclevel = (Integer) s.getAttribute("seclevel");
		// This is an attribute of the user, recorded in the session object

		String query = "select t1.name,t2.Label from shoppingbaskets as t1 LEFT JOIN variablelabels as t2 on (t1.name = t2.Name)  where basketID=\'"
				+ basket + "\'";
		log.fine(" getVars: " + username + ":" + query);
		ResultSet rs = null;
		ConnectDB c = new ConnectDB();
		c.capture();
		try {
			rs = c.doQuery(query);
		} catch (Exception err) {
			log.severe(" Error executing query: " + query + " for shopping basket: " + basket);
			err.printStackTrace();
		}
		// Make sure we don't have baskets without these:
		if (!v.contains("SERNO"))
			v.add("SERNO");
		if (!v.contains("SEX"))
			v.add("SEX");
		if (!v.contains("INF"))
			v.add("INF");
		if (!v.contains("NTAG1"))
			v.add("NTAG1");

		try {
			while (rs.next()) {
				String name = rs.getString(1);
				String label = rs.getString(2);
				if (smod.checkAuth(seclevel, name)) {
					// Have authority to add this Variable, add and test what kind
					// of print to do
					if (!name.equalsIgnoreCase("SERNO") & !name.equalsIgnoreCase("SEX") & !name.equalsIgnoreCase("INF") & !v.contains(name)) {
						v.add(name);  // already added these above
					}
					if (smod.isOpen(name) && smod.hasMsg(name)) {
						// It's open but has special message
						msg2u.display(name, smod.getMsgId(name));
					} else {
						// Is not open (but authorised) but has no special message
						msg2u.quiet(name, 2);
					}
				} else {
					msg2u.display(name, smod.getMsgId(name));
				}
			}
			rs.close();
			c.release();
		} catch (Exception err2) {
			log.severe(" Error stepping through results of " + query);
			err2.printStackTrace();
		}

		String accessLevel = s.getAttribute("accessLevel").toString();
		Page p = new Page("LoadBasket-messageDisplay");
		p.setnumCols(2);
		s.setAttribute("items", v); // Push current list of variables on to the session
		p.setResults(msg2u.msgFlush());
		try {
			p.UserPage(out, "Basket " + basket + " Loaded", s);
		} catch (Exception err3) {
			log.severe(" addVars: Attempt to print tamplate failed");
			err3.printStackTrace();
		}

	}

}
