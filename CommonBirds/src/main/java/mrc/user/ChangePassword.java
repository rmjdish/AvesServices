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
import java.security.*;
import java.util.logging.Logger;

import mrc.db.ConnectDB;
import mrc.util.Page;
import mrc.util.PassStore;

/*
 Class: ChangePassword
 	Allows users to change their password
 */

public class ChangePassword extends HttpServlet 
{  
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
    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException 
    {
        PrintWriter out = response.getWriter();        
        HttpSession s = request.getSession();
        if (s == null || s.getAttribute("username") == null) {
        	request.getRequestDispatcher("/login").forward(request, response);
        }        

        changePWForm(out,s);
    } 

    /*
    Method: doPost
    	Handles the HTTP POST method.
    
    Parameters:
    	request - HttpServletRequest object
    	response - HttpServletResponse object
    	
    About:
    	This method can throw both ServletException and IOException
    */
    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException 
    {
        updateDetails( response,request);
    }

    /*
    Method: changePWForm
    	Displays the change password form.
    
    Parameters:
    	out - PrintWriter object
		s - HttpSession object
		
	About:
    	this method can throw IOException 
    */
    private void changePWForm(PrintWriter out, HttpSession s) throws IOException 
    {
		String accessLevel = s.getAttribute("accessLevel").toString();
        Page p=new Page("ChangePassword-changePWForm");
        String username=(String)s.getAttribute("username");   
        log.fine("Condor: Changing Password for user "+username);
        p.UserPage(out,"Changing Password For "+username,s);

    }

    /*
    Method: updateDetails
    	Updates database record with new password entered on form.
    
    Parameters:
    	request - HttpServletRequest object
    	response - HttpServletResponse object
    	
    About:
    	this method can throw IOException
    */
    private void updateDetails(HttpServletResponse response, HttpServletRequest request) throws IOException 
    {
        Page p=new Page("ChangePassword-PWUpdated");
        PrintWriter out = response.getWriter(); 
        HttpSession s = request.getSession();
		String accessLevel = s.getAttribute("accessLevel").toString();
        String username = s.getAttribute("username").toString();  

        log.fine(" Updated Password for user "+username);
        updateExistingUser(request.getParameter("password1").replaceAll("'", "''"), s);
        p.UserPage(out,"Password Updated For "+username,s);
    }

    /*
    Method: updateExistingUser
    	Updates existing user record using PassStore.
    
    Parameters:
    	s - HTTPSession object
    	password - String new password for user
    	
    See Also:
    	<PassStore>
    */    
    private void updateExistingUser(String password, HttpSession s)
    {
    	
        String SHA1pwd="";
        try
        {
        	SHA1pwd = PassStore.getPass(password);
        }
        catch(Exception e)
        {
        	 e.printStackTrace();
        }
        try 
        {
            String query = "update users set password=\'"+SHA1pwd+"\' " +                
                    "where username=\'"+s.getAttribute("username")+"\'";
            getServletContext().log(s.getAttribute("username").toString()+":"+query);
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


    /*
    Method: getServletInfo()
    	Returns a short description of the servlet.
    	
    Returns:
    	String description
    */
    @Override
	public String getServletInfo() 
    {
        return "This class is responsible for displaying a form to allow the user to change their password.";
    }
}
