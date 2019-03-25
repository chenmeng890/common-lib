package com.yihaodian.search.nlp.segment;

import java.util.LinkedList;
import java.util.List;

import com.yihaodian.search.nlp.model.Context;
import com.yihaodian.search.nlp.model.Dictionary;
import com.yihaodian.search.nlp.model.Hit;
import com.yihaodian.search.nlp.model.SimpleDictionary;




/**
 * 最小粒度切分
 * @author xiongjian
 *
 */

public class MiniSegmenter{
	/*
	 * 已完成处理的位置
	 */
	private int doneIndex;
	/*
	 * Hit对列，记录匹配中的Hit对象
	 */
	private List<Hit> hitList;
	
	private SimpleDictionary dictionary;
	
	public MiniSegmenter(SimpleDictionary dictionary){
		doneIndex = -1;
		//hitList = new ArrayList<Hit>();
		hitList = new LinkedList<Hit>();
		this.dictionary = dictionary;
	}
	
	public void nextWord(char[] segmentBuff , Context context) {		
			if(hitList.size() > 0){
				//处理词段队列
				Hit[] tmpArray = hitList.toArray(new Hit[hitList.size()]);
				for(Hit hit : tmpArray){
					hit = dictionary.matchWithHit(segmentBuff, context.getCursor() , hit);
					
					if(hit.isMatch()){//匹配成词
						//输出当前的词
						String word = String.valueOf(segmentBuff , hit.getBegin() , context.getCursor() - hit.getBegin() + 1);
//						if(word.length()>1)
							context.addWord(word);						
						//更新goneIndex，标识已处理
						if(doneIndex < context.getCursor()){
							doneIndex = context.getCursor();
						}
						else if(hit.isUnmatch()){ //后面不再可能有匹配了
							//移出当前的hit
							hitList.remove(hit);
						}
						
					}else if(hit.isUnmatch()){//不匹配
						//移出当前的hit
						hitList.remove(hit);
					}
				}
			}
			
			//处理以input为开始的一个新hit
			Hit hit = dictionary.match(segmentBuff, context.getCursor() , 1);
			if(hit.isMatch()){//匹配成词
				//输出当前的词
				String word = String.valueOf(segmentBuff , context.getCursor() , 1);
//				if(word.length()>1)
					context.addWord(word);
				//更新doneIndex，标识已处理
				if(doneIndex < context.getCursor()){
					doneIndex = context.getCursor();
				}

				if(hit.isPrefix()){//同时也是前缀
					//向词段队列增加新的Hit
					hitList.add(hit);
				}
				
			}else if(hit.isPrefix()){//前缀，未匹配成词
				//向词段队列增加新的Hit
				hitList.add(hit);
				
			}else if(hit.isUnmatch()){//不匹配，当前的input不是词，也不是词前缀，将其视为分割性的字符
				if(doneIndex >= context.getCursor()){
					//当前不匹配的字符已经被处理过了，不需要再processUnknown
					return;
				}
				
				//更新doneIndex，标识已处理
				doneIndex = context.getCursor();
			}
			
		
		//缓冲区结束临界处理
		if(context.getCursor() == context.getAvailable() - 1){ //读取缓冲区结束的最后一个字符			
			//清空词段队列
			hitList.clear();;
		}
		
	}

	public void reset() {
		//重置已处理标识
		doneIndex = -1;
		hitList.clear();
	}
}
