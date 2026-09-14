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

import java.util.logging.Logger;


/**
 * @author philc
 *
 */
public abstract class Announce {
    protected static String verString = "Common Birds";
    protected static String iAm = "Unknown Host";
    private static final Logger log = Logger.getLogger("mrc.user");

    
    /*
     * Method: tell
     * 	Returns the version identifier of this implementation
     * 
     * Returns:
     * 	verString 
     */    
    public static String tell() {
    	return verString;
    }
    
    
    /*
     * Function: chezmoi
     * 	Returns the hostname of the server running Condor/Swift
     * 
     * Returns:
     * 	urlString in lower case!
     */

    public static String chezmoi() {
	String os = System.getProperty("os.name").toLowerCase();
	String name = null;

	if (os.contains("win")) {
	    name = System.getenv("COMPUTERNAME");
	} else {
	    // 1. Try environment variable first
	    name = System.getenv("HOSTNAME");
        
	    // 2. Fallback to JVM MXBean if HOSTNAME is null
	    if (name == null || name.isEmpty()) {
		String jvmName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
		if (jvmName != null && jvmName.contains("@")) {
		    name = jvmName.split("@")[1];
		}
	    }
	}

	if (name != null) {
	    iAm = name.toLowerCase();
	    //log.info("Resolved Host Name: " + iAm);
	} else {
	    log.warning("Could not determine hostname, defaulting to 'Unknown Host'");
	}
    
	return iAm;
    }
    
}
