package yooso.analyzer;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttributeImpl;

import java.io.Reader;
import java.io.IOException;

public class KnifeAnalyzer extends Analyzer {
    public KnifeAnalyzer() {
    }

    public final TokenStream tokenStream(String string, Reader reader) {
        char[] bufChars = new char[1024];
        try {
            int len = reader.read(bufChars);
            String ts = new String(bufChars, 0, len);
            String[] tas = ts.split(" ");
            Token[] tokens = new Token[tas.length];
            int offset = 0;
            for (int i=0; i < tokens.length; i++) {
                tokens[i] = new Token(tas[i], offset, tas[i].length()) ;
                offset += tas[i].length() + 1;
            }
            return new TestTokenStream(tokens);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static class TestTokenStream extends TokenStream {
    	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    	
        public TestTokenStream (Token[] tokens) {
            this.tokens = tokens;
        }
        Token[] tokens;
        int token_index;

        /** Returns the next token in the stream, or null at EOS. */
        public Token next() throws IOException {
            if (token_index < tokens.length) {
                token_index++;
                return tokens[token_index-1];
            }
            return null;
        }

		@Override
		public boolean incrementToken() throws IOException {
			if (token_index < tokens.length) {
	                token_index++;
	                
	                Token token = tokens[token_index-1];
	                token.copyTo((CharTermAttributeImpl)termAtt);
//	                termAtt.copyBuffer(buffer, offset, length);
	                return true;
	        }
			
			return false;
		}
    }
}
