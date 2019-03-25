package com.yihaodian.search.nlp.segment.lucene;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.util.Version;


public class PunctuationAnalyzer extends Analyzer {

	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
		PunctuationTokenizer tokenizer = new PunctuationTokenizer(reader);
		TokenStream result = new LowerCaseFilter(Version.LUCENE_43, tokenizer);
		return new TokenStreamComponents(tokenizer, result);
	}
}
