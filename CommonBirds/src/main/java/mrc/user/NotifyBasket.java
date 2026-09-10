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
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Logger;

import mrc.util.*;
import mrc.db.ConnectDB;
import mrc.smtp.MailMessage;
import mrc.user.SharedBasket;
import mrc.smtp.MailMessage;


public class NotifyBasket extends HttpServlet 
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -3428125614116115084L;
	private static final Logger log = Logger.getLogger(NotifyBasket.class.getName());


	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException 
    {
        PrintWriter out = response.getWriter();        
        HttpSession s = request.getSession();
        if (request.getParameter("command") != null && request.getParameter("command").equalsIgnoreCase("post"))
        {
        	String basketID=request.getParameter("basketID");
        	try {
				notifyLHA(response,s, basketID);
			} catch (Exception e) {
				log.severe(HostInfo.tell()+" NotifyBasket: error processing basket "+basketID);
				log.severe(HostInfo.tell()+" NotifyBasket: "+e.getMessage());
			}
        } else {
        	printAvailableBaskets(s, out);
        }
    }    
	
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException 
	{
        PrintWriter out = response.getWriter();        
        HttpSession s = request.getSession();
    	String basket=request.getParameter("basketID");
        Page p=new Page("NotifyBasket-requestSent");
    	p.shareForm(out, "Basket Shared And LHA Notified", s, basket);
        
	}
 
    protected void notifyLHA(HttpServletResponse response, HttpSession s, String basketID)
    /*
     * Sends email to LHA about basket s
     * Called from doGet with parameters command=post and basketID set
     */
    	    throws ServletException, IOException 
    	    {
    	        String username=s.getAttribute("username").toString();
    	        MailMessage m = new MailMessage();
    	        m.sendEmailToUser("manager",
    	        		"Request To Build Basket From: "+username,
    	        		username+" has requested basket "+basketID+" be built and scrambled. Please login to Jay, cross-check, and build the data file if appropriate.");
    	        MailMessage m1 = new MailMessage();
    	        m1.sendEmailToUser(username, 
    	        		"Request To Build Basket: "+basketID,
    	        		"Your request to have basket "+basketID+" built and scrambled has been received."+
    	        		"  The LHA SST will now check your request and build the data file if appropriate."+
    	        		"  You will be notified when the data file is available."+
    	        		"  This is an automated MailMessage, please do not reply to it.");
    	        PrintWriter out = response.getWriter();
    	        /* The next bit is non-standard way of sharing basket - trying to bypass JSP Request/Response framework */
    	        addNewBasket(out,s,basketID);  /* Hopefully this shares the basket too */  	        
    	    }
    
    private void printAvailableBaskets(HttpSession s, PrintWriter out) 
    {        
        String username=(String)s.getAttribute("username");
        String query="SELECT distinct basketid, description FROM basketdetails "+
            "where username=\'"+username+"\'";
        log.info(HostInfo.tell()+" : "+query);
        ArrayList<String> results = new ArrayList<String>();
        try 
        {
            ConnectDB c = new ConnectDB();
            c.capture();
            ResultSet rs = c.doQuery(query);
            while (rs.next()) 
            {
            	results.add(rs.getString(1)); // basketID
                results.add(rs.getString(2)); // description
            }
            rs.close();
            c.release();
            Page p=new Page("NotifyBasket-printAvailableBaskets");
            p.setResults(results); // this should be 2 elements plus option per line output
            p.setnumCols(2);
            p.UserPage(out, "Notify LHA About Basket", s);

        } 
        catch (SQLException e) 
        {
        	log.severe("NotifyBasket: error reading avaiable baskets for "+username);
        	log.severe(HostInfo.tell()+" NotifyBasket: "+e.getMessage());
        }
    }
    
    private void addNewBasket(PrintWriter out, HttpSession s, String basket) 
    {
        String username=s.getAttribute("username").toString();
        String query = "insert into sharedbaskets values (\'" + username + "\', \'" + basket + "\', 1, \'\')";
        log.info(HostInfo.tell()+" NotifyBasket: addNewBasket Trying: "+username+":"+query);
        try 
        {
            int r = 0;
            ConnectDB c = new ConnectDB();
            c.capture();
            r = c.doUpdate(query);
            c.release();
            enterDescription(out, s, basket);
        } 
        catch (Exception e) 
        {
        	log.severe(HostInfo.tell()+" NotifyBasket: addNewBasket: Error accessing database: doUpdate");
        	log.severe(HostInfo.tell()+" NotifyBasket: addNewBaseket: "+e.getMessage());
        }
    }

    /** 
    * Inserts a new Basket listing
    * @param basketID SWIFT Basket ID
    * @param out HTML PrintWriter 
     * @throws IOException 
    */
    private void enterDescription(PrintWriter out, HttpSession s, String basketID) throws IOException 
    {
    	Page p = new Page("NotifyBasket-sharedBasketForm");
    	p.shareForm(out, "Share Your Basket", s, basketID);
    }


}
