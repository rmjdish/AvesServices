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

import java.io.*;
import java.sql.*;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.*;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

import mrc.db.ConnectDB;
import mrc.user.PassStore;
import mrc.util.BotCheck;
import mrc.util.Page;
import mrc.util.HostInfo;
import mrc.util.Util;

/* 
 * The @WebServlet statement below is the Servlet ver 3.0 way of declaring
 * the servlet to class mapping. This is an alternative to putting it all
 * in the web.xml file.
 * Phil 15/09/2015
 * Changed from default value syntax to using specified name.
 * Because: If the name attribute is not defined, the fully qualified name of the class is used.
 */

/* Class: Login
 * 	Handles authentication procedures for all users
 * 
 */
public class Login extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger("mrc.user");

	public enum Menu {
		Null, External, Internal, Admin, DSG
	};

	private Menu cando;

	/*
	 * Function: doGet Java JSP (Tomcat) super class method. Handles the HTTP
	 * <code>GET</code> method.
	 * 
	 * 
	 * Parameters: request - HttpServletRequest object that carries around
	 * attributes of the requesting user/session response - HttpServletResponse
	 * object that carries around attributes of the output channel used to deliver
	 * result of request
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Setup the session
		// session object holds state information between HTML requests
		HttpSession s = request.getSession(true); // First thing is to create a session
		PrintWriter out = response.getWriter();
		response.setContentType("text/html;charset=UTF-8");
		s.setAttribute("outchannel", out);
		s.setAttribute("sessionId", s.getId()); // This can be tested for session timeout.
		// The following prints to the Tomcat Error Log
		log.info(HostInfo.tell()+" Login called on: "+HostInfo.chezmoi());
		loginForm(out, s);

	}

	/*
	 * Function: doPost Java JSP (Tomcat) super class method. Handles HTTP
	 * <code>POST</code method.
	 * 
	 * Parameters: request - HttpServletRequest object that carries around
	 * attributes of the requesting user/session response - HttpServletResponse
	 * object that carries around attributes of the output channel used to deliver
	 * result of servlet request
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// This catches the result of the straight Login post method
		// By now we've already created a command processor for this Login
		PrintWriter out = response.getWriter();
		String username = request.getParameter("username").replaceAll("'", "''");
		String password = request.getParameter("password").replaceAll("'", "''");
		String gcaptcha = (String) request.getParameter("g-recaptcha-response");
		boolean human = BotCheck.verifyCapcha(gcaptcha);
		String error = "";
		if (!human) {
			error = "You didn't pass the robot test.  Please try again.  ";
			authenticationFailedPage(request, out, error);
		} else {
			
			if (checkUserRegistration(username, password)) {
				String accountStatus = checkAccountStatus(username);
				String accessLevel = checkUserAccessLevel(username);
				int seclevel = checkUserSecLevel(username);
				String ipaddr = new String();
				ipaddr = request.getRemoteAddr();
				HttpSession s = request.getSession();
				s.setMaxInactiveInterval(3600); // Set the maximum inactive session limit
				ArrayList<String> v = new ArrayList<String>();
				s.setAttribute("accessLevel", accessLevel);
				s.setAttribute("items", v);
				s.setAttribute("username", request.getParameter("username"));
				s.setAttribute("seclevel", seclevel);
				s.setAttribute("ip", ipaddr);
				if (accountStatus.equalsIgnoreCase("APPROVED")) {
					try {
						sessionLog(username, accessLevel, ipaddr);
					} catch (Exception se) {
						log.severe("Login: unable to update session log.");
						se.printStackTrace();
					}
					if (accessLevel.equalsIgnoreCase("admin")) {
						adminUserPage(out, s);
					} else if (accessLevel.equalsIgnoreCase("dsg")) {
						dsgUserPage(out, s);
					} else if (accessLevel.equalsIgnoreCase("internal")) {
						internalUserPage(out, s);
					} else {
						externalUserPage(out, s);
					}
				// Not approved but what kind?
				} else if (accountStatus.equalsIgnoreCase("REJECTED")) {
					applicationRejectedPage(out, s);

				} else if (accountStatus.equalsIgnoreCase("UNVERIFIED")) {
					applicationNotYetApproved(out, s);
				}
			} else { // Didn't pass checkUserRegistration
				error = "Your username or password was incorrect.  Please try again.  ";
				authenticationFailedPage(request, out, error);
			}
		}
	}

	
	/**
	 * Session logging system.
	 * 
	 * @param username    SWIFT username
	 * @param accessLevel SWIFT access level
	 */
	private void sessionLog(String username, String accessLevel, String ipaddr)  {
		String query = "insert into sessions (username,login,accessLevel,ipaddress) values (?, NOW(), ?, ?)";
		HashMap<Integer,Object> theparms = new HashMap<Integer,Object>();
		theparms.put(1, username);
		theparms.put(2, accessLevel);
		theparms.put(3,ipaddr);
		log.fine(HostInfo.tell() + " Login: SessionLog - inserting user details into session table");
		log.fine(HostInfo.tell() + username + ":" + accessLevel + ":"+ ipaddr);
		try {
			ConnectDB c = new ConnectDB();
			c.capture();
			int r = 0;
			r = c.doPUpdate(query,theparms);
			c.release();
			if (r == 0)
				log.warning(HostInfo.tell() + " Login: Error in sessionLog: " + username);
		} catch (Exception e) {
			log.severe(HostInfo.tell()+"SessionLog: failed to execute: "+query);
			e.printStackTrace();
		}
	}

	/**
	 * Displays administrator Login page.
	 * 
	 * @param out HTML PrintWriter
	 * @param p   HTML Condor page
	 * @param s   HTTPSession
	 * @throws IOException
	 */
	private void adminUserPage(PrintWriter out, HttpSession s) throws IOException {
		Page p = new Page("Login-adminUserPage");
		this.cando = Menu.Admin;
		s.setAttribute("menutype", this.cando.toString());
		log.info(HostInfo.tell() + " Login Class: In adminUserPage creating Page object <p> and calling p.UserPage");
		p.UserPage(out, "Administrator Access.", s);
	}

	/**
	 * Notifies Level 1 user that Level 2 access has not yet been approved.
	 * 
	 * @param out HTML PrintWriter
	 * @param s   HTTPSession
	 * @throws IOException
	 */
	private void applicationNotYetApproved(PrintWriter out, HttpSession s) throws IOException {
		Page p = new Page("Login-NotYetApproved");
		log.info(" Login Class: In applicationNotYetApproved creating Page object <p> and calling p.UserPage");
		p.UserPage(out, "Login credentials not yet approved", s);
	}

	/**
	 * Notifies Level 1 user that Level 2 access has been rejected.
	 * 
	 * @param out HTML PrintWriter
	 * @param s   HTTPSession
	 * @throws IOException
	 */
	private void applicationRejectedPage(PrintWriter out, HttpSession s) throws IOException {
		Page p = new Page("Login-applicationRejected");
		p.UserPage(out, "Login credentials not approved", s);
	}

	/**
	 * Notifies user that authentication has failed (eg incorrect password and/or
	 * username combination).
	 * 
	 * @param request servlet request
	 * @param out     HTML PrintWriter
	 * @throws IOException
	 */
	private void authenticationFailedPage(HttpServletRequest request, PrintWriter out, String error)
			throws IOException {
		Page p = new Page("Login-authenticationFailedForm");
		HttpSession session = request.getSession();
		p.UserPage(out, error, session);
	}

	/**
	 * Determines whether <code>username</code> and entered password <code>p1</code>
	 * match what is stored in the database. Note that passwords are not stored in
	 * the database: rather, SHA-1 hashes of passwords are stored, and comparisons
	 * are only ever made between hashes. Note that this makes user password
	 * recovery impossible, but also prevents hacking of all user accounts if the
	 * password database is accessed without authorisation.
	 * 
	 * @param username Username to verify
	 * @param p1       Password entered by the user
	 */
	private boolean checkUserRegistration(String username, String passoffered) {
		boolean authenticated = false;
		String query = null;
		if (Util.mainApp().equalsIgnoreCase("Jay")) {
			query = "select password from rook.users where username=?";
		} else {
			query = "select password from users where username=?";
		}
		HashMap<Integer,Object> theparms = new HashMap<Integer,Object>();
		theparms.put(1, username);
		
		log.info(HostInfo.tell()+" checkUserRegistration: " + username + ":" + query);

		try {
			ConnectDB c = new ConnectDB();
			c.capture();
			ResultSet rs = c.doPQuery(query,theparms);
			String passindb = null;
			while (rs.next()) {
				passindb = rs.getString(1);
			}
			rs.close();
			c.release();
			String salt = PassStore.getSaltFromPass(passindb);
			String encryptedoffer = null;
			if (salt.isEmpty()) {
				encryptedoffer = PassStore.getPass(passoffered);
			} else {
				encryptedoffer = PassStore.getPass(passoffered, salt);
			}
			if (passindb == null) { /* You can't Login if the password field is set to null */
				log.warning(HostInfo.tell() + " Login Class: Password in db is NULL ");
				authenticated = false;
			} else if (StringUtils.equals(passindb, encryptedoffer)) {
				log.fine(HostInfo.tell() + " Login Class: Authentication succeeded for: " + username);
				authenticated = true;
			} else {
				log.fine(HostInfo.tell() + " Login Class: Failed match :" + passindb + " and " + encryptedoffer);
				authenticated = false;
			}
		} catch (Exception e) {
			log.severe(HostInfo.tell()+" Login: Error obtaining password from database.");
		}
		return authenticated;
	}


	private void externalUserPage(PrintWriter out, HttpSession s) throws IOException {
		Page p = new Page("Login-externalUserPage");
		this.cando = Menu.External;
		s.setAttribute("menutype", this.cando.toString());
		log.info(" Login Class: externalUserPage: setting session menutype to " + this.cando.toString());
		p.UserPage(out, HostInfo.tell() + " User.", s);
	}

	/**
	 * Checks account status for user <code>username</code>.
	 * 
	 * @param username Username
	 */
	private String checkAccountStatus(String username) {
		String status = "";
		String query;
		if (Util.mainApp().equalsIgnoreCase("Jay")) {
			query = "select status from rook.users where username=?";
		} else {
			query = "select status from users where username=?";
		}				
		HashMap<Integer,Object> theparms = new HashMap<Integer,Object>();
		theparms.put(1, username);
		log.fine(HostInfo.tell()+" checkAccountStatus: " + username + ":" + query);
		try {
			ConnectDB c = new ConnectDB();
			c.capture();
			ResultSet rs = c.doPQuery(query,theparms);
			while (rs.next()) {
				status = rs.getString(1);
			}
			if (status == null) {
				status = "";
			}
			rs.close();
			c.release();
		} catch (Exception e) {
			log.severe(HostInfo.tell()+" checkAccountStatus Error obtaining status from users table.");
			e.printStackTrace();
		}
		return status;
	}

	/**
	 * Checks access levels for user <code>username</code>.
	 * 
	 * @param username Username
	 */
	private String checkUserAccessLevel(String username) {
		String accessLevel = "external";
		String query;
		if (Util.mainApp().equalsIgnoreCase("Jay")) {
			query = "select rights from rook.privileges where username=?";
		} else {
			query = "select rights from privileges where username=?";
		}
		HashMap<Integer,Object> theparms = new HashMap<Integer,Object>();
		theparms.put(1, username);
		log.fine(HostInfo.tell()+username + ":" + query);
		try {
			ConnectDB c = new ConnectDB();
			c.capture();
			ResultSet rs = c.doPQuery(query,theparms);
			String p2 = null;
			while (rs.next()) {
				p2 = rs.getString(1);
			}
			rs.close();
			c.release();
			if (p2 != null) {
				accessLevel = p2;
			}
		} catch (Exception e) {
			log.severe(HostInfo.tell()+" checkUserAccessLevel: Error obtaining rights for user from privileges table.");
			e.printStackTrace();
		}
		return accessLevel;
	}

	/**
	 * Checks security level for user <code>username</code>.
	 * 
	 * @param username Username
	 */
	private int checkUserSecLevel(String username) {
		int secLevel = 0; // Default value
		String query;
		if (Util.mainApp().equalsIgnoreCase("Jay")) {
			query = "select secLevel from rook.privileges where username=?";
		} else {
			query = "select secLevel from privileges where username=?";
		}
		HashMap<Integer,Object> theparms = new HashMap<Integer,Object>();
		theparms.put(1, username);
		log.fine(HostInfo.tell()+username + ":" + query);
		try {
			ConnectDB c = new ConnectDB();
			c.capture();
			ResultSet rs = c.doPQuery(query,theparms);
			int level = -999;
			while (rs.next()) {
				level = rs.getInt(1);
			}
			rs.close();
			c.release();
			if (level >= 0) {
				secLevel = level;
			} else {
				secLevel = 0;
			}
		} catch (Exception e) {
			log.severe(HostInfo.tell()+" checkUserSecLevel: Error obtaining secLevel for user from priveleges table.");
			e.printStackTrace();
		}
		return secLevel;
	}

	/**
	 * Returns a short description of the servlet.
	 */
	@Override
	public String getServletInfo() {
		return "This class handles the Login procedures for Condor.";
	}


	private void internalUserPage(PrintWriter out, HttpSession s) throws IOException {
		Page p = new Page("Login-internalUserPage");
		this.cando = Menu.Internal;
		s.setAttribute("menutype", this.cando.toString());
		log.info(" Login Class: internalUserPage: setting session menutype to " + this.cando.toString());
		p.UserPage(out, "Internal User Access", s);
	}


	private void dsgUserPage(PrintWriter out, HttpSession s) throws IOException {
		Page p = new Page("Login-dsgUserPage");
		this.cando = Menu.DSG;
		s.setAttribute("menutype", this.cando.toString());
		log.info(" Login Class: dsgUserPage setting session menutype to " + this.cando.toString());
		p.UserPage(out, "Repository Manager Access", s);

	}

	/**
	 * Displays Login form for all users.
	 * 
	 * @param out HTML PrintWriter
	 * @throws IOException
	 */
	private void loginForm(PrintWriter out, HttpSession s) throws IOException {
		Page p = new Page("Login-loginForm");
		this.cando = Menu.Null;
		s.setAttribute("menutype", this.cando.toString());
		log.info("Login: loginForm: "+HostInfo.tell());
		p.UserPage(out, "Welcome to "+HostInfo.tell()+" - Please Login.", s);
	}
}
