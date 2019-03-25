package com.yihaodian.search;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class SearchWordsProcess {
	private SqlMapClientTemplate sqlmct,sqlmctProduct;
	private Analyzer analyzer;
	private Set<String> categoryWords,brandWords,otherWords;
	
	private void export_products(String dataPath) throws IOException {
    	List list = sqlmctProduct.queryForList("listProducts");
    	
		File outFile = new File(dataPath, "products.csv");
		CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(outFile), "GBK"));
	  	writer.writeNext(new String[]{"CAT_NAME", "CN_NAME", "BRAND_NAME", "CODE"});
	  	
		int n = 0;
    	for (Object oitem : list) {
    		Map item = (Map)oitem;
			String catName = (String)item.get("CAT_NAME");
			String prdName = (String)item.get("CN_NAME");
			String brandName = (String)item.get("BRAND_NAME");
			String prdCode = (String)item.get("CODE");
			
		  	writer.writeNext(new String[]{catName, prdName, brandName, prdCode});
		  	
		  	n++;
		  	if (n % 100 == 0) System.out.println(n);
    	}
		writer.close();
	}
	
	private void csv_wordmark(String dataPath) throws IOException {
		File productsFile = new File(dataPath, "products.csv");
		CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(productsFile), "GBK"));
		String[] headers = reader.readNext();
		if (! "CN_NAME".equals(headers[1]))
			throw new RuntimeException("products.csv 文件格式不对.");

		File outFile = new File(dataPath, "wordmark.csv");
		CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(outFile), "GBK"));
	  	writer.writeNext(new String[]{"商品名", "分词结果", "品牌词", "品牌数", "类别词", "类别数", "原始类别", "原始品牌", "商品CODE"});
		
		String[] line = reader.readNext();
		int n = 0;
		while (line != null) {
			String catName = line[0];
			String prdName = line[1];
			String brandName = line[2];
			String prdCode = null;
			if (line.length > 3)
				prdCode = line[3];
			
			try {
				List<String> listSplited = split(prdName);
				
				List<String> listBrands = new ArrayList<String>();
				List<String> listCats = new ArrayList<String>();
				int brackets = 0;
				for (String token : listSplited) {
					if (token.equals("(") || token.equals("（"))
						brackets++;
					else if (token.equals(")") || token.equals("）"))
						brackets--;
					if (brackets > 0) continue;
					
					token = token.toLowerCase();
					//if (token.length() > 1) {
						if (brandWords.contains(token))
							listBrands.add(token);
						if (categoryWords.contains(token))
							listCats.add(token);
					//}
				}
				
				// 品牌 仅留第一个
				while (listBrands.size() > 1) {
					listBrands.remove(listBrands.size()-1);
				}
				
			  	writer.writeNext(new String[]{prdName, list2string(listSplited)
			  			,list2string(listBrands), Integer.toString(listBrands.size())
			  			,list2string(listCats), Integer.toString(listCats.size())
			  			,catName,brandName,prdCode});
			} catch (Exception e) {
				System.err.println(prdName);
			}
		  	
		  	line = reader.readNext();
		  	n++;
		  	if (n % 100 == 0) System.out.println(n);
		}
		
		writer.close();
		reader.close();
	}
	
	private String list2string(List<String> list) {
		String ts = list.toString();
		return ts.substring(1, ts.length()-1);
	}

	private void calcSearchWordType() {
    	List list = sqlmct.queryForList("listSearchWords");
    	for (Object oitem : list) {
    		Map item = (Map)oitem;
    		String keyword = (String)item.get("KEY_WORD");
    		Long id = (Long)item.get("ID");
    		
    		List<String> strSplited = split(keyword);
    		if (strSplited.size() == 1) {
    			String isCatBrand = null;
    			if (categoryWords.contains(strSplited.get(0)))
    				isCatBrand = "Cat";
    			else if (brandWords.contains(strSplited.get(0)))
    				isCatBrand = "Brand";
    			else if (otherWords.contains(strSplited.get(0)))
    				isCatBrand = "Other";
    			else
    				isCatBrand = "Unkown";
    			if (isCatBrand != null) {
    		    	Map map = new HashMap();
    		    	map.put("ID", id);
    		    	map.put("isCatBrand", isCatBrand);
    				sqlmct.update("updateSearchWord", map);
    			}
    		}
    		else if (strSplited.size() == 2) {
    			String isCatBrand0,isCatBrand1;
    			if (brandWords.contains(strSplited.get(0)))
    				isCatBrand0 = "Brand";
    			else if (categoryWords.contains(strSplited.get(0)))
    				isCatBrand0 = "Cat";
    			else if (otherWords.contains(strSplited.get(0)))
    				isCatBrand0 = "Other";
    			else
    				isCatBrand0 = "Unkown";
    			
    			if (categoryWords.contains(strSplited.get(1)))
    				isCatBrand1 = "Cat";
    			else if (brandWords.contains(strSplited.get(1)))
    				isCatBrand1 = "Brand";
    			else if (otherWords.contains(strSplited.get(1)))
    				isCatBrand1 = "Other";
    			else
    				isCatBrand1 = "Unkown";

		    	Map map = new HashMap();
		    	map.put("ID", id);
		    	map.put("isCatBrand", isCatBrand0+isCatBrand1);
		    	map.put("splited", strSplited.get(0)+","+strSplited.get(1));
				sqlmct.update("updateSearchWord", map);
				
	    		System.out.println(keyword + "    " + id + "    " + isCatBrand0+isCatBrand1);
    		}
    	}
	}
	
	private List<String> split(String line) {
		try {
			TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(line));
			List<String> strSplited = new ArrayList<String>();
//			Token token = tokenStream.next();
			CharTermAttribute termAtt = (CharTermAttribute) tokenStream
	          .getAttribute(CharTermAttribute.class);
			while (tokenStream.incrementToken()) {
				strSplited.add(new String(termAtt.buffer(),0,termAtt.length()));
//				token = tokenStream.next();
			}
			return strSplited;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public SearchWordsProcess() throws Exception {
		ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
		sqlmct = (SqlMapClientTemplate)ac.getBean("sqlmct-wordlib");
		sqlmctProduct = (SqlMapClientTemplate)ac.getBean("sqlmct-product");
		analyzer = AnalyzerTest.newReverseAnalyzer();
		
		categoryWords = loadWords("listCategoryWords");
		brandWords = loadWords("listBrandWords");
		otherWords = loadWords("listOtherWords");
	}
	
	private Set<String> loadWords(String statementName) {
		Set<String> result = new HashSet<String>();
    	List list = sqlmct.queryForList(statementName);
    	for (Object oitem : list) {
    		String item = (String)oitem;
    		result.add(item.toLowerCase());
    	}
    	return result;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		SearchWordsProcess process = new SearchWordsProcess();
		//process.calcSearchWordType();
		//process.export_products("D:/workspace/study/search-analyzer/data");
		process.csv_wordmark("D:/workspace/study/search-analyzer/data");
	}

}
