package birch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import chinese_feature_extraction.DF;

public class BIRCH {

	private DF df;
	
	//private String filePath = "C:\\Users\\Administrator\\Desktop\\birch测试数据.txt";
	
	/**
	 * 数据点的属性维度（用于文本聚类则此变量记录的便是特征词的个数）
	 */
    public static int dimen = 0;
    LeafNode leafNodeHead=new LeafNode();
    
    /**
     * 记录数据点的个数，即待聚类文本的篇数
     */
    private int point_num;
    
    /**
     * 接收df实例传过来的待聚类文档名
     */
    private String[] documentName;
    
    /**
     * 接收df实例传过来的特征词
     */
    private ArrayList<String> featureWords;
    
    private HashMap<String, HashMap<String, Double>>documenttfidf;
    
    
    public BIRCH() throws IOException{
    	df = new DF();
    	dimen = df.getFeatureWords().size();
    	documentName = df.getDocumentName();
    	featureWords = df.getFeatureWords();
    	point_num = 0;
    	documenttfidf = new HashMap<String,HashMap<String, Double>>();
    	initDocumennttfidf();
    	
    }
    
    
    /**
     * 计算每篇文本的特征词的tf-idf值
     */
    private void initDocumennttfidf(){
    	for(int i = 0; i < documentName.length; i++){
    		HashMap<String, Double>temphaHashMap = new HashMap<String,Double>();
    		for(int j = 0; j < featureWords.size(); j++){
    			if(df.getText().get(documentName[i]).containsKey(featureWords.get(j))){
    				double value = df.getText().get(documentName[i]).get(featureWords.get(j));
    				value = value * (Math.log(documentName.length / df.getText().get(documentName[i]).get(featureWords.get(j))));
    				temphaHashMap.put(featureWords.get(j), value);
    			}
    			else {
					temphaHashMap.put(featureWords.get(j), 0.0);
				}
    			documenttfidf.put(documentName[i], temphaHashMap);
    		}
    	}
    }
    
    
    
    public  TreeNode buildTree(){
    	//先建立一个叶子节点
    	//调用无参构造函数实例化一个只有空ArrayList的LeafNode对象，其中ArrayList用来存放当前这个叶子节点包含的孩子的个数
        LeafNode leaf=new LeafNode();
        TreeNode root=leaf;
        
      //把叶子节点加入存储叶子节点的双向链表
        leafNodeHead.setNext(leaf);
        leaf.setPre(leafNodeHead);
        
        
        double[] data = new double[dimen];
        for(int i = 0; i < documentName.length; i++){
        	point_num++;
        	for(int j = 0; j < featureWords.size(); j++){
        		data[j] = documenttfidf.get(documentName[i]).get(featureWords.get(j));
        	}
        	String mark=documentName[i];
        	
        	CF cf=new CF(data);
            //调用无参构造函数生成一个空的MinCluster类型的实例
            MinCluster subCluster=new MinCluster();
            //cf和类别信息填充到MinCluster类型的变量中
            subCluster.setCf(cf);
            subCluster.getInst_marks().add(mark);
            
            //把新到的point instance插入树中
            root.absorbSubCluster(subCluster);
            //要始终保证root是树的根节点
            while(root.getParent()!=null){
                root=root.getParent();
            }
        }

        return root;
    }
    
  //打印B-树的所有叶子节点
    public void printLeaf(LeafNode header){
        //point_num清0
        point_num=0;
        while(header.getNext()!=null){
            System.out.println("\n一个叶子节点:");
            header=header.getNext();
            for(MinCluster cluster:header.getChildren()){
                System.out.println("\n一个最小簇:");
                for(String mark:cluster.getInst_marks()){
                    point_num++;
                    System.out.print(mark+"\t");
                }
            }
        }
    }
     
    //打印指定根节点的子树
    public void printTree(TreeNode root){
        if(!root.getClass().getName().equals("birch.LeafNode")){
            NonleafNode nonleaf=(NonleafNode)root;
            for(TreeNode child:nonleaf.getChildren()){
                printTree(child);
            }
        }
        else{
            System.out.println("\n一个叶子节点:");
            LeafNode leaf=(LeafNode)root;
            for(MinCluster cluster:leaf.getChildren()){
                System.out.println("\n一个最小簇:");
                for(String mark:cluster.getInst_marks()){
                    System.out.print(mark+"\t");
                    point_num++;
                }
            }
        }
    }
    
    public static void main(String[] args) throws IOException{
    	BIRCH birch=new BIRCH();
    	TreeNode root=birch.buildTree();
        birch.point_num=0;
        birch.printTree(root);
        System.out.println();
        //birch.printLeaf(birch.leafNodeHead);
        //确认被分类的point instance和扫描数据库时录入的point instance的个数是一致的
        System.out.println(birch.point_num);
    }
    
    
}
