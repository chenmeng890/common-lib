package com.yihaodian.search.catwordr.crf;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * 类别模式配置,格式 key:pattern
 */
public class ByCatNamePattern implements java.io.Externalizable {
	public ByCatNamePattern() {
	}
	
	public ByCatNamePattern(String cfgFile) {
		this.patLines = new ArrayList<String>();
        try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(cfgFile), "UTF-8"));
			String line = in.readLine();
			while(line !=null ) {
				if (! line.trim().isEmpty()) {
					if (line.charAt(0) != '#')
						patLines.add(line);
				}
				line = in.readLine();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		init();
	}
	
	private ArrayList<String> patLines;
	private void init() {
		catNameStrPats = new String[patLines.size()];
		recognizerKeys = new String[patLines.size()];
		for (int i=0; i < patLines.size(); i++) {
			String line = patLines.get(i);
			int tn = line.indexOf(':');
			recognizerKeys[i] = line.substring(0, tn);
			catNameStrPats[i] = line.substring(tn+1);
		}
		catNamePatterns = new Pattern[catNameStrPats.length];
		for (int i=0; i < catNameStrPats.length; i++) {
			catNamePatterns[i] = Pattern.compile(catNameStrPats[i]);
		}
	}
	
	private Pattern[] catNamePatterns;
	private String[] catNameStrPats, recognizerKeys;
	
	public String match(String catgoryName) {
		for (int i=0; i < catNamePatterns.length; i++) {
			if (catNamePatterns[i].matcher(catgoryName).matches()) {
				return recognizerKeys[i];
			}
		}
		return null;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(patLines);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		patLines = (ArrayList<String>)in.readObject();
		this.init();
	}
}
