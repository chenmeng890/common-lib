package com.search.eval.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.search.eval.EvalConfig;
import com.search.eval.EvalResult;
import com.search.eval.EvalResults;
import com.search.eval.Evaltor;
import com.search.eval.SearchDoc;
import com.search.eval.SearchQuery;
import com.search.eval.SearchResult;
import com.search.eval.Searcher;

/**
 * <pre>
 * Evaluation process 
 * 1) if baseline donesn't, create baseline, and pass.
 * 2) if baseline exists, compare current result with baseline, 
 *   2.1) for each query, calc precision/IDCG.
 *   2.2) if precision/IDCG > threshold, then this query result is accepted
 *   2.3) count how mandy query is accpeted
 * 
 * </pre>
 */
public class EvaltorMain {
	static Logger logger = Logger.getLogger(EvaltorMain.class);

	List<SearchQuery> queries = null;
	Map<SearchQuery, SearchResult> baseSearchResults = null;

	Searcher searcher = null;
	Evaltor evaltor = new DCGEvaltor();
	BaselineReader reader = new BaselineReader();

	public EvaltorMain(String targetUrl) {
		init();

		searcher = new SearcherImpl(targetUrl);
	}

	public EvaltorMain() {
		init();
	}

	/**
	 * 
	 */
	void init() {
		String className = EvalConfig.getEvaltorClass();
		try {
			Class<?> clz = Class.forName(className);
			evaltor = (Evaltor) clz.newInstance();
		} catch (Exception e) {
			logger.error("can not instance " + className, e);
		}

		File file = findEvalWord();
		if (file.exists() && file.isFile()) {
			queries = loadQuery(file);
		} else {
			queries = loadQuery(Arrays.asList("牛奶", "咖啡", "巧克力", "奶粉", "奥妙"));
		}

		String cntStr = EvalConfig.getEvalQueryCount();
		int cnt = Math.min(queries.size(), Integer.parseInt(cntStr));
		logger.info(queries);
		logger.info("eval query cnt " + cnt);
		queries = queries.subList(0, cnt);
	}

	/**
	 * 
	 * @return
	 */
	File findEvalWord() {
		String queryFile = EvalConfig.getEvalQuery();
		File file = new File(queryFile);
		if (file.exists() && file.isFile())
			return file;

		URL url = EvaltorMain.class.getClassLoader().getResource("eval_words");
		if (url != null)
			return new File(url.getFile());

		return null;
	}

	/**
	 * 
	 * @param searcher
	 */
	public void setSearcher(Searcher searcher) {
		this.searcher = searcher;
	}

	/**
	 * 
	 * @param queries
	 */
	public void setQuery(List<SearchQuery> queries) {
		this.queries = queries;
	}

	/**
	 * @throws IOException
	 * 
	 */
	public boolean readBaseline() {
		String file = EvalConfig.getEvalBaseline();
		if (!new File(file).exists()) {
			logger.info(file + " baseline not exist");
			return false;
		}

		try {
			baseSearchResults = reader.read(file);

			return baseSearchResults != null && baseSearchResults.size() > 0;
		} catch (IOException e) {
			logger.error("", e);
		}

		return false;
	}

	/**
	 * @throws IOException
	 */
	public void writeBaseline() {
		String file = EvalConfig.getEvalBaseline();

		try {
			reader.save(file, baseSearchResults);
		} catch (IOException e) {
			logger.error("", e);
		}
	}

	/**
	 */
	public void baseline() {
		logger.info("build baseline start");

		if (baseSearchResults == null)
			baseSearchResults = new TreeMap<SearchQuery, SearchResult>(
					SearchQuery.weightComarator);
		else
			baseSearchResults.clear();

		for (int i = 0; i < queries.size(); i++) {
			SearchQuery query = queries.get(i);
			SearchResult result = searcher.search(query);

			baseSearchResults.put(query, result);
		}

		logger.info("build baseline finish");
	}

	public EvalResults eval(boolean resetBaseline) {
		double threshold = Double.parseDouble(EvalConfig.getEvalThreshold());
		return eval(threshold, resetBaseline);
	}

	/**
	 * 
	 * @param resetBaseline
	 *            new search result is kept as baseline if accept
	 * @return
	 */
	public EvalResults eval(double acceptThreshold, boolean resetBaseline) {
		logger.info("eval start");

		// eval
		List<EvalResult> results = new ArrayList<EvalResult>(queries.size());
		Map<SearchQuery, SearchResult> searchResults = new TreeMap<SearchQuery, SearchResult>(
				SearchQuery.weightComarator);
		for (int i = 0; i < queries.size(); i++) {
			SearchQuery query = queries.get(i);
			SearchResult result = searcher.search(query);
			SearchResult baseResult = baseSearchResults.get(query);
			searchResults.put(query, result);

			EvalResult evalResult = evaltor.eval(query, baseResult, result);
			results.add(evalResult);
		}

		// stats
		EvalResults evalResults = new EvalResults();
		evalResults.setResults(results);
		List<EvalResult> fail = new ArrayList<EvalResult>();

		double sum = 0;
		for (EvalResult r : results) {
			if (!r.accept()) {
				fail.add(r);
			}

			sum += r.getResult();
			logger.info(r.getQuery().getQuery() + " " + r.getResult());
		}
		double avg = sum / queries.size();

		evalResults.setTotalCount(results.size());
		evalResults.setFailResults(fail);
		evalResults.setAcceptRatio(avg);
		evalResults.setAcceptThreshold(acceptThreshold);

		// reset
		if (evalResults.accept() && resetBaseline)
			baseSearchResults = searchResults;

		logger.info("total count: " + evalResults.getTotalCount() + ", avg: "
				+ evalResults.getAcceptRatio());
		return evalResults;
	}

	public EvalResult eval(String keyword, Searcher s1, Searcher s2) {
		SearchQuery q = new SearchQuery();
		q.setQuery(keyword);

		SearchResult baseline = s1.search(q);
		SearchResult result = s2.search(q);

		EvalResult evalResult = evaltor.eval(q, baseline, result);

		return evalResult;
	}

	/**
	 * [keyword, categoryId, provinceId, mcSiteId]
	 * 
	 * @param lines
	 * @return
	 */
	public static List<SearchQuery> loadQuery(List<String> lines) {
		List<SearchQuery> queryFromFile = new ArrayList<SearchQuery>();

		int i = 0;
		for (String line : lines) {
			String[] split = line.split(",");
			SearchQuery query = new SearchQuery();

			query.setQuery(split[0]);
			query.setId(i++);
			if (split.length > 1)
				query.setCategoryId(Long.parseLong(split[1]));
			if (split.length > 2)
				query.setProvinceId(Long.parseLong(split[2]));
			if (split.length > 3)
				query.setMcSiteId(Long.parseLong(split[3]));

			queryFromFile.add(query);
		}

		return queryFromFile;
	}

	/**
	 * rebuild query from file
	 * 
	 * @param file
	 */
	public static List<SearchQuery> loadQuery(File file) {
		try {
			logger.info("loading eval words from file "
					+ file.getAbsolutePath());
			List<String> lines = FileUtils.readLines(file, "utf-8");

			return loadQuery(lines);
		} catch (IOException e) {
			logger.error("can not load query from " + file.getAbsolutePath(), e);
		}

		return Collections.EMPTY_LIST;
	}
}
