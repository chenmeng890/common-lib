package com.yihaodian.search;

import com.yihaodian.search.catwordr.CsvProductsReader;
import com.yihaodian.search.catwordr.CsvProductsWriter;
import com.yihaodian.search.catwordr.ProductTextImpl;
import com.yihaodian.search.catwordr.crf.ByCatNamePattern;

public class TestByCatNamePattern {
	final static String basePath = "D:/workspace/branchs/SearchPOS/search-pos/data/";

	public static void main(String[] args) {
		ByCatNamePattern pattern = new ByCatNamePattern(basePath+"train/patterns.cfg");
		CsvProductsReader reader = new CsvProductsReader(basePath+"train/all.csv", true);
		CsvProductsWriter writer = new CsvProductsWriter(basePath+"train/all_pattern.csv", true);
		ProductTextImpl product = reader.next();
		while (product != null) {
			product.setCategoryWords(pattern.match(product.getCategoryName()));
			writer.writeNext(product);
			product = reader.next();
		}
		writer.close();
	}

}
