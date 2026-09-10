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
import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;

public class TreeSearch {

	// Instance variable
	List<CatTree> treelist = new ArrayList<CatTree>();
	// Class variable
	static List<String> template = new ArrayList<String>();
	private static final Logger log = Logger.getLogger("mrc.user");

	
	public TreeSearch(List<CatTree> tlist) {
		// Object created with specific template
		treelist = tlist;
	}
	
	static void printULstart() {
		template.clear(); // Start afresh
		// template.add("<!DOCTYPE html>");
		// template.add("<html>");
		// template.add("<head>");
		// template.add("<title>Category Search Menu</title>");
		// template.add("<link rel=\"stylesheet\" href=\"treeview-navigation.css\">");
		// template.add("<script type=\"text/javascript\" src=\"treeview-navigation.js\"></script>");
		// template.add("<script type=\"text/javascript\" src=\"treeitem.js\"></script>");
		// template.add("<script type=\"text/javascript\" src=\"treeitemClick.js\"></script>");
		// template.add("<script src=\"https://kit.fontawesome.com/8d0f53e3fd.js\" crossorigin=\"anonymous\"></script>");
		// template.add("</head>");
		// template.add("<body>");		
		// template.add("<h1 id=\"tree_label\">Expandable Category Search Menu</h1>");
		template.add("<p>\n");
		template.add("<label>\n");
		template.add("    Category Selected:");
		template.add("    <input name=\"category\" id=\"last_action\" type=\"text\" size=\"50\" readonly=\"\">\n");
		template.add("  </label>\n");
		template.add("</p>\n");
		// template.add("<p>Numbers shown in square brackets are Showcase category labels.<p>");
		// template.add("<p style=\"color:DarkRed;\">Parent categories may show zero items.  Expand these to see subcategory item totals.<p>");
		template.add("<!-- Start of Expandable Tree - All inside <ul>...</ul> -->\n");
		template.add("<div class=\"tree\">\n");
		template.add("<ul class=\"tree\" role=\"tree\"  aria-labelledby=\"tree_label\">\n");
	}

	static void printULend() {
		template.add("</ul>\n");
		template.add("</div>\n");
		// template.add("</body>");
		// template.add("</html>");
	}

	static void beginSection(String sid, int id, int items) {
		template.add("<!-- New Section -->\n");
		template.add("\t<li class=\"section\" role=\"treeitem\" aria-expanded=\"false\" aria-selected=\"false\">\n");
		template.add("\t<span class=\"showcase\">" + sid + " [" + id + "] has " + items + " items</span>\n");
		template.add("\t<ul class=\"section\" role=\"group\">\n");
		template.add("\t<!-- End of Section Header -->\n");
		}

	static void endSection() {
		template.add("<!-- End Section -->\n");
		template.add("</ul>\n");
		template.add("</li>\n");
	}

	static void printPlain(String sid, int id, int items ) {
		template.add("\t<!-- Plain Element -->\n");
		template.add("\t<li role=\"treeitem\"  aria-selected=\"false\" class=\"doc\">" + 
				sid + "[" + id + "] has " + items + " items</li>\n");
	}


    static void printState(CatTree n, Stack<Integer> topstack, String msg) {
	log.info("\nDecision: "+msg);
	log.info("Id="+n.getId()+
			   "\tIs Parent:"+n.getIsParent()+
			   "\tParent Id="+n.getParent()+
			   "\tLabel: "+n.getLabel()+
			   "\tStack:"+topstack);
    }
			   
	public List<String> generateTree() {
		Stack<Integer> penv = new Stack<Integer>();
		// Create Trees
		// code,label,parent,printorder
		// treelist.add( new CatTree(0,"All",0,0) );
		// Get the size of the ArrayList treelist
		int treesize = treelist.size();
		log.info("TreeSearch called with node list of length: "+treesize);
		// treelist must be in parent --> children order so...
		treelist = CatTree.putInTreeOrder(treelist);
		treesize = treelist.size();
		log.info("TreeSearch after sort has node list of length: "+treesize);
		// This next bit is the core algorithm for generating HTML Tree navigation
		// ================================================================
		int curparent = 0; // Current parent is root node in tree
		penv.push(curparent);
		printULstart(); // HTML ul class="tree"
		for (CatTree ct : treelist) { // Loop through all the CatTree objects.
			curparent = ct.getParent();
			if (ct.getIsParent()) { // ct is a heading
				if (curparent == penv.peek()) {
					// New section indented - New parent
					penv.push(ct.getId()); // Push this node onto stack
					//printState(ct, penv, "New Section: Same parent");
					beginSection(ct.getLabel(), ct.getId(), ct.getItems());
				} else { // curparent is from previous heading
					// Parent is previous - outdent
					//printState(ct, penv, "New Section: Previous parent");
					while (ct.getParent() != penv.peek()) {
						curparent = penv.pop();
						endSection();
					}
					penv.push(ct.getId());
					beginSection(ct.getLabel(), ct.getId(), ct.getItems());
				}
			} else if (curparent == penv.peek()) { // plain under same heading
				printPlain(ct.getLabel(), ct.getId(), ct.getItems());
				//printState(ct, penv, "Plain category: Same parent");
			} else { // plain under previous heading
				while (ct.getParent() != penv.peek()) {
					log.fine("TreeSearch Plain category: Outdent: Stack:"+penv);
					curparent = penv.pop();
					endSection();
				}
				//printState(ct, penv, "Plain category: Previous parent");
				printPlain(ct.getLabel(), ct.getId(), ct.getItems());
			}
		}
		endSection();
		printULend(); // End HTML ul tag
		// ================================================================
		// Return template for use...
		//log.info("TreeSearch returning "+template.size()+" lines of HTML\n"+template);
		return template;
		// ==================================================================
	}

}
