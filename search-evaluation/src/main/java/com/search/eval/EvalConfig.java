package com.search.eval;

import java.util.Properties;

import com.yihaodian.configcentre.client.utils.YccGlobalPropertyConfigurer;

/**
 * config for evaluation, such as eval_threshold, query_file, query count
 * 
 */
public class EvalConfig {

	public static String service_url_prod = "http://10.2.1.49:8080/search/mandy";
	public static String service_url_stg = "http://10.63.0.149:8080/search/mandy";
	public static String service_url_test = "http://192.168.128.194:9090/search/mandy";

	private static String[] propNames = new String[] { "mandy_config.properties" };
	private static Properties properties = new Properties();

	static {
		String env = System.getProperty("global.config.path");
		if (env == null)
			System.setProperty("global.config.path", "/var/www/webapps/config");

		String poolId = "yihaodian/mandy";
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
	}

	public static String get(String name) {
		if (name == null) {
			return null;
		}
		return properties.getProperty(name);
	}

	/**
	 * mandy url
	 * 
	 * @return
	 */
	public static String getServiceUrl() {
		return properties.getProperty("search_eval_url", service_url_prod);
	}

	/**
	 * Precision or DCG
	 * 
	 * @return
	 */
	public static String getEvaltorClass() {
		return properties.getProperty("search_eval_class_name",
				"com.search.eval.impl.PrecisionEvaltor");// PrecisionEvaltor,
															// DCGEvaltor
	}

	/**
	 * Threshold for a rank to pass, e.g precision = 0.6, then rank is passing
	 * 
	 * @return
	 */
	public static String getEvalThreshold() {
		return properties.getProperty("search_eval_threshold", "0.6");
	}

	/**
	 * query file, each line is a keyword.
	 * 
	 * @return
	 */
	public static String getEvalQuery() {
		return properties.getProperty("search_eval_query_file",
				"/var/www/data/query");
	}

	/**
	 * eval baseline file.
	 * 
	 * @return
	 */
	public static String getEvalBaseline() {
		return properties.getProperty("search_eval_baseline_file",
				"/var/www/data/baseline");
	}
	
	public static String getCateBaseLine() {
		return properties.getProperty("search_cate_baseline_file",
		       "/var/www/data/catebaseline");
	}

	/**
	 * how mandy query used in eval.
	 * 
	 * @return
	 */
	public static String getEvalQueryCount() {
		return properties.getProperty("search_eval_query_count", "50");
	}

	/**
	 * for a single query, compare the top 20 result.
	 * 
	 * @return
	 */
	public static int getEvalTop() {
		return Integer
				.parseInt(properties.getProperty("search_eval_top", "20"));
	}
}
