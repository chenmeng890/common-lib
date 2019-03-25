package com.yihaodian.store.zk;

public class DumpVersionModifyTest {

	public static void main(String[] args) throws Exception{
		  ZKUtil zk=new ZKUtil("10.4.11.43,10.4.11.44,10.4.11.15");
		  byte[] data=zk.getData("/search/index/data-process/dump.version");
		  System.out.println("production: "+new String(data));
		     
		     zk.changeData("/search/staging/index/data-process/dump.version", data);
		     
		    data=zk.getData("/search/staging/index/data-process/dump.version");
		     System.out.println("staging: "+new String(data));
		     
//		     String dataStr="1420362911000\n"+"2015-01-04_17-15-11";
//		     zk.changeData("/search/staging/index/data-process/dump.version", dataStr.getBytes());
		     
//		     data=zk.getData("/search/staging/index/data-process/dump.version");
//		     System.out.println("staging: "+new String(data));

		     data=zk.getData("/search/index/data-process/2015-04-26_14-00-06.txt");
		     System.out.println("production: "+new String(data));
		     
//		     zk.createNode("/search/staging/index/data-process/2015-04-26_14-00-06.txt");
//		     zk.changeData("/search/staging/index/data-process/2015-04-26_14-00-06.txt", data);
		     
		     data=zk.getData("/search/staging/index/data-process/2015-04-26_14-00-06.txt");
		     System.out.println("staging: "+new String(data));
		 }

}
