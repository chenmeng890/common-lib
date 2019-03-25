package com.yihaodian.wordlabel;

import iitb.CRF.DataSequence;

import java.util.HashMap;

public class CategoryCsvSeqFactory implements CsvDataSequenceFactory {

	@Override
	public DataSequence line2DataSequence(String[] headers, String[] nextLine) {
		return new CategoryCsvDataSequence(headers, nextLine);
	}

}
