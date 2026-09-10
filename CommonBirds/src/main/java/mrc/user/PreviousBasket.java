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
import java.util.logging.Logger;

import mrc.db.*;
import mrc.util.*;

/* Class: PreviousBasket
 * 	Facilitates user-level shopping Basket activities
 * 
 */

public class PreviousBasket extends HttpServlet 
{
    /* Function: doGet
     * 	Part of HttpServlet standard methods.  Handles HTML GET requests
     * 
     * Parameters: 
     * 	request - HttpServletRequest objcect defined to handle GET requests from session user
     * 	response _ HttpServletResponse object defined to build output channel for session user
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// private static final Logger log = Logger.getLogger(HostInfo.tell()+":"+PreviousBasket.class.getName());
	private static final Logger log = Logger.getLogger("mrc.user");
	
	/** 
    * Handles the HTTP <code>GET</code> method.
    * @param request servlet request
    * @param response servlet response
    */
    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException 
    {
        PrintWriter out = response.getWriter();        
        HttpSession s = request.getSession();
        if (request.getParameter("basketID")==null)
        {
            listBaskets(out, s);
        }
        else
        {
            String basket=request.getParameter("basketID");
            displayDescription(out, s, basket);
        }
    } 

    /** 
    * List saved shopping baskets.
    * @param out HTML PrintWriter 
    * @param s HttpSession
    * @throws IOException 
    */
    private void listBaskets(PrintWriter out, HttpSession s) throws IOException 
    {
        ArrayList<String> results = new ArrayList<String>();
        try
        {
        	String username=(String)s.getAttribute("username");
            String query = null;
            if (Util.mainApp().equalsIgnoreCase("Jay")) {
                query="select basketID, description from rook.basketdetails where basketID in  " +
                        "(select basketID from rook.shoppingbaskets where username=\'"+username+"\')" +
                 		" order by createDate";
            } else {
                query="select basketID, description from basketdetails where basketID in  " +
                        "(select basketID from shoppingbaskets where username=\'"+username+"\')" +
                 		" order by createDate";
            }

            log.fine(HostInfo.tell()+" listBaskets: "+username+":"+query);
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
        Page p=new Page("PreviousBasket-displayListOfBaskets");
        p.setnumCols(2);
        p.setResults(results);
        p.UserPage(out, "List of Saved Baskets", s);
    }


    
    
    
    
    
    /** 
    * Iterate through list of previous shopping baskets.
    * @param out HTML PrintWriter 
    * @param Basket Basket ID 
    * @param s HttpSession
    */
    private void displayDescription(PrintWriter out, HttpSession s, String basket) 
    {
        try
        {
            ArrayList<String> results = new ArrayList<String>();
            String username=(String)s.getAttribute("username");            
            String query = null;
            if (Util.mainApp().equalsIgnoreCase("Jay")) {
            	query = "select name, label from rook.variablelabels where name in (select name from rook.shoppingbaskets where basketID=\'"+basket+"\')";
            } else {
            	query = "select name, label from variablelabels where name in (select name from shoppingbaskets where basketID=\'"+basket+"\')";
            }
            getServletContext().log(username+":"+query);
            ConnectDB c = new ConnectDB();
            c.capture();
            ResultSet rs = c.doQuery(query);
            while (rs.next()) {
                results.add(rs.getString(1));
                results.add(rs.getString(2));
            }
            rs.close();
            c.release();
            Page p=new Page("PreviousBasket-displayListOfVariables");
            p.setnumCols(2); // The output table has 4 columns, but only 2 of them come from results
            p.setResults(results);
            p.UserPage(out, "List of Variables in "+basket, s);
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
	public String getServletInfo() 
    {
        return "Lists all user's baskets for loading or sharing";
    }
}
