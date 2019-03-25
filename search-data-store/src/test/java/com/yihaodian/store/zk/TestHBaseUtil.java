package com.yihaodian.store.zk;

import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.hbase.client.Result;

import com.yihaodian.store.hbase.HBaseUtil;


public class TestHBaseUtil{
    
	
	public static void main(String[] args){
		TestHBaseUtil instance=new TestHBaseUtil();
		instance.hbaseUtilTest();
	}
	
	
	public void hbaseUtilTest(){
		HBaseUtil hbaseUtil=new HBaseUtil(HBaseUtil.quorums_dev);
		List<String> familys=new LinkedList<String>();
		familys.add("name");
		familys.add("age");
		try {
			hbaseUtil.creatTable("zfh_test", familys);
			for(int i=0;i<100;i++){
				String value=String.valueOf(new Integer(i+1));
				hbaseUtil.addRecord("zfh_test",String.valueOf(new Integer(i))  ,"name", "", value.getBytes());
			}
			
			hbaseUtil.delRecord("zfh_test", String.valueOf(new Integer(1)));
			byte[] bytes=hbaseUtil.getOneRecordOneValue("zfh_test", String.valueOf(new Integer(2)));
			System.out.println(new String(bytes));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
