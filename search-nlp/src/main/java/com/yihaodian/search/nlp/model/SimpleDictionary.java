package com.yihaodian.search.nlp.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.yihaodian.search.nlp.help.Latin;

/**
 * 分词典，保存某个TYPE的词
 * @author yuqian
 *
 */
public class SimpleDictionary {
	
	private Map<String,Lexeme> latinMap ;//存贮拉丁文关键词
	
	private DictCell cell;//保存CJK关键词
	
	private int type; //词典类型
	
	private int depth; //词典深度，即CJK关键词的最大长度
	
	public SimpleDictionary(int type){
		this.type=type;
		latinMap=new HashMap<String,Lexeme>();
		cell = new DictCell((char) 0);
	}
	

	public Lexeme match(String word){
//		final String word=char2String(charArray,begin,length);
		if(Latin.isLatinString(word,true)){
			return latinMap.get(word);
		}else{
			Lexeme le=cell.match(word.toCharArray(), 0,word.length());
			if(le!=null){
				le.setText(word);
				le.setType(type);
			}
			return le;
		}
	}
	
	
	/**
	 * 检索匹配主词典,
	 * 从已匹配的Hit中直接取出DictSegment，继续向下匹配
	 * @param charArray
	 * @param currentIndex
	 * @param matchedHit
	 * @return Hit
	 */
	public  Hit matchWithHit(char[] charArray , int currentIndex , Hit matchedHit){
		DictCell dc = matchedHit.getMatchedDictCell();
		return dc.match(charArray, currentIndex, 1 , matchedHit);
	}	
	
	public Hit match(char[] charArray, int begin, int length){
		return cell.match(charArray, begin, length, null);
	}
	/**
	 * 往词典中添加关键词（带扩展词）
	 * @param charArray
	 * @param extendWords
	 * @param isExtend
	 */
	public void fillSegment(String word,String[]extendWords,boolean isExtend){
		if(Latin.isLatinString(word,true)){
			fillLatinWord(word, extendWords,null);
			fillNoSpaceLatinWord(word,null);
			if(isExtend){
				if(extendWords!=null&&extendWords.length>0){
					for(String exWord:extendWords){
						fillSegment(exWord, null,false);
					}
				}
			}
		}else{
			if(word.length()>depth){
				depth=word.length();
			}
			cell.fillSegment(word, extendWords, isExtend);
		}
	}
	
	/**
	 * 往词典中添加一组同义词
	 * @param sysnonyms
	 */
	public void fillSegmentBySysnonyms(String[] synonyms){
		for(String sys:synonyms){
			if(Latin.isLatinString(sys,true)){
				fillLatinWord(sys, null, synonyms);
				fillNoSpaceLatinWord(sys, synonyms);
			}else{
				if(sys.length()>depth){
					depth=sys.length();
				}
				cell.fillSegmentBySynonyms(sys,synonyms);
			}
		}
	}
	
	/**
	 * 往拉丁词Map里添加关键词
	 * @param latinWord
	 * @param extendWords
	 */
	public void fillLatinWord(String latinWord,
			String[] extendWords,String[] synonyms){
		Lexeme le=latinMap.get(latinWord);
		if(le==null){
			le=new Lexeme(latinWord,type);
			latinMap.put(latinWord, le);
		}
		if(extendWords!=null){
			le.setExtendWords(extendWords);
		}
		if(synonyms!=null){
			le.setSynonyms(synonyms);
		}
	}
	
	/**
	 * 型号词特殊处理对应的latinMap
	 * @param latinWord
	 * @param extendWords
	 * @param synonyms
	 */
	public void fillNoSpaceLatinWord(String latinWord,String[] synonyms){
		String  noSpaceWord = latinWord.toLowerCase().replaceAll(" ", "");
		if(latinWord.equals(noSpaceWord)){
			return ;
		}
		Lexeme le=latinMap.get(noSpaceWord);
		if(le==null){
			le=new Lexeme(noSpaceWord,type);
			latinMap.put(noSpaceWord, le);
		}
		if(synonyms!=null){
			le.setAllSynonyms(synonyms);
		}else{
			le.setAllSynonyms(new String[]{latinWord});
		}
	}
	
	public static String char2String(char[] charArray,int begin,int length){
		char[] data = new char[length];
		for(int i=0;i<length;i++){
			data[i]=charArray[begin+i];
		}
		return String.valueOf(data);
	}
	
	public int getDepth() {
		return depth;
	}
}
