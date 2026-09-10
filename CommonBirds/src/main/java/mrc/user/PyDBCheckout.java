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

import jakarta.activation.DataHandler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import mrc.db.ConnectDB;
import mrc.smtp.MailMessage;
import mrc.util.*;

import org.apache.commons.codec.binary.StringUtils;
import ucl.service.zip.ZIPService;
import ucl.service.zip.ZIPServiceClient;

/*
 * Class: PyDBCheckout
 * 	Uses the <ZIPService> web service to build user baskets. 
 * 	Requires NSHD data in PostgreSQL tables on the remote web service host
 */
public class PyDBCheckout extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger("mrc.user");
	private static final String PATHSEP = "/";
	private static final String propfile = "system.properties";
	private static final String FILEXT = ".zip";
	private static final String DSGACCESS = "DSG";
	private static final String WINFSEP = "\\";
	private static final String URLSEP = "/";
	private static final String BUILDBASKET = "build";
	private static final String SCRAMBLE = "scramble";
	private static final String PROTO = "http://";

	/*
	 * Function: doGet Within Java Servlet framework (Tomcat) responds to HTML GET
	 * requests
	 * 
	 * Parameters: request - HttpServletRequest object carrying attributes of the
	 * HTML GET response - HttpServletResponse object carrying attributes of the
	 * HTML output channel
	 * 
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		HttpSession s = request.getSession();
		String command = request.getParameter("command");
		if (command == null) {
			log.info(HostInfo.tell() + " GET called with NULL command");
			displayBaskets(out, s);
		} else if (command.equalsIgnoreCase("build")) {
			String basket = request.getParameter("basketID");
			log.info(HostInfo.tell() + " building " + basket);
			buildBasket(request, response, out, s, basket);
		}
	}

	/*
	 * Function: displayQuery Returns a basic SQL query string that will list a
	 * given users previously created baskets.
	 * 
	 * Parameter: username - String containing the Swift user whose baskets are to
	 * be displayed
	 */
	private String displayQuery(String username, String accessLevel) {
		String query = null;
		if (StringUtils.equals(accessLevel.toUpperCase(), DSGACCESS)) {
			query = "select basketID, description from basketdetails order by createDate DESC"; // You see them all
		} else {
			query = "select basketID, description from basketdetails where username=\'" + username
					+ "\' order by createDate DESC";
		}
		return query;
	}

	/*
	 * Procedure: displayBaskets Display a list of previously created shopping
	 * baskets as candidates for turning into datasets. Uses a Pebble form to
	 * display the list and offer the user the chance to select one for turning into
	 * a dataset.
	 * 
	 * Parameters: out - PrintWriter object to be used for displaying results s -
	 * HttpSession object which carries user session attributes
	 */
	private void displayBaskets(PrintWriter out, HttpSession s) {
		String accessLevel = s.getAttribute("accessLevel").toString();
		Page p = new Page("PyDBCheckout-displayBaskets");
		p.setnumCols(2); // This is the number of fields in each row of the results
		ArrayList<String> results = new ArrayList<String>();
		int rowcount = 0;
		String username = (String) s.getAttribute("username");
		String query = displayQuery(username, accessLevel);
		try {

			log.fine(HostInfo.tell() + " PyDBCheckout: " + username + ":" + query);
			ConnectDB c = new ConnectDB();
			ResultSet rs = c.doQuery(query);
			while (rs.next()) {
				rowcount += 1;
				results.add(rs.getString(1)); // basketID
				results.add(rs.getString(2)); // Description

			}
			rs.close();
			p.setResults(results);
			p.setnumResults(rowcount);
			p.UserPage(out, "Build Dataset From Baskets", s); // Populate template and print to output
		} catch (Exception e) {
			log.severe(HostInfo.tell() + " PyDBCheckout: displayBaskets: Error processing " + query);
			e.printStackTrace();
		}
	}

	/*
	 * Function: buildBasket Deletes old basket status in *uploaddownload* table,
	 * inserts new status of WAITING and calls ZIPService to build the basket. It
	 * also sends emails to the user confirming the stage of the process and finally
	 * updates the status of the basket in *uploaddownload* to READY
	 * 
	 * Variables: s - HTTPSession object out - HTML PrintWriter object basketID -
	 * Basket ID String
	 * 
	 * 
	 * See Also: <sendEmailToUser>, <ZIPService>
	 */
	public void buildBasket(HttpServletRequest request, 
							HttpServletResponse response, 
							PrintWriter out, 
							HttpSession s,
							String basketID) 
			throws ServletException, IOException {

		String username = s.getAttribute("username").toString();
		String accessLevel = s.getAttribute("accessLevel").toString();
		String fileref = null;
		String absfileref = null;
		Page p = new Page("PyDBCheckout-buildBasket");
		p.shareForm(out, "Building Dataset", s, basketID);
		String prequery = "delete from rook.uploaddownload where basketID=\'" + basketID + "\'";
		String query = "insert into rook.uploaddownload (basketID, status, createDate) values (\'" + basketID
				+ "\', \'A_WAITING\', now())";
		String dsgMyDBase = "";
		String dirUpload = "";
		String urlUpload = "";
		log.fine(HostInfo.tell() + "PyDBCheckout: about to update project status using "+ propfile);
		try {
			Properties props = new Properties(Util.dnget(this) + PATHSEP + propfile);
			dsgMyDBase = props.keySearch("dsgMyDBase");
			log.fine(HostInfo.tell() + "PyDBCheckout: dsgMyDBase: "+dsgMyDBase);
			dirUpload = System.getProperty("catalina.base") + WINFSEP + props.keySearch("dsgUploadDir"); 
			// above is the destination on the local server
			log.fine(HostInfo.tell() + "PyDBCheckout: dsgUpload: "+dirUpload);
			urlUpload = PROTO + props.keySearch("installServer") + "/" + props.keySearch("dsgUpload");
			// above is the URL to the file on the local server
			log.fine(HostInfo.tell() + "PyDBCheckout: urlUpload: "+urlUpload);
			log.info(HostInfo.tell() + " PyDBCheckout: Absolute path (using catalina.base) to resultant files: "
					+ dirUpload);
			prequery = "delete from " + dsgMyDBase + ".uploaddownload where basketID=\'" + basketID + "\'";
			query = "insert into " + dsgMyDBase + ".uploaddownload (basketID, status, createDate) values (\'" + basketID
					+ "\', \'A_WAITING\', now())";
			updateProjectStatus(prequery, query, username);
		} catch (Exception se) {
			log.severe(HostInfo.tell() + " PyDBCheckout: buildBasket: Error updating project status with " + query);
			se.printStackTrace();
		}

		// Notify User & DSG Note this is blowing up due to interdependencies with jakarta.activation drop for now
		// MailMessage m = new MailMessage();
		// m.sendEmailToUser(username, "A job to build basket " + basketID
		//		+ " has been created. It will be executed on a remote server and the resulting zip archive file will be available by selecting the appropriate link from the Download menu. Please wait for a few minutes before checking if it is available. You will also be notified by email when the file becomes available");

		try {
			// Call Web Service Operation
			// and create a file local to Kiwi/Kiwidev to store the results of the Web
			// Service call
			log.info(HostInfo.tell() + " PyDBCheckout: buildBasket: calling webservice");
			ZIPServiceClient client = new ZIPServiceClient();
			ZIPService port = client.getZIPServicePort();
			DataHandler zipfile = null;
			/*
			 * So far we've got a handle on the service with which we can throw the job and
			 * catch the resulting zip file
			 */
			try {
				port.throwjob(basketID, username); // Instruction to the web service to build the basket and create the
													// ZIP file
				zipfile = port.catchjob(basketID, BUILDBASKET);

			} catch (Exception e) {
				log.severe(HostInfo.tell() + " PyDBCheckout: downloadBasket: Error invoking web service ZIPService");
				e.printStackTrace(out);
			}
			log.info(HostInfo.tell() + " PyDBCheckout: buildBasket: webservice result returned.");
			fileref = urlUpload + URLSEP + basketID + FILEXT;
			absfileref = dirUpload + WINFSEP + basketID + FILEXT;
			log.info(HostInfo.tell() + " PyDBCheckout: buildBasket: trying to upload result to: " + absfileref);
			FileOutputStream outUpload = new FileOutputStream(absfileref); // We're getting back zip files
			if (zipfile != null) {
				log.info(HostInfo.tell() + " PyDBCheckout: ZIPService catchjob returned zipfile refernece");
				zipfile.writeTo(outUpload);
			} else {
				log.warning(HostInfo.tell() + " PyDBCheckout: call to ZIPService catchjob returned null file.");
			}
			outUpload.close();
		} catch (Exception ex) {
			ex.printStackTrace(out);
		}
		// Write to /dsg/upload/$basketid.
		// If successful, update Status to C
		query = "update " + dsgMyDBase + ".uploaddownload set status=\'C_READY\', createDate=now(), fileref=\'"
				+ fileref + "\' where basketID=\'" + basketID + "\'";
		log.fine(HostInfo.tell() + " PyDBCheckout: buildBasket: SQL: " + username + ":" + query);
		try {
			updateProjectStatus(query, username);
		} catch (Exception se) {
			log.warning(HostInfo.tell() + " PyDBCheckout: Error in buildBasket during call to updateProjectStatus: "
					+ query);
			se.printStackTrace();
		}
		// Email user notification that file is ready
		MailMessage m1 = new MailMessage();
		m1.sendEmailToUser(username, "The data file for basket " + basketID
				+ " has now been prepared by the SST at LHA. You may now download the data file. Please contact the LHA if you require any further information.)");
	}


	/*
	 * About: Note there are two overloaded forms of *updateProjectStatus* one with
	 * two queries and another with only one. The two query form is intended to
	 * execute a delete prior to an insert
	 */

	/*
	 * Function: updateProjectStauts Executes the SQL query supplied as parameter
	 * for project status
	 * 
	 * Parameters: query - SQL query which updates status to READY username - Swift
	 * Username of basket owner
	 */
	public void updateProjectStatus(String query, String username) {
		try {
			ConnectDB c = new ConnectDB();
			c.capture();
			int r = 0;
			log.fine(HostInfo.tell() + "PyDBCheckout: updateProjectStatus using: "+query);
			r = c.doUpdate(query);
			if (r == 0)
				log.warning(HostInfo.tell() + " PyDBCheckout: Error in updateProjectStatus: " + username + ":query: "
						+ query);
			else
				c.release();
		} catch (Exception e) {
			log.severe(HostInfo.tell() + "PyDBCheckout: updateProjectStatus: SQL: " + username + ":" + query);
			e.printStackTrace();
		}
	}

	/*
	 * Function: updateProjectStatus Executes the SQL queries supplied as parameters
	 * for project status
	 * 
	 * Parameters: prequery - SQL query which deletes current status query - SQL
	 * query which updates status to WAITING username - Swift Username of basket
	 * owner
	 * 
	 * See Also: <buildBasket>
	 */
	public void updateProjectStatus(String prequery, String query, String username) {
		try {
			ConnectDB c = new ConnectDB();
			c.capture();
			int r = 0;
			r = c.doUpdate(prequery);
			r = c.doUpdate(query);
			if (r == 0)
				log.warning(HostInfo.tell() + " PyDBCheckout: Error in updateProjectStatus:" + username + " : query: "
						+ query);
			else
				c.release();
		} catch (Exception e) {
			log.severe(HostInfo.tell() + "PyDBCheckout: updateProjectStatus: Error executing updates SQL: " + username
					+ ":" + prequery + " and " + query);
			e.printStackTrace();
		}
	}

	/*
	 * Function: getContents Retrieve contents of file <code>aFile</code> and return
	 * as a String
	 * 
	 * Parameter: aFile - Java File object from which contents are to be retrieved
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
			log.severe(HostInfo.tell() + "PyDBCheckout: getContents: file error.");
			ex.printStackTrace();
		}
		return contents.toString();
	}

}
