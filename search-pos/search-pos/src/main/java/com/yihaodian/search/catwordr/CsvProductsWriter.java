package com.yihaodian.search.catwordr;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * 产品数据及识别结果(CSV格式)输出
 */
public class CsvProductsWriter {
	public CsvProductsWriter(String csvFile, boolean forTrain) {
		this.forTrain = forTrain;
		try {
			writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(csvFile), "GBK"));
			String[] headers;
			if (forTrain)
				headers = new String[]{"ProductCode", "ProductName", "BrandName", "CategoryName", "NameSubtitle", 
					"BrandWords", "CategoryWords", "SplitedWords"};
			else
				headers = new String[]{"ProductCode", "ProductName", "BrandName", "CategoryName", "NameSubtitle"};
			writer.writeNext(headers);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private CSVWriter writer;
	private boolean forTrain;
	
	public void writeNext(ProductTextImpl p) {
		String[] nextLine;
		if (forTrain)
			nextLine = new String[]{p.getProductCode(), p.getProductName(), p.getBrandName(), p.getCategoryName(), p.getNameSubtitle(), 
				p.getBrandWords(), p.getCategoryWords(), p.getSplitedWords()};
		else
			nextLine = new String[]{p.getProductCode(), p.getProductName(), p.getBrandName(), p.getCategoryName(), p.getNameSubtitle() };
		writer.writeNext(nextLine);
	}
	
	public void close() {
		try {
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
