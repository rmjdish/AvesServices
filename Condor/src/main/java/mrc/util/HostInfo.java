package mrc.util;

/* Class: SwiftVersion
 * 	Returns information about this version of Condor/Swift
 * 
 *
 */
public final class HostInfo extends Announce {

	
	/*
     * Function: sethost
     * 	Sets the name of this program to instance variable verString
     *
     */
    public static void sethost() {
    	verString = "Condor 3.1";
    }
    
    public static String tell() {
    	//HostInfo me = new HostInfo();
    	sethost();
    	return verString;
    }

}
