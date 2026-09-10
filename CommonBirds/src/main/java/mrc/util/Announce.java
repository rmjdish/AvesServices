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

/**
 * @author philc
 *
 */
public abstract class Announce {
    protected static String verString = "Common Birds";
    protected static String iAm = "Unknown Host";

    
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
		if (os.contains("win")) {
			// System.getenv can return null so beware!
			String name = System.getenv("COMPUTERNAME");
			if (name != null) {
				iAm = name;
			}
		} else if (os.contains("nix") || os.contains("nux") || os.contains("mac os x")) {
			String name = System.getenv("HOSTNAME");
			if (name != null) {
				iAm = name;
			}
		}
    	return iAm.toLowerCase();
    }

}
