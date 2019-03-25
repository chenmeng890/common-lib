package com.yihaodian.crf;

import iitb.CRF.DataSequence;

public class StartFeatureCalculator implements BoolFeatureCalculator {

	private static final long serialVersionUID = 4070659425614489515L;

	@Override
	public boolean calc(DataSequence data, int pos) {
		return pos == 0;
	}

}
