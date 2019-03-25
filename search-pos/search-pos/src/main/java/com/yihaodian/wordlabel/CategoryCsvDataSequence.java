package com.yihaodian.wordlabel;

import java.util.HashMap;

import iitb.CRF.DataSequence;

public class CategoryCsvDataSequence implements DataSequence {

	public CategoryCsvDataSequence(String[] headers, String[] nextLine) {
		this.headers = headers;
		this.nextLine = nextLine;
		HashMap<String, String> line = new HashMap<String, String>();
		for (int i=0; i < headers.length; i++)
			line.put(headers[i], nextLine[i]);
		if (mapHeaders == null) {
			mapHeaders = new HashMap<String, Integer>();
			for (int i=0; i < headers.length; i++)
				mapHeaders.put(headers[i], i);
		}

		allWords = line.get("分词结果").split(", ");
		wordsIndexs = new HashMap<String, Integer>();
		for (int i=0; i < allWords.length; i++) {
			allWords[i] = allWords[i].toLowerCase();
			wordsIndexs.put(allWords[i], i);
		}
		
		isBrandWords = new boolean[allWords.length];
		String[] brandWords = line.get("品牌词").split(", ");
		for (int i=0; i < brandWords.length; i++) {
			if (brandWords[i].isEmpty()) continue;
			brandWords[i] = brandWords[i].toLowerCase();
			isBrandWords[wordsIndexs.get(brandWords[i])] = true;
		}
		
		isCatWords = new boolean[allWords.length];
		String[] catWords = line.get("类别词").split(", ");
		for (int i=0; i < catWords.length; i++) {
			if (catWords[i].isEmpty()) continue;
			catWords[i] = catWords[i].toLowerCase();
			isCatWords[wordsIndexs.get(catWords[i])] = true;
		}
		
		String catSearchName = line.get("原始类别");
		int tn = catSearchName.indexOf(':');
		catSearchName = catSearchName.substring(tn+1).toLowerCase();
		String[] catSearchNames = catSearchName.split("-");
		
		isCatNameWords = new boolean[allWords.length];
		likeCatNameWords = new boolean[allWords.length];
		for (int i=0; i < allWords.length; i++) {
			for (int n=0; n < catSearchNames.length; n++) {
				if (catSearchNames[n].equals(allWords[i])) {
					isCatNameWords[i] = true;
					break;
				}
			}
			for (int n=0; n < catSearchNames.length; n++) {
				for (int m=0; m < allWords[i].length(); m++) {
					if (catSearchNames[n].indexOf(allWords[i].charAt(m)) >= 0) {
						likeCatNameWords[i] = true;
						break ;
					}
				}
				if (likeCatNameWords[i]) break;
			}
		}
		
		labels = new int[allWords.length];
		String labeledCatWord = line.get("标注类别词");
		if (labeledCatWord != null) {
			String[] labeledCatWords = labeledCatWord.split(", ");
			for (int n=0; n < labeledCatWords.length; n++) {
				String labeledWord = labeledCatWords[n].toLowerCase();
				if (! labeledWord.isEmpty()) {
					if (wordsIndexs.get(labeledWord) == null)
						System.out.println(labeledWord +" @ " +nextLine[0]);
					labels[wordsIndexs.get(labeledWord)] = LABEL_catWord;
				}
			}
		}
		
		perhapsCatWords = line.get("类别词");
	}
	
	public String[] headers, nextLine;
	private HashMap<String, Integer> mapHeaders;
	
	public String[] allWords;
	private HashMap<String, Integer> wordsIndexs;
	public boolean[] isBrandWords,isCatWords,isCatNameWords,likeCatNameWords;
	public int[] labels;
	private String perhapsCatWords;
	
	final static int LABEL_other = 0;
	public final static int LABEL_catWord = 1;
	
	public String toLine() {
		StringBuilder line = new StringBuilder();
		for (int i=0; i < allWords.length; i++) {
			if (line.length() > 0)
				line.append(", ");
			line.append(allWords[i]);
		}
		return line.toString() +"  ##  "+ nextLine[mapHeaders.get("原始类别")];
	}
	
	public void calcLabeledCatWords() {
		Integer index = mapHeaders.get("标注类别词");
		if (index == null) return;
		
		StringBuilder labeled = new StringBuilder();
		for (int i=0; i < allWords.length; i++) {
			if (labels[i] == LABEL_catWord) {
				if (labeled.length() > 0)
					labeled.append(", ");
				labeled.append(allWords[i]);
			}
		}
		nextLine[index.intValue()] = labeled.toString();
		
		if (hasNewWord()) {
			nextLine[mapHeaders.get("品牌数").intValue()] = "99999";
		}
	}
	
	private boolean hasNewWord() {
		for (int i=0; i < this.allWords.length; i++) {
			if (this.labels[i] == CategoryCsvDataSequence.LABEL_catWord) {
				if (this.allWords[i].length() == 1)
					return true;
				if (! this.isCatWords[i])
					return true;
			}
		}
		return false;
	}
		  
	@Override
	public int length() {
		return allWords.length;
	}

	@Override
	public int y(int i) {
		return labels[i];
	}

	@Override
	public Object x(int i) {
		return allWords[i];
	}

	@Override
	public void set_y(int i, int label) {
		labels[i] = label;
	}

}
