package com.yihaodian.store.hbasestore.dataconvert;

public class LongConvert implements BaseConvert {

	/**
	 * this method is convert byte[] to String
	 * @param byte[]
	 */
	@Override
	public String convert(byte[] byteArray) {
		return String.valueOf(byteArrayToLong(byteArray));
	}

	/**
	 * Convert byte array to long
	 * @param data
	 * @return
	 */
	public long byteArrayToLong(byte[] data) {
		return byteArrayToLong(data, 0);
	}
	
	/**
	 * 
	 * @param data
	 * @param offset
	 * @return
	 */
	public long byteArrayToLong(byte[] data, int offset) {
        long r = data[offset++];
        r = (r << 8) | ((long)(data[offset++]) & 0xff);
        r = (r << 8) | ((long)(data[offset++]) & 0xff);
        r = (r << 8) | ((long)(data[offset++]) & 0xff);
        r = (r << 8) | ((long)(data[offset++]) & 0xff);
        r = (r << 8) | ((long)(data[offset++]) & 0xff);
        r = (r << 8) | ((long)(data[offset++]) & 0xff);
        r = (r << 8) | ((long)(data[offset++]) & 0xff);
        return r;
	}
}
