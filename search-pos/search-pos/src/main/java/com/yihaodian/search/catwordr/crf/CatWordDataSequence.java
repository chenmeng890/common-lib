package com.yihaodian.search.catwordr.crf;

import java.util.List;

import com.yihaodian.search.catwordr.ProductTextImpl;
import com.yihaodian.search.catwordr.StringUtil;
import com.yihaodian.search.nlp.model.Dictionary;
import com.yihaodian.search.nlp.segment.Segmenter;

import iitb.CRF.DataSequence;

/**
 * 类别词识别的单个CRF样本
 */
public class CatWordDataSequence implements DataSequence {
	public CatWordDataSequence(ProductTextImpl p, boolean forTrain, Segmenter segmenter, Dictionary dict) {
		List<String> segWords = segmenter.segment(p.getProductName());
		allWords = new String[segWords.size()];
		isBrandWords = new boolean[allWords.length];
		isCatWords = new boolean[allWords.length];
		for (int i=0; i < segWords.size(); i++) {
			String segWord = segWords.get(i);
			allWords[i] = segWord;
			isBrandWords[i] = dict.canBeWordType(segWord, Dictionary.WORDTYPE_Brand);
			isCatWords[i] = dict.canBeWordType(segWord, Dictionary.WORDTYPE_Category);
		}
		isFirstBrandWords = new boolean[allWords.length];
		for (int i=0; i < segWords.size(); i++) {
			if (isBrandWords[i]) {
				isFirstBrandWords[i] = true;
				break;
			}
		}
		isLastCatWords = new boolean[allWords.length];
		for (int i=segWords.size()-1; i >= 0; i--) {
			if (isCatWords[i]) {
				isLastCatWords[i] = true;
				break;
			}
		}
		
		labels = new int[allWords.length];
		if (forTrain) {
			labeledWords2type(p.getCategoryWords(), LABEL_catWord);
			labeledWords2type(p.getBrandWords(), LABEL_brandWord);
		}
		
		this.catSearchName = p.getCategoryName();
		String catSearchName = p.getCategoryName();
		int tn = catSearchName.indexOf(':');
		catSearchName = catSearchName.substring(tn+1).toLowerCase();
		String[] catSearchNames = catSearchName.split("-");
		
		isCatNameWords = new boolean[allWords.length];
		likeCatNameWords = new boolean[allWords.length];
		for (int i=0; i < allWords.length; i++) {
			for (int n=0; n < catSearchNames.length; n++) {
				if (catSearchNames[n].equals(allWords[i])) {
					isCatNameWords[i] = true;
					break;
				}
			}
			for (int n=0; n < catSearchNames.length; n++) {
				for (int m=0; m < allWords[i].length(); m++) {
					if (catSearchNames[n].indexOf(allWords[i].charAt(m)) >= 0) {
						likeCatNameWords[i] = true;
						break ;
					}
				}
				if (likeCatNameWords[i]) break;
			}
		}
		
		isBrandNameWords = new boolean[allWords.length];
		String brandName = p.getBrandName();
		if (brandName != null) {
			for (int i=0; i < allWords.length; i++) {
				if (StringUtil.findSubIgnoreCase(brandName, allWords[i]))/*判断是否品牌词的特征*/
					isBrandNameWords[i] = true;
			}
		}
	}
	
	private void labeledWords2type(String labeledWords, int labeledType) {
		if (labeledWords == null) return;
		
		String[] labeledCatWords = StringUtil.split(labeledWords, ',');
		for (int n=0; n < labeledCatWords.length; n++) {
			String labeledWord = labeledCatWords[n];
			if (! labeledWord.isEmpty()) {
				for (int k=0; k < allWords.length; k++) {
					if (allWords[k].equalsIgnoreCase(labeledWord))
						labels[k] = labeledType;
				}
			}
		}
	}
	
	public final static int LABEL_other = 0;
	public final static int LABEL_catWord = 1;
	public final static int LABEL_brandWord = 2;
	
	public String[] allWords;
	public boolean[] isBrandWords,isCatWords;
	public boolean[] isFirstBrandWords,isLastCatWords;
	public int[] labels;
	public boolean[] isCatNameWords,likeCatNameWords;
	public boolean[] isBrandNameWords;
	public String catSearchName;
	  
	@Override
	public int length() {
		return allWords.length;
	}

	@Override
	public int y(int i) {
		return labels[i];
	}

	@Override
	public Object x(int i) {
		return allWords[i];
	}

	@Override
	public void set_y(int i, int label) {
		labels[i] = label;
	}
	
}
