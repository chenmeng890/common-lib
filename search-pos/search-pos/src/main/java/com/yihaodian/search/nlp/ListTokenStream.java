package com.yihaodian.search.nlp;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

public class ListTokenStream extends TokenStream {
	
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

	private String[] tokens;
	private int index;
	
	public ListTokenStream(Collection<String> stokens){
		this.index = 0;
		//去重处理
		if (stokens != null) {
			List<String> setWords = new LinkedList<String>();
			for (String token : stokens) {
				if (token != null && (!setWords.contains(token)))
					setWords.add(token);
			}
			tokens = new String[setWords.size()];
			setWords.toArray(tokens);
		}
		else this.tokens = null;
	}

//	@Override
//	public Token next() throws IOException {
//		if (tokens == null)
//			return null;
//		if(index >= tokens.length){
//			return null;
//		}
//		String word = tokens[index];
//		index++;
//		Token token = new Token(word, 0, word.length());
//		return token;
//	}

	@Override
	public final boolean incrementToken() throws IOException {
		if (tokens == null)
			return false;
		if(index >= tokens.length){
			return false;
		}
		String word = tokens[index];
		index++;
		termAtt.copyBuffer(word.toCharArray(), 0, word.length());
		return true;
	}
	
}
