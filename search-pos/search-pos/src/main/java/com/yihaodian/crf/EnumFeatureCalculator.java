package com.yihaodian.crf;

import iitb.CRF.DataSequence;

public interface EnumFeatureCalculator extends FeatureCalculator {
	/**
	 * 特征值计算. 返回值必须与特征一一对应
	 * @param data
	 * @param pos
	 * @return 计算后的枚举值
	 */
  String calc(DataSequence data, int pos);
}
