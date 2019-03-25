package com.search.eval;

/**
 * result for one query.
 * 
 */
public class EvalResult {
	int id;

	/**
	 * query
	 */
	SearchQuery query;
	/**
	 * base line
	 */
	SearchResult baseSearchResult;
	/**
	 * target
	 */
	SearchResult searchResult;

	/**
	 * result, may be precision/DCG (iscounted cumulative gain), Dependent on
	 * {@link Evaltor} implementation.
	 */
	double result;

	/**
	 * threshold to accept result or not
	 */
	double acceptThreshold;

	public EvalResult() {

	}

	public EvalResult(SearchQuery req, SearchResult baseSearchResult,
			SearchResult searchResult) {
		this.query = req;
		this.baseSearchResult = baseSearchResult;
		this.searchResult = searchResult;
	}

	public SearchQuery getQuery() {
		return query;
	}

	public void setQuery(SearchQuery query) {
		this.query = query;
	}

	public SearchResult getBaseSearchResult() {
		return baseSearchResult;
	}

	public void setBaseSearchResult(SearchResult baseSearchResult) {
		this.baseSearchResult = baseSearchResult;
	}

	public SearchResult getSearchResult() {
		return searchResult;
	}

	public void setSearchResult(SearchResult searchResult) {
		this.searchResult = searchResult;
	}

	public double getResult() {
		return result;
	}

	public void setResult(double result) {
		this.result = result;
	}

	public double getAcceptThreshold() {
		return acceptThreshold;
	}

	public void setAcceptThreshold(double acceptThreshold) {
		this.acceptThreshold = acceptThreshold;
	}

	public boolean accept() {
		return this.result >= this.acceptThreshold;
	}

	public int getId() {
		return query.getId();
	}

	@Override
	public String toString() {
		return "EvalResult [query=" + query + ", baseSearchResult="
				+ baseSearchResult + ", searchResult=" + searchResult
				+ ", result=" + result + ", acceptThreshold=" + acceptThreshold
				+ "]";
	}
}
