package com.yihaodian.search.catwordr.crf;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.regex.Pattern;

import com.yihaodian.search.catwordr.ProductTextImpl;
import com.yihaodian.search.catwordr.Recognizer;
import com.yihaodian.search.nlp.model.Dictionary;
import com.yihaodian.search.nlp.segment.Segmenter;

/**
 * 类别词的CRF识别器(每个类别独立模型. 经测试对比，效果比整个单一模型效果略差)
 */
public class RecognizerByCatNameCRF implements Recognizer, java.io.Externalizable {
	public RecognizerByCatNameCRF() {
	}

	public void init(Segmenter segmenter, Dictionary dict) {
		this.segmenter = segmenter;
		this.dict = dict;
		if (recognizers != null) {
			Collection<RecognizerCRF> orecognizers = recognizers.values();
			for (RecognizerCRF orecognizer : orecognizers) {
				orecognizer.init(segmenter, dict);
			}
		}
	}
	
	private Segmenter segmenter;
	private Dictionary dict;
	
	private HashMap<String, RecognizerCRF> recognizers;
	private Pattern[] catNamePatterns;
	private String[] catNameStrPats, recognizerKeys;
	
	public void train(String trainDataPath) {
		// 加载类别模式配置,格式 key:pattern
		ArrayList<String> lines = new ArrayList<String>();
        try {
			BufferedReader in = new BufferedReader(new FileReader(trainDataPath+"patterns.cfg"));
			String line = in.readLine();
			while(line !=null ) {
				if (line.trim().isEmpty()) continue;
				if (line.charAt(0) == '#') continue;
				lines.add(line);
				line = in.readLine();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		catNameStrPats = new String[lines.size()];
		recognizerKeys = new String[lines.size()];
		for (int i=0; i < lines.size(); i++) {
			String line = lines.get(i);
			int tn = line.indexOf(':');
			recognizerKeys[i] = line.substring(0, tn);
			catNameStrPats[i] = line.substring(tn+1);
		}
		catNamePatterns = new Pattern[catNameStrPats.length];
		for (int i=0; i < catNameStrPats.length; i++) {
			catNamePatterns[i] = Pattern.compile(catNameStrPats[i]);
		}
		
		// 逐个模型进行训练
		recognizers = new HashMap<String, RecognizerCRF>();
		for (int i=0; i < recognizerKeys.length; i++) {
			if (recognizers.containsKey(recognizerKeys[i])) continue;
			
			RecognizerCRF recognizer = new RecognizerCRF();
			recognizer.init(segmenter, dict);
			recognizer.train(trainDataPath + recognizerKeys[i] + ".csv");
			recognizers.put(recognizerKeys[i], recognizer);
		}
	}
	
	@Override
	public void recognize(ProductTextImpl p) {
		for (int i=0; i < catNamePatterns.length; i++) {
			if (catNamePatterns[i].matcher(p.getCategoryName()).matches()) {
				recognizers.get(recognizerKeys[i]).recognize(p);
				return;
			}
		}
		throw new RuntimeException("CatgoryName <"+ p.getCategoryName() +"> no matched pattern.");
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(recognizerKeys);
		out.writeObject(catNameStrPats);
		out.writeObject(recognizers);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		recognizerKeys = (String[])in.readObject();
		catNameStrPats = (String[])in.readObject();
		recognizers = (HashMap<String, RecognizerCRF>)in.readObject();

		catNamePatterns = new Pattern[catNameStrPats.length];
		for (int i=0; i < catNameStrPats.length; i++) {
			catNamePatterns[i] = Pattern.compile(catNameStrPats[i]);
		}
	}

}
