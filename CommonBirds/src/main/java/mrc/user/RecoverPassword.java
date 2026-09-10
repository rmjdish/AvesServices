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
import java.security.*;

import mrc.db.ConnectDB;
import mrc.smtp.MailMessage;
import mrc.util.*;

/*Class:
 * 	Allows users to change their registration details
 * 
 */

public class RecoverPassword extends HttpServlet 
{  
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// private static final Logger log = Logger.getLogger(HostInfo.tell()+":"+SessMgr.class.getName());
	private static final Logger log = Logger.getLogger("mrc.user");
	/** 
    * Handles the HTTP <code>POST</code> method.
    * @param request servlet request
    * @param response servlet response
    */
    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException 
    {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        HttpSession s = request.getSession();
        if (s == null || s.getAttribute("username") == null) {
        	request.getRequestDispatcher("/login").forward(request, response);
        }        
        String email=request.getParameter("email");
        String username=getUsername(email);
        if (username.equalsIgnoreCase(""))
        {
            userUnknown(out, s);
        }
        else
        {    
           userKnown(out,s);
        }
    }

   /** 
    * Update database record - unknown user exception.
    * @param out HTML PrintWriter 
    * @param s HTTPSession
    * @param p Non-authenticated page object
    */
    private void userUnknown(PrintWriter out, HttpSession s) throws IOException 
    {
        Page p = new Page("RecoverPassword-userUnknown");
        p.UserPage(out, "Authentication Failure", s);
    }

    private void userKnown(PrintWriter out, HttpSession s) throws IOException
    {
    	Page p = new Page("RecoverPassword-userKnown");
    	p.UserPage(out,"Password Failure", s);
    }
    
    
    
    /** 
    * Retrieves username from email.
    * @param email User email address 
    */    
    private String getUsername(String email)
    {
        String username="";
        try 
        {
            String query = "select username from users where email=\""+email+"\"";
            ConnectDB c=new ConnectDB();
            c.capture();
            ResultSet rs=c.doQuery(query);
            while (rs.next())
            {
                username=rs.getString(1);
            }
            rs.close();
            c.release();
        } 
        catch(Exception e) 
        {
            e.printStackTrace();
        } 
        return username;
    }

   
    /** 
    * Returns a short description of the servlet.
    */
    @Override
	public String getServletInfo() 
    {
        return "Short description";
    }
}
