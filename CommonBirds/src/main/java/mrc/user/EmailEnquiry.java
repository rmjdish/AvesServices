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
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import mrc.util.Page;
import mrc.db.ConnectDB;
import mrc.smtp.MailMessage;
import mrc.util.HostInfo;

/**
 * Allows administrator to send e-mail to all users
 * @author pwatters
 */
public class EmailEnquiry extends HttpServlet 
{   

	private static final long serialVersionUID = 1L;
	/** 
    * Handles the HTTP <code>GET</code> method.
    * @param request servlet request
    * @param response servlet response
    */
	private static final Logger log = Logger.getLogger(HostInfo.tell()+":"+ConnectDB.class.getName());
	
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException 
    {
        PrintWriter out = response.getWriter();        
        HttpSession s = request.getSession(true);
        s.setAttribute("accessLevel", "enquiry");
        emailForm(out,s);      
    } 

    /** 
    * Handles the HTTP <code>POST</code> method.
    * @param request servlet request
    * @param response servlet response
    */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException 
    {
        String subject="";
        subject = request.getParameter("subject");
        String email="";
        email = request.getParameter("email");
        String body="";
        body = request.getParameter("body");
        String name="";
        name = request.getParameter("name");
        String enquiryType="";
        enquiryType = request.getParameter("enquiryType");
        try
        {        
            sendMessage(subject, email, body, name, enquiryType);
        }
        catch (Exception se)
        {
        	log.severe(HostInfo.tell()+" EmailEnquiry: Error in doPost");
            se.printStackTrace();
        }        
    }

    /** 
    * Returns a short description of the servlet.
    */
    public String getServletInfo() {
        return "Short description";
    }

    /** 
    * Displays email form.
    * @param out HTML PrintWriter 
     * @throws IOException 
    */
    private void emailForm(PrintWriter out, HttpSession s) throws IOException 
    {
    	Map<String,Object> context = new HashMap<>();
    	ArrayList<String> results = new ArrayList<String>();
    	results = listEnquiryTypes();
		String accessLevel = s.getAttribute("accessLevel").toString();
    	Page p=new Page("EmailEnquiry-emailForm");
        context.put("enqTypes", results);
        p.extendPageContext(out, "NSHD Library Search", s, context);       	
    }

     /* Function: ArrayList
    * 		Lists all enquiry types.
    * 
    * Parameters:
    * 	out - HTML PrintWriter 
    */
    private ArrayList<String> listEnquiryTypes() 
    {
    	ArrayList<String> results = new ArrayList<String>();
        String query = "select value from enquirytype order by value asc";
        getServletContext().log("UNKNOWN"+":"+query);
        try 
        {
            ConnectDB c = new ConnectDB();
            ResultSet rs = c.doQuery(query);
            while (rs.next()) 
            {
                results.add(rs.getString(1));
            }
            rs.close();
            c = null;
        } 
        catch (Exception e) 
        {
            log.severe("EmailEnquiry: error obtainin enquiry types.");
            e.printStackTrace();
        }
        return results;
    }
    
    /*Function: sendMessage
    * 	Sends MailMessage to Director.
    * 
    * Parameters:
    * 	subject - Email subject
    * 	email - Email sender
    * 	body - Email body
    * 	name - Email name
    * 	enquiryType - Email enquiry type
    */
    private void sendMessage(String subject, String email, String body, String name, String enquiryType)
    {
        MailMessage m = new MailMessage();
        m.sendEmailToUser("director", subject, body, email);
        int r=recordEnquiry(subject, email, body, name, enquiryType);
        if (r==0)            
            log.warning(HostInfo.tell()+ " EmailEnquiry Error with: "+email);
    }
    
    /*Function: recordEnquiry
    * 	Records enquiry details in database.
    * 
    * Parameters:
    * 	subject - Email subject
    * 	email - Email sender
    * 	body - Email body
    * 	name - Email name
    * 	enquiryType - Email enquiry type
    */
    private int recordEnquiry(String subject, String email, String body, String name, String enquiryType) 
    {
        int r=0;
        String query="insert into enquiries values " +
            "(null, \'"+subject.replaceAll("'", "''")+"\', " + "\'"+body.replaceAll("'", "''")+"\', now(), " +
            "\'"+email.replaceAll("'", "''")+"\', \'"+name.replaceAll("'", "''")+"\', \'"+enquiryType.replaceAll("'", "''")+"\')";
        try 
        {
            ConnectDB c = new ConnectDB();
            r = c.doUpdate(query);
            c = null;
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        return r;
    } 
}
