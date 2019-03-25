package com.yihaodian.common.zk.commonConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.yihaodian.common.zk.YHDSearchZkConfigService;

/**
 * indexSiteId与indexName映射关系的工具类
 * @author xuchaoqun
 *
 */
public class SearchIndexSiteUtil {
	private static final Logger logger = Logger.getLogger(SearchIndexSiteUtil.class);
	private static YHDSearchZkConfigService yHDSearchZkConfigService = YHDSearchZkConfigService.instance;
	private static final String basePath = "indexSiteConfig";
	private static final String indexSiteId = "indexSiteId";//indexSiteId
	
	public enum IndexType {
		inverted_index,//倒排索引
		prod_BDB,//prodBDB
		rank_BDB,//rankBDB
		//注意：要新加成员只能在后面添加
	}
	
	/**
	 * 获取所有indexSiteId与所有index的映射关系
	 * 每次获取都是最新数据
	 * @return
	 */
	public static List<IndexSiteConfig> getAllIndexSiteConfig() {
		Set<String> nodes = yHDSearchZkConfigService.listZkConfigNodes(basePath);
		List<IndexSiteConfig> indexSiteConfigList = new ArrayList<IndexSiteConfig>();
		for (String node : nodes) {
			String indexSiteConfigStr = yHDSearchZkConfigService.takeConfigData(basePath, node);
			IndexSiteConfig indexSiteConfig = toIndexSiteConfig(indexSiteConfigStr);
			if (indexSiteConfig != null) {
				indexSiteConfigList.add(indexSiteConfig);
			}
		}
		return indexSiteConfigList;
	}
	
	/**
	 * 获取指定indexSiteId与所有index的映射关系
	 * 每次获取都是最新数据
	 * @param indexSiteId
	 * @return
	 */

	public static IndexSiteConfig getIndexSiteConfigByIndexSiteId(int indexSiteId) {
		String indexSiteConfigStr = yHDSearchZkConfigService.takeConfigData(basePath, String.valueOf(indexSiteId));
		IndexSiteConfig indexSiteConfig = toIndexSiteConfig(indexSiteConfigStr);
		return indexSiteConfig;
	}
	
	/**
	 * 获取指定indexSiteId与指定index的映射关系
	 * 每次获取都是最新数据
	 * @param indexSiteId
	 * @param indexType: IndexType.inverted_index / IndexType.prod_BDB / IndexType.rank_BDB
	 * @return
	 */
	public static String getIndexNameByIndexSiteIdAndIndexType(int indexSiteId, IndexType type) {
		IndexSiteConfig indexSiteConfig = getIndexSiteConfigByIndexSiteId(indexSiteId);
		if (indexSiteConfig != null) {
			String indexName = null;
			if (type == IndexType.inverted_index) {
				indexName = indexSiteConfig.getInvertedIndex();
			} else if (type == IndexType.prod_BDB) {
				indexName = indexSiteConfig.getProdBDBName();
			} else if (type == IndexType.rank_BDB) {
				indexName = indexSiteConfig.getRankBDBName();
			}
			return indexName;
		} else {
			return null;
		}
	}
	
	/**
	 * 更新一个indexSiteId与所有index的映射关系，不存在则创建
	 * @param indexSiteId
	 * @param indexSiteConfig
	 */
	public static void updateIndexSiteConfigByIndexSiteId(int indexSiteId, IndexSiteConfig indexSiteConfig) {
		String indexSiteConfigStr = fromIndexSiteConfigMap(indexSiteConfig);
		yHDSearchZkConfigService.updateZKConfig(basePath, String.valueOf(indexSiteId), indexSiteConfigStr);
	}
	
	/**
	 * 根据indexSiteId删除其映射关系
	 * @param indexSiteId
	 */
	public static void deleteIndexSiteConfigByIndexSiteId(int indexSiteId) {
		yHDSearchZkConfigService.deleteZKConfig(basePath, String.valueOf(indexSiteId));
	}
	
	/**
	 * 根据基准表名称和indexSiteId生成hbase的表名称
	 * @param baseName
	 * @param indexSiteId
	 * @return eg.baseName="productIndexable",indexSiteId=3,return "productIndexable_sams"
	 */
	public static String getHbaseTableNameByIndexSiteId(String baseName, int indexSiteId) {
		if (indexSiteId == 0 || indexSiteId == 1) {//默认yhd站点
			return baseName;
		} else {
			String indexName = getIndexNameByIndexSiteIdAndIndexType(indexSiteId, IndexType.inverted_index);
			if (indexName == null) {
				return null;
			}
			return baseName + "_" + indexName;
		}
	}
	
	private static IndexSiteConfig toIndexSiteConfig(String indexSiteConfigStr) {
		if (indexSiteConfigStr == null) {
			return null;
		}
		try {
			String[] configsArr = indexSiteConfigStr.split(",");
			if (configsArr != null && configsArr.length > 0) {
				IndexSiteConfig indexSiteConfig = new IndexSiteConfig();
				for (String config : configsArr) {
					String[] configArr = config.split(":");
					if (configArr == null || configArr.length != 2) {
						continue;
					}
					if (StringUtils.equals(configArr[0], indexSiteId)) {
						indexSiteConfig.setIndexSiteId(Integer.parseInt(configArr[1]));
					} else if (StringUtils.equals(configArr[0], IndexType.inverted_index.toString())) {
						indexSiteConfig.setInvertedIndex(StringUtils.isBlank(configArr[1]) ? null : configArr[1]);
					} else if (StringUtils.equals(configArr[0], IndexType.prod_BDB.toString())) {
						indexSiteConfig.setProdBDBName(StringUtils.isBlank(configArr[1]) ? null : configArr[1]);
					} else if (StringUtils.equals(configArr[0], IndexType.rank_BDB.toString())) {
						indexSiteConfig.setRankBDBName(StringUtils.isBlank(configArr[1]) ? null : configArr[1]);
					}
				}
				
				//校验，倒排索引名称不能为空
				if (indexSiteConfig.getIndexSiteId() == -1 || indexSiteConfig.getInvertedIndex() == null) {
					return null;
				} else {
					return indexSiteConfig;
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	private static String fromIndexSiteConfigMap(IndexSiteConfig indexSiteConfig) {
		if (indexSiteConfig == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(indexSiteId).append(":").append(indexSiteConfig.getIndexSiteId()).append(",")
			.append(IndexType.inverted_index).append(":").append(indexSiteConfig.getInvertedIndex()).append(",")
			.append(IndexType.prod_BDB).append(":").append(indexSiteConfig.getProdBDBName() == null ? "" : indexSiteConfig.getProdBDBName()).append(",")
			.append(IndexType.rank_BDB).append(":").append(indexSiteConfig.getRankBDBName() == null ? "" : indexSiteConfig.getRankBDBName());
		return sb.toString();
	}
	
	public static class IndexSiteConfig {
		private int indexSiteId;
		
		private String invertedIndex;//倒排索引名称
		
		private String prodBDBName;//prodBDB名称
		
		private String rankBDBName;//rankBDB名称
		
		public IndexSiteConfig() {
			this.indexSiteId = -1;
		}
		
		public IndexSiteConfig(int indexSiteId, String invertedIndex, String prodBDBName, String rankBDBName) {
			this.indexSiteId = indexSiteId;
			this.invertedIndex = invertedIndex;
			this.prodBDBName = prodBDBName;
			this.rankBDBName = rankBDBName;
		}

		@Override
		public String toString() {
			return "IndexSiteConfig [indexSiteId=" + indexSiteId + ", invertedIndex=" + invertedIndex + ", prodBDBName=" + prodBDBName + ", rankBDBName=" + rankBDBName + "]";
		}

		public int getIndexSiteId() {
			return indexSiteId;
		}

		public String getInvertedIndex() {
			return invertedIndex;
		}

		public String getProdBDBName() {
			return prodBDBName;
		}

		public String getRankBDBName() {
			return rankBDBName;
		}

		public void setIndexSiteId(int indexSiteId) {
			this.indexSiteId = indexSiteId;
		}

		public void setInvertedIndex(String invertedIndex) {
			this.invertedIndex = invertedIndex;
		}

		public void setProdBDBName(String prodBDBName) {
			this.prodBDBName = prodBDBName;
		}

		public void setRankBDBName(String rankBDBName) {
			this.rankBDBName = rankBDBName;
		}
	}
}
