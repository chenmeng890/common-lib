package com.search.eval;

/**
 * SearchResult, simple one of MandyResponse.
 * 
 */
public class SearchResult {
	/**
	 * total hit of given query
	 */
	int totalHit;

	/**
	 * top Docs
	 */
	SearchDoc[] topDocs;

	String luceneCondition;

	public int getTotalHit() {
		return totalHit;
	}

	public void setTotalHit(int totalHit) {
		this.totalHit = totalHit;
	}

	public SearchDoc[] getTopDocs() {
		return topDocs;
	}

	public void setTopDocs(SearchDoc[] topDocs) {
		this.topDocs = topDocs;
	}

	public String getLuceneCondition() {
		return luceneCondition;
	}

	public void setLuceneCondition(String luceneCondition) {
		this.luceneCondition = luceneCondition;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (SearchDoc doc : topDocs) {
			sb.append(doc);
			sb.append("\n");
		}

		return "\nSearchResult [totalHit=" + totalHit + "\n" + luceneCondition
				+ "\ntopDocs=\n" + sb.toString() + "]\n";
	}
}
