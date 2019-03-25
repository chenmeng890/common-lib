package com.yihaodian.search.nlp.segment.lucene;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.apache.log4j.Logger;

import com.yihaodian.search.nlp.help.WordConvert;
import com.yihaodian.search.nlp.segment.Segmenter;

public class ChineseSeg {
    private Logger log = Logger.getLogger(ChineseSeg.class);
    
	private Segmenter segmenter;
	
	private List<String> tokens;
	
	private int index;
	
	private WordConvert wordConvert = WordConvert.getInstance();
	
	public ChineseSeg(Reader input, Segmenter seg) {
		this.segmenter = seg;
	}
	
	
	public void reset(Reader input) {
		this.index = 0;
		
    	StringBuffer str = new StringBuffer();
		BufferedReader br = new BufferedReader(input);
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
				log.error(e);
			}
		}
		tokens = segmenter.segment(
				wordConvert.complexToSimple(str.toString()));
	}
	
	public String next() {
		if(tokens == null || index >= tokens.size())
		{
			return null;
		}
		
		return tokens.get(index ++);
	}
}
