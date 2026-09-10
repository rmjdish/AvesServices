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
import java.nio.file.Path;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.Date;
import java.util.logging.Logger;

import org.simpleframework.xml.*;
import org.simpleframework.xml.core.*;
import org.apache.commons.io.FileUtils;

import mrc.db.ConnectDB;
import mrc.util.BasketXML;
import mrc.util.Page;
import mrc.util.HostInfo;
import mrc.util.Util;

/*
Class: Download
	Allows users to download a Basket in XML file format

*/
public class Download extends HttpServlet 
{
		private static final long serialVersionUID = 1L;
		private static final Logger log = Logger.getLogger("mrc.user");
		/*
		Method: doGet
	    	Handles the HTTP GET method.
	    
	    Parameters:
		 	request  - HttpServletRequest
		 	response - HttpServletResponse
	    */
	    @Override
		protected void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	    {
	        PrintWriter out = response.getWriter();        
	        HttpSession s = request.getSession();
	        if (s == null || s.getAttribute("username") == null) {
	        	request.getRequestDispatcher("/login").forward(request, response);
	        }        
			String command = request.getParameter("command");
			String basket = request.getParameter("basket");  // Remember this could be null
			if (basket == null) {
	        	log.info(HostInfo.tell() + "Download: GET called with command "+command+" and NULL Basket");				
	            listBaskets(out, s);
	        } else if (command.equalsIgnoreCase("toXML"))
	        {
	            String descrip=request.getParameter("description");
	            String username=request.getParameter("username");
	            log.info("Download: GET called with command toXML and Basket -> "+basket);
	            Page p = new Page("Download-XMLDownloadForm");
	            Map<String,Object> ctx = new HashMap<>();
	            ctx.put("basketId", basket);
	            ctx.put("description", descrip);
	            ctx.put("username", username);
	            p.extendPageContext(out, "Download XML Basket", (jakarta.servlet.http.HttpSession) s, ctx);
	        }
	 	} 
	    /*
	     Method: doPost
	     	Handles the HTTP POST method
	     	
	     Parameters:
		 	request  - HttpServletRequest
		 	response - HttpServletResponse
	     
	     */
	    @Override
		protected void doPost(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException 
	    {
	    	PrintWriter out = response.getWriter();
	        String projectid=request.getParameter("projectid");
	        String basketid=request.getParameter("basketID");
	        String description=request.getParameter("description");
            displayXML(request, response, out, basketid, description, projectid);
	    }

	    /*
	     Method: listBaskets
	     	Fetches all baskets for current user and displays in template form
	     	
	     Parameters:
	     	out - PrintWriter
	     	s   - HttpSession
	     	
	     About:
	     	Can throw IOException
	     */
	    private void listBaskets(PrintWriter out, HttpSession s) throws IOException 
	    {
	        ArrayList<String> results = new ArrayList<String>();
	        try
	        {
	        	String username=(String)s.getAttribute("username");
	            String query="";
	            query="select basketID, description from basketdetails where basketID in (select basketID from shoppingbaskets where username=\'"+username+"\')";
	            /*
	             * Field info:  1) Basket id, 2) description 
	             */
	            getServletContext().log(username+":"+query);
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
	        Page p=new Page("Download-displayListOfBasketsForDownload");
	        p.setnumCols(2);
	        p.setResults(results);
	        p.UserPage(out, "List of Saved Baskets", (jakarta.servlet.http.HttpSession) s);
	    }

	    private void displayXML(HttpServletRequest request, HttpServletResponse response, PrintWriter out, String basket, String description, String projectid) 
	    {
	        try
	        {
	        	/*
	        	 * The following two lines are used to access the directory name for files to be downloaded
	        	 * Not sure we're going this way!
	        	ResourceBundle bdl = new PropertyResourceBundle(new FileInputStream(dnget() + "/system.properties"));
	            String dir = bdl.getString("downloadDir");
	            */
	        	HttpSession s = request.getSession();
	    		String accessLevel = s.getAttribute("accessLevel").toString();	        	
	            String username=(String)s.getAttribute("username");            
	            String query="select name, label from variablelabels where name in (select name from shoppingbaskets where basketID=\'"+basket+"\')";
	            /*
	             * Field list is: 1) Variable Name, 2) Variable Label
	             */
	            log.info(HostInfo.tell()+": "+username+": "+query);
	            ConnectDB c = new ConnectDB();
	            c.capture();
	            ResultSet rs = c.doQuery(query);
	            /*
	             * We now have a list of variables; time to instantiate BasketXML objects
	             */
	            BasketXML xbasket = new BasketXML(basket);  // Create the Root XML object
	            xbasket.setUserName(username);              // Associate with the user
	            xbasket.setDescription(description);        // A description of this Basket
	            xbasket.setDate(new Date());                // The creation date, i.e. now
	            xbasket.setProjectId(projectid);
	            while (rs.next()) {
	                xbasket.addVar(rs.getString(1));        // Add a Variable to the list
	            }
	            rs.close();
	            c.release();
	            /*
	             * Now serialise the xbasket object to output
	             */
	            Serializer serializer = new Persister();
	            
	            // Read the name of the directory to create download files in
	            ResourceBundle bdl = new PropertyResourceBundle(new FileInputStream(Util.dnget(this) + "/system.properties"));
	            String dir = bdl.getString("downloadDir");
	            String home = bdl.getString("installRoot");
	            // Make a directory for this user
	            File theDir = new File(dir+"/"+username);
	            log.info("Download: displayXML calling creating -> "+dir+"/"+username);
	            try {
		            FileUtils.forceMkdir(theDir);
	            }
	            catch (Exception e) {
		            e.printStackTrace();
	            }
	            String xoutfname = basket + ".xml"; // New XML file name
	            // combine the directory with the file name
	            try {
		            File xoutf = FileUtils.getFile(theDir, xoutfname);
		            xoutf.setWritable(true);
		            FileUtils.touch(xoutf);  // force create on file	
		            serializer.write(xbasket, xoutf);  // This should write the file to disk
		            Long flength = xoutf.getTotalSpace();
		            xoutf.setReadable(true);
		            log.info("Download: xml file written to -> "+xoutf.getName());
		            log.info("Download: xml file size -> " + flength.toString());
	            }
	            catch (Exception e) {
		            e.printStackTrace();
	            } 
		        Page p=new Page("Download-XMLDownloadChoose");
		        ArrayList<String> results = new ArrayList<String>();
		        String [] wantThese = {"xml"};
		        Iterator<File> xmlFiles = FileUtils.iterateFiles(theDir, wantThese, true);
	            /*
	             *  The problem here is to put in a relative path for a resource
	             *  that will be referenced by Condor/robin/<username>/<basketId>
	             */
		        while (xmlFiles.hasNext()) {
			        results.add(username+"/"+xmlFiles.next().getName());
		        }
		        p.setResults(results);
		        p.UserPage(out, "Choose A Basket To Download", (jakarta.servlet.http.HttpSession) s);
	            
	        }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	        }
	    }

	    
	    
	    /** 
	    * Returns a short description of the servlet.
	    */
	    @Override
		public String getServletInfo() {
	        return "This class implements a Basket download feature that allows users to save an XML representation of their baskets.";
	    }// </editor-fold>


	    /** 
	    * Lists all data sets available for Download.
	    * @param out HTML PrintWriter 
	    * @param p HTML SWIFT page
	    * @param s HTTPSession
	    */

	    
	    

	}


