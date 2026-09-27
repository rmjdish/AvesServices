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
	private static HashMap<String, String> categoryStore = new HashMap<String, String>();
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
		// Existing 2-arg version, unchanged for LoadBasket.java and
		// Trolley.java (the other two callers of this class) - defaults
		// to a generic "added" category rather than requiring every
		// caller to be updated.
		this.display(key, code, "added");
	}

/*
	Function: display (with category)
		Same as <display> above, but also records a category
		("restricted", "linked-restricted", "added", "linked-added")
		against this key, so callers that need to distinguish why a
		message was shown - not just what it says - can do so. Added
		specifically because a linked variable that turned out to be
		restricted was getting its correct restriction message
		silently overwritten by a later, unconditional "also added"
		call using the same key - the category makes it possible to
		fix that at the call site instead of guessing from the
		message text.
*/
	public void display (String key, int code, String category) throws IOException {	
		log.fine(HostInfo.tell()+" MessageRelay: display key: "+key+" Msg: "+messageSet.get(code)+" Category: "+category);
		this.addmsg(key, messageSet.get(code), category);
	}

/*
	Function: displayText (with category)
		Same as <display> above, but takes the message text directly
		rather than looking it up by messageId - for messages built
		up in Java (e.g. prefixing a stored restriction message with
		"This is a linked variable but restricted") rather than
		stored verbatim in the messages table.
*/
	public void displayText (String key, String text, String category) throws IOException {
		log.fine(HostInfo.tell()+" MessageRelay: displayText key: "+key+" Msg: "+text+" Category: "+category);
		this.addmsg(key, text, category);
	}

/*
	Function: quiet
		Same as <display> above
*/	
	public void quiet (String key, int code) throws IOException {	
		this.quiet(key, code, "added");
	}

/*
	Function: quiet (with category)
		Same as <display> (with category) above.
*/	
	public void quiet (String key, int code, String category) throws IOException {	
		log.fine(HostInfo.tell()+" MessageRelay: quiet key: "+key+" Msg: "+messageSet.get(code)+" Category: "+category);
		this.addmsg(key, messageSet.get(code), category);
	}
	
/*	Function: addmsg
		Puts a single message into the buffer to be displayed
*/
	private void addmsg(String key, String msg, String category) {
		final String nullMessage = "No Message";
		if (StringUtils.isAllBlank(msg))
			store.put(key, nullMessage);
		else 
			store.put(key, msg);
		categoryStore.put(key, category);
		log.fine(HostInfo.tell()+" MessageRelay: addmsg store: "+String.valueOf(store));

	}

/*
	Function: msgFlush
		Copies everything from the current store into the Class 
		property sofar and then clears the buffer. Now returns triples
		(<var> <msg> <category> <var> <msg> <category>...) rather than
		pairs, so callers that need the category (the Basket
		Management confirmation page) can group/colour/count/filter by
		it, without affecting anything that only reads the first two
		of every three entries the way it always did.
*/
	public ArrayList<String> msgFlush () {
	    log.fine(HostInfo.tell()+" MessageRelay: msgFlush: store:"+store.toString());
	    sofar.clear();
	    for (String key : store.keySet()) {
	    	sofar.add(key);
	    	sofar.add(store.get(key));
	    	sofar.add(categoryStore.getOrDefault(key, "added"));
	    }
	    store.clear();
	    categoryStore.clear();
	    // There are now triples of elements in a serial list:
	    // <var> <msg> <category> <var> <msg> <category>...

	    return sofar;
	}

}