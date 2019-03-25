package com.search.eval;

import java.io.IOException;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import com.search.eval.impl.BaselineReader;
import com.search.eval.impl.EvaltorMain;

public class BaselineReaderTest {

	@Ignore
	@Test
	public void testWrite() {
		String ip = "10.2.1.49";
		String url = "http://" + ip + ":8080/search/mandy";
		EvaltorMain main = new EvaltorMain(url);
		main.baseline();
		main.writeBaseline();
	}

	@Ignore
	@Test
	public void testRead() throws IOException {
		BaselineReader reader = new BaselineReader();
		Map<SearchQuery, SearchResult> query = reader.read(EvalConfig
				.getEvalBaseline());
	}

}
