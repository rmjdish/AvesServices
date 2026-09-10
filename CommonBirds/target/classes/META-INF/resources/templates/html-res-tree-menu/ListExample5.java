import java.util.*;
import java.io.*;
import java.util.stream.Collectors;

public class ListExample5 {
	// Create a list of CatTree objects
	List<CatTree> treelist = new ArrayList<CatTree>();
	static List<String> template = new ArrayList<String>();

	static void printULstart() {
		template.add("<!DOCTYPE html>");
		template.add("<html>");
		template.add("<head>");
		template.add("<title>Category Search Menu</title>");
		template.add("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
		template.add("<meta content=\"text/html; charset=ISO-8859-1\" http-equiv=\"content-type\">");
		//template.add("<link rel=\"stylesheet\" href=\"treeview-navigation.css\">");
		template.add("<link rel=\"stylesheet\" href=\"SWIFT15.css\">");
		template.add("<link rel=\"stylesheet\" href=\"https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0\" />");
		template.add("<script type=\"text/javascript\" src=\"treeview-navigation.js\"></script>");
		template.add("<script type=\"text/javascript\" src=\"treeitem.js\"></script>");
		template.add("<script type=\"text/javascript\" src=\"treeitemClick.js\"></script>");
		template.add("</head>");
		template.add("<body>");
		template.add("<h1 id=\"tree_label\">Expandable Category Search Menu</h1>");
		template.add("<p>");
		template.add("  <label>");
		template.add("    Category Selected:");
		template.add("    <input id=\"last_action\" type=\"text\" size=\"50\" readonly=\"\">");
		template.add("  </label>");
		template.add("</p>");
		template.add("<!-- Start of Expandable Tree - All inside <ul>...</ul> -->");
		template.add("<div>");
		template.add("<ul role=\"tree\"  aria-labelledby=\"tree_label\">");
	}

	static void printULend() {
		template.add("</ul>");
		template.add("</div>");
		template.add("</body>");
		template.add("</html>");
	}

	static void beginSection(String sid, int id) {
		template.add("<!-- New Section -->");
		template.add("\t<li role=\"treeitem\" aria-expanded=\"false\" aria-selected=\"false\">");
		template.add("\t<span>" + sid + " [" + id + "]</span>");
		template.add("\t<ul role=\"group\">");
		template.add("\t<!-- End of Section Header -->");
	}

	static void endSection() {
		template.add("<!-- End Section -->");
		template.add("</ul>");
		template.add("</li>");
	}

	static void printPlain(String sid, int id) {
		template.add("\t<!-- Plain Element -->");
		template.add("\t<li role=\"treeitem\"  aria-selected=\"false\" class=\"doc\">" + sid + "[" + id + "]</li>");
	}

	static void printState(CatTree n, Stack<Integer> topstack, String msg) {
		System.out.println("\nDecision: " + msg);
		System.out.println("Id=" + n.getId() + "\tIs Parent:" + n.getIsParent() + "\tParent Id=" + n.getParent()
				+ "\tLabel: " + n.getLabel() + "\tStack:" + topstack);
	}

	public static void main(String[] args) throws IOException {
		FileOutputStream stream = new FileOutputStream("demo.html");
		PrintWriter writer = new PrintWriter(stream);
		ListExample5 prog = new ListExample5();
		try {
			prog.start(writer);
			writer.flush();
			stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void start(PrintWriter htmlfile) {
		Stack<Integer> penv = new Stack<Integer>();
		// Create Trees
		// code,label,parent,printorder
		// treelist.add( new CatTree(0,"All",0,0) );
		treelist.add(new CatTree(1, "Contact", 29, 0));
		treelist.add(new CatTree(2, "Anthropometry", 29, 0));
		treelist.add(new CatTree(3, "General health", 29, 0));
		treelist.add(new CatTree(4, "Wellbeing", 29, 0));
		treelist.add(new CatTree(5, "Respiratory health", 29, 0));
		treelist.add(new CatTree(6, "Cardiovascular health", 29, 0));
		treelist.add(new CatTree(7, "Musculoskeletal health and physical capability", 29, 0));
		treelist.add(new CatTree(8, "Mental health", 29, 0));
		treelist.add(new CatTree(9, "Women's health", 29, 0));
		treelist.add(new CatTree(10, "Hospital admissions", 29, 0));
		treelist.add(new CatTree(11, "Medication", 29, 0));
		treelist.add(new CatTree(12, "Other health care", 29, 0));
		treelist.add(new CatTree(13, "Mortality", 29, 0));
		treelist.add(new CatTree(14, "Cognitive capacity", 29, 0));
		treelist.add(new CatTree(15, "Temperament or personality", 29, 0));
		treelist.add(new CatTree(16, "Nutrition", 29, 0));
		treelist.add(new CatTree(17, "Other health behaviours", 29, 0));
		treelist.add(new CatTree(18, "Puberty or fertility history", 29, 0));
		treelist.add(new CatTree(19, "Marital history", 29, 0));
		treelist.add(new CatTree(20, "Household structure", 29, 0));
		treelist.add(new CatTree(21, "Education", 29, 0));
		treelist.add(new CatTree(22, "Social class", 29, 0));
		treelist.add(new CatTree(23, "Employment history", 29, 0));
		treelist.add(new CatTree(24, "Other socioeconomic circumstances", 29, 0));
		treelist.add(new CatTree(25, "Social support or life events", 29, 0));
		treelist.add(new CatTree(26, "DNA samples", 29, 0));
		treelist.add(new CatTree(27, "Blood, urine, and other samples", 29, 0));
		treelist.add(new CatTree(28, "Coronavirus (Covid-19)", 29, 0));
		treelist.add(new CatTree(29, "Legacy NSHD Categories", 0, 0));
		treelist.add(new CatTree(51, "Clinical data", 0, 100));
		treelist.add(new CatTree(52, "Biological samples", 0, 110));
		treelist.add(new CatTree(53, "Genomics", 0, 130));
		treelist.add(new CatTree(54, "Population characteristics", 0, 140));
		treelist.add(new CatTree(55, "Questionnaire data", 0, 120));
		treelist.add(new CatTree(101, "Cognitive function", 51, 100));
		treelist.add(new CatTree(105, "Imaging", 51, 110));
		treelist.add(new CatTree(110, "Physical measures", 51, 120));
		treelist.add(new CatTree(115, "Physical performance", 51, 130));
		treelist.add(new CatTree(201, "Blood assays", 52, 100));
		treelist.add(new CatTree(205, "Urine assays", 52, 110));
		treelist.add(new CatTree(210, "Saliva assays", 52, 120));
		treelist.add(new CatTree(301, "Telomeres", 53, 100));
		treelist.add(new CatTree(302, "Epigenetics", 53, 110));
		treelist.add(new CatTree(401, "Baseline characteristics", 54, 100));
		treelist.add(new CatTree(405, "Ongoing characteristics", 54, 110));
		treelist.add(new CatTree(501, "Family history", 55, 100));
		treelist.add(new CatTree(505, "Medications", 55, 110));
		treelist.add(new CatTree(510, "Medical conditions", 55, 120));
		treelist.add(new CatTree(1011, "Childhood cognition", 101, 100));
		treelist.add(new CatTree(1012, "Reading comprehension", 101, 110));
		treelist.add(new CatTree(1013, "Peg placement", 101, 120));
		treelist.add(new CatTree(1014, "Visual memory", 101, 130));
		treelist.add(new CatTree(1015, "Memory (word list memory test)", 101, 140));
		treelist.add(new CatTree(1016, "Processing speed", 101, 150));
		treelist.add(new CatTree(1017, "National audit reading test (NART)", 101, 160));
		treelist.add(new CatTree(1018, "Verbal fluency", 101, 170));
		treelist.add(new CatTree(1019, "Reaction time", 101, 180));
		treelist.add(new CatTree(1020, "Sensory difficulties", 101, 190));
		treelist.add(new CatTree(1021, "Memory difficulties", 101, 200));
		treelist.add(new CatTree(1022, "ACE-III (cognitive state)", 101, 210));
		treelist.add(new CatTree(1023, "Finger tapping", 101, 220));
		treelist.add(new CatTree(1051, "DXA assessment", 105, 100));
		treelist.add(new CatTree(1052, "Cardiovascular", 105, 110));
		treelist.add(new CatTree(1101, "Actiheart monitor", 110, 110));
		treelist.add(new CatTree(1102, "Anthropometry", 110, 120));
		treelist.add(new CatTree(1103, "Blood pressure", 110, 130));
		treelist.add(new CatTree(1104, "ECG at rest, 12-Lead", 110, 140));
		treelist.add(new CatTree(1105, "Spirometry", 110, 150));
		treelist.add(new CatTree(1151, "Hand grip strength", 115, 100));
		treelist.add(new CatTree(1152, "Chair rises", 115, 110));
		treelist.add(new CatTree(1153, "Standing balance", 115, 120));
		treelist.add(new CatTree(1154, "Timed get up and go", 115, 130));
		treelist.add(new CatTree(1155, "Walk test", 115, 140));
		treelist.add(new CatTree(2011, "Blood biochemistry", 201, 100));
		treelist.add(new CatTree(2012, "NMR metabolomics", 201, 110));
		treelist.add(new CatTree(2013, "Metabolon", 201, 120));
		treelist.add(new CatTree(4051, "Response status", 405, 100));
		treelist.add(new CatTree(5011, "Medical conditions", 501, 100));
		treelist.add(new CatTree(10511, "Bone size, mineral and density by DXA", 1051, 100));
		treelist.add(new CatTree(10512, "Body composition by DXA", 1051, 110));
		treelist.add(new CatTree(10521, "Pulse wave analysis", 1052, 100));
		treelist.add(new CatTree(10522, "Pulse wave velocity", 1052, 110));
		treelist.add(new CatTree(10523, "Cardio MRI", 1052, 120));
		treelist.add(new CatTree(10524, "Echocardiogram", 1052, 130));
		treelist.add(new CatTree(10525, "Carotid ultrasound", 1052, 140));
		treelist.add(new CatTree(11021, "Body size measures", 1102, 100));
		treelist.add(new CatTree(105251, "IMT", 10525, 100));
		// Get the size of the ArrayList treelist
		int treesize = treelist.size();
		// treelist must be in parent --> children order so...
		treelist = CatTree.putInTreeOrder(treelist);
		// This next bit is the core algorithm for generating HTML Tree navigation
		// ================================================================
		int curparent = 0; // Current parent node in tree
		penv.push(curparent);
		printULstart(); // HTML ul class="tree"
		for (CatTree ct : treelist) { // Loop through all the CatTree objects.
			curparent = ct.getParent();
			if (ct.getIsParent()) { // ct is a heading
				if (curparent == penv.peek()) {
					// New section indented - New parent
					penv.push(ct.getId()); // Push this node onto stack
					printState(ct, penv, "New parent - indent ");
					beginSection(ct.getLabel(), ct.getId());
				} else { // curparent is from previous heading
					// Parent is previous - outdent
					while (ct.getParent() != penv.peek()) {
						System.out.println("Current Parent: " + curparent + " Stack: " + penv);
						curparent = penv.pop();
						endSection();
					}
					penv.push(ct.getId());
					printState(ct, penv, "New parent - outdent to previous and pop last id off stack");
					beginSection(ct.getLabel(), ct.getId());
				}
			} else if (curparent == penv.peek()) { // plain under same heading
				printState(ct, penv, "Plain entry - continue under previous heading");
				printPlain(ct.getLabel(), ct.getId());
			} else { // plain under previous heading
				while (ct.getParent() != penv.peek()) {
					System.out.println("Stack: " + penv);
					curparent = penv.pop();
					endSection();
				}
				printState(ct, penv, "Plain entry - continue under previous heading");
				printPlain(ct.getLabel(), ct.getId());
			}
		}
		endSection();
		printULend(); // End HTML ul tag
		// ================================================================
		// Call the template ...
		for (String line : template) {
			htmlfile.println(line);
		}

		// ==================================================================
	}

	private class CatTree {
		int id;
		String sorted;
		String label;
		int parent;
		int printorder;
		boolean isparent;

		public CatTree(int iid, String ilabel, int iparent, int iprintorder) {
			id = iid;
			sorted = Integer.toString(id);
			label = ilabel;
			parent = iparent;
			printorder = iprintorder;
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
			// Get the list of parent node indexes
			List<Integer> parentList = treelist.stream().map(CatTree::getParent).collect(Collectors.toList());
			for (CatTree ct : treelist) {
				if (parentList.contains(ct.getId())) {
					ct.setIsParent(true);
				} else {
					ct.setIsParent(false);
				}
			}
			// Get all top level parents in list Using Lambda Expression
			List<CatTree> parents = treelist.stream().filter(obj -> obj.getIsParent() == true && obj.getParent() == 0)
					.collect(Collectors.toList()); // List of just parent nodes

			for (CatTree node : parents) { // Go through the parents
				results.add(node); //
				List<CatTree> children = getChildrenOf(node, treelist);
				for (CatTree child : children) {
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
}
