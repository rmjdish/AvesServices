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
        HttpSession s = request.getSession();
        String user = request.getParameter("user");
        if (user == null || user.isEmpty()) {
            // No user chosen yet - show the filterable/searchable list of
            // every user, each with its own Reset Password link, rather
            // than the single dropdown this page used before. The table
            // is built directly in Java and passed through as a finished
            // HTML string (see Page.rawTablePage() for why) rather than
            // grouped in a Pebble loop, after two different attempts at
            // the latter each failed in a different way.
            Page p = new Page("ResetPassword-userList");
            String tableHtml = buildUserTableHtml(getUserList(), s.getId());
            p.rawTablePage(out, "Reset a User's Password", s, tableHtml);
        } else {
            // A specific user was chosen from the list - show just the
            // password-entry form for them, with no dropdown at all. The
            // chosen username is passed via setResults(), the same proven
            // pattern updateDetails() below already uses for a single
            // value, rather than assuming how request parameters reach
            // the template directly.
            Page p = new Page("ResetPassword-ResetPassword");
            ArrayList<String> chosen = new ArrayList<String>();
            chosen.add(user);
            p.setResults(chosen);
            p.UserPage(out, "Change Password for " + user, s);
        }
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
    private ArrayList<String[]> getUserList() 
    {
        // Widened from username-only so the list can show a name and
        // institution alongside each username, rather than a bare list of
        // usernames with no other context. Returned as one String[4] per
        // user (username, firstName, lastName, affiliation) - the table
        // HTML is built directly in Java from this (buildUserTableHtml()
        // below), not grouped in a Pebble loop; see Page.rawTablePage()
        // for why.
        String query = "select username, firstName, lastName, affiliation from users where username != \'director\'";
    	ArrayList<String[]> results = new ArrayList<String[]>();
        getServletContext().log("manager:"+query);
        try 
        {
            ConnectDB c = new ConnectDB();
            c.capture();
            ResultSet rs = c.doQuery(query);
            while (rs.next()) 
            {
                results.add(new String[]{ rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4) });
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
     Method: buildUserTableHtml
     	Builds the Reset Password user list table (Username/First Name/
     	Last Name/Institution plus a Reset Password link) directly as an
     	HTML string - see the comment on Page.rawTablePage() for why.

     Parameters:
     	users - one String[4] per user: {username, firstName, lastName, affiliation}
     	sessionID - the current session's ID, needed for each row's own &id= parameter
    */
    private String buildUserTableHtml(ArrayList<String[]> users, String sessionID) {
        StringBuilder html = new StringBuilder();
        if (users.isEmpty()) {
            html.append("<p>There are no users to list.</p>\n");
            return html.toString();
        }
        html.append("<input type=\"text\" id=\"userSearchBox\" class=\"owl-user-search\" placeholder=\"Search by username, name, or institution&hellip;\">\n");
        html.append("<table class=\"owl-table\" id=\"userResetTable\">\n");
        html.append("<tr><th>Username</th><th>First Name</th><th>Last Name</th><th>Institution</th><th class=\"skip-filter\">Reset Password</th></tr>\n");
        for (String[] u : users) {
            String uname = escapeHtml(u[0]);
            String fname = escapeHtml(u[1]);
            String lname = escapeHtml(u[2]);
            String affil = escapeHtml(u[3]);
            html.append("<tr><td>").append(uname).append("</td><td>").append(fname).append("</td><td>")
                .append(lname).append("</td><td>").append(affil).append("</td>")
                .append("<td><a href=\"resetPassword?id=").append(sessionID).append("&user=").append(uname).append("\">Reset Password</a></td></tr>\n");
        }
        html.append("</table>\n");
        html.append("<script src=\"ddtf.js\"></script>\n");
        html.append("<script>\n");
        html.append("$(document).ready(function () {\n");
        html.append("  $('#userResetTable').ddTableFilter();\n");
        html.append("  var searchBox = document.getElementById('userSearchBox');\n");
        html.append("  if (searchBox) {\n");
        html.append("    searchBox.addEventListener('keyup', function () {\n");
        html.append("      var term = searchBox.value.trim().toLowerCase();\n");
        html.append("      var rows = document.querySelectorAll('#userResetTable tr');\n");
        html.append("      for (var i = 1; i < rows.length; i++) {\n");
        html.append("        var text = rows[i].textContent.toLowerCase();\n");
        html.append("        if (term === '' || text.indexOf(term) !== -1) { rows[i].classList.remove('owl-search-hidden'); }\n");
        html.append("        else { rows[i].classList.add('owl-search-hidden'); }\n");
        html.append("      }\n");
        html.append("    });\n");
        html.append("  }\n");
        html.append("});\n");
        html.append("</script>\n");
        return html.toString();
    }

    /*
     Method: escapeHtml
     	Basic HTML-escaping before user-sourced values (name, affiliation)
     	go into a hand-built HTML string - the same precaution already
     	used in Basket.java's own table-building method.
    */
    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
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
