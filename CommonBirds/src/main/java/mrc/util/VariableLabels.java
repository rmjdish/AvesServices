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

/* Class: VariableLabels
 * 	Batch lookup of the human-readable Label for a list of variable
 *  names, from variablelabels. One query per call regardless of how
 *  many names are checked - callers should gather every variable
 *  name a page is about to display and call this once, not once per
 *  row.
 *
 *  Extracted as a shared utility (matching OwlAvailability and
 *  RestrictedVariables' own pattern) rather than left as Basket.
 *  java's private fetchVariableMetadata(), since Variable.java's
 *  Basket Management page needed the same lookup and a servlet
 *  shouldn't instantiate another servlet just to borrow a helper
 *  method. Basket.java's own fetchVariableMetadata() also returns
 *  Form and YEAR, which this doesn't need - kept separate rather
 *  than widening this into carrying columns only one caller uses.
 */
public class VariableLabels {
	private static final Logger log = Logger.getLogger("mrc.user");

	/**
	 * Checks a batch of variable names against variablelabels in one query.
	 * @param variableNames the variable names to check (case-insensitive)
	 * @return a map of lower-cased variable name to its Label - names
	 *         with no matching row, or a null Label, simply aren't present
	 *         in the map; callers should treat a missing key as "no label".
	 */
	public static Map<String, String> checkLabels(List<String> variableNames) {
		Map<String, String> labels = new HashMap<String, String>();
		if (variableNames == null || variableNames.isEmpty()) {
			return labels;
		}
		StringBuilder placeholders = new StringBuilder();
		for (int i = 0; i < variableNames.size(); i++) {
			if (i > 0) {
				placeholders.append(",");
			}
			placeholders.append("?");
		}
		// LOWER() on both sides, matching the same case-insensitive
		// pattern already established in OwlAvailability/RestrictedVariables.
		String query = "SELECT name, Label FROM variablelabels WHERE LOWER(name) IN (" + placeholders + ")";
		try {
			ConnectDB c = new ConnectDB();
			c.capture();
			HashMap<Integer, Object> params = new HashMap<Integer, Object>();
			for (int i = 0; i < variableNames.size(); i++) {
				params.put(i + 1, variableNames.get(i).toLowerCase());
			}
			ResultSet rs = c.doPQuery(query, params);
			if (rs != null) {
				while (rs.next()) {
					String label = rs.getString("Label");
					if (label != null) {
						labels.put(rs.getString("name").toLowerCase(), label);
					}
				}
			}
			log.fine("VariableLabels: checked " + variableNames.size() + " names, "
					+ labels.size() + " labels found");
		} catch (SQLException e) {
			log.severe("VariableLabels: error checking labels: " + e.getMessage());
		}
		return labels;
	}
}
