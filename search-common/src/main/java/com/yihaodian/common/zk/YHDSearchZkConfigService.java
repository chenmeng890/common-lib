package com.yihaodian.common.zk;

import java.util.Properties;

import org.apache.log4j.Logger;

import com.yihaodian.configcentre.client.utils.YccGlobalPropertyConfigurer;

/**
 * YHDSearchZkConfigService 会更新和监控 search zk Config
 * 
 * @author zengfenghua
 *
 */
public class YHDSearchZkConfigService extends ZkConfigService{
	
	private static final Logger log = Logger.getLogger(YHDSearchZkConfigService.class);
	
	private static String zk_quorums;

	private static String[] propNames = new String[] { "hadoop.properties" };
	private static Properties properties = new Properties();
	static {
		String config = System.getProperty("global.config.path");
		if (config == null) {
			System.setProperty("global.config.path", "/var/www/webapps/config");
		}
		String poolId = "yihaodian_search-data-service";
		for (int i = 0; i < propNames.length; i++) {
			String data = YccGlobalPropertyConfigurer.loadConfigString(poolId,
					propNames[i], false);
			if (data == null) {
				continue;
			}
			Properties prop = YccGlobalPropertyConfigurer
					.loadPropertiesFromString(data);
			if (prop != null) {
				properties.putAll(prop);
			}
		}
		zk_quorums=properties.getProperty("zk_quorums");
		log.info("zk_quorums:"+zk_quorums);
		instance=new YHDSearchZkConfigService();
	}

	public static YHDSearchZkConfigService instance;
	
	private YHDSearchZkConfigService() {
		super(new ZkClientService(zk_quorums));
		new ZkConfigServiceManager(this);
	}

	/**
	 * 获取配置，若无该配置，则会使用defaultData创建zk配置，并监听zk数据变化
	 */
	public String getConfigData(String configPath, String configName, String defaultData) {
		return super.getConfigData(configPath, configName, defaultData);
	}
	
	/**
	 * 尝试获取配置，无配置则直接返回null，有配置则会调用super.getConfigData(configPath, configName, defaultData)
	 * note:该方法不会在zk上创建配置；
	 * 		当zk无该配置时不会监听zk，此时每次调用都会与zk通讯，慎用
	 * @param configPath
	 * @param configName
	 * @return
	 */
	public String takeConfigData(String configPath, String configName) {
		return super.takeConfigData(configPath, configName);
	}

	public synchronized void updateZKConfig(String configPath, String configName, String data) {
		super.updateZKConfig(configPath, configName, data);
	}

	public synchronized void updateLocalConfig(String configPath, String configName, String data) {
		super.updateLocalConfig(configPath, configName, data);
	}

}
