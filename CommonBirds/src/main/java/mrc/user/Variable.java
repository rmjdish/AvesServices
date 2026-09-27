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
		} else if (command.equalsIgnoreCase("toggleLinked")) {
			toggleLinkedVariables(out, s);
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
		// Collected up front into a set, rather than processed one at
		// a time straight off the Enumeration - expandVars() needs the
		// full set of names the user actually checked, not just the
		// one currently being iterated, to correctly tell apart a
		// variable that was checked directly (even if it happens to
		// share a longitudinal measure with others) from one that was
		// only pulled in via that expansion. See expandVars() for the
		// full explanation of what this fixes.
		java.util.Set<String> checkedNames = new java.util.HashSet<String>();
		while (e.hasMoreElements()) {
			String name = e.nextElement().toString();
			if (!StringUtils.isBlank(name)) {
				checkedNames.add(name.toLowerCase());
			}
		}
		// One connection, captured once and reused for every checked
		// variable in this Multiple Add request, rather than
		// expandVars() opening and releasing a new one per variable -
		// see expandVars(String, PrintWriter, HttpSession, ConnectDB)
		// for the full explanation.
		ConnectDB multiAddConn = new ConnectDB();
		multiAddConn.capture();
		try {
			for (String name : checkedNames) {
				expandVars(name, out, s, multiAddConn, checkedNames);
			}
		} finally {
			multiAddConn.release();
		}
		s.setAttribute("items", v);
		renderBasketResults(out, s);
	}

	/*
	 Method: hasAnyLinkedAdded
	 	Whether this add event produced any "linked-added" variables at
	 	all - used to decide whether the "keep linked variables" toggle
	 	should appear on the page, independent of its current on/off
	 	state (which is a separate question - see linkedVarsCurrentlyKept()).
	*/
	private boolean hasAnyLinkedAdded(ArrayList<String> triples) {
		for (int i = 0; i < triples.size(); i += 3) {
			if ("linked-added".equals(triples.get(i + 2))) {
				return true;
			}
		}
		return false;
	}

	/*
	 Method: extractLinkedAddedNames
	 	Returns the "linked-added" variable names from a triples list as
	 	a map of lower-cased name to original-cased name - the lower-
	 	cased key supports case-insensitive matching against the
	 	session's basket list, while the original-cased value is what
	 	gets re-inserted if the toggle is switched back on, rather than
	 	assuming lowercase.
	*/
	private java.util.Map<String, String> extractLinkedAddedNames(ArrayList<String> triples) {
		java.util.Map<String, String> names = new java.util.HashMap<String, String>();
		for (int i = 0; i < triples.size(); i += 3) {
			if ("linked-added".equals(triples.get(i + 2))) {
				String original = triples.get(i);
				names.put(original.toLowerCase(), original);
			}
		}
		return names;
	}

	/*
	 Method: linkedVarsCurrentlyKept
	 	Whether this add event's linked variables are currently present
	 	in the session's basket list - checking one is enough to
	 	determine the group's state, since they're always added or
	 	removed together as a group by the toggle. Returns true if
	 	there's nothing to toggle at all (no linked variables from this
	 	add), which is also the correct default state for a fresh add
	 	(they were all just added, so they're all currently present).
	*/
	private boolean linkedVarsCurrentlyKept(HttpSession s, ArrayList<String> triples) {
		java.util.Map<String, String> linkedNames = extractLinkedAddedNames(triples);
		if (linkedNames.isEmpty()) {
			return true;
		}
		ArrayList<String> items = (ArrayList<String>) s.getAttribute("items");
		if (items == null) {
			return false;
		}
		for (String item : items) {
			if (linkedNames.containsKey(item.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	/*
	 Method: filterOutLinkedAdded
	 	Returns a copy of a triples list with "linked-added" entries
	 	removed - everything else (added, added-note, restricted,
	 	linked-restricted) is kept unchanged. Used to build the display
	 	list and recompute counts when the "keep linked variables"
	 	toggle is switched off, without needing a separate counting
	 	method - countByCategory() run on the filtered list already
	 	gives the right numbers.
	*/
	private ArrayList<String> filterOutLinkedAdded(ArrayList<String> triples) {
		ArrayList<String> filtered = new ArrayList<String>();
		for (int i = 0; i < triples.size(); i += 3) {
			if (!"linked-added".equals(triples.get(i + 2))) {
				filtered.add(triples.get(i));
				filtered.add(triples.get(i + 1));
				filtered.add(triples.get(i + 2));
			}
		}
		return filtered;
	}

	/*
	 Method: renderBasketResults
	 	Gathers this request's fresh messages from msg2u.msgFlush(),
	 	sorts and stores them for possible later use by the "keep
	 	linked variables" toggle, then renders. Used by the two places
	 	a fresh add happens: addSingleVar() (the \u03a3 icon) and doPost()
	 	(Multiple Add).
	*/
	private void renderBasketResults(PrintWriter out, HttpSession s) throws IOException {
		ArrayList<String> messages = mrc.util.BasketMessageTriples.sortByCategory(msg2u.msgFlush());
		s.setAttribute("lastAddResults", messages);
		renderBasketResultsFromStored(out, s, messages);
	}

	/*
	 Method: renderBasketResultsFromStored
	 	Renders Basket Management from an already-sorted triples list -
	 	either a fresh add (see renderBasketResults() above) or the
	 	same list re-used by the "toggleLinked" command (see doGet())
	 	after switching the linked variables on or off, so the toggle
	 	can be flipped back and forth without needing to re-run
	 	expandLongVars or re-check authorisation each time.
	*/
	private void renderBasketResultsFromStored(PrintWriter out, HttpSession s, ArrayList<String> messages) throws IOException {
		boolean hasLinkedVars = hasAnyLinkedAdded(messages);
		boolean linkedVarsKept = linkedVarsCurrentlyKept(s, messages);
		ArrayList<String> displayMessages = linkedVarsKept ? messages : filterOutLinkedAdded(messages);

		Page p = new Page("Variable-messageDisplay");
		p.setnumCols(3);
		p.setResults(displayMessages);

		// Every 3rd entry (starting at 0) is a variable name - check
		// them all in one batch query.
		ArrayList<String> namesToCheck = new ArrayList<String>();
		for (int i = 0; i < displayMessages.size(); i += 3) {
			namesToCheck.add(displayMessages.get(i));
		}
		java.util.Set<String> owlAvailable = mrc.util.OwlAvailability.checkAvailable(namesToCheck);
		java.util.Map<String, String> restrictedMessages = mrc.util.RestrictedVariables.checkRestricted(namesToCheck);
		java.util.Map<String, String> variableLabels = mrc.util.VariableLabels.checkLabels(namesToCheck);
		java.util.Map<String, Integer> categoryCounts = mrc.util.BasketMessageTriples.countByCategory(displayMessages);

		java.util.Map<String, Object> extraFlags = new java.util.HashMap<String, Object>();
		extraFlags.put("hasLinkedVars", hasLinkedVars);
		extraFlags.put("linkedVarsKept", linkedVarsKept);
		// The original linked-added count, from the full, unfiltered
		// messages list - kept separate from categoryCounts (which
		// reflects only what's currently displayed) since the toggle's
		// own explanatory text needs to say e.g. "the 5 variables..."
		// consistently regardless of whether they're currently shown.
		extraFlags.put("linkedVarsCount", mrc.util.BasketMessageTriples.countByCategory(messages).getOrDefault("linked-added", 0));
		extraFlags.put("variableLabels", variableLabels);

		p.UserPage(out, "Basket Results", s, owlAvailable, restrictedMessages, categoryCounts, extraFlags);
	}

	/*
	 Method: toggleLinkedVariables
	 	Handles the "keep linked variables" toggle: removes this add
	 	event's linked-added variables from the session's basket list
	 	if they're currently present, or re-adds them (with their
	 	original casing, no re-validation needed since they already
	 	passed authorisation once in this same session) if they're
	 	currently absent - then re-renders the same Basket Management
	 	page from the same stored results, so the toggle can be
	 	switched back and forth freely.
	*/
	private void toggleLinkedVariables(PrintWriter out, HttpSession s) throws IOException {
		ArrayList<String> storedMessages = (ArrayList<String>) s.getAttribute("lastAddResults");
		if (storedMessages == null) {
			storedMessages = new ArrayList<String>();
		}
		java.util.Map<String, String> linkedNames = extractLinkedAddedNames(storedMessages);
		ArrayList<String> items = (ArrayList<String>) s.getAttribute("items");
		if (items == null) {
			items = new ArrayList<String>();
		}
		boolean currentlyKept = linkedVarsCurrentlyKept(s, storedMessages);
		if (currentlyKept) {
			java.util.Iterator<String> it = items.iterator();
			while (it.hasNext()) {
				if (linkedNames.containsKey(it.next().toLowerCase())) {
					it.remove();
				}
			}
		} else {
			for (String originalCaseName : linkedNames.values()) {
				boolean alreadyPresent = false;
				for (String item : items) {
					if (item.equalsIgnoreCase(originalCaseName)) {
						alreadyPresent = true;
						break;
					}
				}
				if (!alreadyPresent) {
					items.add(originalCaseName);
				}
			}
		}
		s.setAttribute("items", items);
		renderBasketResultsFromStored(out, s, storedMessages);
	}

	public void addSingleVar(String name, PrintWriter out, HttpSession s) throws IOException {
		/* @brief Add a single variable to the basket and show output
		 * @param name - String variable name
		 * 
		 */
		
		
		if ( !StringUtils.isBlank(name) ) {
			expandVars(name.toLowerCase(), out, s);
		}
		renderBasketResults(out, s);

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
		// Single-call version, used by addSingleVar() (the \u03a3 icon,
		// one variable per request) - opens its own connection for
		// just this one call, then delegates to the shared-connection
		// version below. The "directly checked" set here is trivially
		// just this one variable, since the \u03a3 icon only ever adds
		// one at a time.
		ConnectDB c = new ConnectDB();
		c.capture();
		try {
			expandVars(name, out, s, c, java.util.Collections.singleton(name));
		} finally {
			c.release();
		}
	}

	private void expandVars(String name, PrintWriter out, HttpSession s, ConnectDB c, java.util.Set<String> checkedNames) {
		// Shared-connection version, used by doPost()'s "Multiple Add"
		// loop - takes an already-captured connection instead of
		// opening a new one per call. Multiple Add previously called
		// the single-connection version above once per checked
		// variable, meaning a new connection was opened and released
		// on every iteration - for dozens of checked variables, that
		// was the dominant cost, far more expensive than the queries
		// themselves once a connection is already open.
		String query = "CALL expandLongVars(\'" + name + "\')";
		log.fine(s.getAttribute("username").toString() + " : " + query);
		try {
			// expandLongVars calls itself recursively, but MySQL's
			// max_sp_recursion_depth defaults to 0 - which disables
			// stored procedure recursion entirely, not "unlimited" as
			// the name might suggest. Every call was silently failing
			// as a result (nothing added, no confirmation message
			// ever generated), not from a crash - the resulting
			// NullPointerException below was already being caught by
			// this method's own try/catch, so callers never saw an
			// error, only an empty result. Raised as a session-level
			// setting, scoped to just this connection, rather than
			// requiring a server-wide MySQL configuration change.
			c.doUpdate("SET SESSION max_sp_recursion_depth = 20");
			ResultSet rs = c.doQuery(query);
			int nrows = 0;
			if (rs != null) {
				while (rs.next()) {
					nrows = nrows + 1;
					String thisvar = rs.getString("Name");
					String status = addVar2Basket(thisvar.toLowerCase(), out, s);
					// Whether this is treated as "directly added" or
					// "linked" now depends on whether the user actually
					// checked this specific variable themselves - not
					// on its position within this one call's result
					// set. The previous nrows > 1 check meant that if
					// several checked variables happened to share the
					// same longitudinal measure, each one's own
					// expandLongVars() call returned the same group,
					// and each call independently (and inconsistently)
					// decided "whichever came back first is added, the
					// rest are linked" - overwriting each other's
					// categorisation for the same names, with the
					// final result depending on the non-deterministic
					// order the checkboxes happened to be processed
					// in. Checking against checkedNames instead means
					// every one of those directly-checked variables
					// is correctly and consistently labelled "added",
					// regardless of processing order or which call's
					// result set it appeared in.
					if (!checkedNames.contains(thisvar.toLowerCase())) {
						// This is a linked variable (auto-added
						// alongside one the user directly checked, not
						// one they checked themselves) - previously this
						// called msg2u.display(thisvar.toLowerCase(), 10)
						// unconditionally for every row past the first,
						// which silently overwrote whatever message
						// addVar2Basket() had just set (since both use
						// the same key, the variable name), including a
						// correct restriction message when the linked
						// variable turned out to be restricted and was
						// correctly NOT added. Now branches on the
						// status addVar2Basket() actually reported, so a
						// restricted linked variable gets a message that
						// says so, rather than the generic "also added"
						// text implying success.
						if ("restricted".equals(status)) {
							java.util.List<String> single = java.util.Collections.singletonList(thisvar.toLowerCase());
							java.util.Map<String, String> restrictionText = mrc.util.RestrictedVariables.checkRestricted(single);
							String reason = restrictionText.getOrDefault(thisvar.toLowerCase(), "No message");
							msg2u.displayText(thisvar.toLowerCase(), "This is a linked variable but restricted. " + reason, "linked-restricted");
						} else {
							msg2u.display(thisvar.toLowerCase(), 10, "linked-added");
						}
					}
				}
				rs.close();
			} else {
				log.severe(HostInfo.tell()+" Variable: expandVars: query returned no result set for: "+name);
			}
			log.info(HostInfo.tell()+" Variable: expandVars: "+String.valueOf(nrows)+" added to basket.");
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

	private String addVar2Basket(String name, PrintWriter out, HttpSession s) throws IOException {
		// Now returns a status string ("added", "added-note",
		// "restricted") instead of void, so callers - specifically
		// expandVars()'s loop over linked variables - can tell whether
		// this specific variable actually got added or was blocked,
		// rather than assuming success unconditionally.
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
		String status;
		// Start looking at the variable name - check if should be replaced first
		// name = replaceWith(name);
		if (smod.checkAuth(seclevel, name)) {
			// Have authority to add this Variable, add and test what kind of
			// print to do
			if (!v.contains(name)) // Don't add it if already there
				v.add(name);
			if (smod.isOpen(name) && smod.hasMsg(name)) {
				// It's open but has special message
				msg2u.display(name, smod.getMsgId(name), "added");
				status = "added-note";
			} else {
				// Is not open (but authorised) OR has no special message
				msg2u.quiet(name, 2, "added");
				status = "added";
			}
		} else {
			msg2u.display(name, smod.getMsgId(name), "restricted");
			status = "restricted";
		}
		/*
		 * We need to push the modified list back onto the session attribute
		 * "items"
		 */
		s.setAttribute("items", v);
		return status;
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

		// Collect every variable name this page will display (the main
		// variable plus every linked/longitudinal variable) and check
		// them all in one batch query, rather than one query per name.
		ArrayList<String> namesToCheck = new ArrayList<String>();
		namesToCheck.add(name);
		for (int i = 1; i < grpmems.size(); i += 7) {
			// grpmems cycles [Public, longVar(name), Label, YEAR, Form, QuestionNumber, CardNumber]
			namesToCheck.add(grpmems.get(i));
		}
		java.util.Set<String> owlAvailable = mrc.util.OwlAvailability.checkAvailable(namesToCheck);
		java.util.Map<String, String> restrictedMessages = mrc.util.RestrictedVariables.checkRestricted(namesToCheck);

		p.variableMetadata(out, "Variable Metadata", s, vardocs, descrips, catlabs, vallabs, grpmems, owlAvailable, restrictedMessages);
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
