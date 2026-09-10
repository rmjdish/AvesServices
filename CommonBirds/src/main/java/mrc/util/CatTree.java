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
import java.util.List;
import java.util.stream.Collectors;

/*
 * Class: CatTree
 * 	Used to enable tree like interface for searches for categories
 */
public class CatTree {
	int id;
	String sorted;
	String label;
	int parent;
	int printorder;
	boolean isparent;
	int items;

	public CatTree(int iid, String ilabel, int iparent, int iprintorder, int iitems) {
		id = iid;
		sorted = Integer.toString(id);
		label = ilabel;
		parent = iparent;
		printorder = iprintorder;
		items = iitems;
		isparent = false;
	}

	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof CatTree))
			return false;
		CatTree o = (CatTree) obj;
		return o.getId() == this.getId();
	}

	public String getSorted() {
		return this.sorted;
	}

	public String getLabel() {
		return this.label;
	}

	public int getId() {
		return this.id;
	}

	public int getParent() {
		return this.parent;
	}

	public boolean getIsParent() {
		return this.isparent;
	}

	public int getItems() {
		return this.items;
	}
	
	public void setItems(int n) {
		this.items = n;
	}
	
	public void setIsParent(boolean truth) {
		this.isparent = truth;
	}

	public int getPrintOrder() {
		return this.printorder;
	}

	public static List<CatTree> putInTreeOrder(List<CatTree> treelist) {
		List<CatTree> results = new ArrayList<CatTree>();
		if (treelist.isEmpty()) { // Don't go any further if empty
			return treelist;
		}
		// Get the list of parent node indexes (functional map)
		List<Integer> parentList = treelist.stream().map(CatTree::getParent).collect(Collectors.toList());
		for (CatTree ct : treelist) {
			if (parentList.contains(ct.getId())) {
				ct.setIsParent(true);
			} else {
				ct.setIsParent(false);
			}
		}
		// Get all top level parents in list Using Lambda Expression in filter
		// (functional reduce)
		List<CatTree> parents = treelist.stream().filter(obj -> obj.getIsParent() == true && obj.getParent() == 0)
				.collect(Collectors.toList()); // List of just parent nodes

		// Now we have a list of top level parents, but not necessarily in printorder, so sort
		//Collections.sort(parents, Comparator.comparing(CatTree::getPrintOrder));
        //Collections.sort(parents, (a, b) -> a.printorder < b.printorder ? -1 : a.printorder == b.printorder ? 0 : 1);
		Comparator<CatTree> PrintOrderComparator
		      = Comparator.comparing(CatTree::getPrintOrder);
		Collections.sort(parents, PrintOrderComparator);

		for (CatTree node : parents) { // Go through the parents
			results.add(node); //
			List<CatTree> children = getChildrenOf(node, treelist);
			for (CatTree child : children) { // Go through the children
				if (!results.contains(child)) { // only add in not already in...
					results.add(child);
				}
			}
		}
		return results;
	}

	public static List<CatTree> getChildrenOf(CatTree node, List<CatTree> treelist) {
		List<CatTree> results = new ArrayList<CatTree>();
		if (treelist.isEmpty()) { // stop here
			return results;
		}
		for (CatTree cand : treelist) { // step through all candidates for children
			int cand_parent = cand.getParent();
			if (cand_parent == node.getId()) { // candidate is child of node
				if (!results.contains(cand)) { // if not already in results...
					results.add(cand); // add candidate to results for children of node
					results.addAll(getChildrenOf(cand, treelist));
				}
			}
		}
		return results;
	}
}
