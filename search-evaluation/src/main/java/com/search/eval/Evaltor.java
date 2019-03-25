package com.search.eval;

/**
 * 
 * To eval a search result, we have different measurement. e.g precision and
 * recall, nDCG.
 * 
 */
public interface Evaltor {
	public EvalResult eval(SearchQuery req, SearchResult baseSearchResult,
			SearchResult searchResult);
}
