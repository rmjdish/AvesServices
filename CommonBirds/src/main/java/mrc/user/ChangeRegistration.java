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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import mrc.util.Page;
import mrc.db.ConnectDB;
import mrc.util.HostInfo;

/*
 Class: ChangeRegistration
 	Allows users to change their registration details
 
*/
public class ChangeRegistration extends HttpServlet 
{  
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// private static final Logger log = Logger.getLogger(HostInfo.tell() + ":" + ChangeRegistration.class.getName());
	private static final Logger log = Logger.getLogger("mrc.user");
	/*
	Method: doGet 
    	Handles the HTTP GET method.
    
    Parameters:
    	request  - HttpServletRequest
	 	response - HttpServletResponse
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
        doChange(out, s);
    } 

    /*
    Method: doPost
    	Handles the HTTP POST method.
    
    Parameters:
    	request -  HttpServletRequest
    	response - HttpServletResponse
    */
    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException 
    {
        PrintWriter out = response.getWriter();
        HttpSession s = request.getSession();
		String accessLevel = s.getAttribute("accessLevel").toString();        
        String username = s.getAttribute("username").toString();
        String email = request.getParameter("email").replaceAll("'", "''").toString();
        boolean e=checkIfEmailRegistered(email, username);
        boolean at=checkForAtSign(email);
        String error="";
        if (e)
        {
            error+="The email address you entered is already taken. Please choose another.";
        }
        if (!at)
        {
            error+="The e-mail address you entered does not contain an at symbol or at least one period. Please enter another.";
        }
        if (error.equalsIgnoreCase(""))
        {   // Output thanks and execute SQL Update
            updateExistingUser(request.getParameter("firstName").replaceAll("'", "''"), 
            		request.getParameter("lastName").replaceAll("'", "''"), 
            		request.getParameter("academicAffiliation").replaceAll("'", "''"), 
            		request.getParameter("address1").replaceAll("'", "''"), 
            		request.getParameter("address2").replaceAll("'", "''"), 
            		request.getParameter("city").replaceAll("'", "''"), 
            		request.getParameter("postcode").replaceAll("'", "''"), 
            		request.getParameter("county").replaceAll("'", "''"), 
            		request.getParameter("phone").replaceAll("'", "''"), 
            		request.getParameter("email").replaceAll("'", "''"), s);
            Page p=new Page("ChangeRegistration-changeRegistrationAcknowledge");  
            log.fine("Condor: Registration Details changed for user "+username);
            p.UserPage(out,"Changed Registration Details For "+username,s);
        }
        else
        {   // No way out with an error! Go round again
            displayForm(out, s, 
                request.getParameter("firstName").replaceAll("'", "''"), 
                request.getParameter("lastName").replaceAll("'", "''"), 
                request.getParameter("academicAffiliation").replaceAll("'", "''"), 
                request.getParameter("address1").replaceAll("'", "''"), 
                request.getParameter("address2").replaceAll("'", "''"), 
                request.getParameter("city").replaceAll("'", "''"), 
                request.getParameter("county").replaceAll("'", "''"), 
                request.getParameter("postcode").replaceAll("'", "''"), 
                request.getParameter("phone").replaceAll("'", "''"), 
                request.getParameter("email").replaceAll("'", "''"));            
        }    
    }

    /*
    Method: checkForAtSign
    	Check for at sign and period in email
    
    Parameter:
    	e - String candidate e-mail address
    */   
    private boolean checkForAtSign(String e)
    {
        boolean match=false;
        if (e.contains("@")&&e.contains("."))
            match=true;
        return match;
    }
    
    /*
    Method: checkIfEmailRegistered
    	Check if username and email is already present in users table.
    	
    Returns:
    	True if both present, False otherwise
    	
   	Parameters:
    	username - String SWIFT user name
    	email    - String user email address 
    */   
    private boolean checkIfEmailRegistered(String email, String username)
    {
        boolean exists=false;
        try 
        {
            String query = "select username from users where email=\'"+email+"\' and username != \'"+username+"\'";
            getServletContext().log(username+":"+query);
            ConnectDB c=new ConnectDB();
            c.capture();
            ResultSet rs=c.doQuery(query);
            if (rs.next())
            {
               exists=true;
            }
            rs.close();
            c.release();
        } 
        catch(Exception e) 
        {
        	log.severe(" ChangeRegistration: error while executing query. ");
            e.printStackTrace();
        } 
        return exists;
    }
    
    /*
    Method: displayForm
    	Displays the change details form populated with existing data.
    	
    Parameters:
    	out         - PrintWriter 
    	s           - HttpSession
     	firstName   - String first name
     	lastName    - String last name
    	affiliation - String institutional affiliation
     	address1    - String first line of address
     	address2    - String second line of address
     	city        - String city
     	county      - String county
    	postcode    - String postcode
     	phone       - String phone number
     	email       - String email address   
     
     About:
     	Can throw IOException 
    */
    private void displayForm(PrintWriter out, HttpSession s, String firstName, String lastName, String affiliation, 
            String address1, String address2, String city, String county, String postcode, 
            String phone, String email) throws IOException 
    {
        Page p=new Page("ChangeRegistration-changeRegistrationForm");
        String username=s.getAttribute("username").toString();   
		String accessLevel = s.getAttribute("accessLevel").toString();
        log.fine("Condor: Changing Registration Details for user "+username);
        Map<String, Object> context = new HashMap<>();
        context.put("firstName", firstName);
        context.put("lastName", lastName);
        context.put("affiliation", affiliation);
        context.put("address1", address1);
        context.put("address2", address2);
        context.put("city", city);
        context.put("county", county);
        context.put("postcode", postcode);
        context.put("phone", phone);
        context.put("email", email);
        p.extendPageContext(out,"Changing Registration Details",s, context);
        
    }


    /*
    Method: updateDetails
    	Updates database record with revised registration data entered on form.

	Parameters:
    	response - HttpServletResponse
    	request  - HttpServletRequest
    */
    @SuppressWarnings("unused")
	private void updateDetails(HttpServletResponse response, HttpServletRequest request) throws IOException 
    {

    }


    /*
    Method: updateExistingUser
    	Updates existing user record.
    
    Parameters:
     	firstName           - String first name
     	lastName            - String last name
     	academicAffiliation - String institutional affiliation
     	address1            - String address line 1
     	address2            - String address line 2
    	city                - String city
    	county              - String county
     	postcode            - String postcode
    	phone               - String phone number
    	email               - String email address   
    	s                   -  HTTPSession
    */    
    private void updateExistingUser(String firstName, String lastName,
            String academicAffiliation, String address1, String address2, String city, String postcode,
            String county, String phone, String email, HttpSession s)
    {
        try 
        {
            String query = "update users set firstName=\'"+firstName+"\', " +
                "lastName=\'"+lastName+"\', " +
                "affiliation=\'"+academicAffiliation+"\', " +
                "address1=\'"+address1+"\', " +
                "address2=\'"+address2+"\', " +
                "city=\'"+city+"\', " +
                "postcode=\'"+postcode+"\', " +
                "county=\'"+county+"\', " +
                "phone=\'"+phone+"\', " +
                "email=\'"+email+"\' " +                
                "where username=\'"+s.getAttribute("username")+"\'";
            String username = s.getAttribute("username").toString();
            log.info(HostInfo.tell()+" updateExistingUser: "+username+" with query: "+query);
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
    Method: doChange
    	Populate existing details of current user registration from database
    	and call displayForm to allow user editing
    	
    Parameters:
     	out - PrintWriter 
     	s   - HttpSession
     	
    About:
     	Can throw IOException 
    */
    private void doChange(PrintWriter out, HttpSession s) throws IOException
    {
        String firstName="";
        String lastName="";
        String affiliation="";
        String address1="";
        String address2="";
        String city="";
        String postcode="";
        String county="";
        String phone="";
        String email="";
        
        try 
        {
            String query = "select firstName, lastName, affiliation, address1, address2, city, postcode, county, phone, email from users where username=\""+s.getAttribute("username")+"\"";
            getServletContext().log(s.getAttribute("username").toString()+":"+query);
            ConnectDB c=new ConnectDB();
            c.capture();
            ResultSet rs=c.doQuery(query);
            while (rs.next())
            {
                firstName=rs.getString(1);
                lastName=rs.getString(2);
                affiliation=rs.getString(3);
                address1=rs.getString(4);
                address2=rs.getString(5);
                city=rs.getString(6);
                postcode=rs.getString(7);
                county=rs.getString(8);
                phone=rs.getString(9);
                email=rs.getString(10);               
            }
            rs.close();
            c.release();
        } 
        catch(Exception e) 
        {
            e.printStackTrace();
        } 
        displayForm(out, s, firstName, lastName, affiliation, address1, address2, city, county, postcode, phone, email);
    }

    /*
    Method: getServletInfo
    	Returns a short description of the servlet as a String.
    */
    @Override
	public String getServletInfo() 
    {
        return "This Servlet allows the user to update their contact details.";
    }
}
