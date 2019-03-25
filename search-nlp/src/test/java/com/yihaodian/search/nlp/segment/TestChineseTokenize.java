package com.yihaodian.search.nlp.segment;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.junit.Assert;
import org.junit.Test;

import com.yihaodian.search.nlp.segment.solr.ChineseTokenizerFactory;

public class TestChineseTokenize {
	private String origin = "康师傅 伊利牛奶,炸薯条";
	private String dicPath = "/var/www/data/mandy/";
	
	@Test
	public void testFirstToken() throws Exception{
		Map<String, String> map = new HashMap<String, String>();
		map.put("mode", "forward");
		map.put("isNeedMiniSeg", "true");
		// 请指定本地词典路径
		map.put("dicPath", dicPath);
		ChineseTokenizerFactory factory = new ChineseTokenizerFactory(map);
		factory.inform(null);
		
		InputStream is = new ByteArrayInputStream(origin.getBytes());
		Reader reader = new InputStreamReader(is);
		Tokenizer t = factory.create(reader);
		
		t.reset();
		
		boolean flag = t.incrementToken();
		Assert.assertEquals(true, flag);
	}
	
	@Test
	public void testSecondToken() throws Exception{
		Map<String, String> map = new HashMap<String, String>();
		map.put("mode", "forward");
		map.put("isNeedMiniSeg", "true");
		// 请指定本地词典路径
		map.put("dicPath", dicPath);
		ChineseTokenizerFactory factory = new ChineseTokenizerFactory(map);
		factory.inform(null);
		
		InputStream is = new ByteArrayInputStream(origin.getBytes());
		Reader reader = new InputStreamReader(is);
		Tokenizer t = factory.create(reader);
		
		t.reset();
		boolean flag = true;
		while (flag) {
			flag = t.incrementToken();
		}
		t.close();
		
		InputStream is1 = new ByteArrayInputStream(origin.getBytes());
		Reader reader1 = new InputStreamReader(is1);
		t.setReader(reader1);
		t.reset();
		flag = t.incrementToken();
		Assert.assertEquals(true, flag);
	}
	
	@Test
	public void testOffset() throws Exception{
		Map<String, String> map = new HashMap<String, String>();
		map.put("mode", "forward");
		map.put("isNeedMiniSeg", "true");
		// 请指定本地词典路径
		map.put("dicPath", dicPath);
		ChineseTokenizerFactory factory = new ChineseTokenizerFactory(map);
		factory.inform(null);
		
		InputStream is = new ByteArrayInputStream(origin.getBytes());
		Reader reader = new InputStreamReader(is);
		Tokenizer t = factory.create(reader);
		boolean flag = true;
		while (flag) {
			flag = t.incrementToken();
			int start = t.getAttribute(OffsetAttribute.class).startOffset();
			String term = t.getAttribute(CharTermAttribute.class).toString();
			int correctStart = origin.indexOf(term);
			Assert.assertEquals(correctStart, start);
		}
	}
}
