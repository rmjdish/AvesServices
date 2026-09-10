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
/*
Class: Util 
	Collection of Swift/Jay utility functions.


See Also: 
	<ConnectDB>, <ConnectionManager>
*/

public final class Util {

	
	private Util () {
		
	}
	
	/*
	 Function: mainApp
	  	Returns the name of the application calling this method
	  	as a string.
	  	Depends on HostInfo being correctly set for each app.
	 
	  Returns:
	  	String name of the application
	 */
    
	public static String mainApp() {
		String db = null;
		String whoami = HostInfo.tell();
		if (whoami.indexOf("Swift") >= 0) {
			return "Swift";
		} else if (whoami.indexOf("Jay") >= 0) {
			return "Jay";
		} else if (whoami.indexOf("Condor") >= 0) {
			return "Condor";
		} else if (whoami.indexOf("Swallow") >= 0) {
			return "Swallow";
		} else {
			return "Other";
		}
	}    
	
	/*
	 Function: wrapGetString
	  	Intended to be used to ensure ResultSet.getString(int)
	  	actually returns a string even if just "NA"
	 
	  Returns:
	  	String token if not null, "NA" otherwise
	 */

	public static String wrapGetString(String token) {
		if (token == null) {
			return "NA";
		} else {
			return token;
		}
	}

	
	/*
	 Function: dnget
	  	Gets local directory name for properties file based on 
	  	the class name of the calling method
	  
	 Parameter:
	  	fobj - The object reference (usually 'this') of the calling method.
	  	
	 Returns:
	 	Full directory path
	 */
	public static String dnget(Object fobj) {
		java.net.URL u = fobj.getClass().getResource(cnget(fobj));
		String dn = u.getPath().replaceAll("%20", " ");
		return dn.substring(0, dn.lastIndexOf("/"));
	}

	/*
	 Function: cnget
	  	Gets local class name of the calling method.
	  
	 Parameters:
	  	fobj - The object reference (usually 'this') of the calling method.
	  	
	 Returns:
	 	The full class name
	 */
	public static String cnget(Object fobj) {
		String c = fobj.getClass().getName();
		c = c.substring(c.lastIndexOf(".") + 1, c.length());
		return c += ".class";
	}

}
