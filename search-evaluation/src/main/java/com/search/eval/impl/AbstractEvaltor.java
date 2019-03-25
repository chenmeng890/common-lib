package com.search.eval.impl;

import com.search.eval.EvalResult;
import com.search.eval.Evaltor;
import com.search.eval.SearchQuery;
import com.search.eval.SearchResult;

/**
 * 
 */
public abstract class AbstractEvaltor implements Evaltor {
	double threshold = 0.6;

	@Override
	public EvalResult eval(SearchQuery req, SearchResult baseSearchResult,
			SearchResult searchResult) {
		EvalResult evalResult = new EvalResult(req, baseSearchResult,
				searchResult);

		double result = 0;
		if (baseSearchResult.getTopDocs() != null
				&& searchResult.getTopDocs() != null) {
			result = eval(baseSearchResult, searchResult);
		}

		double threshold = acceptThreshold();
		evalResult.setResult(result);
		evalResult.setAcceptThreshold(threshold);

		return evalResult;
	}

	public abstract double eval(SearchResult baseSearchResult,
			SearchResult searchResult);

	public double acceptThreshold() {
		return threshold;
	}
}
