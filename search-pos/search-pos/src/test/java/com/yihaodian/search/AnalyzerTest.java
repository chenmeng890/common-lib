package com.yihaodian.search;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import com.yihaodian.search.nlp.segment.ReverseSegmenter;
import com.yihaodian.search.nlp.segment.Segmenter;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.yihaodian.search.nlp.model.Dictionary;
import com.yihaodian.search.nlp.model.Lexeme;

import yooso.analyzer.ReverseAnalyzer;

public class AnalyzerTest {
	final static String basePath = "D:/workspace/branchs/SearchPOS/search-pos/data/";

	public static void main(String[] args) throws Exception {
		
		ReverseAnalyzer analyzer = newReverseAnalyzer();
		//Segmenter segmenter = SegmenterFactory.newDefaultSegmenter("D:/var/www/webapps/fulltext/data/dictionary/Data.txt");
		Dictionary dict = new Dictionary(basePath + "dict", true);
		//Segmenter segmenter = new ReverseSegmenter(dict, ReverseSegmenter.TYPE_LATIN_ORIGINAL, true,true);
		Segmenter segmenter = new ReverseSegmenter(dict, ReverseSegmenter.TYPE_LATIN_ORIGINAL, true,true);
		
	  	test_split_file(basePath, analyzer, segmenter);
	  	
		/*
		test_split(new String[]{"宝贝双星条纹宽松大纽扣T恤"
				,"飞利浦PHILIPS 23寸（宽）显示器 239CL2SB"
				,"亨氏DHA蔬菜营养米粉225g 盒","强生OB卫生棉条(量少型)16条"
				,"超能去渍粉(飘香)1.8Kg", "光明牌香浓高钙甜奶粉350g/袋(25g*14袋)"
				,"三洋(SANYO) PLC-XU350C 投影机", "十八子作好顺七件套刀 S2506"
				,"苹果（APPLE）iPhone4 16G（白色）WCDMA/GSM非合约版"
				,"3M思高 810C(12.7MM×33M)神奇 隐形胶带 透明白色"
	  			,"云南绿A螺旋藻300粒*2罐(付款时输入lvalihe11A即可立省50元)"
	  			,"贝亲2个盒装乳胶奶嘴 (中码)"
	  	}, analyzer); */
	  	
	}

	public static ReverseAnalyzer newReverseAnalyzer() throws Exception {
		yooso.analyzer.Dictionary dictionary = new yooso.analyzer.Dictionary(true);
	    /*
		InputStream dictStream = ReverseAnalyzer.class.getResourceAsStream("/dict/test.dict");
		if (dictStream == null)
			throw new RuntimeException("test.dict 词典丢失,无法加载.");
		dictionary.AddDict(dictStream, false);
	    */
	    
		InputStream dictStream = ReverseAnalyzer.class.getResourceAsStream("/dict/yhd_category.dict");
		if (dictStream == null)
			throw new RuntimeException("yhd_category.dict 词典丢失,无法加载.");
		dictionary.AddDict(dictStream, false);
		
		dictStream = ReverseAnalyzer.class.getResourceAsStream("/dict/yhd_brand.dict");
		if (dictStream == null)
			throw new RuntimeException("yhd_brand.dict 词典丢失,无法加载.");
		dictionary.AddDict(dictStream, false);

		dictStream = ReverseAnalyzer.class.getResourceAsStream("/dict/XNum.dict");
		if (dictStream == null)
			throw new RuntimeException("XNum.dict词典丢失,无法加载.");
		dictionary.AddDict(dictStream, true);

		dictStream = ReverseAnalyzer.class.getResourceAsStream("/dict/stopwords.dict");
		if (dictStream == null)
			throw new RuntimeException("stopwords.dict 词典丢失,无法加载.");
		dictionary.AddDict(dictStream, false);

		dictStream = ReverseAnalyzer.class.getResourceAsStream("/dict/yhd_other.dict");
		if (dictStream == null)
			throw new RuntimeException("yhd_other.dict 词典丢失,无法加载.");
		dictionary.AddDict(dictStream, false);

		dictStream = ReverseAnalyzer.class.getResourceAsStream("/dict/t-base.dic");
		if (dictStream == null)
			throw new RuntimeException("t-base.dic 词典丢失,无法加载.");
		dictionary.AddDict(dictStream, false);

		dictStream = ReverseAnalyzer.class.getResourceAsStream("/dict/brand_sym.dict");
		if (dictStream == null)
			throw new RuntimeException("brand_sym.dict 词典丢失,无法加载.");
		dictionary.AddDict(dictStream, false);

		dictStream = ReverseAnalyzer.class.getResourceAsStream("/dict/tb_brand.dict");
		if (dictStream == null)
			throw new RuntimeException("tb_brand.dict 词典丢失,无法加载.");
		dictionary.AddDict(dictStream, false);

		dictStream = ReverseAnalyzer.class.getResourceAsStream("/dict/tb_category.dict");
		if (dictStream == null)
			throw new RuntimeException("tb_category.dict 词典丢失,无法加载.");
		dictionary.AddDict(dictStream, false);
		
		return new ReverseAnalyzer(dictionary);
	}
	

	static void test_split_file(String dataPath, Analyzer analyzer, Segmenter segmenter) throws IOException {
		File productsFile = new File(dataPath, "products.csv");
		CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(productsFile), "GBK"));
		String[] headers = reader.readNext();
		if (! "CN_NAME".equals(headers[1]))
			throw new RuntimeException("products.csv 文件格式不对.");

		File outFile = new File(dataPath, "splited.csv");
		CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(outFile), "GBK"));
	  	writer.writeNext(new String[]{"商品名", "分词结果", "改版分词", "类别", "品牌"});
		
		String[] line = reader.readNext();
		int n = 0;
		while (line != null) {
			String catName = line[0];
			String prdName = line[1];
			String brandName = line[2];
			
			try {
			  	StringBuilder strSplited = new StringBuilder();
				TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(prdName));
				CharTermAttribute term = tokenStream.getAttribute(CharTermAttribute.class);
				while (tokenStream.incrementToken()) {
					if (strSplited.length() > 0)
						strSplited.append(',');
					strSplited.append(new String(term.buffer(), 0, term.length()));
				}
				
				StringBuilder strSplited2 = new StringBuilder();
				/*List<String> words = segmenter.segment(prdName);
				for (String word : words) {
					if (strSplited2.length() > 0)
						strSplited2.append(',');
					strSplited2.append(word);
				}*/
				List<Lexeme> words = segmenter.segmentComplex(prdName);
				for (Lexeme word : words) {
					if (strSplited2.length() > 0)
						strSplited2.append(',');
					strSplited2.append(word.getText());
				}
				
			  	writer.writeNext(new String[]{prdName, strSplited.toString(), strSplited2.toString()
			  			,catName,brandName});
			} catch (Exception e) {
				System.err.println(prdName);
			}
		  	
		  	line = reader.readNext();
		  	n++;
		  	if (n % 100 == 0) System.out.println(n);
		}
		
		writer.close();
		reader.close();
	}

	static void test_split(String[] lines, Analyzer analyzer) throws IOException {
		for (int i=0; i < lines.length; i++) {
			String line = lines[i];
			
		  	StringBuilder strSplited;
			try {
				TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(line));
				strSplited = new StringBuilder();
				CharTermAttribute term = tokenStream.getAttribute(CharTermAttribute.class);
				while (tokenStream.incrementToken()) {
					if (strSplited.length() > 0)
						strSplited.append(',');
					strSplited.append(new String(term.buffer(), 0, term.length()));
				}
				
				System.out.println(line);
				System.out.println("  => " + strSplited.toString());
			} catch (Exception e) {
				System.err.println(line);
				e.printStackTrace();
			}
		}
	}
}
