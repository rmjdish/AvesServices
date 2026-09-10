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
import java.util.Map;
import java.util.ArrayList;
import java.util.logging.Logger;

import mrc.util.Page;
import mrc.db.*;
import mrc.util.HostInfo;

/*Class: SharedBasket
 * 	Allows users to browse and load baskets made public (shared) by other users
 */
public class SharedBasket extends HttpServlet 
{
	private static final long serialVersionUID = -8853883634176921836L;
	// private static final Logger log = Logger.getLogger(HostInfo.tell()+":"+SharedBasket.class.getName());
	private static final Logger log = Logger.getLogger("mrc.user");
	private static final String NOTSHARED = "0";
	private static final String SHARED = "1";
	/* Function: doGet 
    * 	Overriden from HttpServlet Handles the HTTP <code>GET</code> method requests from Tomcat
    * 
    * Parameters:
    * 	request - HttpServletRequest object supplied by Tomcat
    * 	response - HttpServletResponse object to return to Tomcat
    */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException 
    {
        PrintWriter out = response.getWriter();        
        HttpSession s = request.getSession();
        if (request.getParameter("command")==null)
        {
            listSharedBaskets(out, s);
        }
        else
        {
            if (request.getParameter("command").equalsIgnoreCase("new"))
            { 
                String basket=request.getParameter("basketID");
                try
                {
                    addNewBasket(out, s, basket);
                }
                catch (Exception se)
                {
                	log.severe(HostInfo.tell()+" Error executing addNewBasket");
                    se.printStackTrace();
                }                      
            }
        }
    } 

    /*
     * Function: doGet
     * 	Overriden from HttpServlet Handles the HTTP <code>POST</code> method requests from Tomcat
     * 
     * Parameters:
     * 	request - HttpServletRequest object supplied by Tomcat
     * 	response - HttpServletResponse object to return to Tomcat
    */
    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException 
    {
        PrintWriter out = response.getWriter();  
        HttpSession s = request.getSession();
        update(request,response);
        String basket=request.getParameter("basketID");	
    	Page p = new Page("SharedBasket-endOfProcess");
    	p.shareForm(out, "Basket Successfuly Shared", s, basket);
    }
    
    
    /* Function: update 
    * 	Updates database record with new description of basket entered on form.

    * Parameters:
    * 	request - HttpServletRequest object supplied by Tomcat
    * 	response - HttpServletResponse object to return to Tomcat

    */
    private void update(HttpServletRequest request, HttpServletResponse response ) throws IOException 
    {
    	String username = request.getSession(false).getAttribute("username").toString();
    	String description = request.getParameter("desc").replaceAll("'", "''");
    	String basket = request.getParameter("basketID");
        try
        {
            updateDescription(description,basket,username);
        }
        catch (Exception se)
        {
        	log.severe(HostInfo.tell()+" SharedBasket Error in updateDescription");
            se.printStackTrace();
        }   
    }

    /* Function: updateDescription
    * 	Does the database work for update.

    * Parameters:

    * 	desc - String denoting the new description for the shared basket
    * 	basketID - the Swift Basket ID
    * 	username - the Swift username of the person who created the basket
    * 
    * See Also:
    * 	<update>
    */    
    private void updateDescription(String desc, String basketID, String username)  
    {
        String query = "update sharedbaskets set description=\'" + desc + "\',shared="+SHARED+" "+                
                       "where username=\'"+username+"\' and basketID=\'"+basketID+"\'";
        log.fine(HostInfo.tell()+" updateDescription: " + username + ":" + query);
        try 
        {
            ConnectDB c=new ConnectDB();
            c.capture();
            int r = c.doUpdate(query);
            c.release();
            if (r==0)            
                log.warning(HostInfo.tell()+" SharedBasket: updateDescription: doQuery error for "+username+" and query "+query);        
        } 
        catch(Exception e) 
        {
        	log.severe(HostInfo.tell()+" Error SharedBasket: updateDescription Error accessing database for doUpdate");
            e.printStackTrace();
        } 
    }

    /* Function: addNewBasket
    * 	Inserts a new Basket entry into the sharredbaskets table
    * 
    * Parameters:
    * 	s - HTTPSession
    * 	basket - SWIFT Basket ID
    * 	out - HTML PrintWriter 
    * 	p - Authenticated SWIFT page
    */
    private void addNewBasket(PrintWriter out, HttpSession s, String basket) 
    {
        String username=s.getAttribute("username").toString();
        String query = "insert into sharedbaskets values (\'" + username + "\', \'" + basket + "\'," + NOTSHARED + ",\'\')";
        log.info(HostInfo.tell()+" Trying: "+username+":"+query);
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
        	log.severe(HostInfo.tell()+" SharedBasket: addNewBasket: Error accessing database fro doUpdate");
            e.printStackTrace();
        }
    }

    /* Function: enterDescription
    * 	Emits the template form for sharing a basket
    * 
    * Parameters:
    * 	basketID - SWIFT Basket ID
    * 	out - HTML PrintWriter 
     * 	
     * About: enterDescription
     * 	This function throws IOException
     * 
     *  See Also:
     *  	<addNewBasket>
    */
    private void enterDescription(PrintWriter out, HttpSession s, String basketID) throws IOException 
    {
    	Page p = new Page("SharedBasket-sharedBasketForm");
    	p.shareForm(out, "Share Your Basket", s, basketID);
    }

    /** 
    * Returns a short description of the servlet.
    */
    @Override
	public String getServletInfo() 
    {
        return "Short description";
    }

    /** 
    * List saved shopping baskets.
    * @param out HTML PrintWriter 
    * @param p HTML SWIFT page
    * @param s HttpSession
    */
    private void listSharedBaskets(PrintWriter out, HttpSession s) 
    {
        displayItems(out, s);        // Change to Pebble Template
    }

    /** 
    * Iterate through list of previous shopping baskets.
    * @param out HTML PrintWriter 
    * @param s HTTPSession
    */
    private void displayItems(PrintWriter out, HttpSession s) 
    {
        try
        {
            ArrayList<String> results = new ArrayList<String>();
            String query="select username, basketID, description, shared  from sharedbaskets";
            /*
             * Field list:
             * username is column 1
             * basketID is column 2
             * description is column 3
             * shared is column 4
             */
            String username=s.getAttribute("username").toString();
            getServletContext().log(username+":"+query);
            ConnectDB c = new ConnectDB();
            c.capture();
            ResultSet rs = c.doQuery(query);
            while (rs.next()) {
                results.add(rs.getString(1)); // username
                results.add(rs.getString(2)); // baskekID
                results.add(rs.getString(3)); // description
            }
            rs.close();
            c.release();
            Page p=new Page("SharedBasket-displayListOfSharedBaskets");
            p.setResults(results); // this should be 3 elements plus option per line output
            p.setnumCols(3);
            p.UserPage(out, "Load Shared Basket", s);

        }
        catch (Exception e)
        {
            e.printStackTrace();
            log.severe(HostInfo.tell()+" displayItems: Error executing query.");
        }
    }
    
}
