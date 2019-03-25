package com.yihaodian.search;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import com.yihaodian.search.catwordr.CsvProductsReader;
import com.yihaodian.search.catwordr.ProductTextImpl;
import com.yihaodian.search.nlp.ProductTextAnalyzerImpl;

public class TestProductTextAnalyzerImpl {
	final static String basePath = "D:/linux_fs/var/www/data/";


	public static void main1(String[] args) throws Exception {
		ProductTextAnalyzerImpl analyzer = new ProductTextAnalyzerImpl();
		analyzer.setDictPath(basePath + "dict");
		analyzer.setTrainModelFile(basePath + "model/all.model");
		
		// 测试
		CsvProductsReader reader = new CsvProductsReader(basePath + "食品饮料.csv", false);
		
		int n = 0;
		ProductTextImpl p = reader.next();
		while (p != null) {
			analyzer.analyze(p, true);
			System.out.println("*** " + p.getProductCode() + " *** " + p.getProductName());
			System.out.println("  BrandWords: " + analyzer.getBrandWords());
			System.out.println("  CategoryWords: " + analyzer.getCategoryWords());
			printTokenStream("Brand", analyzer.getBrandTokens());
			printTokenStream("Category", analyzer.getCategoryTokens());
			printTokenStream("Word", analyzer.getWordTokens());
			printTokenStream("WordEx", analyzer.getWordExTokens());
			
			n++;
			if (n % 500 == 0) System.out.println(n);
			p = reader.next();
		}
	}
	
	public static void main(String[] args) throws Exception{
		ProductTextAnalyzerImpl analyzer = new ProductTextAnalyzerImpl();
		analyzer.setDictPath("D:/linux_fs/var/www/data/dict");
		analyzer.setTrainModelFile(basePath + "model/all.model");
		
		ProductTextImpl pt=new ProductTextImpl();
		pt.setProductName("纽倍乐排毒养颜套餐 ");
		pt.setProductCode("0011555854");
		pt.setBrandName("纽倍乐");
		pt.setCategoryName("0-5009-5064-5066:营养保健、健康器械/调节肠胃 /芦荟");
		pt.setNameSubtitle(" ");
		pt.setProductTag(" ");
		analyzer.analyze(pt, true);
		System.out.println("*** " + pt.getProductCode() + " *** " + pt.getProductName());
		System.out.println("  BrandWords: " + analyzer.getBrandWords());
		System.out.println("  CategoryWords: " + analyzer.getCategoryWords());
		printTokenStream("Brand", analyzer.getBrandTokens());
		printTokenStream("Category", analyzer.getCategoryTokens());
		printTokenStream("Word", analyzer.getWordTokens());
		printTokenStream("WordEx", analyzer.getWordExTokens());
		
		pt.setProductName("500元实体1号卡 ");
		pt.setProductCode("0000103297");
		pt.setBrandName(null);
		pt.setCategoryName("0-5009-5064-5066:服装鞋帽-女装-女装T恤-女装无袖T恤");
		pt.setNameSubtitle(" ");
		pt.setProductTag("");
		analyzer.analyze(pt, true);
		System.out.println("*** " + pt.getProductCode() + " *** " + pt.getProductName());
		System.out.println("  BrandWords: " + analyzer.getBrandWords());
		System.out.println("  CategoryWords: " + analyzer.getCategoryWords());
		printTokenStream("Brand", analyzer.getBrandTokens());
		printTokenStream("Category", analyzer.getCategoryTokens());
		printTokenStream("Word", analyzer.getWordTokens());
		printTokenStream("WordEx", analyzer.getWordExTokens());
		
	}
	private static void printTokenStream(String fieldName, TokenStream tokenStream) throws Exception {
		System.out.print("  ");
		System.out.print(fieldName);
		System.out.print(": ");
		
		int i = 0;
		CharTermAttribute term = tokenStream.getAttribute(CharTermAttribute.class);
		while (tokenStream.incrementToken()) {
			if (i > 0)
				System.out.print(',');
			System.out.print(new String(term.buffer(), 0, term.length()));
			
			i++;
		}
		System.out.println();
	}

}
