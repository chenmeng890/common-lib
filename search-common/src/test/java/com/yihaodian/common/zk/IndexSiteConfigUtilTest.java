package com.yihaodian.common.zk;

import org.junit.Assert;
import org.junit.Test;

import com.yihaodian.common.zk.commonConfig.SearchIndexSiteUtil;
import com.yihaodian.common.zk.commonConfig.SearchIndexSiteUtil.IndexSiteConfig;
import com.yihaodian.common.zk.commonConfig.SearchIndexSiteUtil.IndexType;

public class IndexSiteConfigUtilTest {

	@Test
	public void test() {
		int testMcSiteId = Integer.MAX_VALUE;
		String testInvertedIndex = "testInvertedIndex";
		String testProdBDB = "testProdBDB";
		String testRankBDB = "testRankBDB";
		try {
			IndexSiteConfig mcSiteConfig = new IndexSiteConfig();
			mcSiteConfig.setIndexSiteId(testMcSiteId);
			mcSiteConfig.setInvertedIndex(testInvertedIndex);
			mcSiteConfig.setProdBDBName(testProdBDB);
			mcSiteConfig.setRankBDBName(testRankBDB);
			SearchIndexSiteUtil.updateIndexSiteConfigByIndexSiteId(testMcSiteId, mcSiteConfig);
			Thread.sleep(1000);
			Assert.assertEquals(testInvertedIndex, SearchIndexSiteUtil.getIndexNameByIndexSiteIdAndIndexType(testMcSiteId, IndexType.inverted_index));
			Thread.sleep(1000);
			Assert.assertEquals(testProdBDB, SearchIndexSiteUtil.getIndexNameByIndexSiteIdAndIndexType(testMcSiteId, IndexType.prod_BDB));
			Thread.sleep(1000);
			Assert.assertEquals(testRankBDB, SearchIndexSiteUtil.getIndexNameByIndexSiteIdAndIndexType(testMcSiteId, IndexType.rank_BDB));
			Thread.sleep(1000);
			System.out.println(SearchIndexSiteUtil.getAllIndexSiteConfig());
			Thread.sleep(1000);
			System.out.println(SearchIndexSiteUtil.getIndexSiteConfigByIndexSiteId(testMcSiteId));
			Thread.sleep(1000);
			System.out.println(SearchIndexSiteUtil.getIndexNameByIndexSiteIdAndIndexType(testMcSiteId, IndexType.inverted_index));
			Thread.sleep(1000);
			System.out.println(SearchIndexSiteUtil.getIndexNameByIndexSiteIdAndIndexType(testMcSiteId, IndexType.rank_BDB));
			Thread.sleep(1000);
			SearchIndexSiteUtil.deleteIndexSiteConfigByIndexSiteId(testMcSiteId);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		System.out.println(SearchIndexSiteUtil.getIndexNameByIndexSiteIdAndIndexType(4, IndexType.prod_BDB));
	}

}
