package com.search.eval;

import org.junit.Ignore;
import org.junit.Test;

import com.search.eval.impl.EvaltorMain;
import com.search.eval.impl.SearcherImpl;
import com.yihaodian.mandy.service.ProductSearchRequest;

public class EvalTest {

	public void testQuery(String query) {
		EvaltorMain main = new EvaltorMain();

		Searcher s1 = new SearcherImpl("http://10.3.0.153:8080/search/mandy");
		Searcher s2 = new SearcherImpl("http://10.3.0.153:8080/search/mandy",
				-1, ProductSearchRequest.ST_SALENUMBER);

		EvalResult result = main.eval(query, s1, s2);
		System.out.println(result);
	}

	@Test
	@Ignore
	public void testEval() {
		EvaltorMain main = new EvaltorMain();

		Searcher s1 = new SearcherImpl("http://10.3.0.153:8080/search/mandy");
		Searcher s2 = new SearcherImpl("http://10.3.0.153:8080/search/mandy",
				-1, ProductSearchRequest.ST_AGE);

		main.setSearcher(s1);
		main.baseline();
		main.setSearcher(s2);
		main.eval(0.8, false);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		EvalTest test = new EvalTest();
		test.testEval();
		// test.testQuery("咖啡");
	}

}
