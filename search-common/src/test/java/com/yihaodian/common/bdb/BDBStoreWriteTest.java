package com.yihaodian.common.bdb;

import java.io.File;

public class BDBStoreWriteTest {
	
	public static void main(String[] args) {
		String dbPath = "./var/www/data/test/bdbstore";
		String dbName = "test";
//		File dbdir = new File(dbPath);
//		if (dbdir.exists() && dbdir.isDirectory()) {
//			for (File file : dbdir.listFiles()) {
//				file.delete();
//			}
//			dbdir.delete();
//		}
//		
		BDBStore<String, String> store = new MockStore(dbPath, dbName, false, 10);		
		for(long i = 4; i < 10; ++i) {
			String str = Long.toString(i);
			store.put(str, str);
		}
		
		store.close();
	}

}
