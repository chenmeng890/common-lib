package com.yihaodian.search.catwordr;

/**
 * 产品文本信息的词性识别器
 */
public interface Recognizer {
	void recognize(ProductTextImpl p);
}
