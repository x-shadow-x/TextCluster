/**
 * DBSCAN基于密度的文本聚类，使用df做特征词提取
 */

package dbscan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import agens.AGENS;
import chinese_feature_extraction.DF;

public class DBSCAN {
	
	/**
	 * 获得使用DF类提取的特征词和对应的权重
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
	 * 记录每个点距离第四近的点的距离
	 */
	private double[] fourthDis;
	
	/**
	 * 记录每篇文本的向量长度，避免算余弦相似度时重复计算
	 */
	private double[] eachDocumentVectorLength;
	
	/**
	 * 簇的形式，每个簇中存放的是文本的索引
	 */
	private ArrayList<ArrayList<String>> cluster;
	
	/**
	 * 记录每篇文本的特征词的tf-idf值，形式为：<文档名，<词语，对应tfidf值>>
	 */
	private HashMap<String, HashMap<String, Double>>textWordHashMap;
	
	/**
	 * 记录特征词
	 */
	private ArrayList<String> featureWords;
	
	/**
	 * 用来记录有哪些点是核心点，并且这些点的直接密度可达点有哪些
	 */
	private HashMap<Integer, ArrayList<Integer>> coreHashMap;
	
	/**
	 * 用以记录核心点是否已经被处理过，起初此变量记录了所有的核心点，每处理一个核心点就从此集合中移除该数据点
	 */
	private HashSet<Integer> recoreOfCore;
	
	/**
	 * 记录每个数据点的eps领域内的数据点的数目
	 */
	private int[] numOfEachDot;
	
	/**
	 * 指定一个簇中所应该包含的最小数据点数目~也用以判断当前点是否为核心点
	 */
	private double minPts;
	
	/**
	 * 指定Eps领域的半径
	 */
	private double Eps;
	
	/**
	 * 定义线程数目
	 */
	private static final int numOfThread = 8;
	
	/**
	 * 判断各个线程是否处理完毕
	 */
	private boolean[] isend;
	
	public DBSCAN() throws IOException{
		df = new DF();
		documentName = df.getDocumentName();
		distance = new double[documentName.length][documentName.length];
		fourthDis = new double[documentName.length];
		numOfEachDot = new int[documentName.length];
		featureWords = df.getFeatureWords();
		eachDocumentVectorLength = new double[documentName.length];
		cluster = new ArrayList<ArrayList<String>>();
		coreHashMap = new HashMap<Integer,ArrayList<Integer>>();
		recoreOfCore = new HashSet<Integer>();
		
		isend = new boolean[numOfThread];
	    for(int i = 0; i < numOfThread; i++){
	    	isend[i] = false;
	    }
	    
	    initTextWordHashMap();
		initEachDocumentVectorLength();
		calculate();
		choseEps();
		choseMinPts();
		findTheCore();
		System.out.println("Eps = " + Eps);
		System.out.println("minPts = " + minPts);
		
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
	 * 自动选取合适的Eps值
	 */
	private void choseEps(){
		double[][] tempDistance = new double[documentName.length][documentName.length];
		for(int i = 0; i < documentName.length; i++){
			for(int j = 0; j < documentName.length; j++){
				tempDistance[i][j] = distance[i][j];
			}
		}
		
		for(int i = 0; i < documentName.length; i++){
			Arrays.sort(tempDistance[i]);
		}
		for(int i = 0; i < documentName.length; i++){//根据论文上画出的图~指出以第四近的距离的均值作为Eps的值，在程序中distinct数组中存储的是两个数据点的余弦相似度，个第四近等价于相似度第四大
			fourthDis[i] = tempDistance[i][documentName.length - 4];
		}
		double sum = 0.0;
		for(int i = 0; i < documentName.length; i++){
			sum = fourthDis[i] + sum;
		}
		Eps = sum / documentName.length;	
	}
	
	/**
	 * 自动选取合适的minPts
	 */
	private void choseMinPts(){
		for(int i = 0; i < documentName.length; i++){
			int num = 0;
			for(int j = 0; j < documentName.length; j++){
				if(distance[i][j] >= Eps)
					num = num + 1;
			}
			numOfEachDot[i] = num;
		}
		double sum = 0.0;
		for(int i = 0; i < documentName.length; i++){
			sum = sum + numOfEachDot[i];
		}
		minPts = sum / documentName.length;
		
	}
	
	/**
	 * 遍历所有的数据点，找到那些核心点以及其对应的直接密度可达的数据点
	 */
	private void findTheCore(){
		for(int i = 0; i < documentName.length; i++){
			int num = 0;
			ArrayList<Integer> tempArrayList = new ArrayList<Integer>();
			for(int j = 0; j < documentName.length; j++){
				if(distance[i][j] >= Eps){
					num = num + 1;
					tempArrayList.add(j);
				}
			}
			if(num >= minPts){//若条件成立说明当前点是核心点
				coreHashMap.put(i, tempArrayList);
				recoreOfCore.add(i);//将核心点下表加入到记录未处理的核心点的变量中
			}
			else {
				ArrayList<Integer> tempArrayList2 = new ArrayList<Integer>();
				coreHashMap.put(i, tempArrayList2);
			}
		}
	}
	
	
	/*private void findTheCore(){
		for(int i = 0; i < documentName.length; i++){
			int num = 0;
			ArrayList<Integer> tempArrayList = new ArrayList<Integer>();
			for(int j = 0; j < documentName.length; j++){
				if(distance[i][j] >= Eps){
					num = num + 1;
					tempArrayList.add(j);
				}
			}
			if(num >= minPts){//若条件成立说明当前点是核心点
				coreHashMap.put(i, tempArrayList);
				recoreOfCore.add(i);//将核心点下表加入到记录未处理的核心点的变量中
			}
			else {
				ArrayList<Integer> tempArrayList2 = new ArrayList<Integer>();
				coreHashMap.put(i, tempArrayList2);
			}
		}
	}
	*/
	
	/**
	 * dbscan核心代码段
	 */
	 public void dbscan(){
		 for(int i = 0; i < documentName.length; i++){
			 HashSet<Integer> tempHashSet = new HashSet<Integer>();
			 if(recoreOfCore.contains(i)){//条件成立说明当前点是核心点,下面开始处理改核心点对应的直接密度可达的数据点，同时将该核心点从未处理核心点列表中移除
				 recoreOfCore.remove(i);
				 ArrayList<Integer> tempArrayList = coreHashMap.get(i);
				 for(int j = 0; j < tempArrayList.size(); j++){
					 tempHashSet.add(tempArrayList.get(j));
				 }
				 for(int k = 0; k < tempHashSet.size(); k++){//每次运行到这里的初次，tempHashSet中存储的都是和第i个核心点直接密度可达的数据点，进入for循环后遍历此变量以发现是否还有核心点
					 if(recoreOfCore.contains(tempHashSet.toArray()[k])){
						 recoreOfCore.remove(tempHashSet.toArray()[k]);
						 for(int m = 0; m < coreHashMap.get(tempHashSet.toArray()[k]).size(); m++){//---------------A
							 tempHashSet.add(coreHashMap.get(tempHashSet.toArray()[k]).get(m));
						 }
						 //进入if语句块说明在第i个核心点的直接密度可达数据点中发现了新的核心点，进过A处的for循环，tempHashSet有可能增加了新的数据点，
						 //如果tempHashSet新加入了数据点则还需继续判断新加入的点是否也为核心点，因为无法判断新的数据点被加入到tempHashSet中的哪个位置（人家是按哈希来排的嘛~天晓得）
						 //所以将k先置为-1，回到for循环k++，即k又变为0，重新开始遍历tempHashSet，同时用recoreOfCore来保证处理过的核心点被移除出去，以保证不会重复处理核心点，
						 //若想跳出k的for循环，只有tempHashSet中不再有未处理的核心点，此时tempHashSet中就保存了第i个核心点为基础建立的簇所应包含的所有的数据点
						 k = -1;
					 }
				 }//此for循环结束后就找到了一个新的簇以及该簇包含的所有的数据点
				 
				 ArrayList<String> newCluster = new ArrayList<String>();
				 for(int j =0; j < tempHashSet.size(); j++){
					 newCluster.add(documentName[(int) tempHashSet.toArray()[j]]);
				 }
				 cluster.add(newCluster);
			 }
		 }
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
	
	public ArrayList<ArrayList<String>> getCluster() {
		return cluster;
	}
	
	public static void main(String[] args) throws IOException{
		DBSCAN mydbsacn = new DBSCAN();
		long startTime=System.currentTimeMillis();
		System.out.println("begin");
		mydbsacn.dbscan();
		long endTime=System.currentTimeMillis();
		System.out.println((endTime-startTime) + "ms");
		ArrayList<ArrayList<String>> result = mydbsacn.getCluster();
		System.out.println(result.size());
		for(int i = 0; i < result.size();i++){
			for(int j = 0; j < result.get(i).size(); j++){
				System.out.print(result.get(i).toArray()[j] + "  ");
			}
			System.out.println("");
		}
	}
	

}
