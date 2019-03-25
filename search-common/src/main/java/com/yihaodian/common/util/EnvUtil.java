package com.yihaodian.common.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class EnvUtil {
	public static Logger log = Logger.getLogger(EnvUtil.class);
	public static String env;
	
	private static String ENV_PROD = "production";
	private static String ENV_STG = "staging";
	private static String ENV_TEST = "test";
	private static String ENV_PERF = "perf";
	
	/**
	 * 获取当前环境
	 * 线上：ENV_PROD
	 * stg：ENV_STG
	 * 性能：ENV_PERF
	 * 其他：ENV_TEST
	 * 
	 * @return
	 */
	public static String getEnv(){
		if (env != null) {
			return env;
		}
		
		String configDir = System.getProperty("global.config.path");
		if (configDir == null) {
			configDir= "/var/www/webapps/config";
		}
		
		String envFile = configDir + "/env.ini";
		
		String config = null;
		try {
			config = FileUtils.readFileContent(envFile);
		} catch(Exception e){
			log.error("Load env.ini error! Path: " + envFile, e);
		}
		
		if (config == null) {
			return null;
		} else {
			Properties prop = new Properties();
			InputStream is = new ByteArrayInputStream(config.getBytes());
			try{
				prop.load(is);
				if (is != null) {
					is.close();
				}
			} catch(Exception e){
				log.error("Load config error!", e);
			}
			
			String env = prop.getProperty("env").trim();
			if (ENV_PROD.equals(env) || ENV_STG.equals(env) || ENV_PERF.endsWith(env)) {
				return env;
			} else {
				return ENV_TEST;
			}
		}
	}
	
	public static void main(String[] args) {
		System.out.println(getEnv());
	}
	
}
