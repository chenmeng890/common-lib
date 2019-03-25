package com.yihaodian.search.nlp.segment.lucene;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import com.yihaodian.search.nlp.model.Context;
import com.yihaodian.search.nlp.model.SimpleDictionary;
import com.yihaodian.search.nlp.segment.MiniSegmenter;


public class MiniSegmentation {
	
	//默认缓冲区大小
	private static final int BUFF_SIZE = 3072;
	//缓冲区耗尽的临界值
	private static final int BUFF_EXHAUST_CRITICAL = 48;	
    //字符窜读取缓冲
    private char[] segmentBuff;
	//分词器上下文
	private Context context;
	
	private MiniSegmenter segmenter;
	
	
	public MiniSegmentation(SimpleDictionary dictionary) {
        segmentBuff = new char[BUFF_SIZE];
		context = new Context(segmentBuff);
		segmenter = new MiniSegmenter(dictionary);
	}
	
	
	/**
	 * 获取分词后的list
	 */
	public synchronized List<String> segment(String word) throws IOException {
		Reader input = new StringReader(word);
		if(context.getResultSize() == 0){
			/*
			 * 从reader中读取数据，填充buffer
			 * 如果reader是分次读入buffer的，那么buffer要进行移位处理
			 * 移位处理上次读入的但未处理的数据
			 */
			int available = fillBuffer(input);
			
            if(available <= 0){
            	context.resetContext();
                return null;
            }else{
            	//分词处理
            	int analyzedLength = 0;
        		for(int buffIndex = 0 ; buffIndex < available ;  buffIndex++){
        			//移动缓冲区指针
        			context.setCursor(buffIndex);
        			//进行字符规格化（全角转半角，大写转小写处理）
        			segmentBuff[buffIndex] = regularize(segmentBuff[buffIndex]);

        			segmenter.nextWord(segmentBuff , context);
        			analyzedLength++;
        			/*
        			 * 满足一下条件时，
        			 * 1.available == BUFF_SIZE 表示buffer满载
        			 * 2.buffIndex < available - 1 && buffIndex > available - BUFF_EXHAUST_CRITICAL表示当前指针处于临界区内
        			 * 3.!context.isBufferLocked()表示没有segmenter在占用buffer
        			 * 要中断当前循环（buffer要进行移位，并再读取数据的操作）
        			 */        			
        			if(available == BUFF_SIZE
        					&& buffIndex < available - 1   
        					&& buffIndex > available - BUFF_EXHAUST_CRITICAL){

        				break;
        			}
        		}
				
				segmenter.reset();
        		//System.out.println(available + " : " +  buffIndex);
            	//记录最近一次分析的字符长度
        		context.setLastAnalyzed(analyzedLength);
            	//同时累计已分析的字符长度
        		context.setBuffOffset(context.getBuffOffset() + analyzedLength);
            	//读取词元池中的词元
            	return context.getWordList();
            }
		}else{
			//读取词元池中的已有词元
			return context.getWordList();
		}	
	}
	
    /**
     * 根据context的上下文情况，填充segmentBuff 
     * @param reader
     * @return 返回待分析的（有效的）字串长度
     * @throws IOException 
     */
    private int fillBuffer(Reader reader) throws IOException{
    	int readCount = 0;
    	if(context.getBuffOffset() == 0){
    		//首次读取reader
    		readCount = reader.read(segmentBuff);
    	}else{
    		int offset = context.getAvailable() - context.getLastAnalyzed();
    		if(offset > 0){
    			//最近一次读取的>最近一次处理的，将未处理的字串拷贝到segmentBuff头部
    			System.arraycopy(segmentBuff , context.getLastAnalyzed() , this.segmentBuff , 0 , offset);
    			readCount = offset;
    		}
    		//继续读取reader ，以onceReadIn - onceAnalyzed为起始位置，继续填充segmentBuff剩余的部分
    		readCount += reader.read(segmentBuff , offset , BUFF_SIZE - offset);
    	}            	
    	//记录最后一次从Reader中读入的可用字符长度
    	context.setAvailable(readCount);
    	return readCount;
    }
    
	/**
	 * 进行字符规格化（全角转半角，大写转小写处理）
	 * @param input
	 * @return char
	 */
	public static char regularize(char input){
        if (input == 12288) {
            input = (char) 32;
            
        }else if (input > 65280 && input < 65375) {
            input = (char) (input - 65248);
            
        }else if (input >= 'A' && input <= 'Z') {
        	input += 32;
		}
        
        return input;
	}
	
	public void clearContext(){
		context.resetContext();
	}

}
