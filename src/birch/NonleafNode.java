package birch;

import java.util.ArrayList;

public class NonleafNode extends TreeNode{

	private int B=5;
    private ArrayList<TreeNode> children;
 
    public NonleafNode() {
        children=new ArrayList<TreeNode>();
    }
 
    public NonleafNode(double[] data) {
        super(data);
    }
 
    // 节点分裂
    public void split() {
        // 找到距离最远的两个孩子节点
        int c1 = 0;
        int c2 = 0;
        double maxDist = 0;
        int len = this.getChildren().size();
        for (int i = 0; i < len - 1; i++) {
            for (int j = i + 1; j < len; j++) {
                double dist = this.getChildren().get(i)
                        .getDistanceTo(this.getChildren().get(j));
                if (dist > maxDist) {
                    maxDist = dist;
                    c1 = i;
                    c2 = j;
                }
            }
        }
        // 以距离最远的孩子节点为中心，把B+1个孩子分为两个大簇。其中一个簇仍留作本节点的孩子，另外一簇需要新创建一个节点来领养它们
        NonleafNode newNode = new NonleafNode();
        newNode.addChild(this.getChildren().get(c2));
        //如果本节点已经是Root节点，则需要创建一个新的Root节点
        if(this.getParent()==null){
            NonleafNode root= new NonleafNode();
            root.setN(this.getN());
            root.setLS(this.getLS());
            root.setSS(this.getSS());
            root.addChild(this);
            this.setParent(root);
        }
        newNode.setParent(this.getParent());
        ((NonleafNode)this.getParent()).addChild(newNode);
        for (int i = 0; i < len; i++) {
            if (i != c1 && i != c2) {
                if (this.getChildren().get(i)
                        .getDistanceTo(this.getChildren().get(c2)) < this
                        .getChildren().get(i)
                        .getDistanceTo(this.getChildren().get(c1))) {
                    newNode.addChild(this.getChildren().get(i));
                }
            }
        }
        for (TreeNode entry : newNode.getChildren()) {
            newNode.addCF(entry, true);
            this.deleteChild(entry);
            this.addCF(entry, false);
        }
        //如果本节点分裂导致父节点的孩子数超过了分枝因子，引发父节点分裂
        NonleafNode pn=(NonleafNode)this.getParent();
        if(pn.getChildren().size()>B){
            this.getParent().split();
        }
    }
    public void absorbSubCluster(MinCluster cluster){
        //从本节点的孩子中寻找与cluster最近的子节点
        CF cf=cluster.getCf();
        int nearIndex=0;
        double minDist=Double.MAX_VALUE;
        for(int i=0;i<this.getChildren().size();i++){
            double dist=cf.getDistanceTo(this.getChildren().get(i));
            if(dist<minDist){
                nearIndex=i;
            }
        }
        //让那个最近的子节点absorb掉这个新到的cluster
        this.getChildren().get(nearIndex).absorbSubCluster(cluster);
    }
 
    public ArrayList<TreeNode> getChildren() {
        return children;
    }
 
    public void setChildren(ArrayList<TreeNode> children) {
        this.children = children;
    }
 
    public void addChild(TreeNode child) {
        this.children.add(child);
    }
 
    public void deleteChild(TreeNode child) {
        this.children.remove(children.indexOf(child));
    }
 
    public int getB() {
        return B;
    }
 
    public void setB(int b) {
        B = b;
    }
}
