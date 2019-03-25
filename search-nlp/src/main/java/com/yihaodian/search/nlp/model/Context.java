package com.yihaodian.search.nlp.model;

import java.util.ArrayList;
import java.util.List;


/**
 * 分词器上下文状态
 *
 */
public class Context{
	
    //记录Reader内已分析的字串总长度
    //在分多段分析词元时，该变量累计当前的segmentBuff相对于reader的位移
	private int buffOffset;	
	//最近一次读入的,可处理的字串长度
	private int available;
    //最近一次分析的字串长度
    private int lastAnalyzed;	
    //当前缓冲区位置指针
    private int cursor; 
    //字符窜读取缓冲
    @SuppressWarnings("unused")
	private char[] segmentBuff;

    /*
     * 词结果集，存储切分出来的词
     */
	private List<String> wordList;

    
    public Context(char[] segmentBuff){
    	this.segmentBuff = segmentBuff;
    	this.wordList = new ArrayList<String>();
	}
    
    /**
     * 重置上下文
     */
    public void resetContext(){
    	wordList = new ArrayList<String>();
    	buffOffset = 0;
    	available = 0;
    	lastAnalyzed = 0;
    	cursor = 0;
    }
    
	public int getBuffOffset() {
		return buffOffset;
	}


	public void setBuffOffset(int buffOffset) {
		this.buffOffset = buffOffset;
	}

	public int getLastAnalyzed() {
		return lastAnalyzed;
	}


	public void setLastAnalyzed(int lastAnalyzed) {
		this.lastAnalyzed = lastAnalyzed;
	}


	public int getCursor() {
		return cursor;
	}


	public void setCursor(int cursor) {
		this.cursor = cursor;
	}
	
	

	public int getAvailable() {
		return available;
	}

	public void setAvailable(int available) {
		this.available = available;
	}
	

	public List<String> getWordList() {
		return wordList;
	}

	/**
	 * 向分词结果集添加word
	 */
	public void addWord(String word){
			this.wordList.add(word);
	}
	
	/**
	 * 获取分词结果集大小
	 * @return int 分词结果集大小
	 */
	public int getResultSize(){
		return this.wordList.size();
	}

}
