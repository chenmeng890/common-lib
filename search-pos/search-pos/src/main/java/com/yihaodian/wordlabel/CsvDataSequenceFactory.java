package com.yihaodian.wordlabel;

import iitb.CRF.DataSequence;

import java.util.HashMap;

public interface CsvDataSequenceFactory {
	DataSequence line2DataSequence(String[] headers, String[] nextLine);
}
