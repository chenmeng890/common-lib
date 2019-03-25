package com.yihaodian.store.conf;

import java.util.Properties;

import com.yihaodian.configcentre.client.utils.YccGlobalPropertyConfigurer;

/**
 * 
 * @author zengfenghua
 *
 */
public class ConfigCenterUtil {
	
	private static String[] propNames = new String[] {
			"hadoop.properties"};
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
			if (data == null){
				continue;
			}
			Properties prop = YccGlobalPropertyConfigurer
					.loadPropertiesFromString(data);
			if (prop != null) {
				properties.putAll(prop);
			}
		}
	}
	
	public static String get(String name) {
		if (name == null){
			return null;
		}
		return properties.getProperty(name);
	}

	public static String get(String name, String def) {
		String val = get(name);
		return val != null ? val : def;
	}

	public static int getInt(String name) {
		String val = get(name);
		return val == null ? 0 : Integer.parseInt(val);
	}

	public static int getInt(String name, int def) {
		String val = get(name);
		return val != null ? Integer.parseInt(val) : def;
	}
   
	public static long getLong(String name) {
		String val = get(name);
		return val == null ? 0L : Long.parseLong(val);
	}
	
	public static long getLong(String name, long def) {
		String val = get(name);
		return val != null ? Long.parseLong(val) : def;
	}
	
	public static boolean getBool(String name) {
		String val = get(name);
		return val == null ? false : Boolean.parseBoolean(val);
	}

	public static boolean getBool(String name, boolean def) {
		String val = get(name);
		return val != null ? Boolean.parseBoolean(val) : def;
	}

	public static float getFloat(String name) {
		String val = get(name);
		return val == null ? 0 : Float.parseFloat(val) ;
	}

	public static float getFloat(String name, float def) {
		String val = get(name);
		return val != null ? Float.parseFloat(val) : def;
	}

	public static double getDouble(String name) {
		String val = get(name);
		return val == null ? 0 : Double.parseDouble(val);
	}

	public static double getDouble(String name, double def) {
		String val = get(name);
		return val != null ? Double.parseDouble(val) : def;
	}

}
