package com.yihaodian.search.nlp.segment.lucene;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import com.yihaodian.search.nlp.segment.Segmenter;

public class ChineseTokenizer extends Tokenizer {

	private CharTermAttribute termAtt;
	private OffsetAttribute offsetAtt;
	private TypeAttribute typeAtt;
	
	private int start;
	private ChineseSeg seg;
	
	static final String TOKEN_TYPE_NAME = "word";

	public ChineseTokenizer(Segmenter segmenter, Reader input) {
		super(input);

		termAtt = addAttribute(CharTermAttribute.class);
		offsetAtt = addAttribute(OffsetAttribute.class);
		typeAtt = addAttribute(TypeAttribute.class);
		
		seg = new ChineseSeg(input, segmenter);
		start = 0;

	}

	@Override
	public void reset() throws IOException {
		super.reset();
		seg.reset(input);
		start = 0;
	}

	@Override
	public final boolean incrementToken() throws IOException {
		clearAttributes();

		final String word = seg.next();
		if (word != null) {
			final int end = start + word.length();
			termAtt.copyBuffer(word.toCharArray(), 0, word.length());
			offsetAtt.setOffset(start, end);
			typeAtt.setType(TOKEN_TYPE_NAME);
			start = end;
			return true;
		} else {
			end();
			return false;
		}
	}
}
