/**
 * 去停用词器
 */

package chinese_feature_extraction;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;

public class StopWordsHandle {

	/**
	 * 存储停用词表的文档路径
	 */
	String filePath = "E:\\java_Eclipse\\FeatureExtractionForCluster\\中文停用词表.txt";
	
	/**
	 * 存储停用词
	 */
	HashSet<String> stopWords;
	
	/**
	 * 存储去除停用词的文本
	 */
	ArrayList<String> newText;
	
	public StopWordsHandle() throws IOException{
		stopWords = new HashSet<String>();
		newText = new ArrayList<String>();
		InputStreamReader isReader = new InputStreamReader(new FileInputStream(filePath),"GBK");
		BufferedReader reader = new BufferedReader(isReader);
		String aline = "";
		while ((aline = reader.readLine()) != null) {
			stopWords.add(aline);
		}
		isReader.close();
		reader.close();
	}
	
	/**
	 * 获得去除停用词的新的文本
	 * @param text
	 * @return
	 */
	public ArrayList<String> getNewText(String[] text) {
		IsStopWord(text);
		return newText;
	}
	
	/**
	 * 去除原文本的停用词
	 * @param text
	 */
	private void IsStopWord(String[] text)
    {
		newText.clear();
        for(int i = 0;i < text.length; i++)
        {
            if(!(stopWords.contains(text[i])))
            	newText.add(text[i]);
        }
    }
}
