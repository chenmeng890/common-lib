package com.yihaodian.search.nlp.segment;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.yihaodian.search.nlp.SimpleDictionaryType;
import com.yihaodian.search.nlp.help.Latin;
import com.yihaodian.search.nlp.help.Punctuations;
import com.yihaodian.search.nlp.model.Dictionary;
import com.yihaodian.search.nlp.model.Lexeme;
import com.yihaodian.search.nlp.model.SimpleDictionary;
/**
 * 分词器
 * @author yuqian
 *
 */
public abstract class Segmenter {

	 protected Dictionary dictionary;
	 
	 protected int MAX_SIZE=8;
	 
	 protected boolean isReservedStopWords = false;//是否保留停词
	 
	 protected boolean isReservedPunctuation = false;//是否保留标点
	 
	 protected int latinType; //拉丁文的切分格式
	 
	 /*拉丁文的几种切分方式，以900ml为例*/
	 public static final int TYPE_LATIN_MIXED=1; //切分为：900，ml，900ml
	 public static final int TYPE_LATIN_SPLIT=2; //切分为：900，ml
	 public static final int TYPE_LATIN_ORIGINAL=3; //切分为：900ml

	 public static final String Chn_Num_Connector="年月日号第个几多余半";
	 public static final String Chn_Num = "○一二两三四五六七八九十零壹贰叁肆伍陆柒捌玖百千万亿拾佰仟萬億兆卅廿";
	/**
	 * 简洁分词方式
	 * @param sentence
	 * @return
	 */
	public abstract List<String> segment(String sentence);
	
	/**
	 * 复杂分词方式，返回值包含词元类型、词元扩展的描述
	 * @param sentence
	 * @return
	 */
	public abstract List<Lexeme> segmentComplex(String sentence);
	
	
	/**
	 * 判断输入是否包含敏感词
	 * @param sentence
	 * @return
	 */
	/**
	 * 判断是否含有敏感词
	 */
	public boolean isIllegal(String sentence) {
		
		List<Lexeme> lexemes = segmentComplex(sentence);
		if(lexemes==null){
			return false;
		}
		for(Lexeme le:lexemes){
			if(le.getType()==SimpleDictionaryType.TYPE_ILLEGAL){
				return true;
			}
		}
		return false;
	}
	
	public List<String> getLatinToken(String latinString,boolean isReservedPunctuation){
		List<String> ret=new LinkedList<String>();
		int pre=0;
		for(int i=0;i<latinString.length();i++){
			char ch=latinString.charAt(i);
			if(isLetterConnector(ch)&&ch!='.'){
				if(i==0){
					if(isReservedPunctuation){
						ret.add(0,latinString.substring(i,i+1));
					}
					pre=i+1;
				}
				else{
					if(pre<i){
						ret.add(0,latinString.substring(pre, i));
					}
					if(isReservedPunctuation){
						ret.add(0,latinString.substring(i,i+1));
					}
					pre=i+1;
					}
			}
		}
		 if(pre<latinString.length()){
			ret.add(0,latinString.substring(pre));
		 }
		return ret;
	}
	
	
	/**
	 * 获取同义词并对同义词词
	 * @param latinString
	 * @param isContains 时候包含本身。
	 * @return
	 */
	public List<String> splitSynonymsWords(String latinString ,boolean isContains){
		if(latinString == null ||latinString.isEmpty()){
			return null;
		}
		String modelWord = latinString.toLowerCase().replaceAll(" ", "");
		List<Lexeme> lexs = dictionary.matchDict(modelWord);
		if((isContains==false) && (lexs == null || lexs.isEmpty())){
			return null ;
		}
		Set<String> segs=new HashSet<String>();
		String[] synonyms = null;
		if(lexs!=null){
			for (Lexeme lex: lexs) {
				if(lex.getAllSynonyms()!=null){
					synonyms = lex.getAllSynonyms();
					break;
				}
			}
		}
		if(lexs!=null && (!lexs.isEmpty()) && synonyms!=null){
			if(lexs.get(0).getType() == Dictionary.WORDTYPE_MODEL){
				String[] arr = synonyms[0].split(" ");
				for (int i = 0; i < arr.length; i++) {
					segs.add(arr[i]);
				}
				if(isContains){
					for (Lexeme tmpLex: segmentComplex(synonyms[0])) {
						segs.add(tmpLex.getText());
					}
					segs.add(synonyms[0]);
				}
			}else{
				segs.add(synonyms[0]);
			}
		}else{
			segs.add(latinString);
		}
		return new ArrayList<String>(segs);
	}
	
	
	public Lexeme getModelLexeme(String word){
		if(word == null || word.trim().length() == 0) {
            return null;
        }
		SimpleDictionary modelDictionary = dictionary.getMainDictMap().get(String.valueOf(Dictionary.WORDTYPE_MODEL));
		Lexeme lexeme = null;
		if(modelDictionary != null){
			lexeme=modelDictionary.match(word);
		}
		return lexeme;
	}
	
	
	public boolean isAcceptedChar(char input){
		 return isLetterConnector(input)||
		 				Latin.isDigit(input)||Latin.isLetter(input);		 	
	 }
	 
	public boolean isUnAcceptedChar(char input){
		 if(Punctuations.isPunctuation(input)){
			 if(isLetterConnector(input)){
				 return false;
			 }
			 return true;
		 }
		 return false;
	 }
	public boolean isLetterConnector(char input){
			if(LatinSplitor.englishConnectChars.contains(input)){
				return true;
			}
			if(LatinSplitor.arabicNumConnectChars.contains(input)){
				return true;
			}
			return false;
		}
	
	public void setMaxSize(int size){
		this.MAX_SIZE = size;
	}
	
}
