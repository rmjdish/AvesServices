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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import mrc.util.MessageRelay;
import mrc.util.Page;
import mrc.util.SecModel;
import mrc.db.ConnectDB;
import mrc.util.HostInfo;

/*
 Class: SearchFiles
 	Does everything associated with downloading built datasets.
 */
public class SearchFiles extends HttpServlet {
	private static final long serialVersionUID = 5744489881206011158L;
	private static final String LIBDELIM = " : ";
	// private static final Logger log = Logger.getLogger(HostInfo.tell() + ":"
	// + Search.class.getName());
	private static final int OKMSG = 20;
	private static final Logger log = Logger.getLogger("mrc.user");
	private static SecModel smod = new SecModel();
	private static MessageRelay msg2u = new MessageRelay();
	private ArrayList<String> varResults = new ArrayList<String>();

	/*
	 Function: doGet 
	 	Responds to the menu item 'Search->Files' and lists 'long'
	 	datasets available such as genetics files
	 
	 Parameters: 
	 	request - Tomcat HttpServletRequest container object 
	 	response - Tomcat HttpServletResponse container object
	 
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession s = request.getSession();
		PrintWriter out = response.getWriter();
		listDatasetMetadata(request, out, s);
	}

	/*
	 Function: listFiles 
	 	Lists all files available for download.
	 
	 Returns:
	 	HashMap<String,String> of name and description from datasets table
	 */
	private HashMap<String, String> listFiles() {
		HashMap<String, String> results = new HashMap<String, String>();
		String query = "SELECT dname, description from datasets";
		log.info(HostInfo.tell() + ": " + query);
		try {
			ConnectDB c = new ConnectDB();
			c.capture();
			ResultSet rs = c.doQuery(query);
			while (rs.next()) {
				results.put(rs.getString(1), rs.getString(2));
			}
			rs.close();
			c.release();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return results;
	}

	/*
	 Function: doPost 
	 	Processes the form data produced by a "select" option from
	 	the list datasets form.
	  
	 Parameters: 
	 	request - Tomcat HttpServletRequest container object 
	 	response - Tomcat HttpServletResponse container object
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		HttpSession s = request.getSession();
		log.fine(" Search Class: doPost Called.  About to do searchMetadata");
		searchFileMetadata(request, out, s);
		out.flush();

	}

	/*
	 Function: listYears 
	 	Lists all years that data have been collected.  Only
	 	adds years (String values) that correspond to valid ints.
	 
	 Parameters: 
	 	username - Current logged in username 
	 * 
	 * Returns:
	 * 	results - ArrayList<String> of years from variablelabels table
	 */
	private ArrayList<String> listYears(String username) {
		ArrayList<String> results = new ArrayList<String>();
		String query = "SELECT distinct year FROM variablelabels where year > 1945 order by year asc";
		log.info(HostInfo.tell()+" listYears: "+username + ": " + query);
		int yrnum = 0;
		try {
			ConnectDB c = new ConnectDB();
			c.capture();
			ResultSet rs = c.doQuery(query);
			while (rs.next()) {
				try 
				{
					yrnum = Integer.parseUnsignedInt(rs.getString(1));
					results.add(String.valueOf(yrnum));
				} 
				catch (NumberFormatException e)
				{
					// do nothing
				}
			}
			rs.close();
			c.release();
		} catch (Exception e) {
			e.printStackTrace();
			log.severe(HostInfo.tell()+" listYears: Error reading years from table variablelabels.");
		}
		return results;
	}

	/*
	 Function: listDatasetMetadata 
	 	Queries the datasets table in the MySQL
	 	database and lists all members via a form
	 
	 Parameters: 
	 	request - Tomcat HttpServletRequest container object 
	 	out - Tomcat PrintWriter object for use by the form 
	 	s - Tomcat HttpSession object that carries current context info
	 */
	private void listDatasetMetadata(HttpServletRequest request, PrintWriter out, HttpSession s) throws IOException {
		String username = s.getAttribute("username").toString();
		Map<String, Object> context = new HashMap<>();
		ArrayList<String> studyears = listYears(username);
		context.put("studyYears", studyears);
		Page p = new Page("SearchFiles-keyyearSearch");
		log.fine(HostInfo.tell() + " SearchFiles: search using " + p.getTemplate());
		p.extendPageContext(out, "NSHD File Datasets - Search by keyword and year", s, context);
	}
	/*
	 Method: listDatasets 
	 	Returns a list of all the available datasets from the datasets table
	 
	Parameters: 
		 	username - Current logged in username 
	
	Returns:
		results - ArrayList<String> tuple of  name,location,description,year, and
		number of cases from datasets table

	*/
	private ArrayList<String> listDatasets(String username) {
		ArrayList<String> results = new ArrayList<String>();
		String query = "SELECT dname, location, description, year, ncases from dataset " + "ORDER BY dname asc";
		log.fine(HostInfo.tell() + ": " + username + ": " + query);
		try {
			ConnectDB c = new ConnectDB();
			c.capture();
			ResultSet rs = c.doQuery(query);
			while (rs.next()) {
				results.add(rs.getString(1)); // dname
				results.add(rs.getString(2)); // location
				results.add(rs.getString(3)); // description
				results.add(rs.getString(4)); // year
				results.add(rs.getString(5)); // ncases
			}
			rs.close();
			c.release();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return results;
	}

	/*
	 Function: searchFileMetadata 
	 	Searches metadata tables for dataset/file names
	 	and descriptions that contain a specific term.  Calls a Page 
	 	object method to display information in a template
	  
	 Parameters: 
	 	request - HttpServletRequest object
	 	out - HTML PrintWriter object
	 	s - Current HTTPSession object
	 */
	private void searchFileMetadata(HttpServletRequest request, PrintWriter out, HttpSession s) {
		final String SPLITTER = "\\s+|,\\s*"; // spaces and commas
		String username = s.getAttribute("username").toString();
		String accessLevel = (String) s.getAttribute("accessLevel");
		String term = "";
		term = request.getParameter("term").replaceAll("'", "''").trim();
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
			// Add sounds like

		if (!year.equalsIgnoreCase(""))
			// year specified
			query = "select dname, description, tags, lastupdate, year, ncases from datasets "
					+ "where (description " + like + " \'%" + term + "%\' OR dname " + like + " \'%" + term
					+ "%\' OR tags " + like + " \'%" + term + "%\') " + "AND year=\'" + year	+ "\'  order by dname";
		else
			// no year
			query = "select dname, description, tags, lastupdate, year, ncases from datasets "
					+ "where (description " + like + " \'%" + term + "%\' OR dname " + like + " \'%" + term 
					+ "%\' OR tags " + like + " \'%" + term + "%\') " + "order by dname";

		log.fine(HostInfo.tell() + ": " + username + ": " + query); // Log whatever query selected to localhost.date
																		// in Tomcat Logs folder
		try {
			// Submit the query and format the results!!
			ConnectDB c = new ConnectDB();
			c.capture();
			ResultSet rs = c.doQuery(query);
			int rowcount = 0;
			// Query done and results assigned to rs now output form for adding to Trolley
			// How many fields in the ResultSet? There are 6 in all above SQL
			Page p = new Page("SearchFiles-searchResults");
			p.setnumCols(6); // This is the number of fields in each row of the results
			ArrayList<String> results = new ArrayList<String>();
			while (rs.next()) {
				rowcount = rowcount + 1;
				// Move the first row into the results
				results.add(rs.getString(1)); // The dataset/file name
				results.add(rs.getString(2)); // The description of the dataset
				results.add(rs.getString(3)); // The tags
				results.add(rs.getString(4)); // Date of last updated
				results.add(rs.getString(5)); // The year of data collection
				results.add(rs.getString(6)); // The number of cases in the dataset
			}
			// Format the results with a template
			p.setResults(results);
			p.setnumResults(rowcount);
			p.UserPage(out, "NSHD File Dataset Search Results", s); // Populate template and print to output
			rs.close();
			c.release();
		} catch (Exception e) {
			log.severe(HostInfo.tell()+" SearchFiles: Error executing query: "+query);
			e.printStackTrace();
		}
	}

}
