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


package mrc.smtp;

import java.sql.*;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.mail.Message;
import java.util.*;
import java.util.logging.Logger;
import java.io.*;

import mrc.db.ConnectDB;
import mrc.util.HostInfo;
import mrc.util.Util;

/*
 * Class: MailMessage
 * 	Provides mail sending facilities to Swift.  
 */
public class MailMessage 
{    

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(MailMessage.class.getName());
	
	private static String mailHost = null;
	private static String mailSender = null;
	private static String sysVersion = null;
	
	
	public MailMessage()
	{
        ResourceBundle bdl = null;
		try {
			bdl = new PropertyResourceBundle(new FileInputStream(Util.dnget(this) + "/smtp.properties"));
		} catch (FileNotFoundException e) {
			log.severe(HostInfo.tell()+" MailMessage: Error cannot find smtp.properties file.");
			log.severe(HostInfo.tell()+" Error: "+e.getMessage());
		} catch (IOException e) {
			log.severe(HostInfo.tell()+" MailMessage: Error reading smtp.properties file.");
			log.severe(HostInfo.tell()+ " Error: "+e.getMessage());
		}
        mailHost   = bdl.getString("mailHost");
        mailSender = bdl.getString("mailSender");
        sysVersion = bdl.getString("sysVersion");

	}
   /*
    * Function: sendEmailToUser
    * 	Finds email host information from 'smtp.properties' file and uses this to 
    * 	send emails to Swift users and admin staff
    * 
    * Parameters:
    * 	userName - the Swift user name of the mail recipient
    * 	body - the text of the message to be sent 
    */
    public void sendEmailToUser(String userName, String body)
    {
        try
        {
          Properties props = System.getProperties();
          props.put("mail.smtp.host", mailHost);
          Session session = Session.getDefaultInstance(props, null);
          Message msg = new MimeMessage(session);
          msg.setFrom(new InternetAddress(mailSender));
          msg.setRecipients(Message.RecipientType.TO,
          InternetAddress.parse(lookupEmailAddress(userName), false));
          msg.setSubject("["+sysVersion+"] SWIFT System Message");
          msg.setText(body);
          msg.setHeader("X-Mailer", "GMail");
          msg.setSentDate(new java.util.Date());
          //***** UNCOMMENT THE LINE BELOW IN PRODUCTION *****
//          Transport.send(msg);
        }
        catch (Exception ex)
        {
          log.severe(HostInfo.tell()+" MailMessage: "+ex.getMessage());
        }
    }

    /*
     * Function: sendEmailToUser
     * 	Finds email host information from 'smtp.properties' file and uses this to 
     * 	send emails to Swift users and admin staff
     * 
     * Parameters:
     * 	userName - the Swift user name of the mail recipient
     * 	subject - the Subject line of the email to be sent
     * 	body - the text of the message to be sent 
     * 	email - the From field of the message to be sent
     */
    public void sendEmailToUser(String userName, String subject, String body, String email)
    {
        try
        {
          Properties props = System.getProperties();
          props.put("mail.smtp.host", mailHost);
          Session session = Session.getDefaultInstance(props, null);
          Message msg = new MimeMessage(session);
          msg.setFrom(new InternetAddress(email));
          msg.setRecipients(Message.RecipientType.TO,
          InternetAddress.parse(lookupEmailAddress(userName), false));
          msg.setSubject("["+sysVersion+"]"+ subject);
          msg.setText(body);
          msg.setHeader("X-Mailer", "GMail");
          msg.setSentDate(new java.util.Date());
         
          //***** UNCOMMENT THE LINE BELOW IN PRODUCTION *****
          // Transport.send(msg);
        }
        catch (Exception ex)
        {
        	log.severe(HostInfo.tell()+" MailMessage: sendEmailToUser error sending mail (4 arg form).");
        	log.severe(HostInfo.tell()+" Error: "+ex.getMessage());
        }
    }

    /*
     * Function: sendEmailToUser
     * 	Finds email host information from 'smtp.properties' file and uses this to 
     * 	send emails to Swift users and admin staff
     * 
     * Parameters:
     * 	userName - the Swift user name of the mail recipient
     *  subject - the Subject line of the email to be sent
     * 	body - the text of the message to be sent 
     */
    public void sendEmailToUser(String userName, String subject, String body)
    {
        try
        {
          Properties props = System.getProperties();
          props.put("mail.smtp.host", mailHost);
          Session session = Session.getDefaultInstance(props, null);
          Message msg = new MimeMessage(session);
          msg.setFrom(new InternetAddress(mailSender));
          msg.setRecipients(Message.RecipientType.TO,
          InternetAddress.parse(lookupEmailAddress(userName), false));
          msg.setSubject("["+sysVersion+"]"+ subject);
          msg.setText(body);
          msg.setHeader("X-Mailer", "GMail");
          msg.setSentDate(new java.util.Date());
          //***** UNCOMMENT THE LINE BELOW IN PRODUCTION *****
          // Transport.send(msg);
        }
        catch (Exception ex)
        {
        	log.severe(HostInfo.tell()+" MailMessage: sendEmailToUser: Error sending mail (3 arg form)");
        	log.severe(HostInfo.tell()+" Error: "+ex.getMessage());
        }
    }

   /*
    * Function: lookupEmailAddress
    * 	Query the Swift 'users' table to find the email address corresponding to a given Swift user.
    * 
    * Parameter:
    * 	userName - the Swift user name to be searched for
    */
    private String lookupEmailAddress(String userName)
    {
        String emailAddress="";
        String query = "select email from "+sysVersion+".users where username =\'"+userName+"\'";
        System.out.println(query);
        try 
        {
            ConnectDB c = new ConnectDB();
            c.capture();
            ResultSet rs=c.doQuery(query);
            while (rs.next())
            {
                emailAddress=rs.getString(1);
            }
            rs.close();
            c.release();
        }
        catch(SQLException e) 
        {
        	log.severe(HostInfo.tell()+" MailMessage: lookupEmailAddress: Error getting users from database in "+sysVersion);
            log.severe(HostInfo.tell()+" Error: "+e.getMessage());
        } 
        return emailAddress;
    }

}
