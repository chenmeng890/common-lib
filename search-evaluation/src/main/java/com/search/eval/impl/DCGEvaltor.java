package com.search.eval.impl;

import org.apache.log4j.Logger;

import com.search.eval.SearchDoc;
import com.search.eval.SearchResult;

/**
 * see http://en.wikipedia.org/wiki/Discounted_cumulative_gain
 * 
 */

public class DCGEvaltor extends AbstractEvaltor {
	static Logger logger = Logger.getLogger(DCGEvaltor.class);

	// int top = EvalConfig.getEvalTop();

	@Override
	public double eval(SearchResult baseSearchResult, SearchResult searchResult) {
		SearchDoc[] baseTop = baseSearchResult.getTopDocs();
		SearchDoc[] top = searchResult.getTopDocs();

		String[] baseId = new String[baseTop.length];
		for (int i = 0; i < baseTop.length; i++)
			baseId[i] = baseTop[i].getId();

		int total = baseTop.length;
		double DCG = 0;
		int cnt = 0;
		for (int i = 0; i < top.length; i++) {
			String cid = top[i].getId();
			for (int j = 0; j < baseId.length; j++) {
				if (cid.equals(baseId[j])) {
					// for below case, the rel value should be cut down
					// rank 0 --> rank 36
					// rank 36 --> rank 0
					int pos = (int) Math.ceil((j + 1 + i + 1) / 2.0);
					DCG += discountRel(rel(j, total), pos);
					cnt++;
				}
			}
		}

		double iDCG = iDCG(total);
		double nDCG = iDCG > 0 ? DCG / iDCG : 0;

		logger.info("nDCG=" + nDCG + ", precision=" + cnt * 1.0 / total);
		return nDCG;
	}

	public int rel(int pos, int total) {
		// return total - pos;
		return 1;
	}

	/**
	 * suppose total has 36
	 * 
	 * <pre>
	 * rank rel
	 *  1  36
	 *  2  35
	 *  3  34
	 *  4  33
	 *  5  32
	 *  6  31
	 * </pre>
	 * 
	 * @param num
	 * @return
	 */
	public double iDCG(int num) {
		double sum = 0;
		for (int i = 0; i < num; i++) {
			sum += discountRel(rel(i, num), i + 1);
		}

		return sum;
	}

	/**
	 * 
	 * @param rel
	 * @param pos
	 * @return
	 */
	public double discountRel(int rel, int pos) {
		double divisor = Math.pow(2, rel) - 1;
		double denominator = Math.log(pos + 1) / Math.log(2);

		double ret = divisor / denominator;

		if (logger.isDebugEnabled()) {
			logger.debug(ret);
		}

		return ret;
	}

	public static void main(String[] args) {
		DCGEvaltor valtor = new DCGEvaltor();
		double sum = valtor.iDCG(6);
		System.out.println(sum);
	}

}
