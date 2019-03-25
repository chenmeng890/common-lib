package com.yihaodian.store.zk;

import java.io.IOException;
import java.util.Date;

import org.apache.zookeeper.KeeperException;
import org.junit.Test;

import com.yihaodian.store.zk.ZKUtil;
import junit.framework.TestCase;

public class TableVersionModifyTest extends TestCase {

	@Test
	public void testConnect() throws Exception {
//		ZKUtil zk  = null;
//		try {
//			zk = new ZKUtil(ZKUtil.quorums_dev);
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		String path="/test/mandy";
//		while(true){
//			System.out.println("1"+zk.getZkState());
//			
//			//Thread.sleep(40 * 1000);
//			
//			Thread.sleep(5 * 1000);
//			System.out.println("2"+zk.getZkState());
//			
//			if(!zk.exist(path))
//			    zk.createNode(path, true);
//			
//			//Thread.sleep(50 * 1000);
//			
//			System.out.println("3"+zk.getZkState());
//			
//			zk.getChilds(path);
//			
//			//Thread.sleep(50 * 1000);
//			
//			System.out.println("4"+zk.getZkState());
//			
//			zk.changeData(path, "1".getBytes());
//		}
		
	}
	
	public static void main(String[] args) throws Exception{
		 ZKUtil zk=new ZKUtil("10.4.11.43,10.4.11.44,10.4.11.15");
		 
		 String version="search_pm_info,1419918592000\n"+
                      "search_product_today_price,1419918592000\n"+
                      "search_product,1419918592000\n"+
                      "search_product_attribute_value,1419918592000\n"+
                      "search_union_site_area,1419918592000\n"+
                      "search_product_sub_category,1419918592000\n"+
                      "search_similar_product,1419918592000"+"\n"+
                      "search_big_promotion_tags,1419918592000"+"\n"+
                      "search_serial_product,1419918592000"+"\n"+
                      "search_pm_area,1419918592000"+"\n"+
                      "search_product_today_num,1419998300000"+"\n"+
                      "search_b2b_pm_info,1419918592000"+"\n"+
                      "search_rank_track_info,1419918592000"+"\n"+
                      "search_product_pic,1419918592000"+"\n"+
                      "pm_info_merchant_category,1419918592000"+"\n"+
                      "search_combine_product,1419918592000"+"\n"+
                      "search_pro_sales_infos,1419918592000"+"\n"+
                      "search_product_total_num,1419998300000"+"\n"+
                      "search_combine_related_product,1419918592000"+"\n"+
                      "search_product_attribute_item,1419918592000";
         System.out.println(version);
		 byte[] data=zk.getData("/search/staging/index/data-process/2014-12-31_11-58-20.txt");
	     System.out.println("staging: "+new String(data));
	     System.out.println("++++++++++++++++++++++++++++++++++=");
	     zk.changeData("/search/staging/index/data-process/2014-12-31_11-58-20.txt", version.getBytes());
	     
	     data=zk.getData("/search/staging/index/data-process/2014-12-31_11-58-20.txt");
	     System.out.println("staging: "+new String(data));
	}

}
