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
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.io.*;

import mrc.util.Page;
import mrc.db.ConnectDB;
import mrc.util.HostInfo;

/*
 * Class: ApproveUser
 * 	 Available to Director Swift user to approve/reject user registrations and set type as either internal or external if approved 
 */
public class ApproveUser extends HttpServlet {

	private static final long serialVersionUID = 6758863893725207378L;
	// private static final Logger log = Logger.getLogger(HostInfo.tell() + ":" + ApproveUser.class.getName());
	private static final Logger log = Logger.getLogger("mrc.user");
	/* 
    Function: doGet
     	This is called from an administrative user's menu in one of five cases:
     	1. By selecting the "Manage Users -> Approve User" menu item (approve = NULL)
     	2. By selecting the "Reject" option on the List of Users for Approval page (approve = no)
     	3. By selecting the "Review" option on the List of Users for Approval page (approve = review)
     	4. By selecting the "Approve External" option on the list of Users for Approval page (approve = external)
     	5. By selecting the "Approve Internal" option on the list of Users for Approval page (approve = internal)
     
    Parameters:
     	request - HttpServeletRequest object carrying attributes of the HTML GET
     	response - HttpServletResponse object carrying attributes of the HTML output channel
    */
	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException 
    {       
        HttpSession s = request.getSession();
        String adminuser = s.getAttribute("username").toString();
        String order    = request.getParameter("approve");
    	String canduser = request.getParameter("user");        
    	log.info(HostInfo.tell() + " ApproveUser: command: "+order+" admin user is: "+adminuser);
        if (order == null)
        {
            userApprovalList(response, request);
        }
        else 
			switch (order.toLowerCase()) {
			case "no":
				// Reject User 
	            insertApprovals(response, request, canduser);
				rejectUser(response, request, canduser);
				break;
			case "review":
				// Review User
	            displayUserDetails(response, request, canduser);
				break;
			case "external":
				// Approve as external user
	            insertApprovals(response, request, canduser);
				break;
			case "internal":
				// Approve as internal user
	            insertApprovals(response, request, canduser);
				break;
			default:
				// do recover from no order given
				break;
			}

    }	


	/*
	 Function: rejectUser
	  	Puts out a confirmation message that the user has been rejected
	  
	  Parameters:
     	request - HttpServeletRequest object carrying attributes of the HTML GET
     	response - HttpServletResponse object carrying attributes of the HTML output channel
     	canduser - String object carrying the name of the user
	  
	 */
	
    private void rejectUser(HttpServletResponse response, HttpServletRequest request, String canduser) throws IOException {
    	HttpSession s = request.getSession();
        PrintWriter out = response.getWriter();
		Page p = new Page("ApproveUser-rejectUser");
		String message = "The registration for user: " + canduser + " has been rejected.";
		Map<String,Object> context = new HashMap<>();
        context.put("message", message);
        p.extendPageContext(out, "Reject User Registration Request", s, context);
	}



	/*
     Function: displayUserDetails
      	Displays a list of user attributes for a given username if it exists.  Will typically be used as part of 
      	the process of approving a user.  
      
     Parameters:
      	response - HttpServletResponse object passed through from *doGet*
      	request - HttpServletRequest object passed through from *doGet*
      	username - String object carrying the Swift user name to be displayed using Pebble form 'ApproveUser-approveUsers'
    */   
    private void displayUserDetails(HttpServletResponse response, HttpServletRequest request, String username) throws IOException 
    {
        HttpSession s = request.getSession();
        String accessLevel = s.getAttribute("accessLevel").toString();
        PrintWriter out = response.getWriter();
        Page p=new Page("ApproveUser-approveUsers");
        String columnlist = "username,firstName,lastName,affiliation,address1,city,postcode,phone,email,CreateDate";
        String query = "select "+columnlist+" from users where username=\'" + username + "\'";
        log.fine(HostInfo.tell() + " displayUserDetails: for user "+username+":"+query);
        ArrayList<String> results = new ArrayList<String>();
        try 
        {
        	SimpleDateFormat tsf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            ConnectDB c = new ConnectDB();
            c.capture();
            ResultSet rs = c.doQuery(query);
            while (rs.next()) {
            	results.add("Username:");
            	results.add(rs.getString(1)); /* username */
            	results.add("First Name:");
            	results.add(rs.getString(2)); /* firstName */
            	results.add("Last Name:");
            	results.add(rs.getString(3)); /* lastName */
            	results.add("Affiliation:");
            	results.add(rs.getString(4)); /* affiliation */
            	results.add("Address:");
            	results.add(rs.getString(5)); /* address1 */
            	results.add("City:");
            	results.add(rs.getString(6)); /* city */
            	results.add("Postcode:");
            	results.add(rs.getString(7)); /* postcode */
            	results.add("Phone:");
            	results.add(rs.getString(8)); /* phone */
            	results.add("Email:");
            	results.add(rs.getString(9)); /* email */
            	results.add("Date Created:");
            	results.add(tsf.format(rs.getTimestamp(10))); /* CreateDate */
            }
            rs.close();
            c.release();
            p.setResults(results);
            p.UserPage(out, "Approve Users", s);
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }

    /*
     Function: getServletInfo
      	Inherited function that provides short Class documentation
    */
    @Override
    public String getServletInfo() 
    {
        return "Class that allows the admin users to approve user registrations.";
    }

    /*
     Function: insertApprovals
      	Changes the user registration details according to the request from the Director login.  Uses request 
      	parameters to invoke the correct action.
      
     Parameters:
      	response - HttpServletResponse object passed through from *doGet*
      	request - HttpServletRequest object passed through from *doGet*
      	username - the Swift user name to be altered
    */   
    private void insertApprovals(HttpServletResponse response, HttpServletRequest request, String username) throws IOException 
    {
        PrintWriter out = response.getWriter();

        String query = "";
        query = setQueryStatus(request);
        log.fine(HostInfo.tell() + " insertApprovals: "+username+":"+query); 
        try
        {
            updateUserStatus(query, out, request, username);
        }
        catch (Exception se)
        {
        	log.severe(HostInfo.tell()+" ApproveUser: Error in updateUserStatus with: "+query);
            se.printStackTrace();;
        }           
        if (request.getParameter("approve").equalsIgnoreCase("external"))
        {
            try
            {        
                approveExternal(request, query, out, username);
            }
            catch (Exception se)
            {
            	log.severe(HostInfo.tell()+" ApproveUser: Error in approveExternal with: "+query);
                se.printStackTrace();
            }                    
        }  else if (request.getParameter("approve").equalsIgnoreCase("internal"))
        {
        	try
            {        
                approveInternal(request, out, username);
            }
            catch (Exception se)
            {
            	log.severe(HostInfo.tell()+" ApproveUser: Error in approveInternal with: "+query);
            }                    
        }
    }

    /*
     Function: approveExternal
      	Changes the user registration details to be an external Swift user according to the request from the Director login.  
      
     Parameters:
      	response - HttpServletResponse object passed through from *doGet*
      	request - HttpServletRequest object passed through from *doGet*
      	username - the Swift user name to be altered
    */   
    private void approveExternal(HttpServletRequest request, String query, PrintWriter out, String username) 
    {
        query = "insert into privileges values (\'" + username + "\', \'external\',0)";
        log.info("ApproveUser: "+username+":"+query);
        try 
        {
            int r = 0;
            ConnectDB c = new ConnectDB();
            c.capture();
            r = c.doUpdate(query);
            c.release();
            
            if (r==0)            
                log.warning(HostInfo.tell()+ " ApproveUser: Error in approveExternal : "+username);
        } 
        catch (Exception e) 
        {
        	log.severe(HostInfo.tell()+" Error in approveInternal with: "+query);
        }
    }

    /*
     Function: approveInternal
      	Changes the user registration details according to be an internal Swift user according to the request from the Director login.
      
     Parameters:
      	request - HttpServletRequest object passed through from *doGet*
      	out - PrintWriter object associated with the HttpServletResponse from the calling environment
      	username - the Swift user name to be altered

    */   
    private void approveInternal(HttpServletRequest request,  PrintWriter out, String username) 
    {
        String query = "insert into privileges values (\'" + username + "\', \'internal\',1)";
        log.fine(username+":"+query);
        try 
        {
            int r = 0;
            ConnectDB c = new ConnectDB();
            c.capture();
            r = c.doUpdate(query);
            c.release();
            
            if (r==0)            
                log.warning(HostInfo.tell()+" ApproveUser: Error in approveInternal: "+username);
        } 
        catch (Exception e) 
        {
        	log.severe(HostInfo.tell()+" Error in approveInternal with: "+query);
        }
    }
    
    /*
     Function: setQueryStatus
      	Updates the users table setting the status to APPROVED or REJECTED
      
     Parameters:
      	request - HttpServletRequest object passed through from *doGet*
    */   
    private String setQueryStatus(HttpServletRequest request) 
    {
        String query="";
        try
        {        
            if (request.getParameter("approve").equalsIgnoreCase("external") || 
            	request.getParameter("approve").equalsIgnoreCase("internal")) 
            {
                query = "update users set status=\'APPROVED\' where username=\'" + request.getParameter("user") + "\'";
            } 
            else 
            {
                query = "update users set status=\'REJECTED\' where username=\'" + request.getParameter("user") + "\'";
            }
        }
        catch (Exception e)
        {
        	log.severe(HostInfo.tell()+" Error in setQueryStatus with: "+query);
        }        
        return query;
    }

    /*
     Function: updateUserStatus
      	Displays confirmation of changes made to user registration details
      
     Parameters:
      	response - HttpServletResponse object passed through from *doGet*
      	request - HttpServletRequest object passed through from *doGet*
      	username - the Swift user name to be altered
      
    */   
    private void updateUserStatus(String query, PrintWriter out, HttpServletRequest request, String username)
    {
        try 
        {
        	HttpSession s = request.getSession();
    		String accessLevel = s.getAttribute("accessLevel").toString();
    		Page p = new Page("ApproveUser-approveStatus");
            int r = 0;
            ConnectDB c = new ConnectDB();
            c.capture();
            r = c.doUpdate(query); // update status field to either approved or rejected           
            c.release();
            String message = "";
            String order = request.getParameter("approve");
            if (order.equalsIgnoreCase("internal")) 
            {
                message = r + " user record(s) updated for "+username+". Internal access GRANTED.";
            } 
            else
            {
            	message = r + " user record(s) updated for "+username+". External access GRANTED.";
            }
            if (r==0)            
                log.warning(HostInfo.tell()+ " ApproveUser: Error in updateUserStatus: "+username);
            Map<String,Object> context = new HashMap<>();
            context.put("message", message);
            p.extendPageContext(out, "Approve User Status", s, context);
            
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    
    /*
     Function: userApprovalList
      	Displays the list of user registrations waiting for a decision from the Director
      
      Parameters:
      	response - HttpServletResponse object passed through from *doGet*
      	request - HttpServletRequest object passed through from *doGet*
      
   */   
    private void userApprovalList(HttpServletResponse response, HttpServletRequest request) throws IOException 
    {
        PrintWriter out = response.getWriter();
        HttpSession s = request.getSession();
		String accessLevel = s.getAttribute("accessLevel").toString();        
        ArrayList<String> results = new ArrayList<>();
        String query = "select username from users where status = \'unverified\'";
        log.info("Condor userApprovalList inside Servlet ApproveUser");
        getServletContext().log("userApprovalList :"+query);
        Page p = new Page("ApproveUser-userApprovalList");
        try 
        {
            ConnectDB c = new ConnectDB();
            c.capture();
            ResultSet rs = c.doQuery(query);
            while (rs.next()) 
            {
               results.add(rs.getString(1));
              
            }
            rs.close();
            c.release();
        } 
        catch (Exception e) 
        {
        	log.severe(HostInfo.tell()+" Error in userApprovalList with: "+query);
        }
        p.setResults(results);
        p.UserPage(out, "List of Users for Approval", s);
    }


}
