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

package mrc.db;

import java.io.InputStream;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.ArrayList;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Properties;
import java.util.logging.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// 
// import com.mysql.cj.jdbc.Driver;
//
import mrc.util.HostInfo;

/** @brief Manages Database Connections
    
    Class: ConnectionManager 
    Creates a pool of database connections by reading
    properties from a Java properties file given the name <em>db.properties</em>
    and opens connections up to a maximum of *DEPTH*. All unwanted connections
    are returned to the pool. The pool size is monitored every time a connection
    is returned as no longer needed and it is pruned if necessary.
    
    
    @see <ConnectDB>
*/
public class ConnectionManager {

	private static final int DEPTH = 0; // Depth of each pool 0 = no pooling
	private static final int NDBS = 5; // Number of DBs
	private static final Logger log = Logger.getLogger("mrc.user");
	private static ArrayList<ArrayList<Connection>> connectionPool = new ArrayList<ArrayList<Connection>>(NDBS);
	private static int poolID = 0; // default ConnectionManager Id
	private static int[] pooldepth = new int[NDBS];
	private static String[] propfile = new String[NDBS];  // one for each connection pool
	private static boolean doinit = true;
	// These parms are valid for MySQL Connector/J version 8 on Ubuntu 20.04 Tomcat 9
	private static String extjdbcparms = "?sslMode=PREFERRED&autoReconnect=true&failOverReadOnly=false&maxReconnects=10";
	//private static String extjdbcparms = "";

	/** @ brief Constructor: ConnectionManager
	    
	    Creates a new ConnectionManager object with a pool depth set to *DEPTH*.
	    
	    
	    @returns ConnectionManager object.
	*/
	public ConnectionManager() {
		this.initialisePools();
		propfile[0] = "/mrc/db/db.properties";   // default value
		poolID = 0; // default value
	}


    /** @brief Constructor: ConnectionManager
	
	Creates a new ConnectionManager object with a pool depth set to *pooldepth*.
	
	@param String pfile - The name of a java properties file which contains db connection details
	
	
	@teturns ConnectionManager object.
    */
    
    public ConnectionManager(String pfile) {
	this.initialisePools();
	// Have we already created this connectionPool?
	int poolindex = this.whichPool(pfile);
	if (poolindex >= 0) {
	    // we've created this pool before
	    poolID = poolindex;
		} else if (poolID < (NDBS-1)) {
	    /*
	      Assert: poolindex < 0 i.e. pfile not found
	      and have room to create another pool
	      
			  	We may not have any polls at all so far
			  	so poolID will never point to 0 unless
			  	matched to "db.properties" above
	    */
	    poolID += 1; // different pool
	    propfile[poolID] = pfile;
	    pooldepth[poolID] = DEPTH;
	} else {
	    /* oh dear need another pool but max DB pools reached.
	     * Assert: poolindex < 0 and this.managerNumber >= NDBS
	     */
	    log.severe(HostInfo.tell()+" ConnectionManager: no more DB pools available!");
	}
    }
    
    
    
    /**
	  @brief Get a Connection Object
 
	  getConnection is the API call to request a Connection
	  object to be obtained from the existing pool, or a new Connection object is
	  created and returned
	  
	  @param int poolnumber - the index of the pool to be connected with
	  
	  @returns new Connection object attached to the database specified in the
	  db.properties Java properties file.
    */
    public Connection getConnection(int poolnumber) throws Exception {
	// which connectionPool are we using here? use this.managerNumber to delineate
	if (poolnumber >= 0 && poolnumber < NDBS) {
	    /*
	      Assert: the pool number is valid is there anything in this pool alerady?
	    */
	    try {
		ArrayList<Connection> queue = connectionPool.get(poolnumber);
		if (!queue.isEmpty()) {
		    /*
		      Assert: this pool is not empty
		    */
		    log.fine(HostInfo.tell()+" getConnection: pool number: "+poolnumber+" has existing connections");
		    Connection nextConnection = connectionPool.get(poolnumber).get(0); // pull first one off list
		    connectionPool.get(poolnumber).remove(0); // remove it
		    return nextConnection;
		} else {
		    /*
		      Assert: pool is empty
		    */
		    log.fine(HostInfo.tell()+" getConnection: pool number: "+poolnumber+" is empty");					
		    return makeFish();
		}
	    } catch (Exception e) {
		log.severe(HostInfo.tell() + " getConnection: error testing pool for empty.");
		return makeFish();
	    }
	} else {
	    /*
	      Assert: invalid poolnumber so just return a connection anyway
	    */
	    log.fine(HostInfo.tell()+" getConnection: invalid pool number: "+poolnumber);
	    return makeFish();
		}
    }
    
    /**
      @brief makeFish sets up database connection 
      makeFish sets up an initial connection to the back end
      database by reading parameter from a db.properties file and opening a JDBC
      connection using the parameter specified in the file.
      
      @returns indicated Connection object
    */
    private Connection makeFish() throws Exception {
        String url = null;
        Connection fish = null;
        try {
            // 1. Get the property file path (e.g., "/mrc/db/db.properties")
            String thispropf = this.getPropFile();
            
            // 2. Ensure it has a leading slash for absolute classpath lookup
            String resourcePath = thispropf.startsWith("/") ? thispropf : "/" + thispropf;
            
            log.fine(HostInfo.tell() + " makeFish: looking for properties in classpath @ " + resourcePath);
            
            // 3. Load as a stream from the classpath (works for both loose files and JARs)
            try (InputStream is = ConnectionManager.class.getResourceAsStream(resourcePath)) {
                if (is == null) {
                    // This deliberately throws FileNotFoundException to trigger your existing catch block
                    throw new FileNotFoundException("Classpath resource not found: " + resourcePath);
                }
                
                ResourceBundle bdl = new PropertyResourceBundle(is);
                String username = bdl.getString("username");
                String password = bdl.getString("password");
                String hostname = bdl.getString("hostname");
                String vendor = bdl.getString("vendor");
                String port = bdl.getString("port");
                String instance = bdl.getString("instance");
                
                // Now get connection with the parameters 
                log.fine(HostInfo.tell() + " makeFish: username: " + username + " pw hash: " + password.hashCode());
                url = "jdbc:" + vendor + "://" + hostname + ":" + port + "/" + instance + extjdbcparms;
                log.fine(HostInfo.tell() + " makeFish: url: " + url);
                fish = DriverManager.getConnection(url, username, password);
            } // InputStream is automatically closed here
            
        } catch (FileNotFoundException e) {
            log.severe(HostInfo.tell() + " ConnectionManager: File not found Exception " + propfile[poolID]);
            //e.printStackTrace();
            return null;
        } catch (SQLException ex) {
            log.severe(HostInfo.tell() + " ConnectionManager: SQL Exception for " + url);
            log.severe(HostInfo.tell() + ex.getMessage());
            return null;
        }
        log.fine(HostInfo.tell() + " makeFish: Successful Connection.");
        return fish;
    }    
    /**
       @brief Returns conneciton to the pool
       throwBack returns a used Connection object to a pool of
       available connections. If the pool size is greater than DEPTH, the pool is
       culled to that size.
       
       @param fish - A Connection object to be curated
       @param thispool - the index of the pool to store the connection
    */
    public void throwBack(Connection fish, int thispool) throws Exception {
	// Which pool do I throw it back to?
	if (thispool >= 0 && thispool < NDBS) {
	    /*
	      Assert: the poolnumber is valid; is the pool depth getting out of hand?
	    */
	    try {
		while (connectionPool.get(thispool).size() > pooldepth[thispool]) {
		    Connection nextConnection = connectionPool.get(thispool).get(0); // first element on queue
		    connectionPool.get(thispool).remove(0); // remove it from queue
		    nextConnection.close();
		}
		log.fine(HostInfo.tell()+" throwBack: : culling pool "+thispool);
		// Above guarantees that connectionPool.size() <= pooldepth
	    } catch (Exception e) {
		log.severe(HostInfo.tell()+" throwBack: Error testing poolsize or reducing queue length.");
		fish.close();
	    }
	    if (pooldepth[thispool] == 0) {
		fish.close();
		log.fine(HostInfo.tell()+" throwBack: closing last connection in pool "+thispool);
	    } else {
		// this is pooled so push it on the queue
		connectionPool.get(thispool).add(fish);
		log.fine(HostInfo.tell()+" throwBack: adding connection to pool "+thispool);
	    }
	}
    }
    
    /** @brief empty connection pools
	purge empties all Connection pools and kills all open DB
	connections
	  
    */
    public static void purge() throws Exception {
	log.warning(HostInfo.tell()+" ConnectionManager: purge of all pools invoked; resetting managerNumber to 0.");
	poolID = 0;
	for (int i = 0; i < NDBS; i++) {
	    while (!connectionPool.get(i).isEmpty()) {
		Connection nextConnection = connectionPool.get(i).get(0); // remember first element on
		connectionPool.get(i).remove(0);
		nextConnection.close();
	    }			
	}
	// Now connectionPool.size() = 0
    }
    
    /** @brief getter method
	Getter function for current pool index
	
	@returns Integer pool index
    */
    public int getPoolID() {
	return poolID;
    }
    /** @brief getter method
	Getter function for current pool properties file name
	
	@returns String file name
    */
    public String getPropFile() {
	String pfile = ConnectionManager.propfile[this.getPoolID()];
	return pfile;
    }
    /** @brief creates storage for connection objects
	Creates array lists of required size to store connection objects
    */
    private void initialisePools() {
	if (doinit) {
	    for (int i = 0; i < NDBS; i++) {
		ArrayList<Connection> queue = new ArrayList<Connection>();
		connectionPool.add(i, queue);
		propfile[i] = "";
		pooldepth[i] = DEPTH;
	    }
	    doinit = false;
	}
    }
    /** @brief returns pool number given properties file name

      Given the name of a properties file returns the index of the
      connection pool that corresponds to that file
      
      @param String pfile - property file name
      
      @returns int poolindex - index of corresponding pool or -1 if not found
    */
    private int whichPool(String pfile) {
	int poolindex = -1;
	for (int i = 0; i < propfile.length; i++) {
	    if (propfile[i].equalsIgnoreCase(pfile)) {
		poolindex = i;
	    }
	}
	return poolindex;
    }   
}
