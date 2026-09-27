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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import mrc.db.ConnectDB;

/* Class: OwlAvailability
 * 	Checks which of a given list of variable names are available on
 *  OWL. A variable is available on OWL if, in variablelabels, its
 *  public column is set AND it has a non-empty field_id. One batch
 *  query per call regardless of how many names are checked - callers
 *  should gather every variable name a page is about to display and
 *  call this once, not once per row.
 */
public class OwlAvailability {
	private static final Logger log = Logger.getLogger("mrc.user");

	/**
	 * Checks a batch of variable names against variablelabels in one query.
	 * @param variableNames the variable names to check
	 * @return the subset of variableNames that are public and have a field_id (i.e. available on OWL)
	 */
	public static Set<String> checkAvailable(List<String> variableNames) {
		Set<String> available = new HashSet<String>();
		if (variableNames == null || variableNames.isEmpty()) {
			return available;
		}
		StringBuilder placeholders = new StringBuilder();
		for (int i = 0; i < variableNames.size(); i++) {
			if (i > 0) {
				placeholders.append(",");
			}
			placeholders.append("?");
		}
		String query = "SELECT name FROM variablelabels WHERE name IN (" + placeholders + ") "
				+ "AND public = 1 AND field_id IS NOT NULL AND field_id != ''";
		try {
			ConnectDB c = new ConnectDB();
			c.capture();
			HashMap<Integer, Object> params = new HashMap<Integer, Object>();
			for (int i = 0; i < variableNames.size(); i++) {
				params.put(i + 1, variableNames.get(i));
			}
			ResultSet rs = c.doPQuery(query, params);
			if (rs != null) {
				while (rs.next()) {
					available.add(rs.getString(1));
				}
			}
			log.fine("OwlAvailability: checked " + variableNames.size() + " names, "
					+ available.size() + " available on OWL");
		} catch (SQLException e) {
			log.severe("OwlAvailability: error checking OWL availability: " + e.getMessage());
		}
		return available;
	}
}
