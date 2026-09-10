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

import java.io.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

import mrc.util.HostInfo;
import mrc.util.Page;
import mrc.db.ConnectDB;
import mrc.smtp.MailMessage;


/*
 Class: BroadcastEmail
 	Allows administrator to send e-mail to all users
 
 */
public class BroadcastEmail extends HttpServlet 
{   
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger("mrc.user");

	/*
	Method: doGet
	 Handles the HTTP GET method.
    
    Parameters:
    	request - HttpServletRequest object
    	response - HttpServletResponse object
    	
    About:
    	This method can throw both ServletException and IOException
    */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException 
    {
        PrintWriter out = response.getWriter();        
        HttpSession s = request.getSession();
        log.fine(HostInfo.tell()+" BroadcastEmail Class: doGet Called.");
        emailForm(s,out);      

    } 

    /*
    Method: doPost 
    	Handles the HTTP POST method.
    
    Parameters:
    	request - HttpServletRequest object
    	response - HttpServletResponse object
    */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException 
    {
        PrintWriter out = response.getWriter();  
        HttpSession s = request.getSession();
        log.fine(HostInfo.tell()+" BroadcastEmail Class: doPost Called.");
        Page p = new Page("BroadcastEmail-Confirmation");
        p.UserPage(out, "Administrator Access - Broadcast Email Confirmed", s);
        sendMessage(request, s.getAttribute("username").toString());
    }

    /*
    Method: getServletInfo
    	Returns a short description of the servlet.
    	
    Returns:
    	String description
    */
    public String getServletInfo() {
        return "Short description";
    }

    /*
    Method emailForm
    	Displays email form for information entry.
    
    Parameters:
    	out -  PrintWriter object 
     	s - HttpSession object
     
     About:
     	This method throws IOException 
    */
    private void emailForm(HttpSession s, PrintWriter out) throws IOException 
    {
        Page p=new Page("BroadcastEmail-emailForm");
        p.UserPage(out,"Email All "+HostInfo.tell()+" Users",s);
    }

    /*
    Method: listUsers
    	Get an array (vector) of all usernames.

	Returns:
		v - Vector<String> of usernames returned by SQL query
    */
    private Vector<String> listUsers()
    {
        Vector<String> v = new Vector<String>();
        try 
        {
            String query = "SELECT username from users order by username asc";
            log.info(HostInfo.tell()+" BroadcastEmail: listUsers: Query: "+query);
            ConnectDB c = new ConnectDB();
            ResultSet rs = c.doQuery(query);
            while (rs.next()) 
            {
                v.add(rs.getString(1));
            }
            rs.close();
            c = null;
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        return v;
    }

    /*
    Method: sendMessage
    	Sends MailMessage to list of all users.
    
    Parameters:
    	request - HttpServletRequest object
    	username - String SWIFT username
    */
    private void sendMessage(HttpServletRequest request, String username) 
    {
        String subject="";
        subject = request.getParameter("subject");
        String body="";
        body = request.getParameter("body");
        Vector<String> users = listUsers();
        Enumeration<String> u = users.elements();
        while (u.hasMoreElements ()) 
        {
            MailMessage m = new MailMessage();
            m.sendEmailToUser((String)u.nextElement(), subject, body);
        }
    }
}
