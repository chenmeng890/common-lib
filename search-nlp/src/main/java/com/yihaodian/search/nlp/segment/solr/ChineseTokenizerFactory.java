package com.yihaodian.search.nlp.segment.solr;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeSource.AttributeFactory;

import com.yihaodian.search.nlp.model.Dictionary;
import com.yihaodian.search.nlp.segment.Segmenter;
import com.yihaodian.search.nlp.segment.SegmenterFactory;
import com.yihaodian.search.nlp.segment.lucene.ChineseTokenizer;


public class ChineseTokenizerFactory extends TokenizerFactory implements ResourceLoaderAware {

	static final Logger log = Logger.getLogger(ChineseTokenizerFactory.class.getName());
	
	private ThreadLocal<ChineseTokenizer> tokenizerLocal = new ThreadLocal<ChineseTokenizer>();
	
	private Dictionary dict = null;
	
	public static final String FORWARD_SEG = "forward"; //正向最大匹配
	
	public static final String REVERSE_SEG = "reverse"; //逆向最大匹配
	
	public static final String REVERSE_SEG_RP = "reverse_rp"; //逆向最大匹配,保留标点
	
	public static final String FORWORD_REVERSE_SEG = "forward_reverse"; //双向最大匹配
	
	public static final String SLICE_FORWARD_SEG = "slice_forward"; //正向最大+N-gram
	
	public static final String SLICE_REVERSE_SEG = "slice_reverse"; //逆向最大+N-gram
	
	public static final String CHINESE_TOKINZER_DIC = "chinese.tokinzer.dic";

	public ChineseTokenizerFactory(Map<String, String> args) {
		super(args);
	}

	private Segmenter newSeg(Map<String, String> args) {
		log.info("create new Seg ...");
		String mode = args.get("mode");
		String isNeedMiniSeg = args.get("isNeedMiniSeg");
		int segType = 0;
		log.info("mode:" + mode);
		log.info("isNeedMiniSeg:" + isNeedMiniSeg);
		if(mode.equalsIgnoreCase(FORWARD_SEG)) {
			log.info("use forward mode");
			segType = 1;
		} else if(mode.equalsIgnoreCase(REVERSE_SEG)) {
			log.info("use reverse mode");
			segType = 2;
		} else if(mode.equalsIgnoreCase(FORWORD_REVERSE_SEG)) {
			log.info("use forward&reverse mode");
			segType = 3;
		} else if(mode.equalsIgnoreCase(SLICE_FORWARD_SEG)) {
			log.info("use forword&N-Gram mode");
			segType = 4;
		} else if(mode.equalsIgnoreCase(SLICE_REVERSE_SEG)) {
			log.info("use reverse&N-Gram mode");
			segType = 5;
		} else if(mode.equalsIgnoreCase(REVERSE_SEG_RP)) {
			log.info("use reverse mode, reserved punctuation");
			segType = 6;
		} else {
			log.info("incompatible mode");
		}
		return SegmenterFactory.mixSegmenter(dict.getDictPath(), dict, segType, new Boolean(isNeedMiniSeg).booleanValue());
	}

	@Override
	public Tokenizer create(AttributeFactory factory, Reader input) {
		ChineseTokenizer tokenizer = tokenizerLocal.get();
		if(tokenizer == null) {
			tokenizer = newTokenizer(input);
		} else {
			try {
				tokenizer.setReader(input);
			} catch (IOException e) {
				tokenizer = newTokenizer(input);
				log.info("ChineseTokenizer.reset i/o error by:"+e.getMessage());
			}
		}

		return tokenizer;
	}

	private ChineseTokenizer newTokenizer(Reader input) {
		ChineseTokenizer tokenizer = new ChineseTokenizer(newSeg(getOriginalArgs()), input);
		tokenizerLocal.set(tokenizer);
		return tokenizer;
	}

	@Override
	public void inform(ResourceLoader loader) {
		String dictpath = getOriginalArgs().get("dicPath");
		/**
		 * Check if the absolute path is exist.Otherwise, use the relative path.
		 */
		File dictDir = new File(dictpath);
		if (!dictDir.exists()) {
			dictpath = dictpath.substring(1);
			dictDir = new File(dictpath);
			if(!dictDir.exists()){
				dictpath = System.getProperty(CHINESE_TOKINZER_DIC);
			}
		}
		
		dict = Dictionary.getInstance(dictpath, true);
		log.info("dic load... in=" + dict.getDictPath());
	}
	
	/*public static void main(String[] args) {
		String dictpath = new File("var/dict").getAbsolutePath();
		System.out.println(dictpath);
		Dictionary dict = new Dictionary(dictpath, true);
	}*/
}

