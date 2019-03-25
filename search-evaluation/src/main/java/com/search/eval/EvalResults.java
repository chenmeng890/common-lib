package com.search.eval;

import java.util.List;

/**
 * result for amount of query.
 * 
 */
public class EvalResults {
	/**
	 * result for amount of query.
	 */
	List<EvalResult> results;
	/**
	 * result for failed queries
	 */
	List<EvalResult> failResults;

	/**
	 * size of results
	 */
	int totalCount;

	/**
	 */
	double acceptThreshold;

	/**
	 * how mandy query is accepted
	 */
	double acceptRatio;

	public List<EvalResult> getResults() {
		return results;
	}

	public void setResults(List<EvalResult> results) {
		this.results = results;
	}

	public List<EvalResult> getFailResults() {
		return failResults;
	}

	public void setFailResults(List<EvalResult> failResults) {
		this.failResults = failResults;
	}

	public double getAcceptRatio() {
		return acceptRatio;
	}

	public void setAcceptRatio(double acceptRatio) {
		this.acceptRatio = acceptRatio;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public double getAcceptThreshold() {
		return acceptThreshold;
	}

	public void setAcceptThreshold(double acceptThreshold) {
		this.acceptThreshold = acceptThreshold;
	}

	public boolean accept() {
		return this.getAcceptRatio() >= this.getAcceptThreshold();
	}
}
