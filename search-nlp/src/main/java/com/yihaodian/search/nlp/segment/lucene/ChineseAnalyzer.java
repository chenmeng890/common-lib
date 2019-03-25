package com.yihaodian.search.nlp.segment.lucene;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;

import com.yihaodian.search.nlp.model.Dictionary;
import com.yihaodian.search.nlp.model.Lexeme;
import com.yihaodian.search.nlp.segment.Segmenter;
import com.yihaodian.search.nlp.segment.SegmenterFactory;

public final class ChineseAnalyzer extends Analyzer {
	private Logger log = Logger.getLogger(ChineseAnalyzer.class);
	private Segmenter segmenter;
	private boolean isSimpleMode=false; //调用的是否简洁分词
	
	public ChineseAnalyzer(String dictPath) throws IOException{
		this.segmenter = SegmenterFactory.newDefaultSegmenter(dictPath);
	}
	
	public ChineseAnalyzer(Segmenter segmenter){
		this(segmenter,false);
	}
	
	public ChineseAnalyzer(Segmenter segmenter,boolean isSimpleMode){
		this.segmenter = segmenter;
		this.isSimpleMode = isSimpleMode;
	}
	
	public ChineseAnalyzer(){
		
	}

	public List<String> getTokenList(String sentence){
		if(!isSimpleMode){
			List<Lexeme> lexemeList=segmenter.segmentComplex(sentence);
			if(lexemeList==null||lexemeList.isEmpty()){
				return null;
			}
			Set<String> wordSet=new LinkedHashSet<String>();
			String[] extendWords;
			String[] synonyms;
			for(Lexeme le:lexemeList){
				String word=le.getText().trim();
				wordSet.add(word);
				extendWords=le.getExtendWords();
				if(extendWords!=null&&extendWords.length>0){
					for(String ex:extendWords){
						
							wordSet.add(ex.trim());
					}
				}
				synonyms=le.getSynonyms();
				if(synonyms!=null&&synonyms.length>0){
					for(String sy:synonyms){
							wordSet.add(sy.trim());
					}
				}
			}
			return new ArrayList<String>(wordSet);
		}else{
			return segmenter.segment(sentence);
		}
	}
	
	public Segmenter getSegmenter() {
		return segmenter;
	}

	public void setSegmenter(Segmenter segmenter) {
		this.segmenter = segmenter;
	}
	
	
	public static void main(String[] args) throws IOException {
		String dictpath="D:/var/www/data/mandy/dict";

		Dictionary dict = new Dictionary(dictpath, true);
		Segmenter seg = SegmenterFactory.mixSegmenter(dictpath, dict, 1, true);
		ChineseAnalyzer analyzer=new ChineseAnalyzer(seg);
		String sentence="进口咖啡";
		List<String> list=analyzer.getTokenList(sentence);
		for(String s:list){
			System.out.println(s);
		}
		String s = " <12.s?d/f#啊g^一&哦";
	    String resultString = s.replaceAll("(?i)[^a-zA-Z0-9\u4E00-\u9FA5]", "");  
	    System.out.println(resultString);
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
		StringBuffer str = new StringBuffer();
		BufferedReader br = new BufferedReader(reader);
		String strTmp = "";
		try {
			strTmp = br.readLine();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		while (strTmp != null) {
			str.append(strTmp);
			try {
				strTmp = br.readLine();
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}
		List<String> tokens=getTokenList(str.toString());
		return new TokenStreamComponents(new ChineseTokenizer(segmenter, reader), new ChineseTokenStream(tokens));      
        
	}
}
