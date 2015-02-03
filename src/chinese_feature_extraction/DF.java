/**
 * 基于文档频率做特征词提取
 */
package chinese_feature_extraction;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;



/**
 * 基于词语的文档频率进行特征词语的提取
 * @author Administrator
 */
public class DF {
	
	/**
	 * 聚类文本文件路径
	 */
	private String filePath = "E:\\java_Eclipse\\语料\\文本分类语料库\\测试";
	//private String filePath = "C:\\Users\\Administrator\\Desktop\\测试1";
	
	/**
	 * 存储待聚类文本文本的所有分词（不重复），以便作索引，对每一个词计算文档频率，求得符合要求的词语作为聚类的特征词
	 */
	private HashSet<String> allWord;
	
	/**
	 * 存储最后提取出来的特征词
	 */
	private ArrayList<String> featureWords;
	
	/**
	 * 记录文档信息，形式为：文档名——{词1：词频，词2：词频...}
	 * 此变量可以做为其他信息的计算基础，例如在此基础上可以计算tfidf，通过每篇文档的词1，词2这些key可以利用哈希表的优点快速判断该篇文本是否包含特定词，从而快速计算出df
	 */
	private HashMap<String, HashMap<String, Double>> text;
	
	/**
	 * 存放文档名
	 */
	private String[] documentName;
	
	/**
	 * 构建词-文档频率键值对
	 */
	private HashMap<String, Integer> wordDF;
	
	/**
	 * 提取的特征词数目
	 */
	private final int numOfFeature = 2500;
	
	
	
	
	
	public DF() throws IOException{
		
		text = new HashMap<String,HashMap<String, Double>>();
		
		
		File documentDirectory = new File(filePath);
		
		if (!documentDirectory.isDirectory())           //检查这个文件对象是否为文件夹,因为我们是把待处理的文章保存到这个文件夹下的,故要作此判断
        {
            throw new IllegalArgumentException("特征选择文档搜索失败！ [" + filePath + "]");
        }
		documentName = documentDirectory.list();
		
		initWordDocumentAndAllWords();
		
		initWordDF();
		initFeature();
		//System.out.println(allWord);
		System.out.println(featureWords);
	//	for(int i = 0; i < documentName.length; i++){
	//		System.out.println(text.get(documentName[i]));
	//	}

	}
	
	/**
	 * 初始化wordDocument和allwords
	 * @param documentName
	 * @throws IOException
	 */
	private void initWordDocumentAndAllWords() throws IOException{
		
		StopWordsHandle stopWordsHandle = new StopWordsHandle();
		NlpirMethod.Nlpir_init();//初始化分词器
		allWord = new HashSet<String>();
		//wordDocument = new ArrayList<HashSet<String>>();
		for(int i = 0; i < documentName.length; i++){
			InputStreamReader isReader = new InputStreamReader(new FileInputStream(filePath + File.separator + documentName[i]),"GBK");
			BufferedReader reader = new BufferedReader(isReader);
			StringBuilder stringBuilder = new StringBuilder();
			String aline = "";
			while((aline = reader.readLine()) != null){
				stringBuilder.append(aline);
			}
			isReader.close();
			reader.close();
			String []tempStrings=NlpirMethod.NLPIR_ParagraphProcess(stringBuilder.toString(), 0).split(" ");
			ArrayList<String> tempArrayList = new ArrayList<String>(stopWordsHandle.getNewText(tempStrings));//去除停用词
			//eachTextArrayList.add(tempArrayList);
			HashSet<String> tempSet = new HashSet<String>();
			HashMap<String, Double>tempHashMap = new HashMap<String,Double>();
			for(int j = 0; j < tempArrayList.size(); j++){
				tempSet.add(tempArrayList.get(j));
				allWord.add(tempArrayList.get(j));
			}
			String[] tempStrings2 = new String[tempSet.size()];
			tempSet.toArray(tempStrings2);
			for(int j = 0; j < tempStrings2.length; j++){
				tempHashMap.put(tempStrings2[j], 0.0);
			}
			for(int j = 0; j < tempArrayList.size(); j++){
				double value = tempHashMap.get(tempArrayList.get(j)) + 1.0;
				tempHashMap.put(tempArrayList.get(j), value);
			}
			for(int j = 0; j < tempStrings2.length; j++){
				double value = tempHashMap.get(tempStrings2[j]) / tempArrayList.size();
				tempHashMap.put(tempStrings2[j], value);
			}
			text.put(documentName[i], tempHashMap);
			//wordDocument.add(tempSet);
		}
		
	}
	
	
	
	/**
	 * 初始化分词文档频率键值对
	 */
	private void initWordDF(){
		wordDF = new HashMap<String,Integer>();
		String[] wordStrings = new String[allWord.size()];
		allWord.toArray(wordStrings);
		int df = 0;
		for(int i = 0; i < wordStrings.length; i++){
			df = 0;
			for(int j =0; j < documentName.length; j++){
				if(text.get(documentName[j]).containsKey(wordStrings[i])){
					df = df + 1;
				}
			}
			wordDF.put(wordStrings[i], df);
		}
		
	}

	/**
	 * 对分词文档频率键值对进行排序并获取符合要求的特征词语
	 */
	private void initFeature(){
		featureWords = new ArrayList<String>();
		List<Map.Entry<String, Integer>> list_Data = new ArrayList<Map.Entry<String, Integer>>(wordDF.entrySet()); 
	    System.out.println(wordDF);
	    Collections.sort(list_Data, new Comparator<Map.Entry<String, Integer>>(){    
	      public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2)  
	      {  
	        if ((o2.getValue() - o1.getValue())>0)  
	          return 1;  
	        else if((o2.getValue() - o1.getValue())==0)  
	          return 0;  
	        else   
	          return -1;
	        }  
	      }  
	    );  
        
	    for(int i = 0; i < numOfFeature; i++){
	    	featureWords.add(list_Data.get(i).getKey());
	    }
	}

	public ArrayList<String> getFeatureWords(){
		return featureWords;
	}
	
	public HashMap<String, Integer> getWordDF() {
		return wordDF;
	}
	
	public HashMap<String, HashMap<String, Double>> getText() {
		return text;
	}
	
	public String[] getDocumentName() {
		return documentName;
	}
	
	/*
	class TextHandleThread extends Thread{
		private int begin;
		private int end;
		private int index;
		private HashSet<String>threadResult;
		
		public TextHandleThread(int begin,int end,int index){
			
			this.begin = begin;
			this.end = end;
			switch (index) {
			case 0:
				threadResult = threadResult1;
				break;
			case 1:
				threadResult = threadResult2;
				break;
			case 2:
				threadResult = threadResult3;
				break;
			case 3:
				threadResult = threadResult4;
				break;
			case 4:
				threadResult = threadResult5;
				break;
			case 5:
				threadResult = threadResult6;
				break;
			case 6:
				threadResult = threadResult7;
				break;
			case 7:
				threadResult = threadResult8;
				break;
			default:
				break;
			}
			this.index = index;
			
		}
		
		public void run(){
			
			for(int i = begin; i < end; i++){
				try {
					InputStreamReader isReader;
					isReader = new InputStreamReader(new FileInputStream(filePath + File.separator + documentName[i]),"GBK");
					BufferedReader reader = new BufferedReader(isReader);
					StringBuilder stringBuilder = new StringBuilder();
					String aline = "";
					while((aline = reader.readLine()) != null){
						stringBuilder.append(aline);
					}
					isReader.close();
					reader.close();
					String []tempStrings=NlpirMethod.NLPIR_ParagraphProcess(stringBuilder.toString(), 0).split(" ");
					ArrayList<String> tempArrayList = new ArrayList<String>(stopWordsHandle.getNewText(tempStrings));//去除停用词
					
					HashSet<String> tempSet = new HashSet<String>();
					
					for(int j = 0; j < tempArrayList.size(); j++){
						tempSet.add(tempArrayList.get(j));
						threadResult.add(tempArrayList.get(j));
						
					}
					
					HashMap<String, Double> temphHashMap = new HashMap<String,Double>();
					String[] tempStrings2 = new String[tempSet.size()];
					tempSet.toArray(tempStrings2);
					for(int j = 0; j < tempStrings2.length; j++){
						temphHashMap.put(tempStrings2[j], 0.0);
					}//先准备好hashmap避免下面统计词频时重复判断hashmap是否已经包含当前词语
					
					//统计此篇文本各个词语的词频~存成词——词频的键值对的形式
					for(int j = 0; j < tempArrayList.size(); j++){
						double value = temphHashMap.get(tempArrayList.get(j)) + 1.0;
						temphHashMap.put(tempArrayList.get(j), value);
					}
					
					for(int j = 0; j < tempStrings2.length; j++){
						double value = temphHashMap.get(tempArrayList.get(j)) / tempArrayList.size();
						temphHashMap.put(tempStrings2[j], value);
					}//此for循环结束后temphHashMap中存放了此篇文本的各个词语对应的词频
					
					
					text.put(documentName[i], temphHashMap);
					
					
					
				} catch (IOException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
				
				
			}
			System.out.println(index + "true");
			isend[index] = true;
			
		}
	}
	*/
	/*public static void main(String[] args) throws IOException{
		DF df = new DF();
	}*/
	
	
	

}
