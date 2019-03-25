package com.yihaodian.search.nlp.segment;

import java.util.ArrayList;
import java.util.List;

import com.yihaodian.search.nlp.model.Dictionary;
import com.yihaodian.search.nlp.model.Lexeme;
import com.yihaodian.search.nlp.segment.Segmenter;

/**
 * 在原分词的基础上,再进行单字、二元、三元切分
 * @author yuqian
 *
 */
public class WordSliceSegmenter extends Segmenter {

	public WordSliceSegmenter(Segmenter originSegmenter) {
		this.originSegmenter = originSegmenter;
	}
	
	private Segmenter originSegmenter;
	

	/**
	 * 非单子的再细切分
	 * @param originWords 待细切分的词
	 * @return 返回结果不包含原词本身
	 */
	public static List<String> sliceSegment(List<String> originWords){
		if (originWords == null) {
			return null;
		}
		List<String> result = new ArrayList<String>();
		for (String originWord : originWords) {
			if (originWord == null) {continue;}
			int wordLength = originWord.length();
			if (wordLength < 2)  {continue;}
			
			char firstChar = originWord.charAt(0);

			// 中文,进行三元、二元、单字切分
			if (firstChar > 127) { 
				for (int n=wordLength-1; n >= 1; n--) {
					for (int p=0; p <= wordLength-n; p++) {
						result.add(originWord.substring(p, p+n));
					}
				}
			}
			else {
				int ingCharsType = -1; // -1 其它  1 数字 2字母
				int charsStart = -1;
				char[] originChars = originWord.toCharArray();
				List<String> ret = new ArrayList<String>();
				for (int i=0; i <= originChars.length; i++) {
					char tc; 
					if (i == originChars.length){
						tc = ' ';
					}else{
						tc = originChars[i];
					}
					int currCharsType; // -1 其它  1 数字 2字母
					if ((tc>='0')&&(tc<='9')) {
						currCharsType = 1;
					}
					else if ( ((tc>='a')&&(tc<='z')) || ((tc>='A')&&(tc<='Z')) ) {
						currCharsType = 2;
					}
					else {
						currCharsType = -1;
					}
					
					if (currCharsType == ingCharsType)  {continue;}
					if (ingCharsType >= 0) {
						if (! ((charsStart==0)&&(i==originChars.length))) {
							String currStr=originWord.substring(charsStart, i);
							if(!ret.isEmpty()){
								String preStr = ret.get(ret.size()-1);
								preStr+=currStr;
								ret.add(0,preStr);
							}
							ret.add(currStr);
						}
					}
					ingCharsType = currCharsType;
					charsStart = i;
				}
				result.addAll(ret);
			}

		}
		return result;
	}
	
	public List<String> segment(String sentence) {
		List<String> list = originSegmenter.segment(sentence);
		if (list == null) {return null;}
		if (list.isEmpty()) {return list;}
		
		int count = list.size();
		for (int i=0; i < count; i++) {
			String wordString = list.get(i);
			if (wordString == null)  {continue;}
			int wordLength = wordString.length();
			if (wordLength < 2)  {continue;}
			
			char firstChar = wordString.charAt(0);

			// 中文,进行三元、二元、单字切分
			if (firstChar > 127) { 
				for (int n=wordLength-1; n >= 1; n--) {
					for (int p=0; p <= wordLength-n; p++) {
						list.add(wordString.substring(p, p+n));
					}
				}
			}

		}
		return list;
	}

	public List<Lexeme> segmentComplex(String sentence) {
		List<Lexeme> list=originSegmenter.segmentComplex(sentence);
		if (list == null) {return null;}
		if (list.isEmpty()) {return list;}
		int count = list.size();
		for (int i=0; i < count; i++) {
			Lexeme lexeme=list.get(i);
			if(lexeme.getType()==Lexeme.TYPE_DIGIT||
					lexeme.getType()==Lexeme.TYPE_LETTER){
				continue;
			}
			String wordString=lexeme.getText();
			int wordLength=wordString.length();
			if(wordLength <2)  {continue;}
			char firstChar = wordString.charAt(0);

			// 中文,进行三元、二元、单字切分
			if (firstChar > 127) { 
				for (int n=wordLength-1; n >= 1; n--) {
					for (int p=0; p <= wordLength-n; p++) {
						list.add(new Lexeme(wordString.substring(p, p+n)));
					}
				}
			}
		}
		return list;
	}

}
