package com.yihaodian.common.zk.commonConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.yihaodian.common.zk.YHDSearchZkConfigService;

/**
 * mcSiteId与indexName映射关系的工具类
 * @author xuchaoqun
 *
 */
public class McSite2IndexUtil {
	private static final Logger logger = Logger.getLogger(McSite2IndexUtil.class);
	private static YHDSearchZkConfigService yHDSearchZkConfigService = YHDSearchZkConfigService.instance;
	private static final String basePath = "mcSite2IndexConfig";
	private static final String mcSite_id = "mcSiteId";//mcSiteId
	
	public enum IndexType {
		inverted_index,//倒排索引
		prod_BDB,//prodBDB
		rank_BDB,//rankBDB
		//注意：要新加成员只能在后面添加
	}
	
	/**
	 * 获取所有mcSiteId与所有index的映射关系
	 * 每次获取都是最新数据
	 * @return
	 */
	public static List<McSiteConfig> getAllMcSite2IndexConfig() {
		Set<String> nodes = yHDSearchZkConfigService.listZkConfigNodes(basePath);
		List<McSiteConfig> mcSiteConfigList = new ArrayList<McSiteConfig>();
		for (String node : nodes) {
			String mcSiteConfigStr = yHDSearchZkConfigService.getConfigData(basePath, node, null);
			McSiteConfig mcSiteConfig = toMcSiteConfig(mcSiteConfigStr);
			if (mcSiteConfig != null) {
				mcSiteConfigList.add(mcSiteConfig);
			}
		}
		return mcSiteConfigList;
	}
	
	/**
	 * 获取指定mcSiteId与所有index的映射关系
	 * 每次获取都是最新数据
	 * @param mcSiteId
	 * @return
	 */

	public static McSiteConfig getMcSite2IndexConfigByMcSiteId(int mcSiteId) {
		String mcSiteConfigStr = yHDSearchZkConfigService.getConfigData(basePath, String.valueOf(mcSiteId), null);
		McSiteConfig mcSiteConfig = toMcSiteConfig(mcSiteConfigStr);
		return mcSiteConfig;
	}
	
	/**
	 * 获取指定mcSiteId与指定index的映射关系
	 * 每次获取都是最新数据
	 * @param mcSiteId
	 * @param indexType: McSite2IndexUtil.invertedIndex / McSite2IndexUtil.prodBDB / McSite2IndexUtil.rankBDB
	 * @return
	 */
	public static String getIndexNameByMcSiteIdAndIndexType(int mcSiteId, IndexType type) {
		McSiteConfig mcSiteConfig = getMcSite2IndexConfigByMcSiteId(mcSiteId);
		if (mcSiteConfig != null) {
			String indexName = null;
			if (type == IndexType.inverted_index) {
				indexName = mcSiteConfig.getInvertedIndex();
			} else if (type == IndexType.prod_BDB) {
				indexName = mcSiteConfig.getProdBDBName();
			} else if (type == IndexType.rank_BDB) {
				indexName = mcSiteConfig.getRankBDBName();
			}
			return indexName;
		} else {
			return null;
		}
	}
	
	/**
	 * 更新一个mcSiteId与所有index的映射关系，不存在则创建
	 * @param mcSiteId
	 * @param mcSiteConfig
	 */
	public static void updateMcSite2IndexConfigByMcSiteId(int mcSiteId, McSiteConfig mcSiteConfig) {
		String mcSiteConfigStr = fromMcSiteConfigMap(mcSiteConfig);
		yHDSearchZkConfigService.updateZKConfig(basePath, String.valueOf(mcSiteId), mcSiteConfigStr);
	}
	
	/**
	 * 根据mcSiteId删除其映射关系
	 * @param mcSiteId
	 */
	public static void deleteIndexNameByMcSiteId(int mcSiteId) {
		yHDSearchZkConfigService.deleteZKConfig(basePath, String.valueOf(mcSiteId));
	}
	
	/**
	 * 根据基准表名称和mcSiteId生成hbase的表名称
	 * @param baseName
	 * @param mcSiteId
	 * @return eg.baseName="productIndexable",mcSiteId=3,return "productIndexable_sams"
	 */
	public static String getHbaseTableNameByMcSiteId(String baseName, int mcSiteId) {
		if (mcSiteId == 1) {//默认yhd站点
			return baseName;
		} else {
			String indexName = getIndexNameByMcSiteIdAndIndexType(mcSiteId, IndexType.inverted_index);
			if (indexName == null) {
				return null;
			}
			return baseName + "_" + indexName;
		}
	}
	
	private static McSiteConfig toMcSiteConfig(String mcSiteConfigStr) {
		if (mcSiteConfigStr == null) {
			return null;
		}
		try {
			String[] configsArr = mcSiteConfigStr.split(",");
			if (configsArr != null && configsArr.length > 0) {
				McSiteConfig mcSiteConfig = new McSiteConfig();
				for (String config : configsArr) {
					String[] configArr = config.split(":");
					if (configArr == null || configArr.length != 2) {
						continue;
					}
					if (StringUtils.equals(configArr[0], mcSite_id)) {
						mcSiteConfig.setMcSiteId(Integer.parseInt(configArr[1]));
					} else if (StringUtils.equals(configArr[0], IndexType.inverted_index.toString())) {
						mcSiteConfig.setInvertedIndex(StringUtils.isBlank(configArr[1]) ? null : configArr[1]);
					} else if (StringUtils.equals(configArr[0], IndexType.prod_BDB.toString())) {
						mcSiteConfig.setProdBDBName(StringUtils.isBlank(configArr[1]) ? null : configArr[1]);
					} else if (StringUtils.equals(configArr[0], IndexType.rank_BDB.toString())) {
						mcSiteConfig.setRankBDBName(StringUtils.isBlank(configArr[1]) ? null : configArr[1]);
					}
				}
				
				//校验，倒排索引名称不能为空
				if (mcSiteConfig.getMcSiteId() == -1 || mcSiteConfig.getInvertedIndex() == null) {
					return null;
				} else {
					return mcSiteConfig;
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	private static String fromMcSiteConfigMap(McSiteConfig mcSiteConfig) {
		if (mcSiteConfig == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(mcSite_id).append(":").append(mcSiteConfig.getMcSiteId()).append(",")
			.append(IndexType.inverted_index).append(":").append(mcSiteConfig.getInvertedIndex()).append(",")
			.append(IndexType.prod_BDB).append(":").append(mcSiteConfig.getProdBDBName() == null ? "" : mcSiteConfig.getProdBDBName()).append(",")
			.append(IndexType.rank_BDB).append(":").append(mcSiteConfig.getRankBDBName() == null ? "" : mcSiteConfig.getRankBDBName());
		return sb.toString();
	}
	
	public static class McSiteConfig {
		private int mcSiteId;
		
		private String invertedIndex;//倒排索引名称
		
		private String prodBDBName;//prodBDB名称
		
		private String rankBDBName;//rankBDB名称
		
		public McSiteConfig() {
			this.mcSiteId = -1;
		}
		
		public McSiteConfig(int mcSiteId, String invertedIndex, String prodBDBName, String rankBDBName) {
			this.mcSiteId = mcSiteId;
			this.invertedIndex = invertedIndex;
			this.prodBDBName = prodBDBName;
			this.rankBDBName = rankBDBName;
		}

		@Override
		public String toString() {
			return "McSiteConfig [mcSiteId=" + mcSiteId + ", invertedIndex=" + invertedIndex + ", prodBDBName=" + prodBDBName + ", rankBDBName=" + rankBDBName + "]";
		}

		public int getMcSiteId() {
			return mcSiteId;
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

		public void setMcSiteId(int mcSiteId) {
			this.mcSiteId = mcSiteId;
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
