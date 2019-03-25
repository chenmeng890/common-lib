package com.yihaodian.search.nlp.segment.lucene;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;


public final class ChineseTokenStream extends TokenStream {
	
	private List<String> tokens;
	
	private int start;
	
	private int index;
	
	static final String TOKEN_TYPE_NAME = "word";
	
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);

	public ChineseTokenStream(List<String> tokens){
		this.tokens = tokens;
		this.start = 0;
		this.index = 0;
	}


	@Override
	public boolean incrementToken() throws IOException {
		if(index >= tokens.size())
		{
			return false;
		}
		final String word = tokens.get(index ++);
		final int end = start + word.length();
		termAtt.copyBuffer(word.toCharArray(),0, word.length());
		offsetAtt.setOffset(start, end);
		typeAtt.setType(TOKEN_TYPE_NAME);
		start=end;
		return true;
	}

	
}
