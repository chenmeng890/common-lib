package com.search.eval.impl;

import java.util.HashSet;
import java.util.Set;

import com.search.eval.SearchDoc;
import com.search.eval.SearchResult;

/**
 * precision and recall
 * 
 */
public class PrecisionEvaltor extends AbstractEvaltor {

	/**
	 * precision = (relevant docs ^ retrieved docs ) / retrieved docs
	 */
	@Override
	public double eval(SearchResult baseSearchResult, SearchResult searchResult) {
		SearchDoc[] baseTop = baseSearchResult.getTopDocs();
		SearchDoc[] top = searchResult.getTopDocs();

		Set<String> baseUid = new HashSet<String>();
		for (SearchDoc doc : baseTop)
			baseUid.add(doc.getId());

		int hit = 0;
		for (SearchDoc doc : top) {
			if (baseUid.contains(doc.getId()))
				hit++;
		}

		return hit * 1.0 / top.length;
	}
}
