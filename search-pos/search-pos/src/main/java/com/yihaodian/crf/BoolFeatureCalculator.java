package com.yihaodian.crf;

import iitb.CRF.DataSequence;

public interface BoolFeatureCalculator extends FeatureCalculator {
	/**
	 * 特征值计算. 返回是否符合该特征
	 * @param data
	 * @param pos
	 * @return 计算后的枚举值
	 */
  boolean calc(DataSequence data, int pos);
}
