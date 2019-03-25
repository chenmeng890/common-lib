package com.yihaodian.search.nlp.segment.lucene;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;

public final class SingleCharAnalyzer extends Analyzer{

	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
		return new TokenStreamComponents(new SingleCharTokenizer(reader));
	}
}
