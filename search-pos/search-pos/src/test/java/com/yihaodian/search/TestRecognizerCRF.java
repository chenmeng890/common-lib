package com.yihaodian.search;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.yihaodian.search.catwordr.CsvProductsReader;
import com.yihaodian.search.catwordr.CsvProductsWriter;
import com.yihaodian.search.catwordr.ProductTextImpl;
import com.yihaodian.search.catwordr.crf.ByCatNamePattern;
import com.yihaodian.search.catwordr.crf.RecognizerCRF;
import com.yihaodian.search.nlp.model.Dictionary;
import com.yihaodian.search.nlp.segment.ReverseSegmenter;
import com.yihaodian.search.nlp.segment.Segmenter;

public class TestRecognizerCRF {
	final static String basePath = "D:/workspace/branchs/SearchPOS/search-pos/data/";

	public static void main(String[] args) throws Exception {
		Dictionary dict = new Dictionary(basePath + "dict", false);
		Segmenter segmenter = new ReverseSegmenter(dict, ReverseSegmenter.TYPE_LATIN_ORIGINAL, true,true);
		ByCatNamePattern pattern = new ByCatNamePattern(basePath+"train/patterns.cfg");
		RecognizerCRF recognizer = new RecognizerCRF();
		recognizer.init(segmenter, dict, pattern);

		// 训练
		recognizer.train(basePath + "train/all.csv");
		
		// 序列化训练模型
		FileOutputStream fos = new FileOutputStream(basePath + "model/all.model");
	    ObjectOutputStream oos = new ObjectOutputStream(fos);
	    oos.writeObject(recognizer);
	    oos.close();
	    fos.close();
	    
		FileInputStream fis = new FileInputStream(basePath + "model/all.model");
	    ObjectInputStream ois = new ObjectInputStream(fis);
		RecognizerCRF recognizer2 = (RecognizerCRF)ois.readObject();
		recognizer2.init(segmenter, dict);
	    ois.close();
	    fis.close();
	    
	    //RecognizerCRF recognizer2 = recognizer;
	    
		// 测试
		CsvProductsReader reader = new CsvProductsReader(basePath + "数码家电t.csv", false);
		CsvProductsWriter writer = new CsvProductsWriter(basePath + "数码家电t_tested.csv", true);
		
		int n = 0;
		ProductTextImpl p = reader.next();
		while (p != null) {
			recognizer2.recognize(p);
			writer.writeNext(p);
			
			n++;
			if (n % 500 == 0) System.out.println(n);
			p = reader.next();
		}
		writer.close();
	}

}
