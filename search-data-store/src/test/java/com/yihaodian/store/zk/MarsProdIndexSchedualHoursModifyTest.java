package com.yihaodian.store.zk;

public class MarsProdIndexSchedualHoursModifyTest {

	public static void main(String[] args) throws Exception {
		ZKUtil zk = new ZKUtil("10.4.11.43,10.4.11.44,10.4.11.15");
		String zkPath="/search/staging/index/data-process/index.schedule.properties";
		byte[] data = zk.getData(zkPath);
		System.out.println("staging: index.schedule.properties:changeData before:" + new String(data));
		String newData="14,15\n"+"false";
		zk.changeData(zkPath, newData.getBytes());
		data = zk.getData(zkPath);
		System.out.println("staging: index.schedule.properties:changeData after:" + new String(data));
	}

}
