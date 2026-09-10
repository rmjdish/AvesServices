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

import java.sql.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import mrc.util.HostInfo;

/*
 Class: ConnectDB
  Handles all database connections for Jay/Condor/Swift.  
  This class can be instantiated in one of two forms:
  
  If you create an instance using the parameterless constructor
  you get the default ConnectionManager with a pool depth of DEPTH (check in ConnectionManager class)
  
  If you create an instance using the form with the name of a properties file
  you get a different ConnectionManager from the default which can be used in
  in parallel with the default if you need connections to two different databases
  in the same application
  
  It provides two methods: doQuery and doUpdate
  Future enhancements might include transactional support.
  
   
 See Also:
    <ConnectionManager>, <doQuery>, <doUpdate>
 */
public class ConnectDB {
	private static final Logger log = Logger.getLogger("mrc.db");
	// private static ConnectionManager simp = new ConnectionManager(0);
	private static ConnectionManager cmgr = new ConnectionManager();
	private static Connection curcon;
	private int poolnumber = 0;
	private static final String spit =             "{ call Spit(?,?,?) }";
	private static final String indexSearch =      "{ call indexSearch(?,?) }";
	private static final String nameSearch  =      "{ call nameSearch(?,?) }";
	private static final String indexSearchUnion = "{ call indexSearchUnion(?,?) }";
	private static final String librarySearch =    "{ call librarySearch(?,?) }";
	private static final String topicSearch =      "{ call topicSearch(?,?) }";
	private static final String yearSearch =       "{ call yearSearch(?,?) }";
	private static final String categorySearch =   "{ call categorySearch(?,?) }";
	private static final String basketFormCheck =  "{ call jay.i46_basket_form_check(?) }";

	private static CallableStatement spitcall;
	private static CallableStatement indexSearchcall;
	private static CallableStatement nameSearchcall;
	private static CallableStatement indexSearchUnioncall;
	private static CallableStatement librarySearchcall;
	private static CallableStatement topicSearchcall;
	private static CallableStatement yearSearchcall;	
	private static CallableStatement categorySearchcall;	
	private static CallableStatement basketFormCheckcall;
	

	/*
	 * Constructor: ConnectDB
	 * 
	 * Creates a new ConnectDB object having an associated pool of connections
	 * available up to a maximum pool size of DEPTH (see <ConnectionManager>).
	 * Assumes Java properties file is called "./db.properties"
	 * 
	 * Returns: The initialised ConnectDB object.
	 */
	public ConnectDB() {
		this.poolnumber = 0; // Always zero for default call

	}

	/*
	 * Constructor: ConnectDB
	 * 
	 * Creates a new ConnectDB object having an associated pool of connections
	 * available up to a maximum pool size of DEPTH (see <ConnectionManager>).
	 * 
	 * Parameter:
	 * 	 pfile - the name of a Java properties file
	 * 
	 * Returns: The initialised ConnectDB object.
	 */
	public ConnectDB(String pfile) {
		cmgr = new ConnectionManager(pfile);
		this.poolnumber = cmgr.getPoolID();  // This is not the default database

	}

		
	public void capture() {
		try {
			Connection acon = cmgr.getConnection(this.poolnumber);
			log.fine(HostInfo.tell()+" ConnectDB: capture: acquiring connection from pool "+this.poolnumber);
			curcon = acon;
			/*
			 * About: Prepared Statements The following statements set up prepared
			 * statements for calls to the SQL procedures Spit, indexSearch, and
			 * indexSearchUnion.  They are Condor/Swift specific
			 */
		} catch (Exception e) {
			log.warning(" ConnectDB: capture: error opening connection on pool "+this.poolnumber);
			e.printStackTrace();

		}
	}

	public void release() {
		try {
			log.fine(HostInfo.tell()+" ConnectDB: release: returning connection to pool "+this.poolnumber);
			cmgr.throwBack(curcon,this.poolnumber);
		} catch (Exception e) {
			log.warning(" ConnectionDB: release: error closing connection on pool"+this.poolnumber);
			e.printStackTrace();
		}
	}

	/*
	 * Function: doQuery
	 * 
	 * Processes an SQL query for a given SQL string <code>query</code> and returns
	 * whatever response is available as a <code>ResultSet</code>.
	 * 
	 * Parameters: query - the SQL query to be executed
	 * 
	 * 
	 * 
	 * Returns: The resulting ResultSet object
	 */
	public ResultSet doQuery(String query) {
		Statement stmt = null;
		ResultSet rs = null;

		try {
			curcon.setAutoCommit(false);
			//stmt = curcon.createStatement();
			stmt = curcon.prepareStatement(query, 
					ResultSet.TYPE_SCROLL_INSENSITIVE, 
					ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery(query);
			log.fine(HostInfo.tell()+" doQuery: executeQuery");
			curcon.commit();
			curcon.setAutoCommit(true);
		} catch (SQLException e) {
			log.severe(" ConnectDB: error executing query: " + e.getMessage());
		}
		return rs;
	}

	/*
	 * Function: doPQuery Processes an SQL query for a given SQL string
	 * <code>query</code> using prepared statements and returns whatever response is
	 * available as a <code>ResultSet</code>.
	 * 
	 * Parameters: query - the SQL query to be executed
	 *
	 * 
	 * Returns: The resulting ResultSet object
	 */
	public ResultSet doPQuery(String query, HashMap<Integer, Object> theparms) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int r = 0;
		try {
			curcon.setAutoCommit(false);
			stmt = curcon.prepareStatement(query, 
					ResultSet.TYPE_SCROLL_INSENSITIVE, 
					ResultSet.CONCUR_READ_ONLY);
			log.fine(HostInfo.tell()+" doPQuery: prepareStatement");
			for (Map.Entry<Integer, Object> thisone : theparms.entrySet()) {
				r += 1;
				Integer key = thisone.getKey();
				Object value = thisone.getValue();
				if (value instanceof String) {
					stmt.setString(key, value.toString());
				} else if (value instanceof Integer) {
					stmt.setInt(key, ((Integer) value).intValue());
				} else if (value instanceof Float) {
					stmt.setFloat(key, ((Float) value).floatValue());
				} else if (value instanceof Calendar) {
					stmt.setString(key, value.toString());
				}
			}
			// Parameters set now execute the statement
			rs = stmt.executeQuery();
			log.fine(HostInfo.tell()+" doPQuery: executeQuery.");
			curcon.commit();
			curcon.setAutoCommit(true);
		} catch (SQLException e) {
			log.severe(" ConnectDB: error executing query: " + e.getMessage());
		}
		return rs;
	}

	/*
	 * Function: doSpit Calls an SQL procedure with a given SQL parameter string
	 * <code>term</code> using prepared statements and returns whatever response is
	 * available as a <code>ResultSet</code>.
	 * 
	 * Parameters: secLevel - the security level of the user term - the SQL string
	 * to be searched thetype - string indicating the type of search to be done
	 *
	 * 
	 * Returns: The resulting ResultSet object
	 */
	public ResultSet doSpit(int secLevel, String term, String thetype) {
		ResultSet rs = null;
		try {
			spitcall = curcon.prepareCall(spit);
			log.fine(HostInfo.tell()+" doSpit: prepareCall");
			spitcall.setInt(1, secLevel);
			spitcall.setString(2, term);
			spitcall.setString(3, thetype);
			// Parameters set now execute the statement
			rs = spitcall.executeQuery();
			log.fine(HostInfo.tell()+" doSpit: executeQuery");
		} catch (SQLException e) {
			log.severe(" ConnectDB: error calling spit: " + term + ": " + e.getMessage());
		}
		return rs;
	}

	/*
	 * Function: doindexSearch Calls an SQL procedure with a given SQL search string
	 * <code>term</code> using prepared statements and returns whatever response is
	 * available as a <code>ResultSet</code>.
	 * 
	 * Parameters: 
	 *   secLevel - the security level of the user term - the SQL search string
	 *   term - the search term to be used
	 *
	 * 
	 * Returns: The resulting ResultSet object
	 */
	public ResultSet doindexSearch(int secLevel, String term) {
		ResultSet rs = null;
		try {
			indexSearchcall = curcon.prepareCall(indexSearch);
			log.fine(HostInfo.tell()+" doindexSearch: prepareCall");
			indexSearchcall.setInt(1, secLevel);
			indexSearchcall.setString(2, term);
			// Parameters set now execute the statement
			rs = indexSearchcall.executeQuery();
			log.fine(HostInfo.tell()+" doindexSearch: executeQuery");
		} catch (SQLException e) {
			log.severe(" ConnectDB: error calling indexSearch: " + e.getMessage());
		}
		return rs;
	}

	/*
	 * Function: donameSearch Calls an SQL procedure with a given SQL search string
	 * <code>term</code> using prepared statements and returns whatever response is
	 * available as a <code>ResultSet</code>.
	 * 
	 * Parameters: 
	 * 	secLevel - the security level of the user term - the SQL search string
	 *  term - the search term to be used
	 *
	 * 
	 * Returns: The resulting ResultSet object
	 */
	public ResultSet donameSearch(int secLevel, String term) {
		ResultSet rs = null;
		try {
			nameSearchcall = curcon.prepareCall(nameSearch);
			log.fine(HostInfo.tell()+" donameSearch: prepareCall");
			nameSearchcall.setInt(1, secLevel);
			nameSearchcall.setString(2, term);
			// Parameters set now execute the statement
			rs = nameSearchcall.executeQuery();
			log.fine(HostInfo.tell()+" donameSearch: executeQuery");
		} catch (SQLException e) {
			log.severe(" ConnectDB: error calling nameSearch with: " + e.getMessage());
		}
		return rs;
	}

	/*
	 * Function: dolibrarySearch Calls an SQL procedure with a given SQL search string
	 * <code>term</code> using prepared statements and returns whatever response is
	 * available as a <code>ResultSet</code>.
	 * 
	 * Parameters: 
	 * 	secLevel - the security level of the user term - the SQL search string
	 *  term - the search term to be used
	 *
	 * 
	 * Returns: The resulting ResultSet object
	 */
	public ResultSet dolibrarySearch(int secLevel, String term) {
		ResultSet rs = null;
		try {
			librarySearchcall = curcon.prepareCall(librarySearch);
			log.fine(HostInfo.tell()+" dolibrarySearch: prepareCall");
			librarySearchcall.setInt(1, secLevel);
			librarySearchcall.setString(2, term);
			// Parameters set now execute the statement
			rs = librarySearchcall.executeQuery();
			log.fine(HostInfo.tell()+" dolibrarySearch: executeQuery");
		} catch (SQLException e) {
			log.severe(" ConnectDB: error calling librarySearch with: " + e.getMessage());
		}
		return rs;
	}

	/*
	 * Function: docategorySearch Calls an SQL procedure with a given SQL search string
	 * <code>term</code> using prepared statements and returns whatever response is
	 * available as a <code>ResultSet</code>.
	 * 
	 * Parameters: 
	 * 	secLevel - the security level of the user term - the SQL search string
	 *  term - the search term to be used
	 *
	 * 
	 * Returns: The resulting ResultSet object
	 */
	public ResultSet docategorySearch(int secLevel, String term) {
		ResultSet rs = null;
		try {
			categorySearchcall = curcon.prepareCall(categorySearch);
			log.fine(HostInfo.tell()+" docategorySearch: prepareCall");
			categorySearchcall.setInt(1, secLevel);
			categorySearchcall.setString(2, term);
			// Parameters set now execute the statement
			rs = categorySearchcall.executeQuery();
			log.fine(HostInfo.tell()+" docategorySearch: executeQuery");
		} catch (SQLException e) {
			log.severe(" ConnectDB: error calling categorySearch with: " + e.getMessage());
		}
		return rs;
	}

	/*
	 * Function: doyearSearch Calls an SQL procedure with a given SQL search string
	 * <code>term</code> using prepared statements and returns whatever response is
	 * available as a <code>ResultSet</code>.
	 * 
	 * Parameters: 
	 * 	secLevel - the security level of the user term - the SQL search string
	 *  term - the search term to be used
	 *
	 * 
	 * Returns: The resulting ResultSet object
	 */
	public ResultSet doyearSearch(int secLevel, String term) {
		ResultSet rs = null;
		try {
			yearSearchcall = curcon.prepareCall(yearSearch);
			log.fine(HostInfo.tell()+" doyearSearch: prepareCall");
			yearSearchcall.setInt(1, secLevel);
			yearSearchcall.setString(2, term);
			// Parameters set now execute the statement
			rs = yearSearchcall.executeQuery();
			log.fine(HostInfo.tell()+" dotopicSearch: executeQuery");
		} catch (SQLException e) {
			log.severe(" ConnectDB: error calling yearSearch with: " + e.getMessage());
		}
		return rs;
	}



	
	
	/*
	 * Function: dotopicSearch Calls an SQL procedure with a given SQL search string
	 * <code>term</code> using prepared statements and returns whatever response is
	 * available as a <code>ResultSet</code>.
	 * 
	 * Parameters: 
	 * 	secLevel - the security level of the user term - the SQL search string
	 *  term - the search term to be used
	 *
	 * 
	 * Returns: The resulting ResultSet object
	 */
	public ResultSet dotopicSearch(int secLevel, String term) {
		ResultSet rs = null;
		try {
			topicSearchcall = curcon.prepareCall(topicSearch);
			log.fine(HostInfo.tell()+" dotopicSearch: prepareCall");
			topicSearchcall.setInt(1, secLevel);
			topicSearchcall.setString(2, term);
			// Parameters set now execute the statement
			rs = topicSearchcall.executeQuery();
			log.fine(HostInfo.tell()+" dotopicSearch: executeQuery");
		} catch (SQLException e) {
			log.severe(" ConnectDB: error calling topicSearch with: " + e.getMessage());
		}
		return rs;
	}


	/*
	 * Function: doindexSearchUnion Calls an SQL procedure with a given SQL search
	 * string <code>term</code> using prepared statements and returns whatever
	 * response is available as a <code>ResultSet</code>. Searches over the datasets
	 * table as well as variablelabels table.
	 * 
	 * Parameters: secLevel - the security level of the user term - the SQL search
	 * string
	 *
	 * 
	 * Returns: The resulting ResultSet object
	 */
	public ResultSet doindexSearchUnion(int secLevel, String term) {
		ResultSet rs = null;
		try {
			indexSearchUnioncall = curcon.prepareCall(indexSearchUnion);
			log.fine(HostInfo.tell()+" doindexSearchUnion: prepareCall");
			indexSearchUnioncall.setInt(1, secLevel);
			indexSearchUnioncall.setString(2, term);
			// Parameters set now execute the statement
			rs = indexSearchUnioncall.executeQuery();
			log.fine(HostInfo.tell()+ " doindexSearchUnion: executeQuery");
		} catch (SQLException e) {
			log.severe(HostInfo.tell()+ " ConnectDB: error calling indexSearchUnion: " + e.getMessage());
		}
		return rs;
	}

	/*
	 * Function: dobasketFormCheck Calls an SQL procedure with a given SQL search
	 * string <code>term</code> using prepared statements and returns whatever
	 * response is available as a <code>ResultSet</code>. Searches over the datasets
	 * table as well as variablelabels table.
	 * 
	 * Parameters: secLevel - the security level of the user term - the SQL search
	 * string
	 *
	 * 
	 * Returns: The resulting ResultSet object
	 */
	public ResultSet dobasketFormCheck(String term) {
		ResultSet rs = null;
		try {
			basketFormCheckcall = curcon.prepareCall(basketFormCheck);
			log.fine(HostInfo.tell()+" dobasketFormCheck: prepareCall");
			basketFormCheckcall.setString(1, term);
			// Parameters set now execute the statement
			rs = basketFormCheckcall.executeQuery();
			log.fine(HostInfo.tell()+ " dobasketFormCheck: executeQuery");
		} catch (SQLException e) {
			log.severe(HostInfo.tell()+ " ConnectDB: error calling basketFormCheck: " + e.getMessage());
		}
		return rs;
	}
	
	/*
	 Function: doUpdate 
	  Processes an SQL INSERT,UPDATE, or DELETE SQL string
	  query and returns whatever response is available as a simple
	  integer.
	  
	  Parameter: 
	  	update - the dynamic SQL query to be executed
	  
	  
	  Returns: 
	  	An integer corresponding to the number of rows in the result
	 */

	public int doUpdate(String update) {
		final int FAIL = -1;
		Statement stmt = null;
		int r = 0;

		try {
			// curcon.setAutoCommit(false);
			stmt = curcon.createStatement();
			r = stmt.executeUpdate(update);
			// curcon.commit();
			// curcon.setAutoCommit(true);
		} catch (SQLException e) {
			log.severe(HostInfo.tell()+" ConnectDB: doUpdate: SQL error executing: " + update);
			log.severe(HostInfo.tell()+" ConnectDB: doUpdate: "+ e.getMessage());
			r = FAIL;
		} 
		return r;
	}
	/*
	  Function: doPUpdate 
	  	Processes an SQL INSERT,UPDATE, or DELETE SQL string
	  	query using prepared statements and returns whatever response is
	  	available as a simple integer.
	  
	  Parameters: 
	  	update - the SQL query to be executed 
	  	theparms - an associative list of parameter names and values
	  
	 Returns: 
	 
	 	An integer corresponding to the number of rows in the result
	 */

	public int doPUpdate(String update, HashMap<Integer, Object> theparms) {
		PreparedStatement stmt = null;
		int r = 0;

		try {
			curcon.setAutoCommit(false);
			stmt = curcon.prepareStatement(update);
			for (Map.Entry<Integer, Object> thisone : theparms.entrySet()) {
				r += 1;
				Integer key = thisone.getKey();
				Object value = thisone.getValue();
				if (value instanceof String) {
					stmt.setString(key, value.toString());
				} else if (value instanceof Integer) {
					stmt.setInt(key, ((Integer) value).intValue());
				} else if (value instanceof Float) {
					stmt.setFloat(key, ((Float) value).floatValue());
				} else if (value instanceof Calendar) {
					stmt.setString(key, value.toString());
				}
			}
			// Parameters set now execute the statement
			r = stmt.executeUpdate();
			curcon.commit();
			curcon.setAutoCommit(true);
		} catch (SQLException e) {
			log.severe(HostInfo.tell()+" ConnectDB: error executing prepared version of update: " + e.getMessage());
		}
		return r;
	}
	/*
	 Function: close
	  
	  Class method to invoke the purge method of the ConnectionManager
	  object being used. This removes all connection elements from the available
	  pool
	  
	 About:
	 	This method has been set to do nothing except log
	  
	 */
	public static void close() {
		log.fine(HostInfo.tell()+" ConnectDB: close: called to reset pool");
	}

}
