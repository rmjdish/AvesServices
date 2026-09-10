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

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import mrc.db.ConnectDB;
/* Class: MessageRelay
	Utility class that bundles up a range of utilities for relaying 
	messages from the user about adding	variables to baskets

 */
public class MessageRelay {
/*	Variables: MessageRelay
		messageSet - a hashtable of all defined messages from the messages table
		store - a buffer of messages accumulated during the course of some processing
		sofar - a list of all the messages accumulated so far
		log - a Java Util Logging object that outputs to specified Tomcat logs
*/
	private static HashMap<String, String> store = new HashMap<String, String>();
	private static ArrayList<String> sofar = new ArrayList<String>();
	private static HashMap<Integer, String> messageSet = new HashMap<Integer, String>();
	// private static final Logger log = Logger.getLogger(HostInfo.tell()+":"+SessMgr.class.getName());
	private static final Logger log = Logger.getLogger("mrc.user");
/* Constructor:
	Read all existing messages from the messages table into a Hashtable
*/
	public MessageRelay() {
		String queryvarsec = "SELECT messageId, mtext FROM messages";
		try {
        	ConnectDB con = new ConnectDB();
        	con.capture();
        	ResultSet messages = con.doQuery(queryvarsec);  // execute the query
        	while (messages.next()) {
        		// push each message into the hash table
        		messageSet.put(messages.getInt("messageId"), messages.getString("mtext"));
        	} 	// Now we should have all the messages
        	messages.close();
        	con.release(); 
        } 
        catch (Exception e){
        	log.severe(HostInfo.tell()+" MessageRelay: ERROR "+e.toString());
        }
	}

/*
	Function: display
		add a message to the buffer of messages to be displayed
*/	
	public void display (String key, int code) throws IOException {	
		// Now print the message corresponding to number msg
		log.fine(HostInfo.tell()+" MessageRelay: display key: "+key+" Msg: "+messageSet.get(code));
		this.addmsg(key, messageSet.get(code));
	}

/*
	Function: quiet
		Same as <display> above
*/	
	public void quiet (String key, int code) throws IOException {	
		// Now print the message corresponding to number msg
		log.fine(HostInfo.tell()+" MessageRelay: quiet key: "+key+" Msg: "+messageSet.get(code));
		this.addmsg(key, messageSet.get(code));
	}
	
/*	Function: addmsg
		Puts a single message into the buffer to be displayed
*/
	private void addmsg(String key, String msg) {
		final String nullMessage = "No Message";
		if (StringUtils.isAllBlank(msg))
			store.put(key, nullMessage);
		else 
			store.put(key, msg);
		log.fine(HostInfo.tell()+" MessageRelay: addmsg store: "+String.valueOf(store));

	}

/*
	Function: msgFlush
		Copies everything from the current store into the Class 
		property sofar and then clears the buffer
*/
	public ArrayList<String> msgFlush () {
	    log.fine(HostInfo.tell()+" MessageRelay: msgFlush: store:"+store.toString());
	    sofar.clear();
	    for (String key : store.keySet()) {
	    	sofar.add(key);
	    	sofar.add(store.get(key));
	    }
	    store.clear();
	    // There are now pairs of elements in a serial list:
	    // <var> <msg> <var> <msg>...
	    // remove duplicates by throwin out the first of a pair

	    return sofar;
	}

}