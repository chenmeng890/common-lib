package com.yihaodian.search.nlp;

import org.apache.lucene.analysis.TokenStream;

/**
 * 针对产品文本内容进行分词与词性识别
 *  - 用于建索引,不支持多线程
 *  - 返回TokenStream类型的结果可直接用于建索引
 */
public interface ProductTextAnalyzer {
	/**
	 * 进行分词与词性识别
	 * @param ProductText 产品文本信息
	 * @param doPOS 是否进行词性识别处理(药网数据暂不进行词性识别)
	 */
	void analyze(ProductText product, boolean doPOS);
	
	String analyze(ProductText product);
	/**
	 * 返回品牌词(已进行扩展处理)
	 * @return
	 */
	TokenStream getBrandTokens();
	
	/**
	 * 打印扩展后的品牌词
	 * @return
	 */
	String printBrandTokens();
	
	/**
	 * 返回类别词(已进行扩展处理)
	 * @return
	 */
	TokenStream getCategoryTokens();
	
	/**
	 * 打印扩展后的类别词
	 * @return
	 */
	String printCategoryTokens();
	
	/**
	 * 返回所有词(仅按词库进行分词)
	 * @return
	 */
	TokenStream getWordTokens();
	
	/**
	 * 返回识别时的切词（包括标点符号）
	 * @return
	 */
	TokenStream getWordTokensByTrain();
	/**
	 * 返回所有字(分词后再进行二元/单字切分)
	 * @return
	 */
	TokenStream getWordExTokens();
	
	/**
	 * 返回品牌词(未进行扩展处理, 逗号分隔)
	 * @return
	 */
	String getBrandWords();
	
	/**
	 * 返回类别词(未进行扩展处理, 逗号分隔)
	 * @return
	 */
	String getCategoryWords();
	
	void reloadRecognizer();
}
