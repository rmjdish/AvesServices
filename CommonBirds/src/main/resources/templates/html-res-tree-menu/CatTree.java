private class CatTree {
    int id;
    String node;
    int parent;
    boolean isparent;

    public CatTree(int id, String node, int parent, boolean isparent) {
	this.id = id;
	this.node = node;
	this.parent = parent;
	this.isparent = isparent;
    }

    public String getNode() {
	return this.node;
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

}

