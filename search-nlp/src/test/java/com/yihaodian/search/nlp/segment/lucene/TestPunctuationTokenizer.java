package com.yihaodian.search.nlp.segment.lucene;

import static org.junit.Assert.*;
import org.junit.Test;

import java.io.StringReader;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class TestPunctuationTokenizer {

	@Test
	public void testTokenizer() throws Exception {
		String target = "Norm70 yn 你好";
		StringReader reader = new StringReader(target);
		PunctuationTokenizer tokenizer = new PunctuationTokenizer(reader);
		CharTermAttribute term = tokenizer.getAttribute(CharTermAttribute.class);
		StringBuilder sb =new StringBuilder();
		tokenizer.reset();
		while (tokenizer.incrementToken()) {
			sb.append(new String(term.buffer(), 0, term.length())).append("/");
			}
		assertEquals("Norm/70/yn/你/好/",sb.toString());
	}
}
