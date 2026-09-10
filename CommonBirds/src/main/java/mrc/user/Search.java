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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

import mrc.util.Page;
import mrc.util.TreeSearch;
import mrc.db.ConnectDB;
import mrc.util.CatTree;
import mrc.util.HostInfo;

/*
 Class: Search
 	Implements keyword metadata Search access
 */
public class Search extends HttpServlet {

	private static final long serialVersionUID = 5744489881206011158L;
	private static final String LIBDELIM = " : ";
	// private static final Logger log = Logger.getLogger(HostInfo.tell() + ":"
	// + Search.class.getName());
	private static final Logger log = Logger.getLogger("mrc.user");

	
	/*
	 Method: doGet
	 	Handles the HTTP GET method.
	 
	 Parameters:
	 	request -  HttpServletRequest
	 	response - HttpServletResponse
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession s = request.getSession();
		PrintWriter out = response.getWriter();
		response.setContentType("text/html;charset=UTF-8");
		// The following prints to the Tomcat mrc log
		log.fine(HostInfo.tell() + " Search Class: doGet Called.  About to do searchForm");
		String type = request.getParameter("type");
		if (type == null) {
			type = "k"; // A default Search type is keyword Search
		} else if (type.equalsIgnoreCase("lib")) {
			// Ok fancy give me everything in this Card Number
			searchMetadata(request, out, s);
		} else {
			String username = s.getAttribute("username").toString();
			searchForm(out, s, type, username);
		}
	}

	/*
	 Method: doPost
	 	Handles the HTTP POST method.
	 
	 Parameters:
	 	request - HttpServletRequest
	 	response - HttpServletResponse
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		HttpSession s = request.getSession();
		log.fine(" Search Class: doPost Called.  About to do searchMetadata");
		searchMetadata(request, out, s);
		out.flush();
	}

	/*
	 Method: getServletInfo
	 	Returns a short description of the servlet as a String.
	 */
	@Override
	public String getServletInfo() {
		return "This class implements all the " + HostInfo.tell() + " Search operators.";
	}

	/*
	 Method: searchForm
	 	Displays Search form.
	 
	 Parameters:
	 	out      - PrintWriter
	 	s        - HttpSession
	 	type     - Search type
	 	username - Condor username
	 	
	 About:
	 	Can throw IOException
	 */
	private void searchForm(PrintWriter out, HttpSession s, String type, String username) throws IOException {

		Map<String, Object> context = new HashMap<>();
		if (type.equalsIgnoreCase("n")) {
			Page p = new Page("Search-nameSearch");
			log.fine(" Search Servlet: Variable name Search using " + p.getTemplate());
			p.UserPage(out, "NSHD Variable Name Search", s);
		} else if (type.equalsIgnoreCase("k")) {
			Page p = new Page("Search-keySearch");
			log.fine(" Search Servlet: Keyword Search using " + p.getTemplate());
			p.UserPage(out, "NSHD Keyword Search", s);
		} else if (type.equalsIgnoreCase("l")) {
			Page p = new Page("Search-librarySearch");
			log.fine(" Search Servlet: library file Search using " + p.getTemplate());
			ArrayList<String> libfiles = listLibraries(username);
			context.put("libNames", libfiles);
			p.extendPageContext(out, "NSHD Library Search", s, context);
		} else if (type.equalsIgnoreCase("t")) {
			Page p = new Page("Search-topicSearch");
			log.fine(" Search Servlet: topic Search using " + p.getTemplate());
			ArrayList<String> topics = listTopics(username);
			context.put("topics", topics);
			p.extendPageContext(out, "NSHD Topic Search", s, context);
		} else if (type.equalsIgnoreCase("y")) {
			Page p = new Page("Search-yearSearch");
			log.fine(" Search Servlet: Search by year using " + p.getTemplate());
			ArrayList<String> studyears = listYears(username);
			context.put("studyYears", studyears);
			p.extendPageContext(out, "NSHD Search By Year", s, context);
		} else if (type.equalsIgnoreCase("c")) {
			// This is where we need to adjust the display for tree selection style
			Page p = new Page("Search-catTree");
			log.fine(" Search Servlet: category tree search using " + p.getTemplate());
			List<String> htmllines = listCategories(username);
			p.setResults(htmllines);
			p.UserPage(out, "NSHD Search By Category", s);
		} else if (type.equalsIgnoreCase("ky")) {
			Page p = new Page("Search-keyyearSearch");
			log.fine(" Search Servlet: Keyword Search using " + p.getTemplate());
			ArrayList<String> studyears = listYears(username);
			context.put("studyYears", studyears);
			p.extendPageContext(out, "NSHD Search By Keyword And Year", s, context);
		}
	}

	/*
	 Function: listYears 
	 	Lists all years that data have been collected.
	 
	 Parameter: 
	 	username - Condor username
	 	
	 Returns: 
	 	results   - ArrayList<String>	 
	 */
	private ArrayList<String> listYears(String username) {
		ArrayList<String> results = new ArrayList<String>();
		String query = "SELECT distinct year FROM variablelabels where year > 1945 order by year asc";
		getServletContext().log(username + ":" + query);
		try {
			ConnectDB c = new ConnectDB();
			c.capture();
			ResultSet rs = c.doQuery(query);
			while (rs.next()) {
				results.add(rs.getString(1));
			}
			rs.close();
			c.release();
		} catch (Exception e) {
			log.severe(HostInfo.tell()+" listYears SQL: "+e.getMessage());
		}
		return results;
	}

	/*
	 Method: listLibraries
	 	Lists all variables contained in a specific library file.
	  
	 Parameter:
	 	username - Condor username
	 
	 Returns: 
	 	results   - ArrayList<String>	 
	 */
	private ArrayList<String> listLibraries(String username) {
		ArrayList<String> results = new ArrayList<String>();
		String query = "SELECT DISTINCT t1.CardNumber, t2.description FROM "
				+ "variablelabels AS t1 LEFT JOIN filepath AS t2 ON (t1.CardNumber = t2.CardNumber)"
				+ "ORDER BY t1.CardNumber asc";
		getServletContext().log(username + ":" + query);
		try {
			ConnectDB c = new ConnectDB();
			c.capture();
			ResultSet rs = c.doQuery(query);
			while (rs.next()) {
				results.add(rs.getString(1) + LIBDELIM + rs.getString(2));
			}
			rs.close();
			c.release();
		} catch (Exception e) {
			log.severe(HostInfo.tell()+" listLibraries SQL: "+e.getMessage());
		}
		return results;
	}

	/*
	 Method: listTopics
	 	Lists all variables contained in topic defined as a keyword.  Uses
	 	SQL function getTopicFromKeywords defined in db.
	 
	 Parameter:
	 	username  - Condor username
	 
	 Returns:
	 	results   - ArrayList<String>
	 
	 */
	private ArrayList<String> listTopics(String username) {
		ArrayList<String> results = new ArrayList<String>();
		String query = "SELECT distinct getTopicFromKeywords(term) AS 'topic' FROM keywords " + "WHERE term LIKE '%:%' "
				+ "order by topic";
		getServletContext().log(username + ":" + query);
		try {
			ConnectDB c = new ConnectDB();
			c.capture();
			ResultSet rs = c.doQuery(query);
			while (rs.next()) {
				results.add(rs.getString(1));
			}
			rs.close();
			c.release();
		} catch (Exception e) {
			log.severe(HostInfo.tell()+" listTopics SQL: "+e.getMessage());

		}
		return results;
	}

	/*
	 Method: listCategories
	 	Lists all categories for that data have been collected and arranges them
	 	in suitable order to be printed.
	 
	 Parameter:
	 	username - Condor username
	 */
	private List<String> listCategories(String username) {
		// Need to create an Integer -> Object(CatTree) Map 
		List<CatTree> treelist = new ArrayList<CatTree>();
		List<String> htmllines = new ArrayList<String>();
		String query = "SELECT code, label, parent, printorder FROM categorylabels order by parent,code asc";
		log.info("Search:listCategories:User: "+ username + " SQL: " + query);
		try {
			ConnectDB c = new ConnectDB();
			c.capture();
			ResultSet rs = c.doQuery(query);
			while (rs.next()) {
				// Create a Map from code to CatTree object
				// Now get the number of items in this category code
				String subquery = "SELECT num_cat_items(" + rs.getString("code") + ")";
				ResultSet ss = c.doQuery(subquery);
				String items = "0";
				if(ss.next()){
					items = ss.getString(1);
				}
				// First create a CatTree object from the query result row
				CatTree cnode = new CatTree(Integer.valueOf(rs.getString("code")),
						rs.getString("label"),
						Integer.valueOf(rs.getString("parent")),
						Integer.valueOf(rs.getString("printorder")),
						Integer.valueOf(items)
						);
				ss.close();
				// Add code object to List<CatTree>
				treelist.add(cnode);
			}
			rs.close();
			c.release();
		} catch (Exception e) {
			log.severe("Search:listCategories: SEVERE SQL DB ERROR: "+e.getMessage());
		}
		// 
		TreeSearch treeTool = new TreeSearch(treelist);
		htmllines = treeTool.generateTree();
		return htmllines;
	}

	/*
	 Function: searchMetadata 
	 	Searches metadata tables for Variable names and
	 	descriptions that contain a specific term.  Mostly uses
	 	db SQL procedures to do the query construction.
	 
	 Parameters: 
	 	request - HttpServletRequest
	 	out     - PrintWriter
	 	s       - HTTPSession
	 */
	private void searchMetadata(HttpServletRequest request, PrintWriter out, HttpSession s) {
		final String SPLITTER = "\\s+|,\\s*"; // spaces and commas
		String username = s.getAttribute("username").toString();
		String accessLevel = s.getAttribute("accessLevel").toString();
		int secLevel = Integer.parseInt(s.getAttribute("seclevel").toString());
		String library = "";
		library = request.getParameter("library");
		String topic = "";
		topic = request.getParameter("topic");
		String type = "";
		type = request.getParameter("type");
		String category = "";
		category = request.getParameter("category");
		String term = "";
		term = request.getParameter("term");
		String year = "";
		year = request.getParameter("year");
		String query = "";
		request.getParameter("bool1");
		request.getParameter("bool2");
		String soundex = "";
		String like = "";
		soundex = request.getParameter("soundex");
		if (soundex == null)
			like = "like";
		else
			like = "sounds like";
		// Add bool1 and bool2; sounds like

		if (type.equalsIgnoreCase("n")) { // variable name search
			// Variable Name search - taken care of below
			query = "Name Search SQL Procedure Call";
		} else if (type.equalsIgnoreCase("k")) {
			// Keyword search - taken care of below
			query = "Keyword Search SQL Procedure Call";
		} else if (type.equalsIgnoreCase("l") || type.equalsIgnoreCase("lib")) {
			if (!library.isEmpty()) {
				// If there's a colon in the library string extract the first bit only
				if (library.contains(LIBDELIM)) {
					// Split out the first part
					int stp = library.indexOf(LIBDELIM);
					library = library.substring(0, stp);
				}
			} else { // library IS empty
				library = "ALL";
			}
			query = "Library Search SQL Procedure Call";
			term = library;
		} else if (type.equalsIgnoreCase("t")) {
			// topic search taken care of below
			query = "Topic Search SQL Procedure Call";
			if (topic.isEmpty()) {
				term = "ALL";
			} else {
				term = topic;
			}
		} else if (type.equalsIgnoreCase("y")) {
			// year search taken care of below
			query = "Year Search SQL Procedure Call";
			if (year.isEmpty()) {
				term = "ALL";
			} else {
				term = year;
			}
		} else if (type.equalsIgnoreCase("c")) {
			// Taken care of below - Category is numeric so no ALL available
			query = "Category Search SQL Procedure Call";
			// Extract code from TreeSearch 
			category = extractTreeCode(category);
			term = category;
		}
		 // Log whatever query
		log.fine(HostInfo.tell() + " searchMetadata: " + username + " Query: " + query);
		log.fine(HostInfo.tell() + " searchMetadata: SQL calling with secLevel: " + secLevel + " and term: " + term);
		// in Tomcat Logs folder
		try {
			// Submit the query and format the results!!
			ConnectDB c = new ConnectDB();
			c.capture();
			ResultSet rs = null;
			switch (type.toLowerCase()) {
			case "n":
				rs = c.donameSearch(secLevel, term);
				break;
			case "k":
				rs = c.doindexSearchUnion(secLevel, term);
				break;
			case "l":
			case "lib":
				rs = c.dolibrarySearch(secLevel, term);
				break;
			case "t":
				rs = c.dotopicSearch(secLevel, term);
				break;
			case "y":
				rs = c.doyearSearch(secLevel, term);
				break;
			case "c":
				rs = c.docategorySearch(secLevel, term);
				break;
			default:
				rs = c.doQuery(query);
				break;
			}
			int rowcount = 0;
			// Query done and results assigned to rs now output form for adding to Basket
			// How many fields in the ResultSet? There are 6 in all above SQL
			Page p = new Page("Search-searchResults");
			p.setnumCols(7); // This is the number of fields in each row of the results
			ArrayList<String> results = new ArrayList<String>();
			while (rs.next()) {
				rowcount = rowcount + 1;
				results.add(rs.getString(7)); // Public flag; write it first
				results.add(rs.getString(1)); // Variable Name
				results.add(rs.getString(2)); // Variable Label
				results.add(rs.getString(3)); // Year
				results.add(rs.getString(4)); // Form
				results.add(rs.getString(5)); // Question Number
				results.add(rs.getString(6)); // CardNumber
			}
			// Format the results with a template
			p.setResults(results);
			p.setnumResults(rowcount);
			p.UserPage(out, "Search Results", s); // Populate template and print to output
			rs.close();
			c.release();
		} catch (Exception e) {
			log.severe(HostInfo.tell()+" searchMetadata SQL: "+e.getMessage());
		}
	}

	private String extractTreeCode(String category) {
		// The code appears between square brackets e.g. "CatLabel[<code>]"
		String result = StringUtils.substringBetween(category,"[","]");
		if (result == null)
			return "";
		else
			return result;
					
	}
}
