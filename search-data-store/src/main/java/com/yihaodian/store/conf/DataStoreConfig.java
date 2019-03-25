package com.yihaodian.store.conf;

import org.apache.log4j.Logger;

import com.yihaodian.common.util.EnvUtil;

/**
 * 该类用来获取配置参数和环境变量
 * 
 * @author zengfenghua
 *
 */
public class DataStoreConfig {
	private static Logger log = Logger.getLogger(DataStoreConfig.class);
	private static String env = "";
	/**
	 * 获取env
	 */
	static {
		env = EnvUtil.getEnv();
		log.info("env: " + env);
	}
	
	public static String getZk_quorums(){
		return ConfigCenterUtil.get("zk_quorums");
	}
	
	public static String getFs_name(){
		return ConfigCenterUtil.get("ha_fs_name", "hdfs://Search:8020");
	}
	
	public static String getYarn_host(){
		return ConfigCenterUtil.get("yarn_host");
	}
	
	public static String getEnv(){
		return env==null?env:env.trim();
	}
	
	public static void main(String[] args){
		System.out.println(env);
	}
	
}
