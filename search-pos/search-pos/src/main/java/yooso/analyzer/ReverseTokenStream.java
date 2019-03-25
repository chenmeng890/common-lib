package yooso.analyzer;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

public class ReverseTokenStream extends TokenStream {
  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	
  public ReverseTokenStream(Reader reader, ReverseSplitor splitor) {
    this.reader = reader;
    this.splitor = splitor;
    nextDictTokens();
  }
  Reader reader;
  ReverseSplitor splitor;
  
  char[] bufChars = new char[1024];
  int lastReadOffset, lastSplitOffset;
  ArrayList<DictToken> dictTokens;
  int nextTokenIndex;
  
  void nextDictTokens() {
  	if (lastReadOffset < 0) {
  		dictTokens = null;
  		return;
  	}
  	if (lastReadOffset > lastSplitOffset) {
  		int tnCount = lastReadOffset - lastSplitOffset;
  		for (int i=0; i < tnCount; i++)
  			bufChars[i] = bufChars[lastSplitOffset + i];
  		try {
  			lastReadOffset = reader.read(bufChars, tnCount, bufChars.length -tnCount);
  			if (lastReadOffset < 0)
  				lastSplitOffset = tnCount;
  			else {
  				lastReadOffset += tnCount;
  				lastSplitOffset = lastReadOffset;
  			}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
  	}
  	else {
  		try {
				lastReadOffset = reader.read(bufChars);
				lastSplitOffset = lastReadOffset;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
  	}
  	
  	if (lastReadOffset < 0) {
  		if (lastSplitOffset <= 0) {
  			dictTokens = null;
  			return;
  		}
  	}
  	else {
  		// 查找最后一个空格或控制符,作为本段分词的末尾
  		int tn = lastSplitOffset -1;
  		for (; tn >= 0; tn--) {
  			char currChar = bufChars[tn];
  			if (currChar <= ' ') break;
  		}
  		if (tn >= 0)
  			lastSplitOffset = tn +1;
  	}
  	
  	dictTokens = splitor.doSplit(bufChars, 0, lastSplitOffset);
  	/*
  	for (int i=dictTokens.size()-1; i >= 0; i--) {
  		DictToken dictToken = dictTokens.get(i);
  		boolean bDoDelete = false;
  		if (dictToken.length == 1)
  			if (bufChars[dictToken.offset] < 128)
  				bDoDelete = true;
  		if (bDoDelete)
  			dictTokens.remove(i);
  	}
  	*/
  	
  	if (! dictTokens.isEmpty())
  		nextTokenIndex = dictTokens.size() -1;
  	else
  		nextDictTokens();
  }

  /** Returns the next token in the stream, or null at EOS. */
//  public Token next() throws IOException {
//  	if (dictTokens == null) return null;
//  	
//  	DictToken dictToken = dictTokens.get(nextTokenIndex);
//  	String termText;
//  	if (dictToken.tokenStr != null)
//  		termText = dictToken.tokenStr;
//  	else
//  		termText = new String(bufChars, dictToken.offset, dictToken.length);
//  	Token token = new Token(termText, dictToken.offset, dictToken.offset+dictToken.length);
//
//  	if (nextTokenIndex == 0)
//  		nextDictTokens();
//  	else
//  		nextTokenIndex--;
//  	return token;
//  }

@Override
public boolean incrementToken() throws IOException {
  	if (dictTokens == null) return false;
  	
  	DictToken dictToken = dictTokens.get(nextTokenIndex);
  	String termText;
  	if (dictToken.tokenStr != null)
  		termText = dictToken.tokenStr;
  	else
  		termText = new String(bufChars, dictToken.offset, dictToken.length);
//  	Token token = new Token(termText, dictToken.offset, dictToken.offset+dictToken.length);
  	termAtt.copyBuffer(termText.toCharArray(), 0, termText.length());
  	offsetAtt.setOffset(dictToken.offset, dictToken.offset+dictToken.length);

  	if (nextTokenIndex == 0)
  		nextDictTokens();
  	else
  		nextTokenIndex--;
  	return true;
}

}
