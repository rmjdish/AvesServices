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
import java.util.*;
import java.util.logging.Logger;
import java.sql.*;

import mrc.util.Page;
import mrc.util.StoreXMLBasket;
import mrc.db.ConnectDB;
import mrc.user.RandomString;
import mrc.util.HostInfo;

/*
 Class: Basket
  	Facilitates user-level shopping Basket activities
  
 */
public class Basket extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	// private static final Logger log = Logger.getLogger(HostInfo.tell() + ":" + Basket.class.getName());
	private static final Logger log = Logger.getLogger("mrc.user");
	private static final int SUFLEN = 6;
	// The xmlversion field previously stored here (a StoreXMLBasket
	// instance shared via `this`) was a genuine concurrency bug: Basket
	// is a singleton servlet instance shared by every concurrent
	// request across every user in Tomcat's threading model, so this
	// field was shared mutable state - two users saving baskets at the
	// same moment could have their XML data cross-contaminate. Fixed
	// by not storing it on `this` at all: createInternalBasket() now
	// returns the StoreXMLBasket it creates, and callers pass that
	// same object on to insertShoppingBasketRecord() as a local
	// variable, scoped to that one request/thread only.
	/* 
	 Procedure: doGet
	 	Overriden procedure that accepts HTTP GET requests from Tomcat Java Servlet framework
     
     Parameters:
     	request - HttpServletRequest object carrying attributes of the HTML GET
     	response - HttpServletResponse object carrying attributes of the HTML output channel
     
    */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException 
    {
        HttpSession s = request.getSession();
		String accessLevel = s.getAttribute("accessLevel").toString();        
        PrintWriter out = response.getWriter();
        /* Variable: v
        	Stores the current list of variables carried around in session object
        */
        ArrayList<String> v = new ArrayList<String>();
        v = (ArrayList<String>) s.getAttribute("items"); // The Variable list is carried around in the session object
        String username = s.getAttribute("username").toString();
        /* Variable: p
          	Pebble template based object that uses .html file 
          	indicated as parameter to Page constructor
        
            --- Java
            Page p = new Page("Basket-Basket");
			p.setResults(v);
			--- 
         */
        Page p = new Page("Basket-Basket");
        p.setResults(v); // loads current list of variables into Page context
        p.setnumResults(v.size());
        if (request.getParameter("command")!=null)
        {   
            log.fine(HostInfo.tell()+" Basket: GET called with command -> "+request.getParameter("command"));
            if (request.getParameter("command").equalsIgnoreCase("remove"))
            {
                removeItemFromBasket(request, v, s); // this updates the session Variable items attribute
                displayBasket(out,request.getParameter("name")+" has been removed", s, p);
            } 
            else if (request.getParameter("command").equalsIgnoreCase("removeAll"))
            {
                removeAllItemsFromBasket(v, s);
                displayBasket(out,"All variables have been removed from your Basket.",s, p);
            }
            else if (request.getParameter("command").equalsIgnoreCase("saveNew"))
            {
                // what follows is one way of forming a unique Basket name
                String basketID = username +"ZZ"+RandomString.randomstring(SUFLEN); 
                log.info(HostInfo.tell()+" Basket: New Basket name -> "+basketID);
                p.chgTemplate("Basket-saveBasketForm");
                // Work out in advance how many baskets this save will
                // actually produce, so the form can tell the user
                // before they commit - a basket over 500 variables
                // (not counting SERNO/SEX/INF/NTAG1) is split across
                // several saved baskets rather than one oversized one.
                ArrayList<String> nonStandard = nonStandardVars(v);
                List<ArrayList<String>> chunks = chunkVariables(nonStandard);
                List<String> plannedBasketIds = splitBasketIds(basketID, chunks.size());
                // Now call the matching view method in the Page class
                p.shareForm(out, "Enter Basket Details for "+basketID, s, basketID, plannedBasketIds);        
            }
            else if (request.getParameter("command").equalsIgnoreCase("saveExisting"))
            {
            	/*
            	 * Change the template before displaying it
            	 * Also need to get a list of baskets to overwrite
            	 */
                log.fine(HostInfo.tell()+" Basket: saveExisting; changing template to basketoverwrite.");            	
            	p.chgTemplate("Basket-basketoverwrite");
            	p.setResults(getBasketList(out,s));  // Set results attribute in current Page object
            	p.UserPage(out,"Select a Basket to overwrite.",s);
            }
            else if (request.getParameter("command").equalsIgnoreCase("overwrite"))
            {
                String basket=request.getParameter("basketID");
                int r=0;
                try
                {
                    r=deleteItemsFromBasket(s, basket);
                }
                catch (Exception se)
                {
                	log.warning(HostInfo.tell()+ " Basket: Error in deleteItemsFromBasket: "+basket);
                    se.printStackTrace();
                }                      
                int q=0;
                try
                {
                    q=saveItemsInBasket(s, basket, v);
                }
                catch (Exception se)
                {
                	log.warning(HostInfo.tell()+" Basket: Error in saveItemsInBasket: "+basket);
                    se.printStackTrace();
                }                        
                p.UserPage(out,"Basket "+basket+" overwritten with "+q+" variables.", s);
            }
            else 
            {
            	log.warning(HostInfo.tell()+" Basket: cannot match command -> "+request.getParameter("command")); 
            }
        }
        else
        {
            displayBasket(out,"Basket Details", s, p);
        }    
    } 

    /* 
     Method: doPost 
	 	Overriden procedure that accepts HTTP GET requests from Tomcat Java Servlet framework
	 	Allows internal users to save new shopping baskets.
     
     Parameters:
     	request - HttpServletRequest object carrying attributes of the HTML GET
     	response - HttpServletResponse object carrying attributes of the HTML output channel

    */
    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException 
    {        
        PrintWriter out = response.getWriter();        
        HttpSession s = request.getSession();
		String accessLevel = s.getAttribute("accessLevel").toString(); 
		/* Variable: username
		 * 	Swift username of the current user.  Used to look up 
		 * 	baskets.  Carried in the session variable
		 */
        String username=(String)s.getAttribute("username");
		/* Variable: basketID
		 * 	Unique basket identifier to look up 
		 * 	baskets.  Carried in the session variable
		 */
        String basketID=request.getParameter("basketID");
        String description=request.getParameter("description");
        // The database column allows up to 120 characters, but
        // descriptions are capped at 100 here to leave headroom for
        // the "(Basket X of N)" suffix a split save appends
        // afterwards, without the combined result ever risking
        // truncation by the column itself. The save form's own
        // maxlength="100" already prevents this client-side; this is
        // a server-side safety net for a direct/manual submission
        // that bypasses the form.
        if (description != null && description.length() > 100) {
            description = description.substring(0, 100);
        }
        String query = "";
        log.fine(HostInfo.tell()+" Basket: POST method called with Basket ID -> "+basketID);

        // A basket over MAX_VARS_PER_BASKET variables (not counting
        // SERNO/SEX/INF/NTAG1, which go into every split basket
        // separately) is saved as several baskets instead of one -
        // the same split the user was already shown a preview of on
        // the save form (see doGet()'s "saveNew" handler and
        // Basket-saveBasketForm.html). Recomputed here from the same
        // session basket rather than trusted from the form, since
        // the basket could in principle have changed between viewing
        // that preview and submitting this save.
        ArrayList<String> v = (ArrayList<String>) s.getAttribute("items");
        ArrayList<String> nonStandard = nonStandardVars(v);
        List<ArrayList<String>> chunks = chunkVariables(nonStandard);
        List<String> savedBasketIds = splitBasketIds(basketID, chunks.size());

        String cleanUsername = username.replaceAll("'", "''");
        String cleanDescription = description.replaceAll("'", "''");
        for (int i = 0; i < chunks.size(); i++) {
            String thisBasketId = savedBasketIds.get(i);
            // Only extends the description with "(Basket X of N)"
            // when a split actually happened - an ordinary, unsplit
            // save keeps exactly the description the user typed.
            String thisDescription = chunks.size() > 1
                    ? cleanDescription + " (Basket " + (i + 1) + " of " + chunks.size() + ")"
                    : cleanDescription;
            // Captured as a local variable, scoped to just this one
            // chunk of this one save request/thread - not stored on
            // `this`, which is how the cross-request concurrency bug
            // (Basket being a singleton servlet shared by every
            // concurrent user) was fixed. See createInternalBasket()
            // and insertShoppingBasketRecord() for the full explanation.
            StoreXMLBasket xmlversion = null;
            try
            {
                xmlversion = createInternalBasket(cleanUsername, thisBasketId, thisDescription);
            }
            catch (Exception se)
            {
                log.warning(HostInfo.tell()+" Basket: Error in createInternalBasket: "+thisBasketId);
            }        
            log.info(HostInfo.tell()+": "+username+": basket "+thisBasketId);
            if (xmlversion == null) {
                log.severe(HostInfo.tell()+" Basket: createInternalBasket failed for "+thisBasketId+" - skipping insertShoppingBasketRecord for this chunk.");
                continue;
            }
            try
            {
                query = insertShoppingBasketRecord(s, cleanUsername, thisBasketId, chunks.get(i), xmlversion);
            }
            catch (Exception se)
            {
                log.warning(HostInfo.tell()+" Basket: Error in insertShoppingBasketRecord: "+thisBasketId);
            }        
            log.info(" Basket: "+username+":"+query);
        }

		String confirmationTitle;
		if (savedBasketIds.size() > 1) {
			confirmationTitle = "Your basket was too large for one save, so it has been saved as "
					+ savedBasketIds.size() + " separate baskets, listed below for reference:";
		} else {
			confirmationTitle = "Basket "+basketID+" has been saved. Its contents are listed below for reference:";
		}
		// A dedicated, read-only confirmation page - deliberately not
		// View Current Basket (Basket-Basket.html/displayBasket()):
		// that page is for the working, editable basket (Remove
		// links, the SERNO/NTAG1 callout, the "you have N variables"
		// risk-level message) - none of which makes sense for a
		// basket that has already been saved. Built as a small,
		// hand-generated HTML string per saved basket rather than
		// pushed through the generic results/numCols template
		// machinery, since that's built around one flat table, not
		// several independently-grouped ones (one per saved basket).
		StringBuilder combinedHtml = new StringBuilder();
		for (int i = 0; i < chunks.size(); i++) {
			ArrayList<String> varsInThisBasket = new ArrayList<String>(chunks.get(i));
			if (!varsInThisBasket.contains("SERNO")) varsInThisBasket.add("SERNO");
			if (!varsInThisBasket.contains("SEX")) varsInThisBasket.add("SEX");
			if (!varsInThisBasket.contains("INF")) varsInThisBasket.add("INF");
			if (!varsInThisBasket.contains("NTAG1")) varsInThisBasket.add("NTAG1");
			java.util.Map<String, String[]> meta = fetchVariableMetadata(varsInThisBasket);
			combinedHtml.append(buildBasketTableHtml(savedBasketIds.get(i), varsInThisBasket, meta));
		}

		Page p = new Page("Basket-savedConfirmation");
		p.savedBasketsConfirmation(out, confirmationTitle, s, combinedHtml.toString());
    }
    
    
    
    
    /*
     Method: createInternalBasket
     	Insert internal project record, used to describe Basket when shared - not a formal data access proposal! 
     
     Parameters:
     	username - Swift/Condor Username
     	basketID - unique Basket ID
     	description - Basket description
     	
     Returns:
     	The StoreXMLBasket created for this basket, so the caller can
     	pass it on to insertShoppingBasketRecord() as a local variable -
     	previously this was stored on `this.xmlversion` instead, which
     	was a genuine concurrency bug (see the field's removal above
     	for the full explanation). Returns null if the insert failed,
     	so callers should check before using it.
    */
    private StoreXMLBasket createInternalBasket(String username, String basketID, String description) 
    {
        String query="";
        try 
        {
            query = "insert into basketdetails values (\'" + basketID + "\', \'" + description + "\', \'" + username + "\', now())";
            String projectId = "Not-Known";
            StoreXMLBasket xmlversion = new StoreXMLBasket(username,basketID,description,projectId);

            log.info(HostInfo.tell()+": "+username+": "+query);
            ConnectDB c = new ConnectDB();
            c.capture();
            int r = c.doUpdate(query);
            c.release();
            if (r <= 0) {
                log.warning(HostInfo.tell()+ " Basket: Error in createInternalBasket: "+username+" : query: "+query);
                return null;
            }
            return xmlversion;
        } 
        catch (Exception e) 
        {
            log.severe(HostInfo.tell()+" ERROR:Basket:createInternalBasket:"+query);
            return null;
        }
    }

    /* 
    Method: insertShoppingBasketRecord
     Insert approvals record for proposal
    
    Parameters:
    	s -  HTTPSession object
    	username - String object containing user name
    	basketID - String object containing Basket ID
    	
    Returns:
    	query - String SQL used for insertion
    */
	/*
	 A basket over this many variables (not counting SERNO/SEX/INF/
	 NTAG1, which are added to every split basket separately, on top
	 of this limit) is split into several saved baskets rather than
	 one. Matches the 500 figure already used as the recommended
	 maximum throughout the rest of the site (View Current Basket's
	 risk-level note, the menu badge's colour thresholds).
	*/
	private static final int MAX_VARS_PER_BASKET = 500;

	/*
	 Method: nonStandardVars
	 	Returns a copy of the given list with SERNO, SEX, INF and
	 	NTAG1 removed (case-insensitively) - these are added back to
	 	every split basket individually by insertShoppingBasketRecord(),
	 	so they shouldn't count towards the 500-per-basket split limit
	 	or appear twice.
	*/
	private ArrayList<String> nonStandardVars(ArrayList<String> vars) {
		ArrayList<String> result = new ArrayList<String>();
		for (String v : vars) {
			if (!v.equalsIgnoreCase("SERNO") && !v.equalsIgnoreCase("SEX")
					&& !v.equalsIgnoreCase("INF") && !v.equalsIgnoreCase("NTAG1")) {
				result.add(v);
			}
		}
		return result;
	}

	/*
	 Method: chunkVariables
	 	Splits a list of variables into chunks of at most
	 	MAX_VARS_PER_BASKET each, preserving order. An empty input
	 	still returns one (empty) chunk, so a basket containing only
	 	the four standard variables still gets saved as one basket
	 	rather than none.
	*/
	private List<ArrayList<String>> chunkVariables(ArrayList<String> vars) {
		List<ArrayList<String>> chunks = new ArrayList<ArrayList<String>>();
		if (vars.isEmpty()) {
			chunks.add(new ArrayList<String>());
			return chunks;
		}
		for (int i = 0; i < vars.size(); i += MAX_VARS_PER_BASKET) {
			int end = Math.min(i + MAX_VARS_PER_BASKET, vars.size());
			chunks.add(new ArrayList<String>(vars.subList(i, end)));
		}
		return chunks;
	}

	/*
	 Method: splitBasketIds
	 	Given the base basket ID the user was shown and how many
	 	chunks the basket needs to be split into, returns the actual
	 	IDs to save each chunk under: the base ID unchanged if only one
	 	chunk is needed, or the base ID suffixed _1, _2, ... _numChunks
	 	if more than one is needed.
	*/
	private List<String> splitBasketIds(String baseBasketID, int numChunks) {
		List<String> ids = new ArrayList<String>();
		if (numChunks <= 1) {
			ids.add(baseBasketID);
		} else {
			for (int i = 1; i <= numChunks; i++) {
				ids.add(baseBasketID + "_" + i);
			}
		}
		return ids;
	}

    /* 
    Method: insertShoppingBasketRecord
     Takes the variables to save directly, and the StoreXMLBasket
     created for this specific basket by createInternalBasket() -
     passed as a local variable here, rather than read from a shared
     `this.xmlversion` field, since Basket is a singleton servlet
     instance shared by every concurrent request in Tomcat's threading
     model; a shared instance field would have meant two users saving
     baskets at the same moment could cross-contaminate each other's
     XML data. Also accepts an explicit variable list (rather than
     always reading the full session basket) so a single oversized
     basket can be split into several saved baskets, each built from
     its own chunk of variables, in one save request.
    
    Parameters:
    	s -  HTTPSession object
    	username - String object containing user name
    	basketID - String object containing Basket ID
    	varsToSave - the variables for this specific basket (a chunk,
    	  for a split save, or the whole basket otherwise)
    	xmlversion - the StoreXMLBasket returned by this same save's
    	  createInternalBasket() call, local to this one request/thread
    	
    Returns:
    	query - String SQL used for insertion
    */
	private String insertShoppingBasketRecord(HttpSession s, String username, String basketID, ArrayList<String> varsToSave, StoreXMLBasket xmlversion)
    {
        String query="";
         try 
        {
            ArrayList<String> v = new ArrayList<String>(varsToSave);
            // All baskets must have the following four variables
            if (!v.contains("SERNO")) 
            {
                v.add("SERNO");
            }
            if (!v.contains("SEX")) 
            {
                v.add("SEX");
            }
            if (!v.contains("INF")) 
            {
                v.add("INF");
            }
            if (!v.contains("NTAG1"))
            {
            	v.add("NTAG1");
            }
            int r=0;
            ConnectDB c = new ConnectDB();
            c.capture();
            for (String item : v) 
            {
                query = "insert into shoppingbaskets values (\'" + username + "\', \'" + basketID + "\', \'" + item + "\')";
                xmlversion.plusVar(item);
                log.fine(HostInfo.tell()+" insertShoppingBasketRecord: "+username+":"+query);
                r = c.doUpdate(query);
            }
            c.release();
            if (r==0)
                log.warning(HostInfo.tell()+ " Basket Error in insertShoppingBasketRecord: "+username+" : query: "+query); 
            // Finally push XML into store
            xmlversion.storeDoc();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        return query;
    }

    /*
     Method: getServletInfo 
     	Returns a short description of the servlet.
     	
     Returns:
     	String description
    */
    @Override
	public String getServletInfo() 
    {
        return "Class to execute shopping basket manipulation";
    }

    /*
     Method: fetchVariableMetadata
     	Batch lookup of Label, Form and YEAR from variablelabels for a
     	list of variable names, in one query rather than one per
     	variable - extracted from displayBasket() so the save
     	confirmation page (which needs the same lookup, once per saved
     	basket) can reuse it rather than duplicating the query logic.
     	See displayBasket()'s own comments for why variablelabels (not
     	metadata) and LOWER() matching are both used here.
     
     Parameters:
     	vars - the variable names to look up (case-insensitive)
     
     Returns:
     	A map of lower-cased variable name to a 3-element array:
     	[Label, Form, YEAR]. Missing/failed lookups simply aren't
     	present in the map - callers should treat a missing key the
     	same as they treat a null field.
    */
    private java.util.Map<String, String[]> fetchVariableMetadata(ArrayList<String> vars) {
        java.util.Map<String, String[]> metaByName = new java.util.HashMap<String, String[]>();
        if (!vars.isEmpty()) {
            StringBuilder placeholders = new StringBuilder();
            for (int i = 0; i < vars.size(); i++) {
                if (i > 0) placeholders.append(",");
                placeholders.append("?");
            }
            String query = "SELECT name, Label, Form, YEAR FROM variablelabels WHERE LOWER(name) IN (" + placeholders + ")";
            log.info(HostInfo.tell()+" fetchVariableMetadata: "+query);
            try {
                ConnectDB c = new ConnectDB();
                c.capture();
                java.util.HashMap<Integer, Object> params = new java.util.HashMap<Integer, Object>();
                for (int i = 0; i < vars.size(); i++) {
                    params.put(i + 1, vars.get(i).toLowerCase());
                }
                ResultSet rs = c.doPQuery(query, params);
                if (rs != null) {
                    while (rs.next()) {
                        metaByName.put(rs.getString("name").toLowerCase(), new String[]{
                            rs.getString("Label"), rs.getString("Form"), rs.getString("YEAR")
                        });
                    }
                    rs.close();
                }
                c.release();
            } catch (Exception e) {
                log.severe(HostInfo.tell()+" fetchVariableMetadata: Error :"+e.getMessage());
            }
        }
        return metaByName;
    }

    /*
     Method: escapeHtml
     	Minimal HTML-escaping for values that get embedded directly
     	into a hand-built HTML string (the save confirmation page) -
     	variable names/labels come from the database rather than
     	directly from user input, so this is a low-risk precaution
     	rather than a response to any known issue, but cheap enough to
     	apply consistently.
    */
    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    /*
     Method: buildBasketTableHtml
     	Builds a simple, read-only HTML table (name/label/form/year)
     	for one saved basket, for the save confirmation page - no
     	Remove links, no SERNO/NTAG1 callout, no risk-level message,
     	since the basket is already saved at this point and none of
     	that (editing, standard-variable notes, size warnings) applies
     	to a historical record being shown purely for reference. Those
     	all belong on View Current Basket, the working, editable copy.
     
     Parameters:
     	basketId - this specific saved basket's ID (heading for its table)
     	varsInThisBasket - the variables saved into this specific basket
     	metaByName - the batch-fetched Label/Form/YEAR data (see fetchVariableMetadata())
    */
    private String buildBasketTableHtml(String basketId, ArrayList<String> varsInThisBasket, java.util.Map<String, String[]> metaByName) {
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"help-header\"><h3>Basket: ").append(escapeHtml(basketId)).append("</h3></div>\n");
        html.append("<table class=\"owl-table\" style=\"width: 90%; margin-bottom: 24px;\">\n");
        html.append("<tr><th style=\"width:20%\">Variable</th><th style=\"width:50%\">Label</th><th style=\"width:15%\">Form</th><th style=\"width:15%\">Year</th></tr>\n");
        for (String v : varsInThisBasket) {
            String[] meta = metaByName.get(v.toLowerCase());
            String label = meta != null && meta[0] != null ? meta[0] : "NA";
            String form = meta != null && meta[1] != null ? meta[1] : "NA";
            String year = meta != null && meta[2] != null ? meta[2] : "NA";
            html.append("<tr><td>").append(escapeHtml(v.toLowerCase())).append("</td><td>")
                    .append(escapeHtml(label)).append("</td><td>")
                    .append(escapeHtml(form)).append("</td><td>")
                    .append(escapeHtml(year)).append("</td></tr>\n");
        }
        html.append("</table>\n");
        return html.toString();
    }

    /*
     Method: displayBasket
     	Display contents of shopping Basket.
     
     Paramaters:
     	out - PrintWriter object 
    	p -  SWIFT Page object for template
    	s - HttpSession object
      
     About: 
        This function throws IOException 
    */
    private void displayBasket(PrintWriter out, String title, HttpSession s, Page p) throws IOException 
    {
		String accessLevel = s.getAttribute("accessLevel").toString();
		ArrayList<String> vars = new ArrayList<String>(); // The variable names
		ArrayList<String> ents = new ArrayList<String>(); // The entries consisting of var names + labels
        vars = (ArrayList<String>) s.getAttribute("items"); // The Variable list is carried around in the session object

        // SEX and INF are standard variables included in every basket -
        // sort them to the front of the DISPLAYED list only, keeping
        // everything else in its existing relative order. Sorted on a
        // copy, not the session's own "items" list directly - vars
        // above is a reference to that same session-backed object,
        // and sorting it in place would have permanently reordered
        // the actual stored basket, not just this page's display.
        vars = new ArrayList<String>(vars);
        java.util.Collections.sort(vars, new java.util.Comparator<String>() {
            public int compare(String a, String b) {
                boolean aStandard = a.equalsIgnoreCase("sex") || a.equalsIgnoreCase("inf");
                boolean bStandard = b.equalsIgnoreCase("sex") || b.equalsIgnoreCase("inf");
                if (aStandard && !bStandard) return -1;
                if (!aStandard && bStandard) return 1;
                return 0;
            }
        });
        
        // Switched from metadata (VarLabel only) to variablelabels -
        // the table Variable.java's own displayCard() method already
        // queries for Label, Form, and YEAR together, confirmed
        // directly from that existing, working query rather than
        // assumed. variablelabels is also the canonical table used by
        // most of the rest of this codebase (checkAuth, expandLongVars'
        // ReplaceWith lookup, etc.), so likely more reliable generally,
        // not just for the extra columns.
        java.util.Map<String, String[]> metaByName = fetchVariableMetadata(vars);
        for (String v : vars) {
            ents.add(v);
            String[] meta = metaByName.get(v.toLowerCase());
            ents.add(meta != null && meta[0] != null ? meta[0] : "NA");
            ents.add(meta != null && meta[1] != null ? meta[1] : "NA");
            ents.add(meta != null && meta[2] != null ? meta[2] : "NA");
        }
        
        // Iterate over the 
    	p.setnumCols(4);
    	p.setResults(ents);

    	// One batch check against variablelabels for every variable in
    	// this basket, rather than one query per variable.
    	java.util.Set<String> owlAvailable = mrc.util.OwlAvailability.checkAvailable(vars);
    	java.util.Map<String, String> restrictedMessages = mrc.util.RestrictedVariables.checkRestricted(vars);

        // Now call the matching view method in the Page class
        p.UserPage(out,title,s,owlAvailable,restrictedMessages);        

    }

 
    /*
    Method: displayBasketList
    	Iterate through list of previous shopping baskets for overwriting.
    
    Parameters:
    	out - PrintWriter object 
    	s - HttpSession object
    	p - Swift Page object for template
    	
    */
    private void displayBasketList(PrintWriter out, HttpSession s, Page p) 
    {
        try
        {
            String username=s.getAttribute("username").toString();
    		String accessLevel = s.getAttribute("accessLevel").toString();
            String query="";
            query="select basketID, description from basketdetails where basketID in (select basketID from shoppingbaskets where username=\'"+username+"\')";
            ArrayList<String> resmap = new ArrayList<String>(); // List of variables
            log.info(HostInfo.tell()+": "+username+": "+query);
            ConnectDB c = new ConnectDB();
            c.capture();
            ResultSet rs = c.doQuery(query);
            while (rs.next()) 
            {
                resmap.add(rs.getString(1));
                resmap.add(rs.getString(2));
            }
            rs.close();
            c.release();
            p.chgTemplate("Basket-displayListOfBaskets");
        	p.setnumCols(2);
        	p.setResults(resmap);
            p.UserPage(out,username+" Baskets",s);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    /*
     Method: getBasketList
     	Get a list of baskets belonging to the logged in session user
     	
     Parameters:
     	out - PrintWriter object
     	s - HttpSession object
     	
     Returns:
     	results - ArrayList<String> of basketID, description pairs
     */
    
    private ArrayList<String> getBasketList(PrintWriter out, HttpSession s) throws IOException 
    {
        ArrayList<String> results = new ArrayList<String>(); // List of variables
        try
        {
        	String username=(String)s.getAttribute("username");
            String query="";
            query="select basketID, description from basketdetails where basketID in (select basketID from shoppingbaskets where username=\'"+username+"\')";
            log.info(HostInfo.tell()+": "+username+": "+query);
            ConnectDB c = new ConnectDB();
            c.capture();
            ResultSet rs = c.doQuery(query);
            while (rs.next()) 
            {
                results.add(rs.getString(1));
                results.add(rs.getString(2));
            }
            rs.close();
            c.release();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return results;
    }



    /*
    Method: removeItemFromBasket
     	Removes selected item from shopping Basket.
    
    Parameters:
    	request - HttpServletRequest object
    	v - ArrayList containing items in basket
    	s - HttpSession object
    */
    private void removeItemFromBasket(HttpServletRequest request, ArrayList<String> v, HttpSession s) 
    {
        String name = request.getParameter("name");
        v.remove(name);
        s.setAttribute("items", v);
    }
    
    /*
    Method: removeAllItemsFromBasket
    	Removes all items from shopping Basket.
    
    Parameters
    	v - ArrayList containing items
    	s - HttpSession object
    */
    private void removeAllItemsFromBasket(ArrayList<String> v, HttpSession s) 
    {
        v.clear();
        s.setAttribute("items", v);
    }
    
   /*
    Function deleteItemsFromBasket 
    	Deletes all items from saved shopping Basket.  Returns integer number of
    	rows in SQL response
    
    Parameters:
    	basket - String BasketID
    	s - HttpSession object
    	
    Returns:
    	r - int >0 if successful deletion
    */
    private int deleteItemsFromBasket(HttpSession s, String basket) 
    {
        int r=0;
        String username=(String)s.getAttribute("username");
        String query="delete from shoppingbaskets where basketID=\'"+basket+"\'";
        try 
        {
            getServletContext().log(username+":"+query);
            ConnectDB c = new ConnectDB();
            c.capture();
            r = c.doUpdate(query);
            c.release();
            if (r==0)            
                log.warning(HostInfo.tell()+" :"+username+":Basket.deleteItemsFromBasket() failed with query: "+query);
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        return r;
    }

    /* 
    Method: saveItemsInBasket
    	Saves all items from shopping Basket. Creates a Basket description if necessary.
        
    Parameters:
    
    	basket  - String BasketID
    	f  - Enumeration of ArrayList containing items
    	s  - HttpSession object
    	
    Returns:
    	q - int >0 if successful insertion into db
    */
    private int saveItemsInBasket(HttpSession s, String basket, ArrayList<String> f) 
    {
        // Insert all new items in Basket
        int q=0;
        int r=0;
        ListIterator<String> bskvars = f.listIterator();
        String username=(String)s.getAttribute("username");
        ConnectDB c = new ConnectDB();
        c.capture();
        while (bskvars.hasNext()) 
        {
            try 
            {
                String query = "insert into shoppingbaskets values (\'" + username + "\', \'" + basket + "\', \'" + bskvars.next() + "\')";
                r = c.doUpdate(query);
                q=q+r;
            } 
            catch (Exception e) 
            {
            	log.severe(HostInfo.tell()+" Basket: Error in saveItemsInBasket");
                e.printStackTrace();
            }
        }
        c.release();
        if (q==0)            
            log.warning(HostInfo.tell()+ " Basket Error in saveItemsInBasket for user: "+username);        
        return q;
    }


}
