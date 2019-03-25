package com.yihaodian.search.nlp.segment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.yihaodian.search.nlp.help.ChineseDigit;
import com.yihaodian.search.nlp.help.Latin;
import com.yihaodian.search.nlp.model.Dictionary;
import com.yihaodian.search.nlp.model.Lexeme;


/**
 * 逆向最大分词器
 * @author yuqian
 *
 */
public class ReverseSegmenter extends Segmenter{
	
	 private int MAX_SIZE=8;	 	 
	 private boolean isReservedStopWords = false;//是否保留停词	 	 
	 private boolean isReservedPunctuation = false;//是否保留标点
	 private boolean cn2latin=false;
	 private int latinType; //拉丁文的切分格式	 	 
	 /*拉丁文的几种切分方式，以900ml为例*/	 
	 public static final int TYPE_LATIN_MIXED=1; //切分为：900，ml，900ml	 
	 public static final int TYPE_LATIN_SPLIT=2; //切分为：900，ml	 
	 public static final int TYPE_LATIN_ORIGINAL=3; //切分为：900ml	 

	 public static final String Chn_Num_Connector="年月日号第个几多余半";	 
	 public static final String Chn_Num = "○一二两三四五六七八九十零壹贰叁肆伍陆柒捌玖拾百千万亿佰仟萬億兆卅廿";
	 //初始化分词器
	 public ReverseSegmenter(Dictionary dictionary){
		this(dictionary,TYPE_LATIN_ORIGINAL,false,false,false);
	 }
	 public ReverseSegmenter(Dictionary dictionary , int latinType,
			 boolean isReservedStopWords,boolean isReservedPunctuation){
		 this(dictionary,latinType,isReservedStopWords,isReservedPunctuation,false);
	 }
			 
	 /**
	  * 构造函数
	  * @param dictionary 词典
	  * @param latinType  拉丁文的切分方式
	  * @param isReservedStopWords 是否保留停词
	  * @param isReservedPunctuation  是否保留标点
	  * @param cn2latin 是否将中文数字转为阿拉伯数字
	  */
	 public ReverseSegmenter(Dictionary dictionary , int latinType,
			 boolean isReservedStopWords,boolean isReservedPunctuation,
			 boolean cn2latin){
		 this.latinType=latinType;
		 this.dictionary=dictionary;
		 this.isReservedStopWords=isReservedStopWords;
		 this.isReservedPunctuation=isReservedPunctuation;
		 this.cn2latin=cn2latin;
		 if(dictionary.getMaxDepth()>0){
			 this.MAX_SIZE = dictionary.getMaxDepth();
		 }
	 }
	 
	 /**
	  * 简单模式
	  */
	 public List<String> segment(String sentence) {
		 if(sentence==null || sentence.length()==0){
			return null;
		 }
		 sentence = sentence.toLowerCase();
		 char[] textChars = sentence.toCharArray();
		 List<String> ret = new ArrayList<String>();
		 int tail=sentence.length();
		 
		 while(tail>0){

			 char currChar = textChars[tail-1];
			 int head;
			 if(isUnAcceptedChar(currChar)){
				 if(isReservedPunctuation){
					 String word=sentence.substring(tail-1,tail);
					 ret.add(0,word);
				 }
				 tail=tail-1;
				 continue;
			 }
			 boolean isMatchDict = false;
			 if(isAcceptedChar(currChar)){
				 head = getLetterHead(textChars,tail-1);
				 int offset=tail-head;
				 String word=sentence.substring(head,tail);
				 isMatchDict=dictionary.isMatchDict(word,isReservedStopWords);
				 if(isMatchDict){
						ret.add(0,word);
						tail = head;
					}
				 
				 if(offset<MAX_SIZE&&!isMatchDict){
						int tempHead=tail-MAX_SIZE;
						if(tempHead<0) {tempHead=0;}
						for(;tempHead<head;tempHead++){
							word=sentence.substring(tempHead,tail);
							isMatchDict=dictionary.isMatchDict(word,isReservedStopWords);
							if(isMatchDict){
								ret.add(0,word);
								tail = tempHead;
								break;
							}
						}
				 }
				 if(isMatchDict){
					 continue;
					 }
				 else{
					 //实际应该对这些连续字符串再做细致的切分
					 word = sentence.substring(head, tail);
					 splitLatin(word, latinType, ret);
					 tail = head;
					 continue;
				 }
			 }
			 
			 head = tail-MAX_SIZE;
			 if(head<0) {head=0;}
			 for(;head<tail;head++){
				 String word = sentence.substring(head, tail);
				 isMatchDict=dictionary.isMatchDict(word,isReservedStopWords);
				 if(isMatchDict){
					 ret.add(0,word);
					 tail = head;
					 break;
				 }
			 }
			if(!isMatchDict){
				if(Chn_Num.indexOf(currChar)>=0){
						//||Chn_Num_Connector.indexOf(currChar)>=0){
					head=getCnNumberHead(textChars,tail-1);
					String word = sentence.substring(head, tail);
					if(cn2latin){
						int digit=ChineseDigit.parseDigit(word.toCharArray(), tail-head);
						ret.add(0,String.valueOf(digit));
					}else{
						ret.add(0,word);	
					}
					tail = head;
				}else{
					String word = sentence.substring(tail-1, tail);
					 ret.add(0,word);
					 tail--;
				}
			}
		 
		 }
		 return ret;
		}
	 
	 
	 /**
	  * 复杂模式，分词结果为对象（带有词性、同义词等）
	  */
	 public List<Lexeme> segmentComplex(String sentence){
		 if(sentence==null || sentence.length()==0){
			 return null;
		 }
		 sentence = sentence.toLowerCase();
		 char[] textChars = sentence.toCharArray();
		 List<Lexeme> ret = new ArrayList<Lexeme>();
		 int tail=sentence.length();
		 
		 while(tail>0){
			 char currChar = textChars[tail-1];
			 int head;
			 if(isUnAcceptedChar(currChar)){
				 if(isReservedPunctuation){
					 String word=sentence.substring(tail-1,tail);
					 ret.add(0,new Lexeme(word));
				 }
				 tail=tail-1;
				 continue;
			 }
			 boolean isMatchDict = false;
			 if(isAcceptedChar(currChar)){
				 head = getLetterHead(textChars,tail-1);
				 int offset=tail-head;
				 String word=sentence.substring(head,tail);
				 List<Lexeme> leList=dictionary.matchDict(word,isReservedStopWords);
				 if(leList!=null){
						isMatchDict=true;
						listPush(ret, leList);
						tail = head;
					}
				 
				 if(offset<MAX_SIZE&&!isMatchDict){
						int tempHead=tail-MAX_SIZE;
						if(tempHead<0) {tempHead=0;}
						for(;tempHead<head;tempHead++){
							word=sentence.substring(tempHead,tail);
							leList=dictionary.matchDict(word,isReservedStopWords);
							if(leList!=null){
								isMatchDict=true;
								listPush(ret, leList);
								tail = tempHead;
								break;
							}
						}
				 }
				 if(isMatchDict){
					 continue;
				 }
				 else{
					 //实际应该对这些连续字符串再做细致的切分
					 word = sentence.substring(head, tail);
					 splitLatinByLexeme(word, latinType, ret);
					 tail = head;
					 continue;
				 }
			 }
			 
			 head = tail-MAX_SIZE;
			 if(head<0) {head=0;}
			 for(;head<tail;head++){
				 String word = sentence.substring(head, tail);
				 List<Lexeme> le=dictionary.matchDict(word,isReservedStopWords);
				 if(le!=null){
					 isMatchDict=true;
					 listPush(ret, le);
					 tail = head;
					 break;
				 }
			 }
			if(!isMatchDict){
				if(Chn_Num.indexOf(currChar)>=0){//||Chn_Num_Connector.indexOf(currChar)>=0){
					head=getCnNumberHead(textChars,tail-1);
					String word = sentence.substring(head, tail);
					ret.add(0,new Lexeme(word,Lexeme.TYPE_CHINESEDIGIT));
					if(cn2latin){
						int digit=ChineseDigit.parseDigit(word.toCharArray(), tail-head);
						ret.add(0,new Lexeme(String.valueOf(digit)));
					}
					tail = head;
				}else{
					Lexeme les= new Lexeme(sentence, tail-1, tail);
					 ret.add(0,les);
					 tail--;
				}
			}
		 }
		 return ret;
	 }
	 
	 /**
	  * 逆向寻找一个连续字符串（英文+数字+sign）的起点
	  * @param start
	  * @return
	  */
	 private int getLetterHead(char[] textChars,int start){
		 int end=start;
		 while(end>=0){
			 char currChar=textChars[end];
			 if(Latin.isLetter(currChar)||Latin.isDigit(currChar)){
				 end--;
				 continue;
			 }
			 else if(isLetterConnector(currChar)){
				 end--;
				 continue;
			 }
			 else{
				 break;
			 }
		 }
		 
		 return end+1;
	 }
	 
	 /**
	  * 逆向寻找一个连续的中文数字字符串起点
	  * @param start
	  * @return
	  */
	 private int getCnNumberHead(char[] textChars,int start){
		 int end=start;
		 while(end>=0){
			 char currChar=textChars[end];
			 if(Chn_Num.indexOf(currChar)>=0){//||
					// Chn_Num_Connector.indexOf(currChar)>=0){
				 end--;
				 continue;
			 }
			 else{
				 break;
			 }
		 }
		 return end+1;
	 }
	 
		public void splitLatin(String latinString,int type_latin,List<String> ret){
			List<String> tokens = getLatinToken(latinString,isReservedPunctuation);
			if(type_latin==TYPE_LATIN_ORIGINAL){
				for(String token:tokens){
					ret.add(0,token);
				}
			}else if(type_latin==TYPE_LATIN_SPLIT){
				LatinSplitor splitor;
				for(String token:tokens){
					splitor = new LatinSplitor(token);
					Set<String> set=splitor.splitLatinString();
					for(String str:set){
						ret.add(0,str);
					}
				}
			}else if(type_latin==TYPE_LATIN_MIXED){
				LatinSplitor splitor;
				for(String token:tokens){
					splitor = new LatinSplitor(token);
					Set<String> set=splitor.splitLatinString();
					set.add(token);
					for(String str:set){
						ret.add(0,str);
					}
				}
			}
		}
		
		public void splitLatinByLexeme(String latinString,int type_latin,List<Lexeme> ret){
			List<String> tokens = getLatinToken(latinString,isReservedPunctuation);
			if(type_latin==TYPE_LATIN_ORIGINAL){
				for(String token:tokens)
					ret.add(0,new Lexeme(token));
			}else if(type_latin==TYPE_LATIN_SPLIT){
				LatinSplitor splitor;
				for(String token:tokens){
					splitor = new LatinSplitor(token);
					Set<Lexeme> set=splitor.splitLatin();
					for(Lexeme le:set){
						ret.add(0,le);
					}
				}
			}else if(type_latin==TYPE_LATIN_MIXED){
				LatinSplitor splitor;
				for(String token:tokens){
					splitor = new LatinSplitor(token);
					Set<String> set=splitor.splitLatinString();
					set.add(token);
					for(String le:set){
						ret.add(0,new Lexeme(le));
					}
				}
			}
		}

		public static <T> void  listPush(List<T> target,List<T> source){
			 if(source.size()==1){
				 target.add(0, source.get(0));
			 }else{
				 for(T t:source){
					 target.add(0, t);
				 }
			 }
		 }
		
}
