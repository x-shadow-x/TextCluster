/**
 * 凝聚层次聚类,多线程，使用df做特征词提取
 */

package agens;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import chinese_feature_extraction.DF;


public class AGENS {

	/**
	 * 获得使用DF类提取的特征词和对应权重
	 */
	private DF df;
	
	/**
	 * 记录待聚类文本的文档名
	 */
	private String[] documentName;
	
	/**
	 * 以二维数组的形式记录每篇文档到其他所有文档的距离
	 */
	private double[][] distance;
	
	/**
	 * 记录每篇文本的向量长度，避免算余弦相似度时重复计算
	 */
	private double[] eachDocumentVectorLength;

	/**
	 * 簇的形式，每个簇中存放的是文本的索引
	 */
	private ArrayList<ArrayList<Integer>> cluster;
	
	/**
	 * 记录每篇文本的特征词的tf-idf值
	 */
	private HashMap<String, HashMap<String, Double>>textWordHashMap;
	
	/**
	 * 记录特征词
	 */
	private ArrayList<String> featureWords;
	
	/**
	 * 设定簇的个数
	 */
	private static final int N = 7;
	
	/**
	 * 定义线程数目
	 */
	private static final int numOfThread = 8;
	
	/**
	 * 判断各个线程是否处理完毕
	 */
	private boolean[] isend;
	
	
	public AGENS() throws IOException{
		df = new DF();
		documentName = df.getDocumentName();
		distance = new double[documentName.length][documentName.length];
		featureWords = df.getFeatureWords();
		eachDocumentVectorLength = new double[documentName.length];
		cluster = new ArrayList<ArrayList<Integer>>();
		isend = new boolean[numOfThread];
	    for(int i = 0; i < numOfThread; i++){
	    	isend[i] = false;
	    }
		initTextWordHashMap();
		initEachDocumentVectorLength();
		calculate();
		initCluster();
		agens();
		
		for(int i = 0; i < cluster.size(); i++){
			for(int j = 0; j < cluster.get(i).size(); j++){
				System.out.print(documentName[cluster.get(i).get(j)] + "  ");
			}
			System.out.println("");
		}
		
	}
	
	/**
	 * 计算每篇文本的每个特征词的tf-idf值
	 */
	private void initTextWordHashMap(){
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
	 * 计算每篇文本的向量长度
	 */
	private void initEachDocumentVectorLength(){

		for(int i = 0; i < documentName.length; i++){
			double sum = 0.0;
			HashMap<String, Double>tempHashMap = textWordHashMap.get(documentName[i]);
			for(int j = 0; j < featureWords.size(); j++){
				sum = sum + tempHashMap.get(featureWords.get(j)) * tempHashMap.get(featureWords.get(j));
			}
			sum = Math.sqrt(sum);
			eachDocumentVectorLength[i] = sum;
		}
	}

	
	/**
	 * 记录各篇文章到所有文章的距离
	 */
	private void calculate() {
		
		int begin = 0;
		int length = documentName.length / numOfThread;
		
		
		for(int i = 0; i < numOfThread - 1; i++){
			CalculateThread thread = new CalculateThread(begin,begin + length,i);
			thread.start();
			begin = begin + length;
		}
		CalculateThread thread = new CalculateThread(begin, documentName.length,numOfThread - 1);
		thread.start();
		
		boolean isOK = false;
		while(!isOK){
			isOK = true;
			for(int i = 0; i < numOfThread; i++){
				isOK = isOK && isend[i];
			}
		}
		//跳出这个while循环说明所有线程都已处理完毕~distance数组中存储了所有文档两两之间的距离
		
	}
	
	/**
	 * 初始化各个簇
	 */
	private void initCluster(){
		for(int i = 0; i < documentName.length; i++){
			ArrayList<Integer> arrayList = new ArrayList<Integer>();
			arrayList.add(i);
			cluster.add(arrayList);
		}
	}
	
	/**
	 * 实现凝聚层次聚类核心代码段
	 */
	private void agens(){
		//记录最相似的两个簇
		int index1 = 0;
		int index2 = 0;
		double max = 0.0;
		//================================
		for(int i = 0; i < cluster.size(); i++){
			ArrayList<Integer> cluster1 = cluster.get(i);
			for(int j = i + 1; j < cluster.size(); j++){
				ArrayList<Integer> cluster2 = cluster.get(j);
				double sum = 0.0;
				for(int m = 0; m < cluster1.size(); m++){
					for(int n = 0; n < cluster2.size(); n++){
						sum = sum + distance[cluster1.get(m)][cluster2.get(n)];
					}
				}
				sum = sum / (cluster1.size() * cluster2.size());
				if(max < sum){
					max = sum;
					index1 = i;
					index2 = j;
				}
			}
		}
		
		//System.out.println(cluster.get(index1).size());
		//System.out.println(cluster.get(index2).size());
		
		for(int i = 0; i < cluster.get(index2).size(); i++){
			cluster.get(index1).add(cluster.get(index2).get(i));

		}
		cluster.remove(index2);
		
		if(cluster.size() <= N)
			return;
		else 
			agens();
			
	}
	
	class CalculateThread extends Thread{
		
		private int begin;
		private int end;
		private int index;
		
		public CalculateThread(int begin,int end,int index){
			this.begin = begin;
			this.end = end;
			this.index = index;
		}
		
		public void run(){
			
			for(int i = begin; i < end; i++){
				HashMap<String, Double> text = textWordHashMap.get(documentName[i]);
				
				int length = text.size();
				String[] key = new String[length];
				text.keySet().toArray(key);
				for(int j = i; j < documentName.length; j++){
					
					HashMap<String, Double> text2 = textWordHashMap.get(documentName[j]);
					double value = 0.0;
					
					for(int k = 0; k < featureWords.size(); k++){
						value = value + text.get(featureWords.get(k)) * text2.get(featureWords.get(k));
					}
					value = value / (eachDocumentVectorLength[i] * eachDocumentVectorLength[j]);	
		
					distance[i][j] = value; 
					distance[j][i] = value;
				}
			}
			
			isend[index] = true;

		}
	}
	
	/**
	 * 仅仅被凝聚层次聚类实例对象调用输出完成信息，避免凝聚层次聚类算法类的实例对象没有被使用过而弹出警告
	 */
	public void information(){
		System.out.println("AGENS is finish!");
	}
	
	public static void main(String[] args) throws IOException{
		
		AGENS agens = new AGENS();
		agens.information();
	}
	
	
}
