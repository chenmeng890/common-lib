package com.yihaodian.common.email;

import org.apache.log4j.Logger;

public class AlterLevelManager {
    
	private static final Logger log = Logger.getLogger(AlterLevelManager.class);
	
	public static enum ALERT_LEVEL_EM {
		INFO,
		EXCEPTION,
		WARN,
		FATAL
	};
	
	public static boolean isNeedSend( ALERT_LEVEL_EM alterLevel, 
			ALERT_LEVEL_EM alterThreshold) {
		if (alterLevel == null || alterThreshold == null) {
			return true;
		}
		boolean result = false;
		try {
			switch(alterThreshold) {
				case INFO:
					result = true;
					break;
				case EXCEPTION:
					if (alterLevel.equals(ALERT_LEVEL_EM.EXCEPTION) ||
							alterLevel.equals(ALERT_LEVEL_EM.WARN) ||
							alterLevel.equals(ALERT_LEVEL_EM.FATAL)) {
						result = true;
					}
					break;
				case WARN:
					if (alterLevel.equals(ALERT_LEVEL_EM.WARN) ||
							alterLevel.equals(ALERT_LEVEL_EM.FATAL)) {
						result = true;
					}
					break;
				case FATAL:
					if (alterLevel.equals(ALERT_LEVEL_EM.FATAL)) {
						result = true;
					}
					break;
				default:
					log.warn("设置的报警阈值错误，请使用枚举ALERT_LEVEL_EM");
					break;			
			}	
		} catch (Exception e) {
			System.out.println(e);
			log.error("alterThreshold valus:" + alterThreshold, e);
		}
		
		return result;
	}
    
}
