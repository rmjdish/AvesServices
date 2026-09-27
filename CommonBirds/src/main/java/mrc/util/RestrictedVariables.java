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
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import mrc.db.ConnectDB;

/* Class: RestrictedVariables
 * 	Checks which of a given list of variable names are restricted, and
 *  what the restriction message is for each. A variable is
 *  restricted if it appears in varsecmod with secLevel > 0 (the same
 *  definition SecModel.isOpen() already uses elsewhere in this
 *  codebase, for access control rather than display purposes) - the
 *  message text itself comes from a join to messages via messageId.
 *  One batch query per call, same pattern as OwlAvailability.
 */
public class RestrictedVariables {
	private static final Logger log = Logger.getLogger("mrc.user");

	/**
	 * Checks a batch of variable names against varsecmod/messages in one query.
	 * @param variableNames the variable names to check
	 * @return a map of variable name to restriction message, for just the
	 *         subset of variableNames that are actually restricted
	 *         (secLevel > 0). Names with no restriction are simply absent
	 *         from the returned map.
	 */
	public static Map<String, String> checkRestricted(List<String> variableNames) {
		Map<String, String> restricted = new HashMap<String, String>();
		if (variableNames == null || variableNames.isEmpty()) {
			return restricted;
		}
		StringBuilder placeholders = new StringBuilder();
		for (int i = 0; i < variableNames.size(); i++) {
			if (i > 0) {
				placeholders.append(",");
			}
			placeholders.append("?");
		}
		String query = "SELECT LOWER(vs.Name) as vname, m.mtext FROM varsecmod vs "
				+ "JOIN messages m ON vs.messageId = m.messageId "
				+ "WHERE LOWER(vs.Name) IN (" + placeholders + ") AND vs.secLevel > 0";
		try {
			ConnectDB c = new ConnectDB();
			c.capture();
			HashMap<Integer, Object> params = new HashMap<Integer, Object>();
			for (int i = 0; i < variableNames.size(); i++) {
				params.put(i + 1, variableNames.get(i).toLowerCase());
			}
			ResultSet rs = c.doPQuery(query, params);
			// Match results back case-insensitively, but keyed by the
			// original casing that was passed in (matching how the
			// variable name is actually displayed/looked-up in the
			// calling template) - varsecmod's own stored casing isn't
			// guaranteed to match variablelabels.name's casing, so
			// this avoids a silent case-mismatch on template lookup.
			Map<String, String> byLowerName = new HashMap<String, String>();
			if (rs != null) {
				while (rs.next()) {
					byLowerName.put(rs.getString("vname"), rs.getString("mtext"));
				}
			}
			for (String originalName : variableNames) {
				String msg = byLowerName.get(originalName.toLowerCase());
				if (msg != null) {
					restricted.put(originalName, msg);
				}
			}
			log.fine("RestrictedVariables: checked " + variableNames.size() + " names, "
					+ restricted.size() + " restricted");
		} catch (SQLException e) {
			log.severe("RestrictedVariables: error checking restricted status: " + e.getMessage());
		}
		return restricted;
	}
}
