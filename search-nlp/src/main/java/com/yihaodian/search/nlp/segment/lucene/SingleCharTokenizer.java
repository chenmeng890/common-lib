package com.yihaodian.search.nlp.segment.lucene;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import com.yihaodian.search.nlp.help.Punctuations;

/**
 * 单字符切分器
 * @author yuqian
 *
 */
public final class SingleCharTokenizer extends Tokenizer {

	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);

	static final String TOKEN_TYPE_NAME = "char";

	public SingleCharTokenizer(Reader input) {
		super(input);
	}

	private int offset = 0, bufferIndex = 0, dataLen = 0;
	private static final int MAX_WORD_LEN = 255;
	private static final int IO_BUFFER_SIZE = 4096;
	private final char[] ioBuffer = new char[IO_BUFFER_SIZE];

	@Override
	public boolean incrementToken() throws IOException {
		// token.clear();
		int start = bufferIndex;
		int length = 0;
		int numOfP = 0;
		char[] buffer = termAtt.buffer();
		while (true) {

			if (bufferIndex >= dataLen) {
				offset += dataLen;
				dataLen = input.read(ioBuffer);
				if (dataLen == -1) {
					dataLen = 0; // so next offset += dataLen won't decrement
					// offset
					if (length > 0){
						break;
					}else{
						end();
						return false;
					}
				}
				bufferIndex = 0;
			}

			final char c = ioBuffer[bufferIndex++];
			if (!Punctuations.isPunctuation(c)) {
				if (length == buffer.length){
					buffer = termAtt.resizeBuffer(1 + length);
				}

				buffer[length++] = c;
				if (length == MAX_WORD_LEN) {// buffer overflow!
					break;
				}
				break;

			} else {
				numOfP++;
				if (length > 0){ // at non-Letter w/ chars
					break;
				}
			}
		}

		termAtt.setLength(length);
		offsetAtt.setOffset(start + numOfP, start + numOfP + length);
		typeAtt.setType(TOKEN_TYPE_NAME);

		return true;

	}
	
	@Override
	public void reset() throws IOException {
		super.reset();
	    bufferIndex = 0;
	    offset = 0;
	    dataLen = 0;
	  }

}
