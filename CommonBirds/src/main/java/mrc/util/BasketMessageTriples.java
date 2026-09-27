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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* Class: BasketMessageTriples
 * 	Shared operations on the flat <name, message, category> triples
 *  that MessageRelay.msgFlush() returns - counting how many fall into
 *  each category, and sorting them into a fixed display order
 *  (restricted first, then added, then linked-added last).
 *
 *  Extracted from Variable.java (where these were originally private
 *  instance methods, written for the add-variable flow specifically)
 *  into a shared, static utility, since msgFlush() always returns
 *  triples now regardless of caller - LoadBasket.java and Trolley.java
 *  both read from the same MessageRelay and need the same handling,
 *  not a duplicated copy of this logic in each servlet.
 */
public class BasketMessageTriples {

	/*
	 Method: countByCategory
	 	Counts how many variables fall into each category ("restricted",
	 	"linked-restricted", "added", "added-note", "linked-added") in
	 	a flat <name, message, category> triples list.
	*/
	public static Map<String, Integer> countByCategory(List<String> flat) {
		Map<String, Integer> counts = new HashMap<String, Integer>();
		for (int i = 0; i < flat.size(); i += 3) {
			String category = flat.get(i + 2);
			counts.put(category, counts.getOrDefault(category, 0) + 1);
		}
		return counts;
	}

	/*
	 Method: sortByCategory
	 	Takes the flat <name, message, category> triples from
	 	msg2u.msgFlush() (in HashMap iteration order, effectively
	 	random) and reorders them into the groups requested: restricted
	 	(including linked-but-restricted) first, then normally-added,
	 	then added-as-linked last - so a results page can display them
	 	grouped without needing to sort in the template itself. A
	 	caller with no "linked" concept at all (e.g. LoadBasket.java,
	 	which loads an already-saved basket rather than adding new
	 	variables) will simply never have any category ranked last;
	 	the ranking still applies safely either way.
	*/
	public static ArrayList<String> sortByCategory(List<String> flat) {
		List<String[]> triples = new ArrayList<String[]>();
		for (int i = 0; i < flat.size(); i += 3) {
			triples.add(new String[]{ flat.get(i), flat.get(i+1), flat.get(i+2) });
		}
		Collections.sort(triples, new Comparator<String[]>() {
			public int compare(String[] a, String[] b) {
				return categoryRank(a[2]) - categoryRank(b[2]);
			}
			private int categoryRank(String category) {
				if ("restricted".equals(category) || "linked-restricted".equals(category)) return 0;
				if ("added".equals(category) || "added-note".equals(category)) return 1;
				return 2; // linked-added
			}
		});
		ArrayList<String> sorted = new ArrayList<String>();
		for (String[] triple : triples) {
			sorted.add(triple[0]);
			sorted.add(triple[1]);
			sorted.add(triple[2]);
		}
		return sorted;
	}
}
