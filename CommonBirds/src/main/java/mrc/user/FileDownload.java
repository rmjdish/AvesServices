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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.sql.*;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import mrc.util.Page;
import mrc.db.ConnectDB;
import mrc.util.HostInfo;

import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;


/* Class:
 * 	Allows users to Download data  as zipped mixture of CSV and syntax file formats
 * 
 */
public class FileDownload extends HttpServlet 
{
    /**
	 * 
	 */
	private static final String filext = ".zip";
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger("mrc.user");
	private static final String WINFSEP = "\\";
	private static final String URLSEP = "/";
	private static final String FILEXT = ".zip";
	private static final String HOSTDOMAIN = ".ad.ucl.ac.uk";

	/** 
    * Handles the HTTP <code>GET</code> method.
    * @param request servlet request
    * @param response servlet response
    */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException 
    {
        PrintWriter out = response.getWriter();        
        HttpSession s = request.getSession();
        listAvailableDatasets(s, out);
    } 


    /** 
    * Returns a short description of the servlet.
    */
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>


	private String availableQuery(String username) {
		String query = null;
		if (HostInfo.tell().indexOf("Jay") < 0) {  // This is not Jay
			query = "select basketid, status, description, username, createDate " + "from rook.basketlist "
					+ "where username=\'" + username + "\' " + " UNION "
					+ "select basketID, status, description, username, createDate " + "from rook.trolleylist "
					+ "where username=\'" + username + "\' ";
		} else {
			query = "select basketid, status, description, username, createDate from rook.basketlist";
		}
		return query;
	}    
	
    /** 
    * Lists all data sets available for FileDownload.
    * @param out HTML PrintWriter 
    * @param p HTML SWIFT page
    * @param s HTTPSession
    */
    private void listAvailableDatasets(HttpSession s, PrintWriter out) 
    {        
		String accessLevel = s.getAttribute("accessLevel").toString();
        Page p=new Page("FileDownload-listAvailableDatasets");
        String username    = s.getAttribute("username").toString();

        p.setnumCols(6); // This is the number of fields in each row of the results
        ArrayList<String> results = new ArrayList<String>();
        int rowcount = 0;
        boolean available = false;
        String query = availableQuery(username);
        String host = HostInfo.chezmoi();
        if (!host.toLowerCase().contains(HOSTDOMAIN)) {
        	host += HOSTDOMAIN;
        }
        log.fine(HostInfo.tell()+" FileDownload for "+username+" using query "+query);
        try {
            ConnectDB c = new ConnectDB();
            c.capture();
            ResultSet rs = c.doQuery(query);
            ResourceBundle bdl = new PropertyResourceBundle(new FileInputStream(dnget() + "/system.properties"));
            String dir = bdl.getString("installRoot"); // deployment name inside Tomcat webapps folder
            String urlUpload = "http://"+host+URLSEP+bdl.getString("dsgUpload");  // this is the URL to the file on the local server
            String dirUpload = System.getProperty("catalina.base")+ WINFSEP+bdl.getString("dsgUploadDir");
            /*
             * Key
             * 1 basketid
             * 2 status
             * 3 description
             * 4 username
             * 5 createDate
             * 
             */
            while (rs.next()) 
            {
            	rowcount += 1;
            	available = false;
            	String basketID   = rs.getString(1);  // basketid
				/*
				 * About: Nasty situation where the status value of the column may be null in
				 * the database apart from the values tested below The solution is to call
				 * getString on the column and then use rs.wasNull() to test for the presence of
				 * SQL NULL values. Otherwise this will throw null pointer exception
				 */
				String status      = rs.getString(2);
				String description = rs.getString(3);
				String uname	   = rs.getString(4);
				String createdate  = rs.getString(5);
				File ziphome = new File(dirUpload);
				FileFilter zipFilter = new WildcardFileFilter(basketID+"*.zip");
				File[] zipfiles = ziphome.listFiles(zipFilter);
				for (File thiszip : zipfiles) {
					if (thiszip.getName().contains(basketID) || accessLevel.equalsIgnoreCase("DSG") ) { // show if it's one of my files
						results.add(basketID);
						results.add("Dataset Available");
						results.add(description);
						results.add(uname);
						results.add(createdate);
						results.add(urlUpload+URLSEP+thiszip.getName());					
					}
				}
            }
            rs.close();
            c.release();
            p.setResults(results);
        	p.setnumResults(rowcount);
            p.UserPage(out, "Download Datasets", s);  // Populate template and print to output

        } catch (Exception e) {
        	log.severe(HostInfo.tell()+" FileDownload: Error finding files.");
            e.printStackTrace();
        }
    }
  /** 
    * Gets local directory name for properties file.
   */
   public String dnget()
   {
      java.net.URL u = this.getClass().getResource(cnget());  
      String dn = u.getPath().replaceAll("%20", " ");  
      return dn.substring(0,dn.lastIndexOf("/"));
   }

/** 
* Gets local class name for properties file.
*/
   public String cnget()
   {
      String c = this.getClass().getName();
      c = c.substring(c.lastIndexOf(".") + 1,c.length());
      return c+=".class";
   }

}
