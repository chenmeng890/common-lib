package com.yihaodian.search.catwordr;

import java.util.List;

import com.yihaodian.search.nlp.model.Dictionary;
import com.yihaodian.search.nlp.segment.ReverseSegmenter;
import com.yihaodian.search.nlp.segment.Segmenter;

/**
 * 词性识别器  - 根据词库设置进行机械识别
 */
public class SimpleRecognizer implements Recognizer {
	
	public SimpleRecognizer(String dictPath) {
		dict = new Dictionary(dictPath, false);
		segmenter = new ReverseSegmenter(dict, ReverseSegmenter.TYPE_LATIN_ORIGINAL, true,true);
	}
	
	private Dictionary dict;
	private Segmenter segmenter;
	
	public void recognize(ProductTextImpl p) {
		List<String> segWords = segmenter.segment(p.getProductName());
		
		StringBuilder strWords = new StringBuilder();
		StringBuilder strCatWords = new StringBuilder();
		StringBuilder strBrandWords = new StringBuilder();
		for (String segWord : segWords) {
			if (strWords.length() > 0)
				strWords.append(',');
			strWords.append(segWord);

			if (dict.canBeWordType(segWord, Dictionary.WORDTYPE_Category)) {
				if (strCatWords.length() > 0)
					strCatWords.append(',');
				strCatWords.append(segWord);
			}
			if (dict.canBeWordType(segWord, Dictionary.WORDTYPE_Brand)) {
				if (strBrandWords.length() > 0)
					strBrandWords.append(',');
				strBrandWords.append(segWord);
			}
		}
		p.setSplitedWords(strWords.toString());
		p.setCategoryWords(strCatWords.toString());
		p.setBrandWords(strBrandWords.toString());
	}
}
