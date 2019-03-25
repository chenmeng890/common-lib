package com.yihaodian.search.catwordr;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import au.com.bytecode.opencsv.CSVReader;

/**
 * 产品数据(CSV格式)读取
 */
public class CsvProductsReader {
	public CsvProductsReader(String csvFile, boolean forTrain) {
		this.forTrain = forTrain;
		try {
			reader = new CSVReader(new InputStreamReader(new FileInputStream(csvFile), "GBK"));
			
			String[] headers = reader.readNext();
			if (headers == null)
				throw new RuntimeException("No csv header.");
			mapHeaders = new HashMap<String, Integer>();
			for (int i=0; i < headers.length; i++) {
				mapHeaders.put(headers[i], i);
			}
			
			String[] mustHeaders;
			if (forTrain)
				mustHeaders = new String[]{"ProductName", "BrandName", "CategoryName",
					"BrandWords", "CategoryWords"};
			else
				mustHeaders = new String[]{"ProductName", "BrandName", "CategoryName"};
			for (int i=0; i < mustHeaders.length; i++) {
				if (! mapHeaders.containsKey(mustHeaders[i]))
					throw new RuntimeException("No csv header <"+ mustHeaders[i] +">.");;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private boolean forTrain;
	private CSVReader reader;
	private HashMap<String, Integer> mapHeaders;
	
	public ProductTextImpl next() {
		String[] nextLine;
		try {
			nextLine = reader.readNext();
			if (nextLine == null) {
				return null;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		ProductTextImpl nextProductText = new ProductTextImpl();
		nextProductText.setProductName(nextLine[mapHeaders.get("ProductName")]);
		nextProductText.setBrandName(nextLine[mapHeaders.get("BrandName")]);
		nextProductText.setCategoryName(nextLine[mapHeaders.get("CategoryName")]);
		if (mapHeaders.containsKey("NameSubtitle"))
			nextProductText.setNameSubtitle(nextLine[mapHeaders.get("NameSubtitle")]);
		if (mapHeaders.containsKey("ProductCode"))
			nextProductText.setProductCode(nextLine[mapHeaders.get("ProductCode")]);
		if (forTrain) {
			nextProductText.setBrandWords(nextLine[mapHeaders.get("BrandWords")]);
			nextProductText.setCategoryWords(nextLine[mapHeaders.get("CategoryWords")]);
			if (mapHeaders.containsKey("SplitedWords"))
				nextProductText.setSplitedWords(nextLine[mapHeaders.get("SplitedWords")]);
		}
		return nextProductText;
	}
	
	public void close() {
		try {
			reader.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
