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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import mrc.db.ConnectDB;
import mrc.smtp.MailMessage;
import mrc.util.*;
import mrc.smtp.MailMessage;
import ucl.service.zip.ZIPService;
import ucl.service.zip.ZIPServiceClient;

/*
 * Class: Trolley
 * 	Manages the Trolley objects that are the file based equivalent of Baskets
 * 	Specifically does the same as PyDBCheckout to bring about the download of 
 * 	trollies
 */
public class Trolley extends HttpServlet {
	private static final String TROLMARKER = "TT";
	private static final String FILEXT = ".zip";
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger("mrc.user");
	private static final int SUFLEN = 6;
	private static final int OKMSG = 20;
	private static final int EXCEEDEDMAX = 22;
	private static final String BUILDBASKET = "build";
	private static final String SCRAMBLE = "scramble";
	private static SecModel smod = new SecModel();
	private static MessageRelay msg2u = new MessageRelay();

	/*
	 * Procedure: doGet Receives initial request from menu choice and sets up
	 * 
	 * Parameters: request - HttpServletRequest object carrying attributes of the
	 * HTML GET response - HttpServletResponse object carrying attributes of the
	 * HTML output channel
	 * 
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession s = request.getSession();
		PrintWriter out = response.getWriter();
		String username = s.getAttribute("username").toString();
		String accessLevel = s.getAttribute("accessLevel").toString();
		ArrayList<String> sack = new ArrayList<String>();

	    if (request.getParameter("command")!=null)
	      { 
	    	if (request.getParameter("command").equalsIgnoreCase("build")) 
	    	{
	    		String trolleyID   = request.getParameter("trolleyID");
	    		String description = request.getParameter("description");
	    		sack = (ArrayList<String>) s.getAttribute("trolley");
	    		int numinsack = 0;
	    		for (String item : sack) {
	    			try {
	    				numinsack++;
	    				putTrolley(username, trolleyID, item);  // Puts entries into trollies table
	    			} catch (Exception e) {
	    				log.severe(HostInfo.tell() + " Error adding item to trolley: " + item);
	    				e.printStackTrace();
	    			}
	    		}
	    		try {
		    		createInternalTrolley(username,trolleyID,description);	    			
	    		} catch (Exception e) {
	    			log.warning(HostInfo.tell()+" Trolley: Error updating trolleycontents.");
	    			e.printStackTrace();
	    		}
	    		log.info(HostInfo.tell() + " Trolley: added " + numinsack + " file entries in the trollies table for "
	    				+ trolleyID);
	    		// Set Trolley up for download
	    		buildTrolley(s, out, trolleyID);
	    		// Lastly pick out all uses trollies and display status
	    		
	      } 
	    	else if (request.getParameter("command").equalsIgnoreCase("save")) {
	    		Page p = new Page("Trolley-saveTrolley");
	    		String trolleyID = request.getParameter("trolleyID");
	    		p.shareForm(out, "Enter Basket Details for "+trolleyID, s, trolleyID);        
	    	}
	    }


	}
	
    /*
     * Function: createInternalTrolley
    * 	Insert internal project record, used to describe Basket when shared - not a formal data access proposal! 
    * 
    * Parameters:
    * 	username - Swift/Condor Username
    * 	trolleyID - unique Basket ID
    * 	description - Basket description
    */
    private String createInternalTrolley(String username, String trolleyID, String description) 
    {
        String query="";
        try 
        {
            query = "insert into trolleycontents values (\'" + trolleyID + "\', \'" + description + "\', \'" + username + "\', now())";
            log.info(HostInfo.tell()+" createInternalTrolley: "+username+": "+query);
            ConnectDB c = new ConnectDB();
            c.capture();
            int r = c.doUpdate(query);
            c.release();
            if (r==0)            
                log.warning(HostInfo.tell()+ " Trolley: Error in createInternalTrolley: "+username);            
        } 
        catch (Exception e) 
        {
        	log.severe(HostInfo.tell()+" createInternalTrolley: failed to put entry into trolleycontents for trolley: "+trolleyID);
            e.printStackTrace();
        }
        return query;
    }



	private ArrayList<String> listDatasets(String username) {
		ArrayList<String> results = new ArrayList<String>();
		String query = "SELECT dname, location, description, year, ncases from datasets ORDER BY dname asc";
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
	 * Function: doPost 
	 * 	Processes Forms returned from SearchFiles where user has selected a number of datasets	
	 * 	purpose is to select these (up to a max number) and add them to a session object
	 * 	and check the authority of the user to add each one selected.  Only put in trolley
	 * 	if username has authority to do so.  At end shows what's in the trolley
	 * 
	 * Parameters: 
	 * 	request - HttpServletRequest object carrying attributes of the  HTML GET 
	 * 	response - HttpServletResponse object carrying attributes of the HTML output channel
	 * 
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		HttpSession s = request.getSession();
		String username = s.getAttribute("username").toString();
		Map<String, Object> context = new HashMap<>();
		ArrayList<String> datasets = listDatasets(username);
		context.put("datasetNames", datasets);
		int seclevel = (Integer) s.getAttribute("seclevel");
		ResourceBundle bdl = new PropertyResourceBundle(new FileInputStream(dnget() + "/system.properties"));
		Integer maxallowed = Integer.parseInt(bdl.getString("dsgTrolleyLimit"));
		// This is an attribute of the user, recorded in the session object
		if (smod == null) {
			log.severe(HostInfo.tell() + " SearchFiles: Error with security model : checking will be faulty!");
		}
		if (msg2u == null) {
			log.severe(HostInfo.tell() + " SearchFiles: Error with message relay : output unreliable!");
		} else {
			msg2u.msgFlush();
		}
		ArrayList<String> sack = new ArrayList<String>();
		Enumeration<String> elist = request.getParameterNames();
		int numinsack = 0;
		while (elist.hasMoreElements()) {
			String dsetname = elist.nextElement().toString();
			if (numinsack > maxallowed) {
				msg2u.display(dsetname, EXCEEDEDMAX);
			} else {
				if (smod.checkAuth(seclevel, dsetname)) {
					// Have authority to download this dataset, add and test what kind
					// of print to do
					numinsack++;
					sack.add(dsetname);
					if (smod.isOpen(dsetname) && smod.hasMsg(dsetname)) {
						// It's open but has special message
						msg2u.display(dsetname, smod.getMsgId(dsetname));
						// Put special message for this variable in the buffer
					} else {
						// Is not open (but authorised) OR has no special message
						msg2u.quiet(dsetname, OKMSG);
					}
				} else {
					// Is not Authorised
					msg2u.display(dsetname, smod.getMsgId(dsetname));
				}
			}
		}
		// Save sack as a trolley element of session
		s.setAttribute("trolley", sack);
		// Save record of trolley
		String trolleyID = username + TROLMARKER + RandomString.randomstring(SUFLEN);
		s.setAttribute("trolleyID", trolleyID);
		Page p = new Page("Trolley-messageDisplay");
		p.setnumCols(2);
		p.setResults(msg2u.msgFlush());
        p.UserPage(out,trolleyID,s);
	}

	/*
	 * Function: putTrolley Insert internal project record, used to describe Basket
	 * when shared - not a formal data access proposal!
	 * 
	 * Parameters: username - Swift/Condor Username basketID - unique Trolley ID
	 * description - Basket description dname - Name of
	 */
	private void putTrolley(String username, String trolleyID, String dname)  {
		String query = "";
		try {
			query = "insert into trollies values (\'" + trolleyID + "\', \'" + username + "\', \'" + dname
					+ "\', now())";
			log.info(HostInfo.tell() + " putTrolley: " + username + ": " + query);
			ConnectDB c = new ConnectDB();
			c.capture();
			int r = c.doUpdate(query);
			c.release();
			if (r == 0)
				log.warning(HostInfo.tell()+ " Trolley: Error in putTrolley: " + username);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void buildTrolley(HttpSession s, PrintWriter out, String trolleyID) throws ServletException, IOException {

		String username = s.getAttribute("username").toString();
		String accessLevel = s.getAttribute("accessLevel").toString();
		Page p = new Page("PyDBCheckout-buildBasket");
		p.shareForm(out, "Building Dataset", s, trolleyID);
		ResourceBundle bdl = new PropertyResourceBundle(new FileInputStream(dnget() + "/system.properties"));
		String prequery = "delete from uploaddownload where basketID=\'" + trolleyID + "\'";
		String query = "insert into uploaddownload (basketID, status, createDate) values(\'" + trolleyID + "\', \'A_WAITING\', now())";
		try {
			PyDBCheckout handle = new PyDBCheckout();
			handle.updateProjectStatus(prequery, query, username);
		} catch (Exception se) {
			log.severe(HostInfo.tell() + " Trolley: buildTrolley: Error updating project status with " + query);
			se.printStackTrace();
		}
		// Notify User & DSG
		MailMessage m = new MailMessage();
		m.sendEmailToUser(username, "A job to create the dataset downloads "
				+ " has been created. It will be executed on a remote server and the resulting zip archive file will be available by selecting the appropriate link from the Download menu. Please wait for a few minutes before checking if it is available. You will also be notified by email when the file becomes available");

		try {
			// Call Web Service Operation
			// and create a file local to Kiwi/Kiwidev to store the results of the Web
			// Service call
			log.info(HostInfo.tell() + "Trolley: buildTrolley: calling webservice");
			ZIPServiceClient client = new ZIPServiceClient();
			ZIPService port = client.getZIPServicePort();
			DataHandler zipfile = null;
			/*
			 * So far we've got a handle on the service with which we can throw the job and
			 * catch the resulting zip file
			 */
			try {
				port.throwjob(trolleyID, username); // Instruction to the web service to build the basket and create the
													// ZIP file
				zipfile = port.catchjob(trolleyID,BUILDBASKET);

			} catch (Exception e) {
				log.severe(HostInfo.tell() + "Trolley: downloadTrolley: Error invoking web service ZIPService");
				e.printStackTrace(out);
			}
			log.info(HostInfo.tell() + "Trolley: buildTrolley: webservice result returned.");
			ResourceBundle bdlUpload = new PropertyResourceBundle(new FileInputStream(dnget() + "/system.properties"));
			String dirUpload = bdlUpload.getString("dsgUploadDir"); // this is the destination on the local server
			log.info(HostInfo.tell() + "Trolley: buildTrolley: result uploaded to: " + dirUpload);
			// We're getting back zip files
			FileOutputStream outUpload = new FileOutputStream(dirUpload + File.separator + trolleyID + FILEXT); 																									
			if (zipfile != null) {
				log.info(HostInfo.tell() + " Trolley: ZIPService catchjob returned zipfile refernece");
				zipfile.writeTo(outUpload);
			} else {
				log.warning(HostInfo.tell() + " Trolley: call to ZIPService catchjob returned null file.");
			}
			outUpload.close();
		} catch (Exception ex) {
			ex.printStackTrace(out);
		}
		// Write to /dsg/upload/$basketid.
		// If successful, update Status to C
		try {
			query = "update uploaddownload set status=\'C_READY\', createDate=now() where basketID=\'" + trolleyID + "\'";
			PyDBCheckout handle = new PyDBCheckout();
			handle.updateProjectStatus(query, username);
		} catch (Exception se) {
			log.severe(HostInfo.tell() + " Trolley: buildTrolley: Error updating project status with " + query);
			se.printStackTrace();
		}
		// Email user notification that file is ready
		MailMessage m1 = new MailMessage();
		m1.sendEmailToUser(username, "Your requested dataset downloads "
				+ " have now been prepared by the SST at LHA. You may now download the archive file. Please contact the LHA if you require any further information.)");
	}

	/*
	 * Function: dnget Gets local directory name for properties file.
	 */
	public String dnget() {
		java.net.URL u = this.getClass().getResource(cnget());
		String dn = u.getPath().replaceAll("%20", " ");
		return dn.substring(0, dn.lastIndexOf("/"));
	}

	/*
	 * Function: cnget Gets local class name for properties file.
	 */
	public String cnget() {
		String c = this.getClass().getName();
		c = c.substring(c.lastIndexOf(".") + 1, c.length());
		return c += ".class";
	}

}
