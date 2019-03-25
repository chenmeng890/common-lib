package com.yihaodian.search.nlp.segment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;


import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.yihaodian.search.nlp.model.Dictionary;
import com.yihaodian.search.nlp.model.Lexeme;

public class TestSegmenter {
    private ForwardSegmenter fSeg;
    private ReverseSegmenter rSeg;
    
    public void init(){
//    	String dictpath="D:/workspace/main/search-nlp/src/test/resources/dictionary";
    	String dictpath ="/var/www/data/mandy/";
		Dictionary dictionary=new Dictionary(dictpath,true);
		fSeg=new ForwardSegmenter(dictionary);
		rSeg=new ReverseSegmenter(dictionary);
    }
    
    public void testSegmenter(String dataPath)throws IOException{
    	if(fSeg==null){
			init();
		}
    	File productsFile = new File(dataPath, "products.csv");
		CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(productsFile), "GBK"));
		String[] headers = reader.readNext();
		if (! "CN_NAME".equals(headers[1]))
			throw new RuntimeException("products.csv 文件格式不对.");

		File outFile = new File(dataPath, "splited.csv");
		CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(outFile), "GBK"));
	  	writer.writeNext(new String[]{"商品名", "正向最大分词", "逆向最大分词"});
		
		String[] line = reader.readNext();
		int n = 0;
		while (line != null) {
//			String catName = line[0];
			String prdName = line[1];
//			String brandName = line[2];
			
			try {
			  
				
				StringBuilder strSplited1 = new StringBuilder();
				
				List<Lexeme> words = fSeg.segmentComplex(prdName);
				for (Lexeme word : words) {
					if (strSplited1.length() > 0)
						strSplited1.append(',');
					strSplited1.append(word.getText());
				}
				
				StringBuilder strSplited2 = new StringBuilder();
				List<Lexeme> lexemes = rSeg.segmentComplex(prdName);
				for(Lexeme le:lexemes){
					if (strSplited2.length() > 0)
						strSplited2.append(',');
					strSplited2.append(le.getText());
				}
				
			  	writer.writeNext(new String[]{prdName, strSplited1.toString()
			  			,strSplited2.toString()});
			} catch (Exception e) {
				System.err.println(prdName);
				e.printStackTrace();
			}
		  	
		  	line = reader.readNext();
		  	n++;
		  	if (n % 100 == 0) 
		  		System.out.println(n+"------"+prdName);
		}
		
		writer.close();
		reader.close();
    }
    
//    public static void main(String[] args) {
//    	TestSegmenter test=new TestSegmenter();
//    	String basePath = "D:/workspace/main/search-pos/search-pos/data/";
//    	test.init();
//    	try {
//			test.testSegmenter(basePath);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}
