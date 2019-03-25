package com.yihaodian.crf;

import iitb.CRF.DataSequence;

public class SimpleEdgeFeatureCalculator implements EdgeFeatureCalculator,BoolFeatureCalculator {

	private static final long serialVersionUID = 106375552680194718L;

	@Override
	public boolean calc(DataSequence data, int pos) {
		return pos > 0;
	}
	
}
