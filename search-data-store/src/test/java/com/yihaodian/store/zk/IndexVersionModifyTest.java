package com.yihaodian.store.zk;

public class IndexVersionModifyTest {
	
	public static void main(String[] args) throws Exception{
		  ZKUtil zk=new ZKUtil("10.4.11.43,10.4.11.44,10.4.11.15");
		  byte[] data=zk.getData("/search/index/data-process/index.data.properties");
		  System.out.println("production: "+new String(data));
		     String dataStr="2015-04-27,14-00-06";
		     zk.changeData("/search/index/data-process/index.data.properties", dataStr.getBytes());
		     data=zk.getData("/search/staging/index/data-process/index.data.properties");
		     System.out.println("staging: "+new String(data));
		 }

}
