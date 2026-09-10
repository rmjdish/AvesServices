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
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.sql.*;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

import mrc.util.MessageRelay;
import mrc.util.Page;
import mrc.util.SecModel;
import mrc.db.ConnectDB;
import mrc.util.HostInfo;
import mrc.util.Util;

/* Class: Variable
 * Support ArrayList operations for shopping Basket items
 * This gets called from the Search results pages to
 * add individual or groups of variables to the current Basket
 * It is also the basis of viewing the details of a variable
 * 
 */

public class Variable extends HttpServlet implements Serializable {

	private static final long serialVersionUID = 8405216918030547120L;
	// private static final Logger log = Logger.getLogger(HostInfo.tell()+":"+SessMgr.class.getName());
	private static final Logger log = Logger.getLogger("mrc.user");

	/**
	 * Handles the HTTP <code>GET</code> method.
	 * 
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 */
	// The following are class variables
	private static SecModel smod = new SecModel();
	private static MessageRelay msg2u = new MessageRelay();
	// This is an instance variable
	private ArrayList<String> varResults = new ArrayList<String>();

	/* Function: doGet
	 * 
	 * Parameters:
	 * 	request - Tomcat HttpServletRequest container object
	 * 	response - Tomcat HttpServletResponse container object
	 * 	
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		HttpSession s = request.getSession();      
		String username = s.getAttribute("username").toString();
		String command = request.getParameter("command");
		String name = request.getParameter("name");
		if (command.equalsIgnoreCase("addCard")) {
			addSingleVar(name, out, s);
		} else if (command.equalsIgnoreCase("displayCard")) {
			displayCard(name, out, s, username);
		}
	}

	/* Function: doPost
	 * 	Porcesses the form data produced by a "Multiple Add" option from Search Results.
	 * 
	 * Parameters:
	 * 	request - Tomcat HttpServletRequest container object
	 * 	response - Tomcat HttpServletResponse container object
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		HttpSession s = request.getSession();      
		String username = s.getAttribute("username").toString();
		int seclevel = (Integer) s.getAttribute("seclevel"); 
		// This is an attribute of the user, recorded in the session object

		// Add posted variables
		ArrayList<String> v = new ArrayList<String>();
		if (smod == null) {
			log.severe(HostInfo.tell()+" : Error with security model : checking will be faulty!");
		}
		if (msg2u == null) {
			log.severe(HostInfo.tell()+" : Error with message relay : output unreliable!");
		} else {
			// This is not in the right place
			// msg2u.msgFlush();
		}
		// v becomes a list of Variable names
		v = (ArrayList<String>) s.getAttribute("items"); 
		/* Pull the current list out of the session attributes
		 * So now there are 2 lists of variables:
		 *   1) the list of variables already in the Basket - this is s.getAttribute("items")
		 *   2) the list of variables checked or "clicked" on the Search results form - 
		 *      this is request.getParameterNames()
		 *      
		 * Adding SERNO, NTAG, SEX and INF if not already in ArrayList 
		 * is taken care of in expandVars/addVar2Basket
		 */
		Enumeration<String> e = request.getParameterNames();
		while (e.hasMoreElements()) {
			String name = e.nextElement().toString();
			if ( !StringUtils.isBlank(name) ) {
				expandVars(name.toLowerCase(), out, s);
			}
		}
		s.setAttribute("items", v);
		Page p = new Page("Variable-messageDisplay");
		p.setnumCols(2);
		p.setResults(msg2u.msgFlush()); // Gather messages for display
		p.UserPage(out, "Basket Results", s);
	}

	public void addSingleVar(String name, PrintWriter out, HttpSession s) {
		/* @brief Add a single variable to the basket and show output
		 * @param name - String variable name
		 * 
		 */
		
		
		if ( !StringUtils.isBlank(name) ) {
			expandVars(name.toLowerCase(), out, s);
		}
		Page p = new Page("Variable-messageDisplay");
		p.setnumCols(2);
		p.setResults(msg2u.msgFlush()); // Gather messages for display
		p.UserPage(out, "Basket Results", s);

	}
	/*
	 Function: expandVars
	 	This method is used as a shim between the doGet and doPost methods to
	 	allow Swift to expand a single Variable into a group where they are
	 	either longitudinal or scale variables
	 	
	 Parameters:
	 	name - Variable name
	 	out - PrintWriter object
	 	s - Current HttpSession object
	 */
	private void expandVars(String name, PrintWriter out, HttpSession s) {
		String query = "CALL expandLongVars(\'" + name + "\')";
		log.fine(s.getAttribute("username").toString() + " : " + query);
		try {
			ConnectDB c = new ConnectDB();
			c.capture();
			ResultSet rs = c.doQuery(query);
			int nrows = 0;
			while (rs.next()) {
				nrows = nrows + 1;
				String thisvar = rs.getString("Name");
				addVar2Basket(thisvar.toLowerCase(), out, s);
				if (nrows > 1) {
					msg2u.display(thisvar.toLowerCase(), 10);
				}
			}
			log.info(HostInfo.tell()+" Variable: expandVars: "+String.valueOf(nrows)+" added to basket.");
			rs.close();
			c.release();
		} catch (Exception e) {
			log.severe(HostInfo.tell()+" Variable: expandVars: Error "+e);	}
	}
	/*
	 Function addVar2Basket
	 	This does the bulk of the work to add a single Variable i.e. 'name'
	 	to a Basket contained in the session parameter 'items' If called from
	 	doGet then probably a single Variable to be added whereas if called
	 	from doPost it's probably a list of variables that are being added in
	 	a loop. Either way the session parameter 'items' is updated to
	 	reflect this.
	 
	 Parameters:
	 	name - Variable name
	 	out - PrintWriter object
	 	s - Current HttpSession object
	 */

	private void addVar2Basket(String name, PrintWriter out, HttpSession s) throws IOException {
		String username = s.getAttribute("username").toString();
		int seclevel = (Integer) s.getAttribute("seclevel"); 
		// This is an attribute of the user, recorded in the session object
		ArrayList<String> v = new ArrayList<String>();
		v = (ArrayList<String>) s.getAttribute("items");  //List of things already in basket
		ArrayList<String> results = new ArrayList<String>();
		String logtext = username + " sec level [" + seclevel + "] has items " + v.toString();
		log.fine(logtext);
		// Add SERNO, SEX and INF if not already in ArrayList
		if (!v.contains("SERNO"))
			v.add("SERNO");
		if (!v.contains("SEX"))
			v.add("SEX");
		if (!v.contains("INF"))
			v.add("INF");
		if (!v.contains("NTAG1"))
			v.add("NTAG1");
		if (smod == null) {
			log.severe(HostInfo.tell()+" Error with security model : checking will be faulty!");
		}
		if (msg2u == null) {
			log.severe(HostInfo.tell()+" Error with message relay : output unreliable!");
		}
		// Start looking at the variable name - check if should be replaced first
		// name = replaceWith(name);
		if (smod.checkAuth(seclevel, name)) {
			// Have authority to add this Variable, add and test what kind of
			// print to do
			if (!v.contains(name)) // Don't add it if already there
				v.add(name);
			if (smod.isOpen(name) && smod.hasMsg(name)) {
				// It's open but has special message
				msg2u.display(name, smod.getMsgId(name));
			} else {
				// Is not open (but authorised) OR has no special message
				msg2u.quiet(name, 2);
			}
		} else {
			msg2u.display(name, smod.getMsgId(name));
		}
		/*
		 * We need to push the modified list back onto the session attribute
		 * "items"
		 */
		s.setAttribute("items", v);
	}

	
	/*
	 Method: replaceWith
	 	Checks if a variable has a designated replacement.
	 	Uses ReplaceWith field of variablelabels table
	 
	 Parameters:
	 	name - Name of variable to be checked
	 
	 Returns:
	 	String name unchanged or name of replacement variable if exists
	 */
	String replaceWith(String name) {
		String replacement = name; // The default is return original variable
		String query = "select ReplaceWith from variablelabels where name = \'" + name + "\' and NOT ReplaceWith is NULL";
		log.info(HostInfo.tell()+" replaceWith: query: " + query);
		try {
			ConnectDB c = new ConnectDB();
			c.capture();
			ResultSet rs = c.doQuery(query);
			// You would think rs.first() would work with rs.getString, but you'd be wrong!
			while (rs.next()) {  // Navigate to first row if exists
				String fieldvalue = rs.getString(1);
				// be a bit caustion with what you get back
				if ( StringUtils.isNotBlank(fieldvalue) ) {
					replacement = fieldvalue;
					log.info(HostInfo.tell()+" replaceWith: replacement found for "+name+" ==> "+replacement);
				}
			}
			rs.close();
			c.release();
		} catch (Exception e) {
			log.severe(HostInfo.tell()+" replaceWith: Error geting result string:"+e);
		}
		return replacement;
	}
	
	
	/**
	 Method displayCategories
	 	Returns list of category memberships for a specific variable.
	 
	 Parameters:
	 	name - Name of variable to be checked
	 	username - Current logged in user
	 	
	 Returns:
	 	ArrayList<String> of labels from categorylabels table
	 */
	private HashMap<String, String> displayCategories(String name, String username) {
		String query = "SELECT code, label, descript FROM categorylabels WHERE code IN " +
				"(select code from categorymembers where name = \'" +
				name + "\')";
		log.fine(HostInfo.tell()+" displayCategories: "+username+":"+query);
		HashMap<String, String> catlabs = new HashMap<String, String>();
		try {
			ConnectDB c = new ConnectDB();
			c.capture();
			ResultSet rs = c.doQuery(query);
			rs.beforeFirst();
			while (rs.next()) {
				String combstyle = rs.getString("label")+" ["+rs.getString("code")+"]";
				catlabs.put(combstyle, rs.getString("descript"));
			}
			rs.close();
			c.release();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return catlabs;
	}

	/*
	 Method: displayValueLabels
	 	Returns a list of value labels for a specific variable.
	 
	 Parameters:
	 	name - Name of variable to be checked
	 	username - Current logged in user
	 	
	 Returns:
	 	ArrayList<String> of labels from valuelabels table
	 */
	private ArrayList<ArrayList<String>> displayValueLabels(String name, String username) {
		String query = "CALL getMetaForVar(\'" + name + "\')";
		/*
		 * getMetaForVar returns the following fields: 
		 * Tab  | Name  | VarLabel | Public | Value | ValueLabel | Missing |
		 * col 1| col 2 | col 3    | col 4  | col 5 | col 6      | col 7   |
		 */
		log.fine(HostInfo.tell()+" displayValueLabels: "+username+":"+query);
		ArrayList<ArrayList<String>> vallabs = new ArrayList<ArrayList<String>>();
		ArrayList<String> thisone = new ArrayList<String>();
		// Going to construct an array of two element arrays
		try {
			ConnectDB c = new ConnectDB();
			c.capture();
			ResultSet rs = c.doQuery(query);
			rs.beforeFirst();
			while (rs.next()) {
				thisone.add(rs.getString("Value")); // element 1 - the value
				thisone.add(rs.getString("ValueLabel")); // element 2 - the label
				vallabs.add((ArrayList<String>) thisone.clone()); // store a clone
				thisone.clear(); // clear the object for the next one
			}
			rs.close();
			c.release();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return vallabs;
	}

	/*
	 Method: displayDescriptives
	 	Returns frequency information from the descriptives table
	 	for a specific variable.
	 
	 Parameters:
	 	name - Name of variable to be checked
	 	username - Current logged in user
	 	
	 Returns:
	 	Text of frequencies information from descriptives table
	 */
	private String displayDescriptives(String name, String username) {
		String query = "SELECT descriptives FROM descriptives where name = \'" + name + "\'";
		String these = new String();
		log.fine(HostInfo.tell()+" displayDescriptives: "+username+":"+query);	
		try {

			ConnectDB c = new ConnectDB();
			c.capture();
			ResultSet rs = c.doQuery(query);
			while (rs.next()) {
				these = these + rs.getString(1);
			}
			rs.close();
			c.release();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return these;
	}
	/*
	 Method: displayDocs
	 	Returns list of associated documents from the docs table
	 	for a specific variable.
	 
	 Parameters:
	 	name - Name of variable to be checked
	 	cardNumber - Library file/dataset to which variable belongs
	 	s - Current HTTPSession object
	 	username - Current logged in user
	 	
	 Returns:
	 	ArrayList<String> of file names from docs table, if they exist
	 */
	   public ArrayList<String> displayDocs (String name, String cardNumber, HttpSession s, String username)
	    {
	      String query = "select distinct docname from docs where name =\'"+name+"\' or CardNumber = \'"+cardNumber+"\'";
	      log.info(HostInfo.tell()+" displayDocs: "+username+":"+query);
	      ArrayList<String> these = new ArrayList<String>();
	      try 
	      {
	    	  ConnectDB c=new ConnectDB();
	    	  c.capture();
	    	  ResultSet rs=c.doQuery(query);
	    	  getServletContext().log(username+" : "+query);
	    	  while (rs.next())
	    	  {
	    		  String thisdoc = rs.getString(1);
	    		  /*
	    		   * This should pick out each document that is listed under
	    		   * this Variable name.  Print them out as rows in the 	
	    		   * table already built in displayCard
	    		   */
	    		  these.add(thisdoc);
	    	  }
	    	  rs.close();
	    	  c.release();
	      }
	      catch(Exception e) 
	      {
	    	  log.severe(HostInfo.tell()+" disdplayDocs: Error :"+e.getMessage());
	      } 
	      return these;
	    }

		/*
		 Method: displayGroupMems
		 	Returns list of associated variables from the longitudinalvars table
		 	for a specific variable.
		 
		 Parameters:
		 	name - Name of variable to be checked
		 	s - Current HTTPSession object
		 	username - Current logged in user
		 	
		 Returns:
		 	ArrayList<String> of variable names from longitudinalvars table, if they exist
		 */
		   public ArrayList<String> displayGroupMems (String name)
		    {
		      String query = "CALL getGroupsForVar(\'"+name+"\')";
		      log.info(HostInfo.tell()+" displayGroupMems Query:"+query);
		      ArrayList<String> these = new ArrayList<String>();
		      try 
		      {
		    	  ConnectDB c=new ConnectDB();
		    	  c.capture();
		    	  ResultSet rs=c.doQuery(query);
		    	  while (rs.next())
		    	  {
		    		  /*
		    		   * This should pick out each variable that is a member
		    		   * of the same group in longitudinalvars as this Variable name.
		    		   * 
		    		   */
		    		  these.add(rs.getString("Public")); // Writing Public first as per Search.java
		    		  these.add(rs.getString("longVar"));
		    		  these.add(rs.getString("Label"));
		    		  these.add(rs.getString("YEAR"));
		    		  these.add(rs.getString("Form"));
		    		  these.add(rs.getString("QuestionNumber"));
		    		  these.add(rs.getString("CardNumber"));
		    	  }
		    	  rs.close();
		    	  c.release();
		      }
		      catch(Exception e) 
		      {
		    	  log.severe(HostInfo.tell()+" displayGroupMems: Error :"+e.getMessage());
		      } 
		      return these;
		    }
		   
		/*
		Method: displayCard
		 	This queries the variablelabels table for info on a specific variable
		 	and calls a Page object to display the information in a template output

	 	Parameters:
	 		name - Name of variable to be checked
	 		cardNumber - Library file/dataset to which variable belongs
	 		s - Current HTTPSession object
	 		username - Current logged in user
		 		 
		 */   
	   private void displayCard(String name, PrintWriter out, HttpSession s, String username) throws IOException {
		
		   
		String query = "select Label, CardNumber, Form, QuestionNumber, YEAR, Derived, field_id, " +
				"ReplaceWith, units, senstv, r_pub, r_sen, notes " +
				"from variablelabels where name =\'" + name + "\'";
		log.info(HostInfo.tell()+" displayCard: "+username + ": " + query);
		ArrayList<String> results = new ArrayList<String>();		
		String descrips = new String(); 			   // Not a list - descriptives get returned as a blob
		HashMap<String, String> catlabs = new HashMap<String, String>(); 					   // A simple list
		ArrayList<String> vardocs = new ArrayList<String>();
		ArrayList<String> grpmems = new ArrayList<String>();
		ArrayList<ArrayList<String>> vallabs = new ArrayList<ArrayList<String>>(); // A list of 2-lists
		descrips = displayDescriptives(name, username);
		vallabs = displayValueLabels(name, username);
		catlabs = displayCategories(name, username);
		grpmems = displayGroupMems(name);

		Page p = new Page("Variable-displayCard");
		p.setnumCols(14);
		/*
		 * Query Results from above:
		 * Field 1 -- Label
		 * Field 2 -- CardNumber
		 * Field 3 -- Form
		 * Field 4 -- QuestionNumber
		 * Field 5 -- YEAR
		 * Field 6 -- Derived
		 * Field 7 -- field_id
		 * Field 8 -- ReplaceWith
		 * Field 9 -- units
		 * Field 10 - senstv
		 * Field 11 - r_pub
		 * Field 12 - r_sen
		 * Field 13 - notes
		 * 
		 */
		try {
			int seclevel=(Integer) s.getAttribute("seclevel");
			ConnectDB c = new ConnectDB();
			c.capture();
			ResultSet rs = c.doQuery(query);
			String field = "";
			results.add(name); // Save Variable name as                                     Column 1
			while (rs.next()) {
				results.add(Util.wrapGetString(rs.getString(7))); // Save field_id          Column 2
				results.add(Util.wrapGetString(rs.getString(1))); // Save Variable label    Column 3
				field = Util.wrapGetString(rs.getString(2));
				results.add(field); // Save Card Number       Column 4
				if (seclevel > 0) {
					vardocs = displayDocs(name, field, s, username);
				}
				results.add(Util.wrapGetString(rs.getString(3))); // Save Form              Column 5
				results.add(Util.wrapGetString(rs.getString(4))); // Save Question Number   Column 6
				results.add(Util.wrapGetString(rs.getString(5))); // Save Year              Column 7
				results.add(Util.wrapGetString(rs.getString(6))); // Save Derived Status    Column 8
				results.add(Util.wrapGetString(rs.getString(8))); // Save ReplaceWith       Column 9
				results.add(Util.wrapGetString(rs.getString(9))); // Save units             Column 10
				results.add(Util.wrapGetString(rs.getString(10))); // Save Sensitive        Column 11
				results.add(Util.wrapGetString(rs.getString(11))); // Save Reason Public    Column 12
				results.add(Util.wrapGetString(rs.getString(12))); // Save Reason Sensitive Column 13
				results.add(Util.wrapGetString(rs.getString(13))); // Save Notes            Column 14				
				rs.close();
			c.release();
			}
		} 
		catch (Exception e) {
			log.info(HostInfo.tell()+" displayCard: Error: "+ e.getMessage());
		}
		p.setResults(results); // push the whole set of results into the context
		p.variableMetadata(out, "Variable Metadata", s, vardocs, descrips, catlabs, vallabs, grpmems);
	}

	public void initVarResults() {
		this.varResults = new ArrayList<String>();
	}

	public void setVarResults(String var, String msg) {
		this.varResults.add(var);
		this.varResults.add(msg);
	}

	public ArrayList<String> getVarResults() {
		return this.varResults;
	}

	/*
	 Method: getServletInfo
	 	Returns a short description of the servlet.
	 */
	@Override
	public String getServletInfo() {
		return "This Class does most of the work in making decision on adding variables to baskets and reporting on baskets";
	}
}
