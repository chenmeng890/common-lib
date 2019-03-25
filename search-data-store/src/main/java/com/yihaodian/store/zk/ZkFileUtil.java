package com.yihaodian.store.zk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.yihaodian.common.util.NetUtil;
import com.yihaodian.store.conf.DataStoreConfig;

public class ZkFileUtil {

	private static Logger log = Logger.getLogger(ZkFileUtil.class);

	private String _zk_quorums = "yhd-hadoop01.int.yihaodian.com,"
			+ "yhd-hadoop02.int.yihaodian.com,yhd-hadoop03.int.yihaodian.com";

	private String _env = "";

	private String prefix = "/search";

	private ZKUtil zk;

	private static ZkFileUtil instance = new ZkFileUtil();

	public static ZkFileUtil getInstance() {
		return instance;
	}

	private ZkFileUtil() {
		this._zk_quorums=DataStoreConfig.getZk_quorums();

		String env=DataStoreConfig.getEnv();
		if (!(env.trim()).equals("production")) {
			_env = env;
		}

		// double check env by ip address
		String ip= NetUtil.getLocalIP();
		if(ip.startsWith("10.63.")){
			_env = "staging";
		}
		
		if (StringUtils.isEmpty(_env)) {
			prefix = "/search/";
		} else {
			prefix = "/search/" + _env + "/";
		}

		log.info("env: " + _env + ", prefix: " + prefix);

		try {
			zk = new ZKUtil(_zk_quorums);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} 

	}

	private String getZkFilePath(String path) {
		String trimPath = path;
		if (trimPath.startsWith("/")) {
			trimPath = trimPath.substring(1);
		}

		return prefix + trimPath;
	}

	/**
	 * Sync file content from local to remote zk
	 * @param local
	 * @param remote
	 */
	public void syncToZk(String local, String remote) {
		File file = new File(local);
		if (file.exists()) {
			try {
				List<String> data = FileUtils.readLines(file);
				if(data != null && !data.isEmpty()) {
					save(remote, data);
				}
			}catch (IOException e) {
				log.error("Failed to sync to zk " + local);
			}
		}		
	}
	
	/**
	 * Sync file content from remote zk to local file
	 * @param local
	 * @param remote
	 */
	public void syncFromZk(String local, String remote) {
		try {
			List<String> data = load(remote);
			if(data != null && !data.isEmpty()) {
				File file = new File(local);
				if (file.exists()) {
					FileUtils.deleteQuietly(file);
				}				
				file = new File(local);
				FileUtils.writeLines(file, data);				
			}
		}catch (IOException e) {
			log.error("Failed to sync to zk " + local);
		}
	}
	
	/**
	 * Save data to zk
	 * @param path
	 * @param data
	 */
	public void save(String path, List<String> data) {
		try {
			String zkPath = getZkFilePath(path);
			if (!zk.exist(zkPath)) {
				createNode(zkPath);
			}
			zk.changeData(zkPath, toZkData(data));
		} catch (Exception e) {
			log.error("Failed to save data, path " + path + ", data " + data, e);
		}
	}

	/**
	 * Read data from zk
	 * @param path
	 * @return
	 */
	public List<String> load(String path) {
		List<String> ret = new ArrayList<String>();

		try {
			String zkPath = getZkFilePath(path);
			if (zk.exist(zkPath)) {
				byte[] data = zk.getData(zkPath);
				ret = fromZkData(data);
			}
		} catch (Exception e) {
			log.error("Failed to save data, path " + path, e);
		}
		return ret;
	}

	private void createNode(String path) {
		try {
			String parent = getParentPath(path);
			if(parent==null || "".equals(parent.trim()) || "/".equals(parent.trim())){
				zk.createNode(path);
				return;
			}
			if (!zk.exist(parent)) {
				createNode(parent);
			}
			zk.createNode(path);
		} catch (Exception e) {
			log.error("Failed to create node, path " + path, e);
		}
	}

	private String getParentPath(String path) {
		return path.substring(0, path.lastIndexOf('/'));
	}

	private byte[] toZkData(List<String> data) {

		StringBuilder sb = new StringBuilder();
		for (String s : data) {
			if (!StringUtils.isEmpty(s)) {
				sb.append(s).append("\n");
			}
		}
		return sb.toString().getBytes();
	}

	private List<String> fromZkData(byte[] data) {
		List<String> ret = new ArrayList<String>();

		String str = new String(data);
		String[] strs = str.split("\n");
		if (strs != null && strs.length > 0) {
			for (String s : strs) {
				if (!StringUtils.isEmpty(s)) {
					ret.add(s);
				}
			}
		}

		return ret;
	}

	/**
	 * Usage: ZkFileUtil syncToZk local remote
	 * @param args
	 */
	public static void main(String[] args) {
		List<String> data=new ArrayList<String>();
		//data.add("1407993212000");
		data.add("2014-08-14,13-13-32");
		ZkFileUtil.getInstance().save("/index/data-process/index.data.properties", data);
	}
}
