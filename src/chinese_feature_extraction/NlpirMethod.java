package chinese_feature_extraction;

import chinese_feature_extraction.NlpirLibrary.CLibraryNlpir;

import org.apache.log4j.Logger;



public class NlpirMethod {
	private static Logger logger = Logger.getLogger(NlpirMethod.class.getName());
	@SuppressWarnings("unused")
	private static final int GBK_CODE = 0;// 默认支持GBK编码
	private static final int UTF8_CODE = 1;// UTF8编码
	@SuppressWarnings("unused")
	private static final int BIG5_CODE = 2;// BIG5编码
	@SuppressWarnings("unused")
	private static final int GBK_FANTI_CODE = 3;// GBK编码，里面包含繁体字
	
	
	public static void main(String[]arg){
		Nlpir_init();
		System.out.println(NLPIR_ParagraphProcess("默认支持GBK编码",0));
	}
	public static boolean Nlpir_init(){
		logger.debug("初始化开始");
		String argu="";
		int init_flag = CLibraryNlpir.Instance.NLPIR_Init(// .getBytes(system_charset)
				argu, UTF8_CODE, "0");
		if (0 == init_flag) {
			logger.debug("初始化失败！");
			return false;
		}
		logger.debug("初始化成功。。。");
		return true;
		
	}
	/**
	 * 通过传入字符串进行分词
	 * @param sSrc
	 * @param bPOSTagged
	 * @return
	 */
	public static String NLPIR_ParagraphProcess(String sSrc, int bPOSTagged){
		String ParticipleResult=CLibraryNlpir.Instance.NLPIR_ParagraphProcess(sSrc, bPOSTagged);
		//logger.debug("分词结束");
		return ParticipleResult;
	}
	/**
	 * 通过传入文章名称进行分词
	 * @param sSourceFilename
	 * @param sResultFilename
	 * @param bPOStagged
	 * @return
	 */
	public static double NLPIR_FileProcess(String sSourceFilename,
			String sResultFilename, int bPOStagged){
		double d=CLibraryNlpir.Instance.NLPIR_FileProcess(sSourceFilename, sResultFilename, bPOStagged);
		return d;
	}
	/**
	 * 封装为含三个参数的关键词提取
	 * @param sLine
	 * @param nMaxKeyLimit
	 * @param bWeightOut
	 * @return
	 */
	public static String NLPIR_GetKeyWords(String sLine, int nMaxKeyLimit,
			boolean bWeightOut){
		String strOfKeyword="";
		if(sLine.length() < 100){
			logger.debug("将要提取关键词的文本内容太少");
		}else{
	     strOfKeyword=CLibraryNlpir.Instance.NLPIR_GetKeyWords(sLine, nMaxKeyLimit, bWeightOut);
		}
	     return strOfKeyword;
	}
	/**
	 * 封装为只含一个参数的关键词提取方法
	 * @param sLine:要提取关键词的文本
	 *    设置为：nMaxKeyLimit=10;  bWeightOut=false;
	 * @return
	 */
	public static String NLPIR_GetKeyWords(String sLine){
		int nMaxKeyLimit=10;
		boolean bWeightOut=false;
		String strOfKeyword="";
		if(sLine.length() < 100){
			logger.debug("将要提取关键词的文本内容太少");
		}else{
	     strOfKeyword=CLibraryNlpir.Instance.NLPIR_GetKeyWords(sLine, nMaxKeyLimit, bWeightOut);
		}
	     return strOfKeyword;
	}
	/**
	 * 封装为含三个参数的新词提取方法
	 * @param sLine
	 * @param nMaxKeyLimit
	 * @param bWeightOut
	 * @return
	 */
	public static String NLPIR_GetNewWords(String sLine, int nMaxKeyLimit,
			boolean bWeightOut){
		
		String strOfNewword="";
		if(sLine.length() < 100){
			logger.debug("将要提取新词的文本内容太少");
		}else{
			strOfNewword=CLibraryNlpir.Instance.NLPIR_GetNewWords(sLine, nMaxKeyLimit, bWeightOut);
		}
	     return strOfNewword;
	}
	/**
	 * 封装为含一个参数的新词提取方法
	 * @param sLine
	 * @return
	 */
	public static String NLPIR_GetNewWords(String sLine){
		int nMaxKeyLimit=10;
		boolean bWeightOut=false;
		String strOfNewword="";
		if(sLine.length() < 100){
			logger.debug("将要提取新词的文本内容太少");
		}else{
			strOfNewword=CLibraryNlpir.Instance.NLPIR_GetNewWords(sLine, nMaxKeyLimit, bWeightOut);
		}
	     return strOfNewword;
	}
	/**
	 * 封装为含有三个参数的通过传入文件路径来提取关键词的方法
	 * @param filePath：文件路径
	 * @param nMaxKeyLimit
	 * @param bWeightOut
	 * @return
	 */
	public static String NLPIR_GetFileKeyWords(String filePath, int nMaxKeyLimit,
			boolean bWeightOut){
		   String strOfKeyWord=CLibraryNlpir.Instance.NLPIR_GetFileKeyWords(filePath, nMaxKeyLimit, bWeightOut);
		   return strOfKeyWord;
		
	}
	/**
	 * 封装为含有一个参数的通过传入文件路径来提新词的方法
	 * @param filePath：文件路径
	 * @return：关键词结果集
	 */
	public static String NLPIR_GetFileKeyWords(String filePath){
		   int nMaxKeyLimit=10;
		   boolean bWeightOut=false;
		   String strOfKeyWord=CLibraryNlpir.Instance.NLPIR_GetFileKeyWords(filePath, nMaxKeyLimit, bWeightOut);
		   return strOfKeyWord;
		
	}
	/**
	 * 封装为含有三个参数的通过传入文件路径来提取新词的方法
	 * @param filePath：文件路径
	 * @param nMaxKeyLimit:最大新词量
	 * @param bWeightOut：是否输出权重
	 * @return：新词结果集
	 */
	public static String NLPIR_GetFileNewWords(String filePath, int nMaxKeyLimit,
			boolean bWeightOut){
		   String strOfNewWord=CLibraryNlpir.Instance.NLPIR_GetFileNewWords(filePath, nMaxKeyLimit, bWeightOut);
		   return strOfNewWord;
		
	}
	/**
	 * 封装为含有一个参数的通过传入文件路径来提取新词的方法
	 * @param filePath ：文件路径
	 * @return：新词结果集
	 */
	public static String NLPIR_GetFileNewWords(String filePath){
		   int nMaxKeyLimit=10;
		   boolean bWeightOut=false;
		   String strOfNewWord=CLibraryNlpir.Instance.NLPIR_GetFileNewWords(filePath, nMaxKeyLimit, bWeightOut);
		   return strOfNewWord;
		
	}
	public static int NLPIR_AddUserWord(String userWord){
		return CLibraryNlpir.Instance.NLPIR_AddUserWord(userWord);
	}
	/**
	 * 
	 * @param dictFileName:用户自定义词典路径
	 * @return 是否添加成功
	 */
	public static int NLPIR_ImportUserDict(String dictFileName){
		return CLibraryNlpir.Instance.NLPIR_ImportUserDict(dictFileName);
	}
	
}
