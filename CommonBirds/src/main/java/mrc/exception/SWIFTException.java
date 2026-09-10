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


package mrc.exception;
import java.util.logging.Logger;

import mrc.smtp.MailMessage;
import mrc.util.HostInfo;

/**
 * Handles all customised exceptions for SWIFT. 
 * @author LHA SST
 */
public class SWIFTException extends Exception
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -5077335107520850969L;
	private static final Logger log = Logger.getLogger(HostInfo.tell()+":"+SWIFTException.class.getName());
	private String error;

    /** 
    * Default error code when not provided by calling method.
    */
    public SWIFTException()
    {
        super();             
        this.error = HostInfo.tell()+" Indeterminate Error";
    }
  
    /** 
    * Default error code when provided by calling method.
    * @param _error error code when provided by calling method
    */
    public SWIFTException(String _error)
    {
        super(_error);     
        this.error = _error;  
    }

    /** 
    * Sends email to manager with reported error and logs to stdout.
    */
    public String getError()
    {
        try
        {        
            MailMessage m = new MailMessage();
            m.sendEmailToUser("manager", this.error);
            log.severe(" error: "+this.error);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }        

    return this.error;
    }
}