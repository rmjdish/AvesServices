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


package mrc.util;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

import mrc.db.ConnectDB;
/*
 * Function: SecModel
 * 	Implements a three level security model based on integer levels.  Used by
 * 	servlets choosing whether to add variables/datasets to baskets/file downloads.
 */
public class SecModel {
	private static final Logger log = Logger.getLogger("mrc.user");
    private static String queryvarsec = "SELECT LOWER(Name) as Name, secLevel, messageId FROM varsecmod";
    // Having written the queries they need to be executed inside a try ... catch block
    // Use a hash table to store the Variable name/sec level pairs
    /*
     * Variable: secdetails
     * 	A static (one per class) map attribute which is used to store variable to security level values 
     */
    private static Hashtable<String, Integer> secdetails = new Hashtable<String, Integer>();
    // Also use a hash table to store the Variable name/messageNum pairs
    /*
     * Variable: secmessage
     * 	A static (one per class) map attribute which is used to store variable to message values
     */
    private static Hashtable<String, Integer> secmessage = new Hashtable<String, Integer>();
    private static String lastCheck = "Initial State";
    // Default constructor next
    /*
     * Constructor: SecModel
     */
    public SecModel () {
        try {
        	secdetails.clear();
        	secmessage.clear();
        	ConnectDB con = new ConnectDB();
        	con.capture();
        	ResultSet vseclevels = con.doQuery(queryvarsec);  // execute the SQL query
        	int recnt = 0;
        	while (vseclevels.next()) {
        		// push each Variable element security level into the hashtable secdetails
        		secdetails.put(vseclevels.getString("Name"), vseclevels.getInt("secLevel"));
        		secmessage.put(vseclevels.getString("Name"), vseclevels.getInt("messageId"));
        		recnt += 1;
        	}
        	// Now we should have all the security vars
        	vseclevels.close();
        	con.release();
        	log.info(HostInfo.tell()+" Number of variables hashed from varsecmod: "+String.valueOf(recnt));
        }
        catch (Exception e){
        	e.printStackTrace();
        }
    	
    }
    
    /*
     * Function: checkAuth
     * 	Checks whether the security level associated with a variable is less than 
     * 	a specific security level
     *
     * Parameters:
     * 	seclevel - a specified security level
     * 	vname - a variable name from the repository
     * 
     * Returns:
     * 	True if variable has no associated security level or the associated security level is greater
     * 	than or equal to a specified value.  False otherwise
     */
    public boolean checkAuth(int seclevel, String vname) {
    	// if the Variable is in the varsecmod table and ...
    	// the user's security level is gte than the Variable's -> ok
    	if (secdetails.containsKey(vname) && seclevel >= secdetails.get(vname) ) {
    		lastCheck = "checkAuth: var "+
    				vname+
    				" var secLevel: "+
    				secdetails.get(vname).toString()+
    				" user secLevel: "+
    				String.valueOf(seclevel);
    		log.info(HostInfo.tell()+" "+lastCheck);
    		return true;
    	} 
    	// if the Variable is in the varsecmod table 
    	// but the user's security level is less than the Variable's -> fail
    	else if (secdetails.containsKey(vname) && seclevel < secdetails.get(vname) ) {
    		lastCheck = "checkAuth: var "+
    				vname.toLowerCase()+ // the variable name key
    				" var secLevel: "+
    				secdetails.get(vname).toString()+ // the secLevel value
    				" user secLevel: "+
    				String.valueOf(seclevel);
    		log.info(HostInfo.tell()+" "+lastCheck);
    		return false;
    	}
    	// it's not in the varsecmod table so -> ok
    	else {
    		lastCheck = "checkAuth: var "+vname+" not matched in hashed varsecmod array.";
    		log.info(HostInfo.tell()+" "+lastCheck);
    		return true;
    	}
    }
    
    /*
     * Function: isOpen
     * 	Checks whether a variable is not in the varsecmod table or, is in it but has a 
     * 	security level of zero
     * 
     * Parameters:
     * 	vname - a variable name from the repository
     * 
     * Returns:
     * 	true if the Variable is either not in the varsecmmod table or is in it with a secLevel of 0
     */
    public boolean isOpen(String vname) {
    	// return true if the Variable is either not in the varsecmmod table or is in it with a secLevel of 0
    	if (secdetails.containsKey(vname)) {
    		// variable is in hashed array of varsecmod table 
    		if (secdetails.get(vname) == 0) {
    			lastCheck = "isOpen: var "+
    			vname+
    			" is in hashed varsecmod but has 0 secLevel";
    			log.info(HostInfo.tell()+" "+lastCheck);    		
    			return true;
    		} else {
    			lastCheck = "isOpen: var "+
    			vname+
    			" not in hashed varsecmod, secLevel assumed to be 0";
    			log.info(HostInfo.tell()+" "+lastCheck);    		
    			return false;
    		}
    	} else {
    		return true;
    	}
    }
    
    public String debug() {
    	return lastCheck;
    }
    
    /*
     * Function: failAuth
     * 	Returns the opposite of checkAuth
     * 
     * Parameters:
     * 	seclevel - a specified security level
     * 	vname - a variable name from the repository
     * 
     * Returns:
     * 	NOT checkAuth(seclevel,vname)
     * 
     * See Also:
     * 	<checkAuth>
     */
    public boolean failAuth(int seclevel, String vname) {
    	return !this.checkAuth(seclevel, vname);
    }
    
    /*
     * Function: hasMSG
     * 	Checks whether a variable name appears in the hashed message table
     * 
     * Parameters:
     * 	vname - a variable name from the repository
     * 
     * Returns:
     * 	True if secmessage.containsKey(vname)
     * 
     */
    public boolean hasMsg(String vname) {
    	return secmessage.containsKey(vname);
    }
    
    /*
     * Function: getMsgId
     * 	Returns the integer message id associated with a variable in the repository or zero.
     * 
     * Parameters:
     * 	vname - a variable name from the repository
     * 
     * Returns:
     * 	Integer value 0..Max Message Id
     * 
     */
    public int getMsgId(String vname) {
    	if (secmessage.containsKey(vname) ) {
    		return secmessage.get(vname);
    	} else {
    		return 0;
    	}
    }

}