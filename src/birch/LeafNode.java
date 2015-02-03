package birch;

import java.util.ArrayList;

public class LeafNode extends TreeNode{

	/**
	 * 每个叶子节点（LeafNode）最多有10个MinCluster
	 */
    private int L=20;
    
    /**
     *每一个MinCluster的直径不能超过T=2.8 
     */
    private double T=1.5;
    
    /**
     * 当前叶子节点的孩子，每个孩子都包含若干个MinCluster形式的数据
     */
    private ArrayList<MinCluster> children;
    
   
    //叶子节点之间要要通过双向链表连接即要有记录当前叶子节点的左右兄弟节点的变量
     
    /**
     * 当前叶子节点的前一个叶子节点（可理解为其左兄弟节点）
     */
    private LeafNode pre;
    
    /**
     * 当前叶子节点的后一个叶子节点（可理解为其有兄弟节点）
     */
    private LeafNode next;
 
    public LeafNode() {
        children=new ArrayList<MinCluster>();
    }
 
    public LeafNode(double[] data) {
        super(data);
    }
 
    // 节点分裂
    public void split() {
        // 找到距离最远的两个孩子节点
        int c1 = 0;
        int c2 = 0;
        double maxDist = 0;
        //获得当前节点的孩子节点的个数len，然后遍历这len个孩子节点，找到孩子节点中距离最远的两个孩子
        int len = this.getChildren().size();
        for (int i = 0; i < len - 1; i++) {
            for (int j = i + 1; j < len; j++) {
                //double dist = this.getChildren().get(i).getCf()
                //        .getDistanceTo(this.getChildren().get(j).getCf());
            	double dist = MinCluster.getDiameter(this.getChildren().get(i), this
                        .getChildren().get(j));
                if (dist > maxDist) {
                    maxDist = dist;
                    c1 = i;
                    c2 = j;
                }
            }
        }
        // 以距离最远的孩子节点为中心，把B+1个孩子分为两个大簇。其中一个簇仍留作本节点的孩子，另外一簇需要新创建一个节点来领养它们
        LeafNode newNode = new LeafNode();
        newNode.addChild(this.getChildren().get(c2));
        // 如果本节点已经是Root节点，则需要创建一个新的Root节点
        if (this.getParent() == null) {
            NonleafNode root = new NonleafNode();
            root.setN(this.getN());
            root.setLS(this.getLS());
            root.setSS(this.getSS());
            this.setParent(root);
            root.addChild(this);
        }
        //建立新节点和本节点的父节点的父子关系
        //即当前节点和新实例化的节点指向同一个父亲
        newNode.setParent(this.getParent());
        //当前节点的父节点将新实例化的节点加入到其孩子节点的集合中
        ((NonleafNode)this.getParent()).addChild(newNode);
        //把离newNode近的孩子节点归到newNode这个簇里面
        for (int i = 0; i < len; i++) {
            if (i != c1 && i != c2) {
            	if(MinCluster.getDiameter(this.getChildren().get(c2), this.getChildren().get(i)) < 
            			MinCluster.getDiameter(this.getChildren().get(c1), this.getChildren().get(i))){
                    newNode.addChild(this.getChildren().get(i));
                }
            }
        }
        //把离newNode近的孩子节点从本节点中删除
        for (MinCluster cluster : newNode.getChildren()) {
            newNode.addCF(cluster.getCf(), true);
            this.deleteChild(cluster);
            this.addCF(cluster.getCf(), false);
        }
        // 把新增加的LeafNode添加到LeafNode双向链表中
        if (this.getNext() != null) {
            newNode.setNext(this.getNext());
            this.getNext().setPre(newNode);
        }
        this.setNext(newNode);
        newNode.setPre(this);
        // 如果本节点分裂导致父节点的孩子数超过了分枝因子，引发父节点分裂
        NonleafNode pn = (NonleafNode) this.getParent();
        if (pn.getChildren().size() > pn.getB()) {
            this.getParent().split();
        }
    }
 
    @Override
    /**
     * 实现父类TreeNote的方法用以添加新的MinCluster簇
     */
    public void absorbSubCluster(MinCluster cluster) {
        // 先试图找到叶子节点的孩子（一些subcluster）中与cluster最近的簇
        CF cf = cluster.getCf();
        int nearIndex = 0;
        double minDist = Double.MAX_VALUE;
        //len即为调用此函数的根节点（也可能是叶子节点）的孩子的个数
        int len = this.getChildren().size();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
            	double dist = MinCluster.getDiameter(cluster, this
                        .getChildren().get(i));
                if (dist < minDist) {
                    nearIndex = i;
                }
            }
            // 计算两个簇合并后的直径
            double mergeDiameter = cf.getDistanceTo(this.getChildren().get(nearIndex)
                           .getCf());
            // 如果合并后发现簇的直径超过了阈值，则把cluster作为一个单独的孩子插入本叶子节点下
            if (mergeDiameter > T) {
                this.addChild(cluster);
                if (this.getChildren().size() > L) {
                    this.split();
                }
            }
            // 如果不超过阈值，则直接合并两个簇
            // 即如果不超过阈值，则把当前的cluster插入到当前叶子节点的孩子节点中离它·最近的一个孩子节点中
            else {
                this.getChildren().get(nearIndex).mergeCluster(cluster);
            }
        }
        // 创建B树之初，叶子节点还没有children
        else {
            this.addChild(cluster);
        }
        this.addCFUpToRoot(cluster.getCf());
    }
 
    public ArrayList<MinCluster> getChildren() {
        return children;
    }
 
    public void setChildren(ArrayList<MinCluster> children) {
        this.children = children;
    }
 
    public void addChild(MinCluster child) {
        this.children.add(child);
    }
 
    public void deleteChild(MinCluster child) {
        this.children.remove(children.indexOf(child));
    }
 
    public LeafNode getPre() {
        return pre;
    }
 
    public void setPre(LeafNode pre) {
        this.pre = pre;
    }
 
    public LeafNode getNext() {
        return next;
    }
 
    public void setNext(LeafNode next) {
        this.next = next;
    }
 
    public int getL() {
        return L;
    }
 
    public void setL(int l) {
        L = l;
    }
 
    public double getT() {
        return T;
    }
 
    public void setT(double t) {
        T = t;
    }
}
