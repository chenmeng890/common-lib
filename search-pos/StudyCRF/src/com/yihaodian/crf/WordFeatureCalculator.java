package com.yihaodian.crf;

import iitb.CRF.DataSequence;

public class WordFeatureCalculator implements EnumFeatureCalculator {

	private static final long serialVersionUID = 4596043013261411598L;

	@Override
	public String calc(DataSequence data, int pos) {
		return (String)data.x(pos);
	}

}
