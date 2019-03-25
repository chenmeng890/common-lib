package com.yihaodian.store.hbasestore.dataconvert;

public class StringConvert implements BaseConvert {

	/**
	 * this method is convert byte[] to String
	 * @param byte[]
	 */
	@Override
	public String convert(byte[] byteArray) {
		return String.valueOf(byteArray);
	}

}
