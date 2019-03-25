package com.yihaodian.store.hbasestore;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class HashHBaseObjectStoreTest {
	
     static class HashTest{
		private int id;
		private String name;
		public HashTest() {
			super();
		}
		public HashTest(int id, String name) {
			super();
			this.id = id;
			this.name = name;
		}
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		@Override
		public String toString() {
			return "HashTest [id=" + id + ", name=" + name + "]";
		}
	}
	
    static List<HashTest> tests=new ArrayList<HashTest>();
    
    static HashHBaseObjectStore<HashTest> hashHBaseObjectStore=new HashHBaseObjectStore<HashTest>(HashTest.class);
     
	static {
		for(int i=0;i<100;i++){
			tests.add(new HashTest(i,"i-"+i));
		}
	}
	
	@Test
	public void testPut() throws Exception{
		for(int i=0;i<tests.size();i++){
			hashHBaseObjectStore.put(String.valueOf(tests.get(i).getId()), tests.get(i));
		}
	}
	
	@Test
	public void testGet() throws Exception{
		System.out.println("in testGet()");
		for(int i=0;i<tests.size();i++){
			HashTest hashTest=hashHBaseObjectStore.get(String.valueOf(tests.get(i).getId()));
			System.out.println(hashTest);
		}
	}
	
	@Test
	public void testScanner() throws Exception{
		System.out.println("in testScanner()");
		HBaseScanner<HbaseObject<HashTest>> hbaseScanner=hashHBaseObjectStore.getHBaseScanner("40", "70");
		while(hbaseScanner.hasNext()){
			System.out.println(hbaseScanner.next().getData());
		}
	}

}
