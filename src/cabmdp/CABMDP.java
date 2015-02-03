/**
 * 一趟聚类
 */

package cabmdp;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import chinese_feature_extraction.DF;

public class CABMDP {

	/**
	 * 记录簇中心的值
	 */
	private ArrayList<HashMap<String, Double>>center;	
	/**
	 * 记录各个簇的文本的名字，即最后结果
	 */
	private ArrayList<ArrayList<String>> cluster;	
	/**
	 * 记录特征词
	 */
	private ArrayList<String> featureWords;
	
	/**
	 * 记录没篇文本的特征词的tf-idf值
	 */
	private HashMap<String, HashMap<String, Double>>textWordHashMap;
	/**
	 * 记录各个文档的名字
	 */
	private String[] documentName;
	/**
	 * 获得待聚类文本的存放路径
	 */
	private String filePath;

	
	/**
	 * 保存每篇文本的向量程度，在计算余弦相似度时防止重复计算
	 */
	private HashMap<String, Double> eachTextVectorLength;
	/**
	 * 文本到簇中心的相似性阈值，若计算结果小于此阈值则新生成一个簇中心
	 */
	private double R;
	
	/**
	 * 在文档集合中随机选取的N0对文本用以计算平均距离以确定阈值范围
	 */
	private static final int N0 = 2500;
	private DF df;
	
	public CABMDP() throws IOException{
		filePath = "E:\\java_Eclipse\\语料\\文本分类语料库\\测试";
		//filePath = "C:\\Users\\Administrator\\Desktop\\测试";
		center = new ArrayList<HashMap<String, Double>>();
		df = new DF();
		featureWords = new ArrayList<String>(df.getFeatureWords());
		cluster = new ArrayList<ArrayList<String>>();
		initDocumentNAme();
		initTextWordHashMap();
		initEachTextVectorLength();
		initCenter();
		calaulateR();
		calculate();
		for(int i = 0; i < cluster.size(); i++){
			System.out.println("+++" + cluster.get(i));
		}
		System.out.println("");
		
	}
	/**
	 * 初始化documentName
	 */
	private void initDocumentNAme(){
		File documentDirectory = new File(filePath);
		
		if (!documentDirectory.isDirectory())           //检查这个文件对象是否为文件夹,因为我们是把待处理的文章保存到这个文件夹下的,故要作此判断
        {
            throw new IllegalArgumentException("特征选择文档搜索失败！ [" + filePath + "]");
        }
		documentName = documentDirectory.list();
	}

	
	/**
	 * 初始化textWordHashMap
	 */
	private void initTextWordHashMap(){
		HashMap<String, Integer>wordDF = df.getWordDF();
		textWordHashMap = new HashMap<String,HashMap<String, Double>>();
		HashMap<String, HashMap<String, Double>> text = df.getText();
		for(int i = 0; i < documentName.length; i++){
			
			HashMap<String, Double> wordtf = text.get(documentName[i]);
			HashMap<String, Double> temphHashMap = new HashMap<String,Double>();
			for(int j = 0; j < featureWords.size(); j++){
				//System.out.println(featureWords.get(j));
				//System.out.println(documentName[i]);
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
	private void initEachTextVectorLength(){
		eachTextVectorLength = new HashMap<String, Double>();
		for(int i = 0; i < documentName.length; i++){
			double value = 0.0;
			HashMap<String, Double> temphHashMap = new HashMap<String,Double>(textWordHashMap.get(documentName[i]));
			for(int j = 0; j < featureWords.size(); j++){
				//System.out.println("***" + temphHashMap.get(featureWords.get(j)));
				value = value + temphHashMap.get(featureWords.get(j)) * temphHashMap.get(featureWords.get(j));
				//System.out.println(j + "---" + value);
			}
			value = Math.sqrt(value);
			eachTextVectorLength.put(documentName[i], value);
		}
	}

	/**
	 * 初始化簇中心
	 */
 	private void initCenter(){
		center = new ArrayList<HashMap<String, Double>>();
		int index = (int)(Math.random() * documentName.length);
		HashMap<String, Double> temphHashMap = new HashMap<String,Double>(textWordHashMap.get(documentName[index]));
		center.add(temphHashMap);
		ArrayList<String> firstText = new ArrayList<String>();
		firstText.add(documentName[index]);
		cluster.add(firstText);
	}
 	/**
 	 * 计算阈值
 	 */
 	private void calaulateR(){
 		double[] eachDistant = new double[N0];
 		for(int i = 0; i < N0; i++){
 			
 			int index1 = (int)(Math.random() * documentName.length);
 			int index2 = (int)(Math.random() * documentName.length);
 			while(index2 == index1){
 				index2 = (int)(Math.random() * documentName.length);
 			}
 			
 			double value = 0.0;
 			double A = 0.0;
 			double B = eachTextVectorLength.get(documentName[index1]);
 			double C = eachTextVectorLength.get(documentName[index2]);
 	
 			for(int j = 0; j < featureWords.size(); j++){
 				//System.out.println("***" + textWordHashMap.get(documentName[0]).get(featureWords.get(j)));
 				A = A + textWordHashMap.get(documentName[index1]).get(featureWords.get(j)) * 
 						textWordHashMap.get(documentName[index2]).get(featureWords.get(j));
 				//System.out.println(j + "---" + A);
 			}
 			value = A / (B * C);
 			eachDistant[i] = value;
 		}
 		
 		double EX = 0.0;
 		double DX = 0.0;
 		for(int i = 0; i < N0; i++){
 			EX = EX + eachDistant[i];
 		}
 		EX = EX / N0;
 		for(int i = 0; i < N0; i++){
 			DX = DX + (eachDistant[i] - EX) * (eachDistant[i] - EX);
 		}
 		DX = Math.sqrt(DX / N0 );
 		
 		//R = EX + 0.7 * DX;
 		R = EX + 0.3 * DX;
 	}
 	
 	/**
 	 * 一趟聚类算法函数
 	 */
	private void calculate(){
		for(int i = 0; i < documentName.length; i++){
			double max = 0.0;
			int index = 0;
			HashMap<String, Double> textHashMap = new HashMap<String,Double>(textWordHashMap.get(documentName[i]));
			double C = eachTextVectorLength.get(documentName[i]);
			for(int j = 0; j < center.size(); j++){
				double temp = 0.0;
				double A = 0.0;
				double B = 0.0;
				for(int m = 0; m < featureWords.size(); m++){
					A = A + textHashMap.get(featureWords.get(m)) * center.get(j).get(featureWords.get(m));
					B = B + center.get(j).get(featureWords.get(m)) * center.get(j).get(featureWords.get(m));
				}
				
				temp = A / (Math.sqrt(B) * C);
				if(temp > max){
					max = temp;
					index = j;	
				}
			}
			//判断是将当期文本归入最近的簇还是新生成一个簇
			if(max >= R){
				cluster.get(index).add(documentName[i]);
				updateCenter(index,textHashMap);
				
			}
			else {
				center.add(textHashMap);
				ArrayList<String> newCluster = new ArrayList<String>();
				newCluster.add(documentName[i]);
				cluster.add(newCluster);
			}
		}
	}
	
	/**
	 * 更新簇中心
	 * @param index
	 * @param textHashMap
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
	 * 纯粹给实例化对象调用一下输出完成信息，避免在主函数中实例的一趟聚类的功能类的对象后没有调用而弹出警告
	 */
	public void information(){
		System.out.println("CABMDP has finish!");
	}
	
	

	
	public static void main(String[] args) throws IOException{
		CABMDP c1 = new CABMDP(); 
		c1.information();
	}
	
}
