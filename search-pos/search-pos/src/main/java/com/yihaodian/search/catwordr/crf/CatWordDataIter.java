package com.yihaodian.search.catwordr.crf;

import java.util.Iterator;
import java.util.Vector;

import iitb.CRF.DataIter;
import iitb.CRF.DataSequence;

public class CatWordDataIter implements DataIter {
	public CatWordDataIter(Vector<CatWordDataSequence> datas) {
		this.datas = datas;
	}
	
	private Vector<CatWordDataSequence> datas;
	private Iterator<CatWordDataSequence> iterDatas;

	@Override
	public void startScan() {
		iterDatas = datas.iterator();
	}

	@Override
	public boolean hasNext() {
		return iterDatas.hasNext();
	}

	@Override
	public DataSequence next() {
		return iterDatas.next();
	}
}
