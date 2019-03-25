package com.yihaodian.wordlabel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import au.com.bytecode.opencsv.CSVReader;

import iitb.CRF.DataIter;
import iitb.CRF.DataSequence;

public class CsvDataIter implements DataIter {
	public CsvDataIter(File csvFile, CsvDataSequenceFactory factory) {
		this.csvFile = csvFile;
		this.factory = factory;
		startScan();
	}

	public CsvDataIter(String csvFile, CsvDataSequenceFactory factory) {
		this(new File(csvFile), factory);
	}
	
	private File csvFile;
	private CsvDataSequenceFactory factory;
	private CSVReader reader;
	private String[] headers, nextLine;

	@Override
	public void startScan() {
		try {
			if (reader != null)
				reader.close();
			reader = new CSVReader(new InputStreamReader(new FileInputStream(csvFile), "GBK"));
			headers = reader.readNext();
			nextLine = reader.readNext();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean hasNext() {
		return nextLine != null;
	}

	@Override
	public DataSequence next() {
		if (nextLine == null)
			return null;
		DataSequence result = factory.line2DataSequence(headers, nextLine);
		
		try {
			nextLine = reader.readNext();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

}
