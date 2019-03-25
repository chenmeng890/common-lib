package com.yihaodian.search.nlp.segment.lucene;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import com.yihaodian.search.nlp.help.Latin;
import com.yihaodian.search.nlp.help.Punctuations;


/**
 * 基于标点的切分器
 * @author yuqian
 *
 */
public final class PunctuationTokenizer extends Tokenizer {
	
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	
	public PunctuationTokenizer(Reader input) {
		super(input);
	}

	private int offset = 0, bufferIndex = 0, dataLen = 0;
	private static final int MAX_WORD_LEN = 255;
	private static final int IO_BUFFER_SIZE = 4096;
	private final char[] ioBuffer = new char[IO_BUFFER_SIZE];

	@Override
	public boolean incrementToken() throws IOException {
		int length = 0;

		int digitType = 0;

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

			if (!Punctuations.isPunctuation(c)) { // if it's a token char
	
				if (length == buffer.length){
					buffer = termAtt.resizeBuffer(1 + length);
				}

				if (Latin.isDigit(c)) {
					if (length == 0) {
						digitType = 1;
					} else if (digitType != 1) {
						bufferIndex--;
						break;
					}
				} 
				/*else if (ChineseDigit.isDigit(c)) {
					if (length == 0) {
						digitType = 2;
					} else if (digitType != 2) {
						bufferIndex--;
						break;
					}
				}*/ 
				else if (length > 0 && digitType>0) {
					bufferIndex--;
					break;
				}

				buffer[length++] = c; // buffer it

				if(digitType>0) {continue;}
					
				if (length == MAX_WORD_LEN){ // buffer overflow!
					break;
				}
				if (isCJ(c)){
					if(length>1){
						bufferIndex--;
						length--;
					}
					break;
				}

			} else if (length > 0){ // at non-Letter w/ chars
				break; // return 'em
			}
		}


		termAtt.setLength(length);
		offsetAtt.setOffset(offset, offset + length);
		return true;
	}


	@Override
	public void reset() throws IOException {
		super.reset();
		bufferIndex = 0;
		offset = 0;
		dataLen = 0;
	}

	// Chinese and Japanese (but NOT Korean)
	public static boolean isCJ(int c) {
		return (0x3100 <= c && c <= 0x312f) || (0x3040 <= c && c <= 0x309F)
				|| (0x30A0 <= c && c <= 0x30FF) || (0x31F0 <= c && c <= 0x31FF)
				|| (0x3300 <= c && c <= 0x337f) || (0x3400 <= c && c <= 0x4dbf)
				|| (0x4e00 <= c && c <= 0x9fff) || (0xf900 <= c && c <= 0xfaff)
				|| (0xff65 <= c && c <= 0xff9f);
	}

	public static boolean isCJString(String s){
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (isCJ(c)){
				continue;
			}
			return false;
		}
		return true;
	}
	
	
}
