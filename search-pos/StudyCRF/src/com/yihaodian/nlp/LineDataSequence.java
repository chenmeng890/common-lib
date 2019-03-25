package com.yihaodian.nlp;

import iitb.CRF.DataSequence;

public class LineDataSequence implements DataSequence {
	public final int POS_Begin = 0;
	public final int POS_Middle = 1;
	public final int POS_End = 2;
	
	String[] chars;
	int[] labels;
	
	public LineDataSequence(String[] words) {
		int count = 0;
		for (int i=0; i < words.length; i++)
			count += words[i].length();
		
		chars = new String[count];
		labels = new int[count];
		int index = 0;
		for (int i=0; i < words.length; i++) {
			char[] wordChars = words[i].toCharArray();
			for (int k=0; k < wordChars.length; k++) {
				chars[index] = String.valueOf(wordChars[k]);
				if (k == 0)
					labels[index] = POS_Begin;
				else if (k == wordChars.length-1)
					labels[index] = POS_End;
				else
					labels[index] = POS_Middle;
				index++;
			}
		}
	}
	
	public String toLine() {
		StringBuilder ts = new StringBuilder();
		for (int i=0; i < chars.length; i++) {
			if (labels[i] == POS_Begin) {
				if (i!=0)
					ts.append(", ");
			}
			else if (labels[i] == POS_End) {
				if (i!=chars.length-1)
					if (labels[i+1] != POS_Begin)
						ts.append("*");
			}
			else {
				if (i!=0)
					if (labels[i-1] != POS_Begin)
						ts.append("*");
				if (i!=chars.length-1)
					if (labels[i+1] != POS_End)
						ts.append("*");
			}
			ts.append(chars[i]);
		}
		return ts.toString();
	}

	@Override
	public int length() {
		return labels.length;
	}

	@Override
	public int y(int i) {
		return labels[i];
	}

	@Override
	public Object x(int i) {
		return chars[i];
	}

	@Override
	public void set_y(int i, int label) {
		labels[i] = label;
	}

}
