package com.search.eval;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.search.eval.impl.DCGEvaltor;
import com.search.eval.impl.PrecisionEvaltor;
import com.search.eval.impl.ProductDoc;
import com.yihaodian.mandy.model.Product;

public class EvalTest2 {

	public static SearchResult readResult(String file) throws IOException {
		List<String> lines = FileUtils.readLines(new File(file));

		SearchDoc[] topDocs = new SearchDoc[20];
		int i = 0;
		for (String line : lines) {
			String[] split = line.split(" ");
			ProductDoc productDoc = new ProductDoc();
			Product product = new Product();
			product.setPmInfoId(Long.parseLong(split[0]));
			product.setCnName(split[1]);
			productDoc.setProduct(product);

			topDocs[i++] = productDoc;
		}

		SearchResult result = new SearchResult();
		result.setTopDocs(topDocs);

		return result;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		DCGEvaltor evaltor = new DCGEvaltor();
		// PrecisionEvaltor evaltor = new PrecisionEvaltor();

		for (int i = 0; i < 5; i++) {
			SearchResult baseline = readResult("baseline" + i);
			SearchResult result = readResult("result" + i);
			double r = evaltor.eval(baseline, result);
		}
		// System.out.println(baseline);
		// System.out.println(result);
	}

}
