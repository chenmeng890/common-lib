package com.yihaodian.search.nlp.segment.lucene;

import static org.junit.Assert.*;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class TestSingleCharTokenizer {

	@Test
	public void testTokenizer() throws IOException{
		String target = "N70 yn";
		StringReader reader = new StringReader(target);
		SingleCharTokenizer tokenizer = new SingleCharTokenizer(reader);

		CharTermAttribute term = tokenizer
				.getAttribute(CharTermAttribute.class);
		StringBuilder sb =new StringBuilder();
		tokenizer.reset();
		while (tokenizer.incrementToken()) {
			sb.append(new String(term.buffer(), 0, term.length())).append("/");
       }
		assertEquals("N/7/0/y/n/",sb.toString());
	}
}
