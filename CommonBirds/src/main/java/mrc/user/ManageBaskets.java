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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.activation.DataHandler;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import mrc.db.ConnectDB;
import mrc.smtp.MailMessage;
import mrc.util.*;
import ucl.service.zip.ZIPService;
import ucl.service.zip.ZIPServiceClient;

/**
 * Not clear what to do with this Class Could be used by people with top level
 * to build any basket for any user.
 * 
 * Discuss??
 * 
 * @author LHA SST
 */
public class ManageBaskets extends HttpServlet {

	/**
	 * This one is still to be converted to use
	 */
	private static final long serialVersionUID = -3267104841147242422L;
	private static final Logger log = Logger.getLogger("mrc.user");
	private static final String filext = ".zip";
	private static final String basketid = "basketID";
	private static final String BUILDBASKET = "build";
	private static final String WINFSEP = "\\";
	private static final String URLSEP = "/";
	private static final String FILEXT = ".zip";

	private String displayQuery(String username) {
		String query = "select basketID, description from basketdetails order by createDate DESC";
		return query;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Set the response MailMessage's MIME type for transferring SPSS .sav files
		// response.setContentType("application/octet-stream; charset=binary");
		PrintWriter out = response.getWriter();
		HttpSession s = request.getSession();
		String command = request.getParameter("command");
		if (command == null) {
			log.info(HostInfo.tell() + " doGet: called with null command ");
			listAllBaskets(request, response);
		} else if (command.equalsIgnoreCase("build")) {
			String basket = request.getParameter(basketid);
			log.info(HostInfo.tell() + " PyDBCheckout: building " + basket);
			buildBasket(request, response, out, s, basket);
		} else if (command.equalsIgnoreCase("download")) {
			listAvailableDatasets(s, out);
		} else if (command.equalsIgnoreCase("modify")) {

		}
	}

	/*
	 * There are two signatures for this helper method: first with 2 queries to be
	 * run one after the other second with 1 query to run
	 */

	private void updateProjectStatus(String prequery, String query, PrintWriter out, String username)
			 {
		int r = 0;
		try {
			ConnectDB c = new ConnectDB();
			c.capture();
			r = c.doUpdate(prequery);
			r = c.doUpdate(query);
			c.release();
		} catch (Exception se) {
			log.severe(
					HostInfo.tell() + " updateProjectStatus with prequery error executing doUpdate: " + prequery);
			se.printStackTrace();
		}
		if (r == 0)
			log.warning(HostInfo.tell() + " ManageBaskets: updateProjectStatus Error: " + username
					+ " failed for query: " + query);
		else

		;
	}

	private void updateProjectStatus(String query, PrintWriter out, String username)  {
		try {
			ConnectDB c = new ConnectDB();
			c.capture();
			int r = 0;
			r = c.doUpdate(query);
			if (r == 0)
				log.warning(HostInfo.tell() + " ManageBaskets:" + username
						+ ":updateProjectStatus() failed for query: " + query);
			else
				c.release();
			;
		} catch (Exception e) {
			log.severe(HostInfo.tell() + " updateProjectStatus single query error executing doUpdate: " + query);
			e.printStackTrace();
		}
	}

	private void buildBasket(HttpServletRequest request, HttpServletResponse response, PrintWriter out, HttpSession s,
			String basketID) throws ServletException, IOException {

		String username = s.getAttribute("username").toString();
		String accessLevel = s.getAttribute("accessLevel").toString();
		Page p = new Page("PyDBCheckout-buildBasket");
		p.shareForm(out, "Build a basket", s, basketID);
		ResourceBundle bdl = new PropertyResourceBundle(new FileInputStream(Util.dnget(this) + "/system.properties"));
		String prequery = "delete from uploaddownload where basketID=\'" + basketID + "\'";
		String query = "insert into uploaddownload (basketID,status,createDate) values (\'" + basketID + "\', \'A_WAITING\', now())";
		log.info(HostInfo.tell() + " buildBasket: " + username + " : " + query);
		try {
			updateProjectStatus(prequery, query, out, username);
		} catch (Exception se) {
			log.severe(HostInfo.tell() + " buildBasket: Error updating project status with " + query);
			se.printStackTrace();
		}

		// Notify User & DSG
		MailMessage m = new MailMessage();
		m.sendEmailToUser(username, "A job to build basket " + basketID
				+ " has been created. It will be executed on a remote server and the resulting zip archive file will be available by selecting the appropriate link from the Download menu. Please wait for a few minutes before checking if it is available. You will also be notified by email when the file becomes available");
		String dir = bdl.getString("dsgDownloadDir"); // Get the value of the dsgDownloadDir key in properties file

		try { // Call Web Service Operation
				// and create a file local to Kiwi/Kiwidev to store the results of the Web
				// Service call
			log.info(HostInfo.tell()+ " buildBasket: calling webservice for basket "+basketID+" and user "+username);
			ZIPServiceClient client = new ZIPServiceClient();
			ZIPService port = client.getZIPServicePort();
			DataHandler zipfile = null;
			/*
			 * So far we've got a handle on the service with which we can throw the job and
			 * catch the resulting zip file
			 */
			try {
				port.throwjob(basketID, username); // Instruction to the web service to build the basket and create the ZIP file													
				zipfile = port.catchjob(basketID,BUILDBASKET);

			} catch (Exception e) {
				log.severe(HostInfo.tell() + " ManageBaskets: buildBasket: Error invoking web service ZIPService");
				e.printStackTrace(out);
			}
			log.fine(HostInfo.tell() + " ManageBaskets: buildBasket: webservice result returned.");
			ResourceBundle bdlUpload = new PropertyResourceBundle(new FileInputStream(Util.dnget(this) + "/system.properties"));
			String dirUpload = bdlUpload.getString("dsgUploadDir"); // this is the destination on the local server
			log.fine(HostInfo.tell() + " ManageBaskets: buildBasket: result uploaded to: " + dirUpload);

			FileOutputStream outUpload = new FileOutputStream(dirUpload + "\\" + basketID + filext); // We're getting
																										// back zip
																										// files
			if (zipfile != null) {
				log.fine(HostInfo.tell() + "  ManageBaskets: buildBasket: ZIPService catchjob returned zipfile refernece");
				zipfile.writeTo(outUpload);
			} else {
				log.warning(HostInfo.tell() + " ManageBaskets: buildBasket: call to ZIPService catchjob returned null file.");
			}
			outUpload.close();
		} catch (Exception ex) {
			ex.printStackTrace(out);
		}
		// Write to /dsg/upload/$basketid.
		// If successful, update Status to C
		query = "update uploaddownload set status=\'C_READY\', createDate=now() where basketID=\'" + basketID + "\'";
		log.fine(HostInfo.tell() + " ManageBaskets: buildBasket: SQL: " + username + ":" + query);
		try {
			updateProjectStatus(query, out, username);
		} catch (Exception se) {
			se.printStackTrace();
			log.severe(HostInfo.tell() + " ManageBaskets: buildBasket: Error in updateProjectStatus: "+ query);
		}
		// Email user notification that file is ready
		MailMessage m1 = new MailMessage();
		m1.sendEmailToUser(username, "The data file for basket " + basketID
				+ " has now been prepared by the SST at LHA. You may now download the data file. Please contact the LHA if you require any further information.)");
	}

	private String availableQuery(String username) {
		String query = null;
		if (HostInfo.tell().indexOf("Jay") < 0) {  // This is not Jay
			query =   "select basketid, status, description, username, createDate " + "from rook.basketlist "
					+ " UNION "
					+ "select basketID, status, description, username, createDate " + "from rook.trolleylist "; 
		} else {  // This is Jay
			query = "select basketid, status, description, username, createDate from jay.basketlist";
		}
		return query;
	}    

	private void listAvailableDatasets(HttpSession s, PrintWriter out) {
		String accessLevel = s.getAttribute("accessLevel").toString();
		Page p = new Page("FileDownload-listAvailableDatasets");
		String username = s.getAttribute("username").toString();
		p.setnumCols(6); // This is the number of fields in each row of the results
		ArrayList<String> results = new ArrayList<String>();
		int rowcount = 0;
		boolean available = false;
		String query = availableQuery(username);
		log.info(HostInfo.tell() + " listAvailableDatasets: " + username + ":" + query);
		try {
			ConnectDB c = new ConnectDB();
			c.capture();
			ResultSet rs = c.doQuery(query);
			ResourceBundle bdl = new PropertyResourceBundle(new FileInputStream(Util.dnget(this) + "/system.properties"));
			String dir = bdl.getString("installRoot");
			String urlUpload = "http://"+bdl.getString("installServer")+"/"+dir+"/"+bdl.getString("dsgScrambleDir"); // this is the URL to the file on the local server
			String dirUpload = System.getProperty("catalina.base")+ WINFSEP+bdl.getString("dsgUploadDir"); // this is the absolute file destination on the local server
			/*
			 * Key 1 basketid 2 status 3 description 4 username 5 createDate
			 * 
			 */
			while (rs.next()) {
				rowcount += 1;
				available = false;
				String basketID = rs.getString(1); // basketid
				/*
				 * About: Nasty situation where the status value of the column may be null in
				 * the database apart from the values tested below The solution is to call
				 * getString on the column and then use rs.wasNull() to test for the presence of
				 * SQL NULL values. Otherwise this will throw null pointer exception
				 */
				String status      = rs.getString(2); // Status maybe NULL
				String description = rs.getString(3); // Basket description given by creator
				String uname       = rs.getString(4); // User name of basket creator
				String createdate  = rs.getString(5); // Date basket basket built into dataset
				File ziphome = new File(dirUpload);
				FileFilter zipFilter = new WildcardFileFilter(basketID+"*.zip");
				File[] zipfiles = ziphome.listFiles(zipFilter); // generate a list of matching files (possibly empty)
				for (File thiszip : zipfiles) {
					results.add(basketID);
					results.add("Available");
					results.add(description);
					results.add(uname);
					results.add(createdate);
					results.add(urlUpload+URLSEP+thiszip.getName());
				}
			}
			rs.close();
			c.release();
			p.setResults(results);
			p.setnumResults(rowcount);
			p.UserPage(out, "Download Any Dataset", s); // Populate template and print to output

		} catch (SQLException se) {
			log.severe(HostInfo.tell() + " ManageBaskets: SQL Exception executing " + query);
			se.printStackTrace();
		} catch (Exception e) {
			log.severe(HostInfo.tell()
					+ " ManageBaskets: listAvailableDatasets: Failed to process information from listbaskets view");
			e.printStackTrace();
		}
	}

	/**
	 * Display contents of shopping basket.
	 * 
	 * @param out      HTML PrintWriter
	 * @param basketID Basket ID
	 */
	private void variableList(PrintWriter out, String basketID) {

	}

	/**
	 * Check if variable name exists.
	 * 
	 * @param name Variable name
	 */
	private boolean checkVariableName(String name) {
		return true;
	}

	/**
	 * Inserts one item into saved shopping basket.
	 * 
	 * @param out      HTML PrintWriter
	 * @param basketID Basket ID
	 * @param username SWIFT username
	 * @param name     Variable name
	 */
	private void insertVariable(PrintWriter out, String username, String basketID, String name)  {
	}

	/**
	 * Inserts one item into saved shopping basket.
	 * 
	 * @param out      HTML PrintWriter
	 * @param basketID Basket ID
	 * @param username SWIFT username
	 */
	private void addVariable(String username, PrintWriter out, String basketID) {
	}

	/**
	 * Inserts one item into saved shopping basket - error
	 * 
	 * @param out      HTML PrintWriter
	 * @param basketID Basket ID
	 * @param username SWIFT username
	 * @param name     Invalid variable name
	 */
	private void addVariable(PrintWriter out, String basketID, String name, String username) {
	}

	/**
	 * Deletes one item from saved shopping basket.
	 * 
	 * @param basketID Basket ID
	 * @param username SWIFT username
	 * @param name     Invalid variable name
	 */
	private void deleteItemFromBasket(String username, String basketID, String name) {
	}

	/**
	 * Display contents of shopping basket.
	 * 
	 * @param out      HTML PrintWriter
	 * @param basketID Basket ID
	 * @param e        Enumerated list of variables
	 * @param username SWIFT username
	 */
	private void displayBasket(PrintWriter out, Enumeration e, String basketID, String username) {
	}

	/**
	 * Iterate through list of items in shopping basket.
	 * 
	 * @param out      HTML PrintWriter
	 * @param e        Enumerated list of variables
	 * @param basketID Basket ID
	 */
	private void displayItems(Enumeration e, PrintWriter out, String basketID) {
		while (e.hasMoreElements()) {
			String item = e.nextElement().toString();

		}
	}

	/*
	 * Function: listAllBaskets Lists baskets ready for review
	 * 
	 * Parameters: request - servlet request response - servlet response
	 */
	private void listAllBaskets(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		Page p = new Page("ManageBaskets-StatusOfExternalBaskets");

		HttpSession s = request.getSession();
		String username = s.getAttribute("username").toString();
		p.setnumCols(5); // This is the number of fields in each row of the results
		ArrayList<String> results = new ArrayList<String>();
		int rowcount = 0;

		/*
		 * Notice that this method lists only external user baskets; internal users can
		 * build their own
		 */ String query = "select basketID, status, Description, username, createDate " + "from basketlist "
				+ "order by createDate DESC";
		log.info(HostInfo.tell() + " listAllBAskets: " + query);
		try {
			ConnectDB c = new ConnectDB();
			c.capture();
			ResultSet rs = c.doQuery(query);
			while (rs.next()) {
				rowcount += 1;
				results.add(rs.getString(1)); // basketid
				results.add(rs.getString(2)); // status
				results.add(rs.getString(3)); // description
				results.add(rs.getString(4)); // username
				results.add(rs.getString(5)); // createdate

			}
			rs.close();
			c.release();
			p.setResults(results);
			p.setnumResults(rowcount);
			p.UserPage(out, "Manage Baskets", s); // Populate template and print to output
			c = null;
		} catch (Exception e) {
			log.severe(HostInfo.tell() + " listAllBaskets Error executing query:" + query);
			e.printStackTrace();
		}
		/*
		 * out.println("        </tr>");
		 */
	}

	/**
	 * Build basket and send script to back end server
	 * 
	 * @param request  servlet request
	 * @param s        HTTPSession
	 * @param basketID Basket ID
	 * @param out      HTML PrintWriter
	 */
	private void buildBasket(HttpServletRequest request, HttpSession s, PrintWriter out, String basketID) {
	}

	/**
	 * Returns a short description of the servlet.
	 */
	public String getServletInfo() {
		return "Basket management functions";
	}

	/**
	 * Get username from basketID
	 * 
	 * @param basketID Basket ID
	 */
	private String getUsername(String basketID) {
		String username = "";

		String query = "select username from basketdetails where basketID=\"" + basketID + "\"";
		try {
			ConnectDB c = new ConnectDB();
			c.capture();
			ResultSet rs = c.doQuery(query);
			while (rs.next()) {
				username = rs.getString(1);
			}
			rs.close();
			c.release();
		} catch (Exception e) {
			log.severe(HostInfo.tell() + " getUsername Error executing query: " + query);
			e.printStackTrace();
		}
		return username;
	}

	/**
	 * Retrieve contents of file <code>aFile</code>
	 * 
	 * @param aFile file to read
	 */
	public String getContents(File aFile) {
		StringBuilder contents = new StringBuilder();
		try {
			BufferedReader input = new BufferedReader(new FileReader(aFile));
			try {
				String line = null;
				while ((line = input.readLine()) != null) {
					contents.append(line);
					contents.append(System.getProperty("line.separator"));
				}
			} finally {
				input.close();
			}
		} catch (IOException ex) {
			log.severe(HostInfo.tell() + " getContents Error reading from file.");
			ex.printStackTrace();
		}
		return contents.toString();
	}

	private void createDownloadFile(HttpServletRequest request, String basketID) {

	}

	/**
	 * Gathers OR'ed list of variables from shopping basket for project
	 * <code>project</code>.
	 * 
	 * @param e Variable list from shopping basket ArrayList
	 */
	private String getOrList(Enumeration<String> e) {
		String query = "";
		String name = e.nextElement().toString();
		query = query + "\'" + name + "\'";
		if (e.hasMoreElements())
			query = query + " or name=";
		while (e.hasMoreElements()) {
			name = e.nextElement().toString();
			query = query + "\'" + name + "\'";
			if (e.hasMoreElements())
				query = query + " or name=";
		}
		return query;
	}
}
