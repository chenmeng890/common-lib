package com.yihaodian.search.util;

import java.io.File;

public class UtilFuction {
	
	/**
	 * 创建目录
	 * @param path
	 * @return
	 */
	public static boolean createDirectory(String path){
		boolean mk = false;
		File file = new File(path);
		if(!file.exists()){
			mk = file.mkdirs();
		}
		return mk;
	}
}
