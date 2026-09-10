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
import java.net.URL;
import java.sql.*;
import java.util.Map;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;

import jakarta.servlet.*;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

import java.security.*;

import mrc.db.ConnectDB;
import mrc.smtp.MailMessage;
import mrc.util.*;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import javax.net.ssl.HttpsURLConnection;


/*
 * Class: Register
 * 	Handles all user registration behaviour
 * 
 * 
 * 
 */

public class Register extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3346082720821625831L;
	// private static final Logger log =
	// Logger.getLogger(HostInfo.tell()+":"+Register.class.getName());
	private static final Logger log = Logger.getLogger("mrc.user");
	public static final String url = "https://www.google.com/recaptcha/api/siteverify";
	/*
	 * About: The Google reCAPTCHA version is v2.  Tried v3 first but could not get it
	 * 	to return a value.  V2 operates well.  The secrets below are both v2 secrets.
	 */
	public static final String skylarksecret = "6LcTunkUAAAAAOrMcI5u0AvfjirH1tqpkmWrpSbE";
	public static final String kiwidevsecret = "6LeBf3kUAAAAAMVpf2q_dNLkoaT7FVZTh8U6RIhU";
	private final static String USER_AGENT = "Mozilla/5.0";

	/**
	 * Handles the HTTP <code>GET</code> method.
	 * 
	 * @param request  servlet request
	 * @param response servlet response
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Setup a session for registration only
		// session object holds state information between servlet requests
		HttpSession s = request.getSession(true); // First thing is to create a session
		PrintWriter out = response.getWriter();
		response.setContentType("text/html;charset=UTF-8");
		s.setAttribute("outchannel", out);
		s.setAttribute("sessionId", s.getId()); // This can be tested for session timeout.
		registrationForm(out, request);

	}

	/** 
    * Handles the HTTP <code>POST</code> method.
    * @param request servlet request
    * @param response servlet response
    */
    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException 
    {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter(); 
        HttpSession s = request.getSession();
        String username =    (String)request.getParameter("userName").replaceAll("'", "''");
        String email =       (String)request.getParameter("email").replaceAll("'", "''");
        String gcaptcha =    (String)request.getParameter("g-recaptcha-response");
        Enumeration<String> parmlist = request.getParameterNames();       
        while (parmlist.hasMoreElements()) {
        	String parm = parmlist.nextElement();
        	log.fine(HostInfo.tell()+" Register: HttpServletRequest Parameters: "+parm+" = "+request.getParameter(parm));
        }
        boolean badname=checkIfUsernameRegistered(username);
        boolean human=BotCheck.verifyCapcha(gcaptcha);
        boolean special=checkSpecialChars(username);
        boolean bademail=checkIfEmailRegistered(email, username);
        boolean containsat=checkForAtSign(email);
        boolean pwordsmatch=checkIfPasswordsMatch(request.getParameter("desiredPassword1"), request.getParameter("desiredPassword2"));
        String error="";
        if (!human)
        { 
        	error+="You didn't pass the robot test.  Please try again.  ";
        }
        if (badname)
        {
            error+="The username you entered contains illegal characters or is already taken. Please choose another.  ";
        }
        if (bademail)
        {
            error+="The email address you entered is already taken. Please choose another.  ";
        }
        if (!pwordsmatch)
        {
            error+="The passwords you entered do not match. Please enter them again.  ";
        }
        if (special)
        {
            error+="Your username cannot start with a special character, quotes or a number. Please enter it again.  ";
        }
        if (!containsat)
        {
            error+="The e-mail address you entered does not contain an at symbol or at least one period. Please enter another.  ";
        }
        if (error.equalsIgnoreCase(""))
        {
            registrationConfirmed(out, request);      
            try
            {
                insertNewUser(request.getParameter("userName").replaceAll("'", "''"), 
                        request.getParameter("desiredPassword1").replaceAll("'", "''"),
                        request.getParameter("firstName").replaceAll("'", "''"), 
                        request.getParameter("lastName").replaceAll("'", "''"), 
                        request.getParameter("academicAffiliation").replaceAll("'", "''"), 
                        request.getParameter("address1").replaceAll("'", "''"), 
                        request.getParameter("address2").replaceAll("'", "''"), 
                        request.getParameter("city").replaceAll("'", "''"), 
                        request.getParameter("postcode").replaceAll("'", "''"), 
                        request.getParameter("county").replaceAll("'", "''"), 
                        request.getParameter("phone").replaceAll("'", "''"), 
                        request.getParameter("email").replaceAll("'", "''"));
            }
            catch (Exception se)
            {
            	log.severe("Register: error inserting user details into DB.");
                se.printStackTrace();
            }        
            notifyDirector(request);
        }
        else
        {
            registrationForm(out, request, request.getParameter("userName").replaceAll("'", "''"), 
                    request.getParameter("desiredPassword1").replaceAll("'", "''"),
                    request.getParameter("firstName").replaceAll("'", "''"), 
                    request.getParameter("lastName").replaceAll("'", "''"), 
                    request.getParameter("academicAffiliation").replaceAll("'", "''"), 
                    request.getParameter("address1").replaceAll("'", "''"), 
                    request.getParameter("address2").replaceAll("'", "''"), 
                    request.getParameter("city").replaceAll("'", "''"), 
                    request.getParameter("postcode").replaceAll("'", "''"), 
                    request.getParameter("county").replaceAll("'", "''"), 
                    request.getParameter("phone").replaceAll("'", "''"), 
                    request.getParameter("email").replaceAll("'", "''"), 
                    error);            
        }
   }

	/*
	 * Function checkIfUserNameRegistered
	 * 	Returns a boolean value depending on the string username:
	 * 	True if username is already in the users table or if username contains
	 * 		an illegal character
	 * 	False otherwise
	 * 
	 * Parameters:
	 * 	username - string containing users candidate username
	 *  
	 * See Also:
	 * 	<Login>, <Register>
	 */
	private boolean checkIfUsernameRegistered(String username) {
		boolean exists = false;
		if ( username.matches(".*\\W+.*") ) {  // \W is any char not in [a-zA-Z_0-9]
			log.info(HostInfo.tell() + "Register: Illegal chars in suggested name: " + username);
			return true;
		} else {
			log.info(HostInfo.tell() + "Register: No illegal chars in suggested name: " + username);
		}
		try {
			String query = "select username from users where username=?";
			log.fine(HostInfo.tell()+"checkIfUsernameRegisterd: "+username + ":" + query);
			HashMap<Integer,Object> parmlist = new HashMap<Integer,Object>();
			parmlist.put(1, username);
			ConnectDB c = new ConnectDB();
			c.capture();
			ResultSet rs = c.doPQuery(query,parmlist);
			if (rs.next()) {
				exists = true;
			}
			rs.close();
			c.release();
		} catch (Exception e) {
			log.severe("Register: error checking if username registere.");
			e.printStackTrace();
		}
		return exists;
	}

	/**
	 * Check if <code>password1</code> and <code>password2</code> match
	 * 
	 * @param username SWIFT username
	 */
	private boolean checkIfPasswordsMatch(String p1, String p2) {
		boolean match = false;
		if (p1.equalsIgnoreCase(p2))
			match = true;
		return match;
	}

	/**
	 * Check for at sign and period in email
	 * 
	 * @param email SWIFT e-mail address
	 */
	private boolean checkForAtSign(String e) {
		boolean match = false;
		if (e.contains("@") && e.contains("."))
			match = true;
		return match;
	}

	/**
	 * Check if username <code>u</code> contains special characters
	 * 
	 * @param username SWIFT username
	 */
	private boolean checkSpecialChars(String u) {
		boolean special = false;
		if (u.startsWith("\'") || u.startsWith("\"") || u.startsWith("!") || u.startsWith("@") || u.startsWith("#")
				|| u.startsWith("$") || u.startsWith("%") || u.startsWith("^") || u.startsWith("&") || u.startsWith("*")
				|| u.startsWith("-") || u.startsWith("_") || u.startsWith("(") || u.startsWith(")") || u.startsWith("+")
				|| u.startsWith("=") || u.startsWith("\\") || u.startsWith("|") || u.startsWith("{")
				|| u.startsWith("}") || u.startsWith("[") || u.startsWith("]") || u.startsWith(":") || u.startsWith(";")
				|| u.startsWith("~") || u.startsWith("`") || u.startsWith("<") || u.startsWith(">") || u.startsWith(",")
				|| u.startsWith(".") || u.startsWith("1") || u.startsWith("2") || u.startsWith("3") || u.startsWith("4")
				|| u.startsWith("5") || u.startsWith("6") || u.startsWith("7") || u.startsWith("8") || u.startsWith("9")
				|| u.startsWith("0"))
			special = true;
		return special;
	}

	/**
	 * Check if <code>username</code> is registered
	 * 
	 * @param username SWIFT username
	 * @param email    User email address
	 */
	private boolean checkIfEmailRegistered(String email, String username) {
		boolean exists = false;
		try {
			String query = "select username from users where email=?";
			log.fine(HostInfo.tell()+" checkIfEmailRegisterd: "+username + ":" + query);
			HashMap<Integer,Object> parmlist = new HashMap<Integer,Object>();
			parmlist.put(1, email);
			ConnectDB c = new ConnectDB();
			c.capture();
			ResultSet rs = c.doPQuery(query,parmlist);
			if (rs.next()) {
				exists = true;
			}
			rs.close();
			c.release();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return exists;
	}

	/**
	 * Displays the change details form populated with existing data.
	 * 
	 * @param userName            Desired username
	 * @param desiredPassword     Desired password after matching confirmed
	 * @param firstName           First name
	 * @param lastName            Last name
	 * @param academicAffiliation Affiliation
	 * @param address1            Address line 1
	 * @param address2            Address line 2
	 * @param city                City
	 * @param county              County
	 * @param postcode            Postcode
	 * @param phone               Phone number
	 * @param email               Email address
	 */
	private void insertNewUser(String userName, String desiredPassword, String firstName, String lastName,
			String academicAffiliation, String address1, String address2, String city, String postcode, String county,
			String phone, String email)  {
		String SHA1pwd = "";
		try {
			SHA1pwd = PassStore.getPass(desiredPassword);

		} catch (Exception e) {
			log.severe("Register: error generating password.");
			e.printStackTrace();
		}

		String query = "insert into users values (?,?,?,?,?,?,?,?,?,?,?,?,\'UNVERIFIED\',\'N\', now())";
		HashMap<Integer,Object> parmlist = new HashMap<Integer,Object>();
		parmlist.put(1, userName);
		parmlist.put(2,firstName);
		parmlist.put(3,lastName);
		parmlist.put(4, academicAffiliation);
		parmlist.put(5, address1);
		parmlist.put(6, address2);
		parmlist.put(7, city);
		parmlist.put(8, postcode);
		parmlist.put(9, county);
		parmlist.put(10, phone);
		parmlist.put(11, email);
		parmlist.put(12, SHA1pwd);
		log.fine(HostInfo.tell()+" insertNewUser:"+userName + ":" + query);
		try {
			ConnectDB c = new ConnectDB();
			c.capture();
			int r = c.doPUpdate(query,parmlist);
			if (r == 0)
				log.warning(HostInfo.tell()+ " Register: Error in insertNewUser: " + userName);
			c.release();
		} catch (Exception e) {
			log.severe("Register: error inserting user into DB.");
			e.printStackTrace();
		}
	}

	/**
	 * Returns a short description of the servlet.
	 */
	@Override
	public String getServletInfo() {
		return "Short description";
	}

	/**
	 * Notifies the Director that a new user has registered seeking Level 2 access.
	 * 
	 * @param request servlet request
	 */
	private void notifyDirector(HttpServletRequest request) {
		MailMessage m = new MailMessage();
		String body = "Please update the database record for " + request.getParameter("userName")
				+ " if their access is approved.";
		m.sendEmailToUser("director", body);
	}

	/**
	 * Confirms that application has been received for Level 2 access.
	 * 
	 * @param out     HTML PrintWriter
	 * @param request servlet request
	 * @throws IOException
	 */
	private void registrationConfirmed(PrintWriter out, HttpServletRequest request) throws IOException {
		HttpSession s = request.getSession();
		Page p = new Page("Register-registrationConfirmed");

		Map<String, Object> details = new HashMap<>();
		details.put("firstName", request.getParameter("firstName"));
		details.put("lastName", request.getParameter("lastName"));
		details.put("userName", request.getParameter("userName"));
		details.put("password", request.getParameter("desiredPassword1"));
		p.extendPageContext(out, "Registration for Condor Confirmed", s, details);
	}

	/**
	 * Displays user registration form.
	 * 
	 * @param out HTML PrintWriter
	 * @throws IOException
	 */
	private void registrationForm(PrintWriter out, HttpServletRequest request) throws IOException {
		HttpSession s = request.getSession();
		Page p = new Page("Register-registrationForm");
		p.UserPage(out, "User Registration", s); // Populate template and print to output
	}

	/**
	 * Displays user registration form.
	 * 
	 * @param out                 HTML PrintWriter
	 * @param userName            Desired username
	 * @param firstName           First name
	 * @param lastName            Last name
	 * @param academicAffiliation Affiliation
	 * @param address1            Address line 1
	 * @param address2            Address line 2
	 * @param city                City
	 * @param county              County
	 * @param postcode            Postcode
	 * @param phone               Phone number
	 * @param email               Email address
	 * @param msg                 Error message
	 * @throws IOException
	 */
	private void registrationForm(PrintWriter out, HttpServletRequest request, String userName, String desiredPassword1,
			String firstName, String lastName, String academicAffiliation, String address1, String address2,
			String city, String postcode, String county, String phone, String email, String msg) throws IOException {
		HttpSession s = request.getSession();
		Page p = new Page("Register-registrationForm");
		Map<String, Object> details = new HashMap<>();
		details.put("firstName", firstName);
		details.put("lastName", lastName);
		details.put("userName", userName);
		details.put("academicAffiliation", academicAffiliation);
		details.put("address1", address1);
		details.put("address2", address2);
		details.put("city", city);
		details.put("county", county);
		details.put("postcode", postcode);
		details.put("phone", phone);
		details.put("email", email);
		p.extendPageContext(out, msg, s, details);
	}
}
