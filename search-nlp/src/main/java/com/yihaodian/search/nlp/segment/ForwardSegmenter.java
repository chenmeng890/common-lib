package com.yihaodian.search.nlp.segment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.yihaodian.search.nlp.help.Latin;
import com.yihaodian.search.nlp.model.Dictionary;
import com.yihaodian.search.nlp.model.Lexeme;

/**
 * 正向最大分词器
 * @author xiongjian
 *
 */
public class ForwardSegmenter extends Segmenter {

	 //初始化分词器
	 public ForwardSegmenter(Dictionary dictionary){
		this(dictionary,TYPE_LATIN_ORIGINAL,false,false);
	 }
	 
	 /**
	  * 构造函数
	  * @param dictionary 词典
	  * @param latinType  拉丁文的切分方式
	  * @param isReservedStopWords 是否保留停词
	  * @param isReservedPunctuation  是否保留标点
	  */
	 public ForwardSegmenter(Dictionary dictionary , int latinType,
			 boolean isReservedStopWords,boolean isReservedPunctuation){
		 this.latinType=latinType;
		 this.dictionary=dictionary;
		 this.isReservedStopWords=isReservedStopWords;
		 this.isReservedPunctuation=isReservedPunctuation;
		 if(dictionary.getMaxDepth()>0){
			 this.MAX_SIZE = dictionary.getMaxDepth();
		 }
	 }
	
	@Override
	public List<String> segment(String sentence) {
		if(sentence==null || sentence.length()==0){
				return null;
		}
		sentence = sentence.toLowerCase();
		char[] textChars = sentence.toCharArray();
		List<String> ret = new ArrayList<String>();
		int head = 0;
		int targetLength = sentence.length();
		
		while(head<targetLength){
			char currChar = textChars[head];
			int tail;
			if(isUnAcceptedChar(currChar)){
				 if(isReservedPunctuation){
					 String word=sentence.substring(head,head+1);
					 ret.add(ret.size(),word);
				 }
				 head=head+1;
				 continue;
			}
			boolean isMatchDict = false;
			if(isAcceptedChar(currChar)){
				 tail = getLetterHead(textChars,head);
				 int offset=tail-head;
				 String word=sentence.substring(head,tail);
				 isMatchDict=dictionary.isMatchDict(word,isReservedStopWords);
				 if(isMatchDict){
						ret.add(ret.size(),word);
						head = tail;
					}
				 
				 if(offset<MAX_SIZE&&!isMatchDict){
						int tempTail=head+MAX_SIZE;
						if(tempTail>targetLength) {tempTail=targetLength;}
						for(;tail<tempTail;tempTail--){
							word=sentence.substring(head,tempTail);
							isMatchDict=dictionary.isMatchDict(word,isReservedStopWords);
							if(isMatchDict){
								ret.add(ret.size(),word);
								head = tempTail;
								break;
							}
						}
				 }
				 if(isMatchDict){
					 continue;
				 }else{
					 //实际应该对这些连续字符串再做细致的切分
					 word = sentence.substring(head, tail);
					 splitLatin(word, latinType, ret);
					 head = tail;
					 continue;
				 }
			 }
			tail = head + MAX_SIZE;
			if(tail>targetLength) {tail=targetLength;}
			for(;head<tail;tail--){
				String word = sentence.substring(head, tail);
				isMatchDict=dictionary.isMatchDict(word,isReservedStopWords);
				 if(isMatchDict){
					 ret.add(ret.size(),word);
					 head = tail;
					 break;
				 }
			}
			if(!isMatchDict){
				if(Chn_Num.indexOf(currChar)>=0||Chn_Num_Connector.indexOf(currChar)>=0){
					tail=getCnNumberHead(textChars,head);
					String word = sentence.substring(head, tail);
					ret.add(ret.size(),word);
					head = tail;
				}else{
					String word = sentence.substring(head, head+1);
					 ret.add(ret.size(),word);
					 head++;
				}
			}
		}
		
		return ret;
	}

	@Override
	public List<Lexeme> segmentComplex(String sentence) {
		if(sentence==null || sentence.length()==0){
			return null; 
		}
		sentence = sentence.toLowerCase();
		char[] textChars = sentence.toCharArray();
		List<Lexeme> ret = new ArrayList<Lexeme>();
		int head = 0;
		int targetLength = sentence.length();

		while(head<targetLength){
			char currChar = textChars[head];
			int tail;
			if(isUnAcceptedChar(currChar)){
				 if(isReservedPunctuation){
					 String word=sentence.substring(head,head+1);
					 ret.add(ret.size(),new Lexeme(word));
				 }
				 head=head+1;
				 continue;
			}
			boolean isMatchDict = false;
			if(isAcceptedChar(currChar)){
				 tail = getLetterHead(textChars,head);
				 int offset=tail-head;
				 String word=sentence.substring(head,tail);
				 List<Lexeme> leList=dictionary.matchDict(word,isReservedStopWords);
				 if(leList!=null){
					    isMatchDict=true;
					    listPush(ret, leList);
						head = tail;
					}
				 
				 if(offset<MAX_SIZE&&!isMatchDict){
						int tempTail=head+MAX_SIZE;
						if(tempTail>targetLength) {tempTail=targetLength;}
						for(;tail<tempTail;tempTail--){
							word=sentence.substring(head,tempTail);
							leList=dictionary.matchDict(word,isReservedStopWords);
							if(leList!=null){
								isMatchDict=true;
								listPush(ret, leList);
								head = tempTail;
								break;
							}
						}
				 }
				 if(isMatchDict){
					 continue;
				 }else{
					 //实际应该对这些连续字符串再做细致的切分
					 word = sentence.substring(head, tail);
					 splitLatinByLexeme(word, latinType, ret);
					 head = tail;
					 continue;
				 }
			 }
			tail = head + MAX_SIZE;
			if(tail>targetLength) {tail=targetLength;}
			for(;head<tail;tail--){
				String word = sentence.substring(head, tail);
				List<Lexeme> le=dictionary.matchDict(word,isReservedStopWords);
				 if(le!=null){
					 isMatchDict=true;
					 listPush(ret, le);
					 head = tail;
					 break;
				 }
			}
			if(!isMatchDict){
				if(Chn_Num.indexOf(currChar)>=0||Chn_Num_Connector.indexOf(currChar)>=0){
					tail=getCnNumberHead(textChars,head);
					String word = sentence.substring(head, tail);
					ret.add(ret.size(),new Lexeme(word));
					head = tail;
				}else{
					Lexeme les= new Lexeme(sentence,head, head+1);
					ret.add(ret.size(),les);
					head++;
				}
			}
		}
		return ret;
	}
	
	 /**
	  * 正向寻找一个连续字符串（英文+数字+sign）的起点
	  * @param start
	  * @return
	  */
	 private int getLetterHead(char[] textChars,int start){
		 int end=start;
		 while(end<textChars.length){
			 char currChar=textChars[end];
			 if(Latin.isLetter(currChar)||Latin.isDigit(currChar)){
				 end++;
				 continue;
			 }
			 else if(isLetterConnector(currChar)){
				 end++;
				 continue;
			 }
			 else{
				 break;
			 }
		 }
		 
		 return end;
	 }	
	 
	 /**
	  * 正向寻找一个连续的中文数字字符串起点
	  * @param start
	  * @return
	  */
	 private int getCnNumberHead(char[] textChars,int start){
		 int end=start;
		 while(end<textChars.length){
			 char currChar=textChars[end];
			 if(Chn_Num.indexOf(currChar)>=0||
					 Chn_Num_Connector.indexOf(currChar)>=0){
				 end++;
				 continue;
			 }
			 else{
				 break;
			 }
		 }
		 return end;
	 }

		public void splitLatin(String latinString,int type_latin,List<String> ret){
			List<String> tokens = getLatinToken(latinString,isReservedPunctuation);
			if(type_latin==TYPE_LATIN_ORIGINAL){
				for(String token:tokens)
					ret.add(ret.size(),token);
			}else if(type_latin==TYPE_LATIN_SPLIT){
				LatinSplitor splitor;
				for(String token:tokens){
					splitor = new LatinSplitor(token);
					Set<String> set=splitor.splitLatinString();
					for(String str:set){
						ret.add(ret.size(),str);
					}
				}
			}else if(type_latin==TYPE_LATIN_MIXED){
				LatinSplitor splitor;
				for(String token:tokens){
					splitor = new LatinSplitor(token);
					Set<String> set=splitor.splitLatinString();
					set.add(token);
					for(String str:set){
						ret.add(ret.size(),str);
					}
				}
			}
		}
		
		public void splitLatinByLexeme(String latinString,int type_latin,List<Lexeme> ret){
			List<String> tokens = getLatinToken(latinString,isReservedPunctuation);
			if(type_latin==TYPE_LATIN_ORIGINAL){
				for(String token:tokens){
					ret.add(ret.size(),new Lexeme(token));
				}
			}else if(type_latin==TYPE_LATIN_SPLIT){
				LatinSplitor splitor;
				for(String token:tokens){
					splitor = new LatinSplitor(token);
					Set<Lexeme> set=splitor.splitLatin();
					for(Lexeme le:set){
						ret.add(ret.size(),le);
					}
				}
			}else if(type_latin==TYPE_LATIN_MIXED){
				LatinSplitor splitor;
				for(String token:tokens){
					splitor = new LatinSplitor(token);
					Set<String> set=splitor.splitLatinString();
					set.add(token);
					for(String le:set){
						ret.add(ret.size(),new Lexeme(le));
					}
				}
			}
		}
		
		public static <T> void  listPush(List<T> target,List<T> source){
			 if(source.size()==1){
				 target.add(target.size(), source.get(0));
			 }
			 else{
				 for(T t:source){
					 target.add(target.size(), t);
				 }
			 }
		 }
}
