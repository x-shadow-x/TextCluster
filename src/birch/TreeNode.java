package birch;

public abstract class TreeNode extends CF{

	private TreeNode parent;
	 
    public TreeNode() {
         
    }
     
    public TreeNode(double[] data) {
        super(data);
    }
 
    public TreeNode getParent() {
        return parent;
    }
 
    public void setParent(TreeNode parent) {
        this.parent = parent;
    }
     
    public void addCFUpToRoot(CF cf){
        TreeNode node=this;
        while(node!=null){
        	//addCF是TreNode继承自CF的方法
            node.addCF(cf, true);
            //getParent是TreeNode自己的方法
            node=node.getParent();
        }
    }
     
    abstract void split();
     
    abstract void absorbSubCluster(MinCluster cluster);
}
