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

package mrc.user;

import java.sql.ResultSet;
import java.util.logging.Logger;

import mrc.db.ConnectDB;

public class Helper {

	// private static final Logger log = Logger.getLogger(HostInfo.tell() + ":" + Helper.class.getName());
	private static final Logger log = Logger.getLogger("mrc.user");
	public boolean orphaned(String name, String username) {
		boolean orphan = false;
		String query = "select name from orphans where name = \'" + name + "\'";
		try {
			ConnectDB c = new ConnectDB();
			c.capture();
			ResultSet rs = c.doQuery(query);
			if (rs.first()) {
				orphan = true;
			}
			rs.close();
			c.release();
		} catch (Exception e) {
			log.severe(" Failed query: "+query);
			e.printStackTrace();
		}
		return orphan;
	}

	public boolean illegal(String name, String username) {
		boolean illegal = false;
		String query = "select name from illegals where name = \'" + name + "\'";
		try {
			ConnectDB c = new ConnectDB();
			c.capture();
			ResultSet rs = c.doQuery(query);
			if (rs.first()) {
				illegal = true;
			}
			rs.close();
			c.release();
		} catch (Exception e) {
			log.severe(" Failed query: "+query);
			e.printStackTrace();
		}
		return illegal;
	}
}
