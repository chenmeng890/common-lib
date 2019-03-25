package com.yihaodian.search.nlp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

//import com.yihaodian.search.nlp.model.Dict;
import com.yihaodian.search.nlp.model.Dictionary;
import com.yihaodian.search.nlp.model.Lexeme;
import com.yihaodian.search.nlp.segment.LatinSplitor;
import com.yihaodian.search.nlp.segment.MixSegmenter;
import com.yihaodian.search.nlp.segment.ReverseSegmenter;
import com.yihaodian.search.nlp.segment.Segmenter;
import com.yihaodian.search.nlp.segment.SegmenterFactory;
//import com.yihaodian.search.nlp.segment.impl.BackwardMaxSegmenter;

public class TestReverseSegment {
	public static void main(String[] args) throws Exception {
		String dictpath="D:/workspace/main/search-nlp/src/test/resources/dictionary";
		Dictionary dict=new Dictionary(dictpath,true);
		dict.setMaxDepth(8);
//		ReverseSegmenter newSeg = new ReverseSegmenter(dict,3,true,true,true);
		Segmenter newSeg=SegmenterFactory.newQuerySegmenter(dict);
		/*String dictPath="D:\\linux_fs\\var\\www\\data\\Data.txt";
		Dict dictionary = new Dict();
		dictionary.setDictPath(dictPath);
		dictionary.load();
		
		BackwardMaxSegmenter oldSeg = new BackwardMaxSegmenter();
		oldSeg.setDict(dictionary);*/
		
		String str="一九九七年七月  P700i";
		List<String> list=newSeg.segment(str);
		if(list==null)
			System.out.println("error!");
		else{
			for(String le:list)
				System.out.println(le);
		}
		/*String keywordsCsvFile="D:/pName.csv";
		File csvFile=new File(keywordsCsvFile);
		CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(csvFile), "GBK"));
		String [] headLine = reader.readNext();
		if(!"PRODUCT_CNAME".equals(headLine[1]))
			throw new RuntimeException("Error ProductCnName CSV file head.");
		String outFile = keywordsCsvFile.substring(0, keywordsCsvFile.length()-4) +" 2.csv";
		CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(outFile), "GBK"));
		String[] writeLine = {"产品名称","新版分词","旧版分词"};
//		writer.writeNext(writeLine);
		
		String [] nextLine;
		int n=0;
		long t1=System.currentTimeMillis();
	    while ((nextLine = reader.readNext()) != null) {
	    	String productName=nextLine[1];
	    	String words1 =getNewWords(productName, newSeg);
//	    	String words2 =getOldWords(productName, oldSeg);
	    	writeLine[0]=productName;
	    	writeLine[1]=words1;
//	    	writeLine[2]=words2;
	    	writer.writeNext(writeLine);
	    	
	    	n++;
	    	if (n % 100 == 0)
	    		System.out.println(n);
	    }
//	    writer.close();
	    reader.close();
	    System.out.println(System.currentTimeMillis()-t1);*/
		
	}

	public static String getNewWords(String str,ReverseSegmenter segment){ 
		List<String> list=segment.segment(str);
		if(list!=null){
			Set<String> set=new HashSet<String>();
			StringBuilder sb=new StringBuilder();
			for(String le:list){
				if(!set.contains(le)){
					set.add(le);
					sb.append(le).append("/");
				}
			}
			if(sb.length()>0)
				return sb.toString();
		}
		return null;
	}
	
	public static String getOldWords(String str,Segmenter segment){
		List<String> list=segment.segment(str);
		if(list!=null&&!list.isEmpty()){
			StringBuilder sb=new StringBuilder();
			for(String s:list)
				sb.append(s).append("/");
			return sb.toString();
		}
		return null;
		
	}
}
