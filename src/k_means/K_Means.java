package k_means;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import chinese_feature_extraction.DF;



public class K_Means {
	
	
	private static int N = 0;
	
	private DF df;
	
	/**
	 * 将所有待聚类文档以向量空间的形式表示同时用tf-idf值填充向量空间
	 */
	private HashMap<String, HashMap<String, Double>> textWordHashMap;
	
	/**
	 * 记录中心的值
	 */
	private ArrayList<HashMap<String, Double>> center;
	
	/**
	 * 记录文本的名字以将其归入适合的簇中心
	 */
	private String[] documentName;
	
	/**
	 * 因为在计算某一篇文章和各个中心的余弦相似度时，这篇文章的向量长度是不变的，没有必要重复计算，故设置此变量用以保存每篇文本的向量长度
	 */
	private HashMap<String, Double>textVectorLengthHashMap;
	
	/**
	 * 记录各个簇中的文本的名字，即最后结果
	 */
	private ArrayList<ArrayList<String>> cluster;
	
	/**
	 * 记录特征词作为余弦相似度等计算的索引
	 */
	private ArrayList<String>featureWords;
	
	/**
	 * 中心簇的个数
	 */
	private static int k = 7;
	
	public K_Means() throws IOException{
		center = new ArrayList<HashMap<String, Double>>();
	//	center2 = new ArrayList<HashMap<String, Double>>();
		df = new DF();//时间长
		documentName = df.getDocumentName();
		textVectorLengthHashMap = new HashMap<String,Double>();
		featureWords = df.getFeatureWords();
		cluster = new ArrayList<ArrayList<String>>();
		initTextWordsHashMap();
		initTextVectorLengthHashMap();
		initCluster();
		initCenter();
		calculate();
	}
	
	/**
	 * 计算每篇文本的每个特征词的tf-idf值并填充到textWordHashMap中
	 */
	private void initTextWordsHashMap(){
		HashMap<String, Integer>wordDF = df.getWordDF();
		textWordHashMap = new HashMap<String,HashMap<String, Double>>();
		HashMap<String, HashMap<String, Double>> text = df.getText();
		for(int i = 0; i < documentName.length; i++){
			HashMap<String, Double> wordtf = text.get(documentName[i]);
			HashMap<String, Double> temphHashMap = new HashMap<String,Double>();
			for(int j = 0; j < featureWords.size(); j++){
				if(wordtf.containsKey(featureWords.get(j))){
					double value = wordtf.get(featureWords.get(j));
					value = value * Math.log(documentName.length / wordDF.get(featureWords.get(j)));
					temphHashMap.put(featureWords.get(j), value);
				}
				else {
					temphHashMap.put(featureWords.get(j), 0.0);
				}	
			}
			textWordHashMap.put(documentName[i], temphHashMap);
		}
	}
	
	/**
	 * 初始化聚类中心，采用随机选取聚类中心的方式
	 */
	private void initCenter(){
		HashSet<Integer> set = new HashSet<Integer>();//用以记录随机生成的文本的索引，防止出现重复，导致真实产生的中心小于k值
		
		while(set.size() < k){
			int centerIndex = (int)(Math.random() * documentName.length);
			if(!(set.contains(centerIndex)))
				set.add(centerIndex);
			else 
				continue;
			center.add(textWordHashMap.get(documentName[centerIndex]));
			
		}
		/*for(int i = 0; i < 144; i=i+18){
			center.add(textWordHashMap.get(documentName[i]));
			center2.add(textWordHashMap.get(documentName[i]));
		}*/
		
	}
	
	
	/**
	 * 计算每篇文本的向量长度
	 */
	private void initTextVectorLengthHashMap(){
		double C = 0.0;
		for(int i = 0; i < documentName.length; i++){
			for(int j = 0; j < featureWords.size(); j++){
				HashMap<String, Double> textHashMap = textWordHashMap.get(documentName[i]);
				C = C + textHashMap.get(featureWords.get(j)) * textHashMap.get(featureWords.get(j));
			}
			C = Math.sqrt(C);
			textVectorLengthHashMap.put(documentName[i], C);	
		}
		System.out.println(textVectorLengthHashMap);

	}
	
	
	/**
	 * 初始化存放结果的cluster
	 */
	private void initCluster(){
		for(int i = 0; i < k; i++){
			cluster.add(new ArrayList<String>());
		}
	}
	
	
	/**
	 * 更新簇中心的值
	 */
	private void updateCenter(int index,HashMap<String, Double>textHashMap){
		HashMap<String, Double>tempHashMap = center.get(index);
		int length = cluster.get(index).size();
		for(int i = 0; i < featureWords.size(); i++){//以特征词作为索引
			double value = ((length - 1) * tempHashMap.get(featureWords.get(i)) + textHashMap.get(featureWords.get(i))) / length;
			tempHashMap.put(featureWords.get(i), value);
		}
	}
	
	
	/**
	 * 判断所有簇的中心是否发生了变化，即是否需要继续递归
	 * @return
	 */
	private boolean isContinue(ArrayList<HashMap<String, Double>> center2){
		if(center.equals(center2))
			return false;//簇中心没有发生变化，k-means算法结束
		else 
			return true;//簇中心发生了变化，需要继续迭代
		
	}
	

	/**
	 * 计算各个文本到各个中心的距离
	 */
	private void calculate(){
		
		for(int i = 0; i < k; i++){
			cluster.get(i).add("center");
		}
		
		System.out.println("N = ===========================" + N);
		N = N + 1;
		
		ArrayList<HashMap<String, Double>> center2 = new ArrayList<HashMap<String,Double>>();
		for(int i = 0; i < center.size(); i++){
			center2.add(new HashMap<String,Double>(center.get(i)));
		}
		
		
		HashMap<String, Double> centerValueHashMap = new HashMap<String,Double>();
		
		for(int i = 0; i < documentName.length; i++){
			HashMap<String, Double> textHashMap = textWordHashMap.get(documentName[i]);
			double max = 0.0;
			double C = textVectorLengthHashMap.get(documentName[i]);//直接获得当前文本的向量长度，避免重复计算
			
			int index = 0;//记录当前文本所属簇的索引
			for(int j = 0; j < center.size(); j++){
				centerValueHashMap = center.get(j);

				double temp = 0.0;
				double A = 0.0;//余弦相似度公式中分子部分，即当前文档和当前选定的簇中心的各个向量的分量的乘积和
				double B = 0.0;//当前选定的簇的中心的向量长度
				
				for(int m = 0; m < featureWords.size(); m++){
					A = A + centerValueHashMap.get(featureWords.get(m)) * textHashMap.get(featureWords.get(m));
					B = B + centerValueHashMap.get(featureWords.get(m)) * centerValueHashMap.get(featureWords.get(m));
				}
				temp = A / (C * Math.sqrt(B));
				
				
				//判断与当前中心的相似度是否比先前最优的簇中心的相似度还要大，若是，则更新当前文本的最优簇中心,即更新索引值
				 
				if(max < temp){
					max = temp;
					index = j;
				}
			}//计算完成后即得到当前第i篇文本所属的最优簇中心
			
			//同步更新该中心的值
			cluster.get(index).add(documentName[i]);
			updateCenter(index,textHashMap);
			//cluster.get(index).add(documentName[i]);
		}
		
		
		if(isContinue(center2)){
			if (N == 24)
				return;
			for(int i = 0; i < k; i++){
				cluster.get(i).clear();
			}
			
			calculate();
		}
	}

	public ArrayList<ArrayList<String>> getCluster() {
		return cluster;
	}
	
	public ArrayList<String> getFeatureWordStrings() {
		return featureWords;
	}
	
	public static void main(String[] args) throws IOException{
		K_Means k_means = new K_Means();
		
		ArrayList<ArrayList<String>> result = k_means.getCluster();
		for(int i = 0; i < 7; i++){
			System.out.println(result.get(i));
		}
		
		System.out.println(k_means.getFeatureWordStrings());

	}
}
