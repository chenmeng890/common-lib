package com.yihaodian.search;

import com.yihaodian.search.catwordr.CsvProductsReader;
import com.yihaodian.search.catwordr.CsvProductsWriter;
import com.yihaodian.search.catwordr.ProductTextImpl;
import com.yihaodian.search.catwordr.SimpleRecognizer;

/**
 * 词性识别器 的测试代码
 */
public class TestSimpleRecognizer {
	final static String basePath = "D:/workspace/branchs/SearchPOS/search-pos/data/";

	public static void main(String[] args) {
		SimpleRecognizer recognizer = new SimpleRecognizer(basePath + "dict");
		//CsvProductsReader reader = new CsvProductsReader(basePath + "productst.csv", false);
		//CsvProductsWriter writer = new CsvProductsWriter(basePath + "productst r.csv", true);
		CsvProductsReader reader = new CsvProductsReader(basePath + "train/all.csv", false);
		CsvProductsWriter writer = new CsvProductsWriter(basePath + "train/all simple.csv", true);
		
		int n = 0;
		ProductTextImpl p = reader.next();
		while (p != null) {
			recognizer.recognize(p);
			writer.writeNext(p);
			
			n++;
			if (n % 500 == 0) System.out.println(n);
			p = reader.next();
		}
		writer.close();
	}

}
