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
import java.util.*;
import java.util.logging.Logger;
import java.sql.*;

import mrc.util.Page;
import mrc.util.StoreXMLBasket;
import mrc.db.ConnectDB;
import mrc.user.RandomString;
import mrc.util.HostInfo;

/*
 Class: Basket
  	Facilitates user-level shopping Basket activities
  
 */
public class Basket extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	// private static final Logger log = Logger.getLogger(HostInfo.tell() + ":" + Basket.class.getName());
	private static final Logger log = Logger.getLogger("mrc.user");
	private static final int SUFLEN = 6;
	/*
	 Variable: xmlversion
	  	Stores a serialised version of a basket see <StoreXMLBasket>
	  	
	 */
	private StoreXMLBasket xmlversion;
	/* 
	 Procedure: doGet
	 	Overriden procedure that accepts HTTP GET requests from Tomcat Java Servlet framework
     
     Parameters:
     	request - HttpServletRequest object carrying attributes of the HTML GET
     	response - HttpServletResponse object carrying attributes of the HTML output channel
     
    */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException 
    {
        HttpSession s = request.getSession();
		String accessLevel = s.getAttribute("accessLevel").toString();        
        PrintWriter out = response.getWriter();
        /* Variable: v
        	Stores the current list of variables carried around in session object
        */
        ArrayList<String> v = new ArrayList<String>();
        v = (ArrayList<String>) s.getAttribute("items"); // The Variable list is carried around in the session object
        String username = s.getAttribute("username").toString();
        /* Variable: p
          	Pebble template based object that uses .html file 
          	indicated as parameter to Page constructor
        
            --- Java
            Page p = new Page("Basket-Basket");
			p.setResults(v);
			--- 
         */
        Page p = new Page("Basket-Basket");
        p.setResults(v); // loads current list of variables into Page context
        p.setnumResults(v.size());
        if (request.getParameter("command")!=null)
        {   
            log.fine(HostInfo.tell()+" Basket: GET called with command -> "+request.getParameter("command"));
            if (request.getParameter("command").equalsIgnoreCase("remove"))
            {
                removeItemFromBasket(request, v, s); // this updates the session Variable items attribute
                displayBasket(out,request.getParameter("name")+" has been removed", s, p);
            } 
            else if (request.getParameter("command").equalsIgnoreCase("removeAll"))
            {
                removeAllItemsFromBasket(v, s);
                displayBasket(out,"All variables have been removed from your Basket.",s, p);
            }
            else if (request.getParameter("command").equalsIgnoreCase("saveNew"))
            {
                // what follows is one way of forming a unique Basket name
                String basketID = username +"ZZ"+RandomString.randomstring(SUFLEN); 
                log.info(HostInfo.tell()+" Basket: New Basket name -> "+basketID);
                p.chgTemplate("Basket-saveBasketForm");
                // Now call the matching view method in the Page class
                p.shareForm(out, "Enter Basket Details for "+basketID, s, basketID);        
            }
            else if (request.getParameter("command").equalsIgnoreCase("saveExisting"))
            {
            	/*
            	 * Change the template before displaying it
            	 * Also need to get a list of baskets to overwrite
            	 */
                log.fine(HostInfo.tell()+" Basket: saveExisting; changing template to basketoverwrite.");            	
            	p.chgTemplate("Basket-basketoverwrite");
            	p.setResults(getBasketList(out,s));  // Set results attribute in current Page object
            	p.UserPage(out,"Select a Basket to overwrite.",s);
            }
            else if (request.getParameter("command").equalsIgnoreCase("overwrite"))
            {
                String basket=request.getParameter("basketID");
                int r=0;
                try
                {
                    r=deleteItemsFromBasket(s, basket);
                }
                catch (Exception se)
                {
                	log.warning(HostInfo.tell()+ " Basket: Error in deleteItemsFromBasket: "+basket);
                    se.printStackTrace();
                }                      
                int q=0;
                try
                {
                    q=saveItemsInBasket(s, basket, v);
                }
                catch (Exception se)
                {
                	log.warning(HostInfo.tell()+" Basket: Error in saveItemsInBasket: "+basket);
                    se.printStackTrace();
                }                        
                p.UserPage(out,"Basket "+basket+" overwritten with "+q+" variables.", s);
            }
            else 
            {
            	log.warning(HostInfo.tell()+" Basket: cannot match command -> "+request.getParameter("command")); 
            }
        }
        else
        {
            displayBasket(out,"Basket Details", s, p);
        }    
    } 

    /* 
     Method: doPost 
	 	Overriden procedure that accepts HTTP GET requests from Tomcat Java Servlet framework
	 	Allows internal users to save new shopping baskets.
     
     Parameters:
     	request - HttpServletRequest object carrying attributes of the HTML GET
     	response - HttpServletResponse object carrying attributes of the HTML output channel

    */
    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException 
    {        
        PrintWriter out = response.getWriter();        
        HttpSession s = request.getSession();
		String accessLevel = s.getAttribute("accessLevel").toString(); 
		/* Variable: username
		 * 	Swift username of the current user.  Used to look up 
		 * 	baskets.  Carried in the session variable
		 */
        String username=(String)s.getAttribute("username");
		/* Variable: basketID
		 * 	Unique basket identifier to look up 
		 * 	baskets.  Carried in the session variable
		 */
        String basketID=request.getParameter("basketID");
        String description=request.getParameter("description");
        String query = "";
        log.fine(HostInfo.tell()+" Basket: POST method called with Basket ID -> "+basketID);
        try
        {
            query=createInternalBasket(username.replaceAll("'", "''"), 
            		basketID, description.replaceAll("'", "''"));
        }
        catch (Exception se)
        {
        	log.warning(HostInfo.tell()+" Basket: Error in createInternalBasket: "+basketID);
        }        
        log.info(HostInfo.tell()+": "+username+": "+query);
        try
        {
            query = insertShoppingBasketRecord(s, username.replaceAll("'", "''"), basketID);
        }
        catch (Exception se)
        {
        	log.warning(HostInfo.tell()+" Basket: Error in insertShoppingBasketRecord: "+basketID);
        }        
        log.info(" Basket: "+username+":"+query);
        Page p=new Page("Basket-Basket");
		ArrayList<String> v = new ArrayList<String>();
		v = (ArrayList<String>) s.getAttribute("items"); 
		v = addVarLabels(v);
		p.setResults(v);
        p.UserPage(out,"Basket "+basketID+" has been saved",s);
    }
    
    /* @brief Take a list of var names and add varlabels for each one 
     * @parm items - List of variablenames
     * @returns List of <var name> <var label> pairs
     */
    private ArrayList<String> addVarLabels(ArrayList<String> items) {
    	ArrayList<String> results = new ArrayList<String>();
    	for (String var : items) {
    		results.add(var);
    		// Now get the label
    		String query = "CALL getLabel('"+var+"')"; 
  	      	try 
  	      	{
  	    	  ConnectDB c=new ConnectDB();
  	    	  c.capture();
  	    	  ResultSet rs=c.doQuery(query);
  	    	  if(rs.next()) 
  	    	  {
  	    		  String thislab = rs.getString("Label"); // the var label
  	    		  results.add(thislab);
  	    	  } else {
  	    		  results.add("NA");
  	    	  }
  	    	  rs.close();
  	    	  c.release();
  	      	}
  	        catch (Exception e) 
  	        {
  	            log.severe(HostInfo.tell()+" ERROR:Basket:addVarLabels:"+query);
  	        }

    	}
    	return results;
    }
    
    
    
    
    /*
     Method: createInternalBasket
     	Insert internal project record, used to describe Basket when shared - not a formal data access proposal! 
     
     Parameters:
     	username - Swift/Condor Username
     	basketID - unique Basket ID
     	description - Basket description
     	
     Returns:
     	query - String SQL used for insertion
    */
    private String createInternalBasket(String username, String basketID, String description) 
    {
        String query="";
        try 
        {
            query = "insert into basketdetails values (\'" + basketID + "\', \'" + description + "\', \'" + username + "\', now())";
            String projectId = "Not-Known";
            this.xmlversion = new StoreXMLBasket(username,basketID,description,projectId);

            log.info(HostInfo.tell()+": "+username+": "+query);
            ConnectDB c = new ConnectDB();
            c.capture();
            int r = c.doUpdate(query);
            c.release();
            if (r <= 0)            
                log.warning(HostInfo.tell()+ " Basket: Error in createInternalBasket: "+username+" : query: "+query);            
        } 
        catch (Exception e) 
        {
            log.severe(HostInfo.tell()+" ERROR:Basket:createInternalBasket:"+query);
        }
        return query;
    }

    /* 
    Method: insertShoppingBasketRecord
     Insert approvals record for proposal
    
    Parameters:
    	s -  HTTPSession object
    	username - String object containing user name
    	basketID - String object containing Basket ID
    	
    Returns:
    	query - String SQL used for insertion
    */
	private String insertShoppingBasketRecord(HttpSession s, String username, String basketID)
    {
        String query="";
         try 
        {
            ArrayList<String> v = new ArrayList<String>();
            v = (ArrayList<String>) s.getAttribute("items");
            // All baskets must have the following four variables
            if (!v.contains("SERNO")) 
            {
                v.add("SERNO");
            }
            if (!v.contains("SEX")) 
            {
                v.add("SEX");
            }
            if (!v.contains("INF")) 
            {
                v.add("INF");
            }
            if (!v.contains("NTAG1"))
            {
            	v.add("NTAG1");
            }
            int r=0;
            ConnectDB c = new ConnectDB();
            c.capture();
            for (String item : v) 
            {
                query = "insert into shoppingbaskets values (\'" + username + "\', \'" + basketID + "\', \'" + item + "\')";
                this.xmlversion.plusVar(item);
                log.fine(HostInfo.tell()+" insertShoppingBasketRecord: "+username+":"+query);
                r = c.doUpdate(query);
            }
            c.release();
            if (r==0)
                log.warning(HostInfo.tell()+ " Basket Error in insertShoppingBasketRecord: "+username+" : query: "+query); 
            // Finally push XML into store
            this.xmlversion.storeDoc();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        return query;
    }

    /*
     Method: getServletInfo 
     	Returns a short description of the servlet.
     	
     Returns:
     	String description
    */
    @Override
	public String getServletInfo() 
    {
        return "Class to execute shopping basket manipulation";
    }

    /*
     Method: displayBasket
     	Display contents of shopping Basket.
     
     Paramaters:
     	out - PrintWriter object 
    	p -  SWIFT Page object for template
    	s - HttpSession object
      
     About: 
        This function throws IOException 
    */
    private void displayBasket(PrintWriter out, String title, HttpSession s, Page p) throws IOException 
    {
		String accessLevel = s.getAttribute("accessLevel").toString();
		ArrayList<String> vars = new ArrayList<String>(); // The variable names
		ArrayList<String> ents = new ArrayList<String>(); // The entries consisting of var names + labels
        vars = (ArrayList<String>) s.getAttribute("items"); // The Variable list is carried around in the session object
        
        for(String v:vars) {
        	String query = "CALL getMetaForVar(\'"+v+"\')";
  	      	log.info(HostInfo.tell()+" displayBasket: "+query);
  	      	ents.add(v);
  	      	try 
  	      	{
  	    	  ConnectDB c=new ConnectDB();
  	    	  c.capture();
  	    	  ResultSet rs=c.doQuery(query);
  	    	  if(rs.next()) 
  	    	  {
  	    		  String thislab = rs.getString(3); // the var label
  	    		  ents.add(thislab);
  	    	  } else {
  	    		  ents.add("NA");
  	    	  }
  	    	  rs.close();
  	    	  c.release();
  	      	}
  	      	catch(Exception e) 
  	      	{
  	    	  log.severe(HostInfo.tell()+" displayBasket: Error :"+e.getMessage());
  	      	} 
        }
        
        // Iterate over the 
    	p.setnumCols(2);
    	p.setResults(ents);
        // Now call the matching view method in the Page class
        p.UserPage(out,title,s);        

    }

 
    /*
    Method: displayBasketList
    	Iterate through list of previous shopping baskets for overwriting.
    
    Parameters:
    	out - PrintWriter object 
    	s - HttpSession object
    	p - Swift Page object for template
    	
    */
    private void displayBasketList(PrintWriter out, HttpSession s, Page p) 
    {
        try
        {
            String username=s.getAttribute("username").toString();
    		String accessLevel = s.getAttribute("accessLevel").toString();
            String query="";
            query="select basketID, description from basketdetails where basketID in (select basketID from shoppingbaskets where username=\'"+username+"\')";
            ArrayList<String> resmap = new ArrayList<String>(); // List of variables
            log.info(HostInfo.tell()+": "+username+": "+query);
            ConnectDB c = new ConnectDB();
            c.capture();
            ResultSet rs = c.doQuery(query);
            while (rs.next()) 
            {
                resmap.add(rs.getString(1));
                resmap.add(rs.getString(2));
            }
            rs.close();
            c.release();
            p.chgTemplate("Basket-displayListOfBaskets");
        	p.setnumCols(2);
        	p.setResults(resmap);
            p.UserPage(out,username+" Baskets",s);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    /*
     Method: getBasketList
     	Get a list of baskets belonging to the logged in session user
     	
     Parameters:
     	out - PrintWriter object
     	s - HttpSession object
     	
     Returns:
     	results - ArrayList<String> of basketID, description pairs
     */
    
    private ArrayList<String> getBasketList(PrintWriter out, HttpSession s) throws IOException 
    {
        ArrayList<String> results = new ArrayList<String>(); // List of variables
        try
        {
        	String username=(String)s.getAttribute("username");
            String query="";
            query="select basketID, description from basketdetails where basketID in (select basketID from shoppingbaskets where username=\'"+username+"\')";
            log.info(HostInfo.tell()+": "+username+": "+query);
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
        return results;
    }



    /*
    Method: removeItemFromBasket
     	Removes selected item from shopping Basket.
    
    Parameters:
    	request - HttpServletRequest object
    	v - ArrayList containing items in basket
    	s - HttpSession object
    */
    private void removeItemFromBasket(HttpServletRequest request, ArrayList<String> v, HttpSession s) 
    {
        String name = request.getParameter("name");
        v.remove(name);
        s.setAttribute("items", v);
    }
    
    /*
    Method: removeAllItemsFromBasket
    	Removes all items from shopping Basket.
    
    Parameters
    	v - ArrayList containing items
    	s - HttpSession object
    */
    private void removeAllItemsFromBasket(ArrayList<String> v, HttpSession s) 
    {
        v.clear();
        s.setAttribute("items", v);
    }
    
   /*
    Function deleteItemsFromBasket 
    	Deletes all items from saved shopping Basket.  Returns integer number of
    	rows in SQL response
    
    Parameters:
    	basket - String BasketID
    	s - HttpSession object
    	
    Returns:
    	r - int >0 if successful deletion
    */
    private int deleteItemsFromBasket(HttpSession s, String basket) 
    {
        int r=0;
        String username=(String)s.getAttribute("username");
        String query="delete from shoppingbaskets where basketID=\'"+basket+"\'";
        try 
        {
            getServletContext().log(username+":"+query);
            ConnectDB c = new ConnectDB();
            c.capture();
            r = c.doUpdate(query);
            c.release();
            if (r==0)            
                log.warning(HostInfo.tell()+" :"+username+":Basket.deleteItemsFromBasket() failed with query: "+query);
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        return r;
    }

    /* 
    Method: saveItemsInBasket
    	Saves all items from shopping Basket. Creates a Basket description if necessary.
        
    Parameters:
    
    	basket  - String BasketID
    	f  - Enumeration of ArrayList containing items
    	s  - HttpSession object
    	
    Returns:
    	q - int >0 if successful insertion into db
    */
    private int saveItemsInBasket(HttpSession s, String basket, ArrayList<String> f) 
    {
        // Insert all new items in Basket
        int q=0;
        int r=0;
        ListIterator<String> bskvars = f.listIterator();
        String username=(String)s.getAttribute("username");
        ConnectDB c = new ConnectDB();
        c.capture();
        while (bskvars.hasNext()) 
        {
            try 
            {
                String query = "insert into shoppingbaskets values (\'" + username + "\', \'" + basket + "\', \'" + bskvars.next() + "\')";
                r = c.doUpdate(query);
                q=q+r;
            } 
            catch (Exception e) 
            {
            	log.severe(HostInfo.tell()+" Basket: Error in saveItemsInBasket");
                e.printStackTrace();
            }
        }
        c.release();
        if (q==0)            
            log.warning(HostInfo.tell()+ " Basket Error in saveItemsInBasket for user: "+username);        
        return q;
    }


}
