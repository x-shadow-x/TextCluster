package birch;

import java.util.ArrayList;

public class MinCluster {

	private CF cf;
    private ArrayList<String> inst_marks;
     
    public MinCluster(){
        cf=new CF();
        inst_marks=new ArrayList<String>();
    }
 
    public CF getCf() {
        return cf;
    }
 
    public void setCf(CF cf) {
        this.cf = cf;
    }
 
    public ArrayList<String> getInst_marks() {
        return inst_marks;
    }
 
    public void setInst_marks(ArrayList<String> inst_marks) {
        this.inst_marks = inst_marks;
    }
     
    //计算两个簇之间的距离
    public static double getDiameter(CF cf){
        double diameter=0.0;
        int n=cf.getN();
        for(int i=0;i<cf.getLS().length;i++){
            double ls=cf.getLS()[i];
            double ss=cf.getSS()[i];
            diameter=diameter+(2*n*ss-2*ls*ls);
        }
        diameter=diameter/(n*n-n);
        return Math.sqrt(diameter);
    }
     
    //计算和另外一个簇合并后的直径
    public static double getDiameter(MinCluster cluster1,MinCluster cluster2){
        CF cf=new CF(cluster1.getCf());
        cf.addCF(cluster2.getCf(), true);
        return getDiameter(cf);
    }
    
    /**
     * 合并两个簇
     * @param cluster
     */
    public void mergeCluster(MinCluster cluster){
    	//调用此函数的是与当前cluster最接近的一个孩子节点，this指的就是这个孩子节点
        this.getCf().addCF(cluster.getCf(), true);
        //cluster的类型虽然是MinCluster，但一个cluster不一定只包含一个数据点，故在下面的foe循环保证将新加进来的cluster中的每个数据点的类别信息都加进新的cluster中，当然下面的for循环和本身算法关系不大
        //仅仅是为了输出结果时可以看到数据点原本类别，方便我们评估结果
        for(int i=0;i<cluster.getInst_marks().size();i++){
            this.getInst_marks().add(cluster.getInst_marks().get(i));
        }
    }
}
