package com.yihaodian.search;


import java.io.File;
import java.io.IOException;


import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class TestBrandWords {

	public static void main(String[] args) throws Exception {
		String indexDir="F:/index";
		FSDirectory directory=FSDirectory.open(new File(indexDir));
		IndexReader reader=IndexReader.open(directory);
		int maxNum=reader.numDocs();
		for(int i=0;i<maxNum;i++){
			Document doc=reader.document(i);
			String brandWords=doc.get("brandWords");
			System.out.println("brandWords:"+brandWords);
			Fieldable brand=doc.getFieldable("brand_words");
			if(brand==null) continue;
			TokenStream tokenStream=brand.tokenStreamValue();
			if(tokenStream!=null)
				printTokenStream("Brand",tokenStream);
		}
		reader.close();
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
