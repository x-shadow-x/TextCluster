package chinese_feature_extraction;

import com.sun.jna.Library;
import com.sun.jna.Native;

public class NlpirLibrary {

	// 定义接口CLibrary，继承自com.sun.jna.Library
	public interface CLibraryNlpir extends Library {
		// 定义并初始化接口的静态变量，通过JNA调用NLPIR.dll;
		CLibraryNlpir Instance = (CLibraryNlpir) Native.loadLibrary("NLPIR",
				CLibraryNlpir.class);

		/**
		 * 
		 * @param sDataPath
		 *            系统文件夹Data的父目录，一般设为“”，表示寻找项目下的Data文件夹
		 * @param encoding
		 *            设置分词编码，只能处理相应初始编码的文件.
		 *            默认为GBK，0：GBK；1：UTF-8;2:BIG5;3:GBK_FANTI
		 * @param sLicenceCode
		 *            licenseCode默认为"0"
		 * @return Return true if init succeed. Otherwise return false
		 */
		public int NLPIR_Init(String sDataPath, int encoding,
				String sLicenceCode);

		public String NLPIR_GetLastErrorMsg();

		/**
		 * 
		 * @param sSrc
		 *            :The source paragraph(将要分词的数据）
		 * @param bPOSTagged
		 *            :Judge whether need POS tagging, 0 for no tag; 1 for
		 *            tagging（是否含有词性标注）
		 * @return:处理后的分词结果
		 */
		public String NLPIR_ParagraphProcess(String sSrc, int bPOSTagged);

		/**
		 * 
		 * @param sSourceFilename
		 *            :The source file name to be analysized;（将要处理的文件名称）
		 * @param sResultFilename
		 *            :The result file name to store the results.（处理后的内容的保存路径）
		 * @param bPOStagged
		 *            : Judge whether need POS tagging, 0 for no tag; 1 for
		 *            tagging; default:1.（是否含有词性标注）
		 * @return：Return true if processing succeed. Otherwise return false.
		 */
		public double NLPIR_FileProcess(String sSourceFilename,
				String sResultFilename, int bPOStagged);

		/**
		 * 
		 * @param sLine
		 *            ：the input text.（将要提取关键词的文本）
		 * @param nMaxKeyLimit
		 *            ：the maximum number of key words.（关键词的最大数量）
		 * @param bWeightOut
		 *            ： whether the keyword weight output or not（关键词的权重是否输出）
		 * @return:Return the keywords list if excute succeed. otherwise return
		 *                NULL.(返回关键词集）
		 */
		public String NLPIR_GetKeyWords(String sLine, int nMaxKeyLimit,
				boolean bWeightOut);

		/**
		 * 
		 * @param sLine
		 *            ：the input text.（将要提取新词的文本）
		 * @param nMaxKeyLimit
		 *            ：the maximum number of key words.（关键词的最大数量）
		 * @param bWeightOut
		 *            ： whether the keyword weight output or not（关键词的权重是否输出）
		 * @return:Return the new words list if excute succeed. otherwise return
		 *                NULL.(返回新词集）
		 */
		public String NLPIR_GetNewWords(String sLine, int nMaxKeyLimit,
				boolean bWeightOut);

		public String NLPIR_GetFileKeyWords(String string, int nMaxKeyLimit,
				boolean bWeightOut);

		public String NLPIR_GetFileNewWords(String string, int nMaxKeyLimit,
				boolean bWeightOut);
        public int NLPIR_AddUserWord(String userWord);
    	public int NLPIR_ImportUserDict(String dictFileName);
		public void NLPIR_Exit();
	}	
}

