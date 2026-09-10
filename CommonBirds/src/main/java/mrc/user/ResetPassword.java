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
import java.util.ArrayList;
import java.util.logging.Logger;
import java.io.*;

import mrc.db.ConnectDB;
import mrc.user.PassStore;
import mrc.util.Page;

/**
 * Allows the director to reset the password of a user
 * @author LHA SST
 */

public class ResetPassword extends HttpServlet 
{  
    /**
	 * 
	 */
	private static final long serialVersionUID = -3029009241278251943L;
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
        Page p=new Page("ResetPassword-ResetPassword");
        HttpSession s = request.getSession();
        p.setResults(getUserList());
        p.UserPage(out, "Change Password", s);
    } 

    /** 
    * Handles the HTTP <code>POST</code> method.
    * @param request servlet request
    * @param response servlet response
    */
    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException 
    {
        updateExistingUser(request.getParameter("username").replaceAll("'", "''"), 
        		request.getParameter("password1").replaceAll("'", "''"));
        updateDetails(request,response);
    }

    /** 
    * Displays the reset password form.
    * @param out HTTP output
    */

    /** 
    * Lists all users except Director.
    * @param out HTML PrintWriter 
    * @param username Username
    */
    private ArrayList<String> getUserList() 
    {
        String query = "select username from users where username != \'director\'";
    	ArrayList<String> results = new ArrayList<String>();
        getServletContext().log("manager:"+query);
        try 
        {
            ConnectDB c = new ConnectDB();
            c.capture();
            ResultSet rs = c.doQuery(query);
            while (rs.next()) 
            {
                results.add(rs.getString(1));          }
            rs.close();
            c.release();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        return results;
    }

    /** 
    * Updates database record with new password entered on form.
    * @param request servlet request
    * @param response servlet response
    */
    private void updateDetails(HttpServletRequest request, HttpServletResponse response) throws IOException 
    {
        PrintWriter out = response.getWriter();
    	Page p = new Page("ResetPassword-passwordReset");
    	ArrayList<String> results = new ArrayList<String>();
    	results.add(request.getParameter("username"));
    	p.setResults(results);
        HttpSession s = request.getSession();
        p.UserPage(out, "Password Has Been Reset", s);
    }

    /** 
    * Updates existing user record.
    * @param username SWIFT username
    * @param password New password
    */    
    private void updateExistingUser(String username, String password)
    {
        try 
        {
            String SHA1pwd=PassStore.getPass(password);
            String query = "update users set password=\'"+SHA1pwd+"\' " +                
                    "where username=\'"+username+"\'";
            getServletContext().log(username+":"+query);
            ConnectDB c=new ConnectDB();
            c.capture();
            c.doUpdate(query);
            c.release();
        } 
        catch(Exception e) 
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
        return "This servlet allows an admin user to reset a user's password";
    }
}
