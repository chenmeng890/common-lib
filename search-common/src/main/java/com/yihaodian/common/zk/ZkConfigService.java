package com.yihaodian.common.zk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.yihaodian.common.thread.NamedThreadFactory;
import com.yihaodian.common.util.EnvUtil;
import com.yihaodian.common.util.FileUtils;

/**
 * ZkConfigService用来监听zk Node的数据
 * YHDSearchZkConfigService 是ZkConfigService的子类
 * 
 * @author zengfenghua
 *
 */
public class ZkConfigService {
	
	private static final Logger log = Logger.getLogger(ZkConfigService.class);
	private Map<String,String> configDatas = new HashMap<String,String>();
	private Gson gson = new Gson();

	protected ZkClient zkClient;
	private static String zk_path_prefix;
	private static String localPrefixPath;
	private int connectCount=0;
	
	// 监听配置内容变化
	private Map<String, List<ConfigChangeListener>> configListeners = new HashMap<String, List<ConfigChangeListener>>();
	
	/**
	 * 配置更改listener调度线程池。
	 * maxThread：10
	 * queue：100
	 * 队列达到上限后，任务直接丢弃
	 */
	private ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
			2, 10, 60L, TimeUnit.SECONDS,
             new LinkedBlockingQueue<Runnable>(100),
			new NamedThreadFactory("zkConfig-listener"),
			new ThreadPoolExecutor.AbortPolicy());
	
	static {
		String env = EnvUtil.getEnv();
		zk_path_prefix = "/search/" + env + "/SearchConfig/";
		localPrefixPath = "/var/www/webapps/config/SearchConfig/";
	}
	
	public ZkConfigService(ZkClient zkClient){
		this.zkClient = zkClient;
	}
	
	private synchronized String loadConfig(final String configZkPath, final String defaultData) {		
		byte[] data = null;
		final String localConfigPath = localPrefixPath + configZkPath.substring(zk_path_prefix.length());
		int count=0;
		while(count++<3){
			boolean succ=true;
			try {
				checkConnect();
				checkZkPathAndSetData(configZkPath, defaultData);
				data = zkClient.getNode(configZkPath,
						new ZkClient.NodeListener() {
							@Override
							public void onNodeDeleted(String id) {
								log.info("onNodeDeleted triggered:" + configZkPath);
								configDatas.remove(configZkPath);
								delete(localConfigPath);//删除本地配置
							}

							@Override
							public void onNodeDataChanged(String id) {
								log.info("onNodeDataChanged triggered:" + configZkPath);
								loadConfig(configZkPath, defaultData);
								List<ConfigChangeListener> listeners = configListeners.get(configZkPath);
								if (listeners != null) {
									for (final ConfigChangeListener listener : listeners) {
										threadPool.submit(new Runnable() {
											@Override
											public void run() {
												listener.onConfigChange(configDatas.get(configZkPath));
											}
										});
									}
								}
							}

							@Override
							public void onNodeCreated(String id) {
							}
						});
			} catch (Throwable e) {//这里的连接异常在zkClient.getNode内部被吃掉了，未抛出来
				log.error("can't get config from zk path:" + configZkPath, e);
				succ=false;
			}
			if(succ){
				break;
			}
		}		
		if (data != null && data.length>0) {
			configDatas.put(configZkPath, new String(data));
			backup(new String(data), localConfigPath);//备份数据
		} else {//zk读取数据失败，尝试从本地读取
			String dataStr = (String) recovery(String.class, localConfigPath);
			if (dataStr != null) {
				configDatas.put(configZkPath, dataStr);
			}
		}
		log.info("loadConfig configZkPath:" + configZkPath+",data:"+ configDatas.get(configZkPath));
		return configDatas.get(configZkPath);
	}

	/**
	 * 
	 * @param prefixPath
	 * @return
	 */
	public Set<String> listZkConfigNodes(String prefixPath) {
		String configZkPath = getConfigPath(prefixPath, null);
		int count=0;
		while(count++<3){
			boolean succ=true;
			try {				
				checkConnect();
				checkZkPathExists(configZkPath);
				Set<String> pathSet = zkClient.listNodes(configZkPath, null);
				log.info("listZkConfigPaths sucessful configZkPath:"+configZkPath);
				return pathSet;
			} catch (Exception e) {
				log.error("listZkConfigPaths error,configZkPath:"+configZkPath, e);
				succ=false;
			}
			if(succ){
				break;
			}
		}	
		return null;
	}
	
	/**
	 * 
	 * @param path
	 * @param configName
	 * @param defaultData
	 * @return
	 */
	public String getConfigData(String path, String configName, String defaultData) {
		String configZkPath = getConfigPath(path, configName);
		if (configDatas.containsKey(configZkPath)) {
			return configDatas.get(configZkPath);
		} 
		synchronized (this) {
			if (configDatas.containsKey(configZkPath)) {
				return configDatas.get(configZkPath);
			}
			String zkConfig=loadConfig(configZkPath, defaultData);
			configDatas.put(configZkPath, zkConfig);
			return zkConfig;
		}
		
	}
	
	public String takeConfigData(String path, String configName) {
		String configZkPath = getConfigPath(path, configName);
		if (configDatas.containsKey(configZkPath)) {
			return configDatas.get(configZkPath);
		} 
		synchronized (this) {
			if (configDatas.containsKey(configZkPath)) {
				return configDatas.get(configZkPath);
			}
			byte[] data = null;
			int count=0;
			while(count++<3){
				boolean succ=true;
				try {
					checkConnect();
					data = zkClient.getNode(configZkPath, null);
				} catch (InterruptedException e) {
					log.error("takeConfigData error, configZkPath=" + configZkPath, e);
					succ=false;
				}
				if(succ){
					break;
				}
			}
			
			if (data != null && data.length > 0) {
				String zkConfig = loadConfig(configZkPath, new String(data));
				configDatas.put(configZkPath, zkConfig);
				return zkConfig;
			} else {
				log.warn("takeConfigData null, configZkPath=" + configZkPath);
				return null;
			}
		}
	}
	
	public synchronized void deleteZKConfig(String path, String configName) {
		String configZkPath = getConfigPath(path, configName);
		int count=0;
		while(count++<3){
			boolean succ=true;
			try {				
				checkConnect();
				checkZkPathExists(configZkPath);
				zkClient.deleteNode(configZkPath);
				log.info("deleteZKConfig sucessful configZkPath:"+configZkPath);
			} catch (Exception e) {
				log.error("Delete zk config error,configZkPath:"+configZkPath, e);
				succ=false;
			}
			if(succ){
				break;
			}
		}		
	}
	
	public synchronized void updateZKConfig(String path, String configName, String data) {
		String configZkPath = getConfigPath(path, configName);
		int count=0;
		while(count++<3){
			boolean succ=true;
			try {				
				checkConnect();
				checkZkPathExists(configZkPath);
				zkClient.setOrCreatePersistentNode(configZkPath, data.getBytes());
				log.info("updateZKConfig sucessful configZkPath:"+configZkPath+",data:"+data);
			} catch (Throwable e) {
				log.error("Update zk config error,configZkPath:"+configZkPath+",data:" + data, e);
				succ=false;
			}
			if(succ){
				break;
			}
		}		
	}
	
	public synchronized void updateLocalConfig(String path, String configName, String data) {
		String configZkPath = getConfigPath(path, configName);
		configDatas.put(configZkPath, data);
	}
	
	/**
	 * 给zk config路径添加监听
	 * @param path
	 * @param configName
	 * @param listener
	 */
	public void addConfigChangeListener(String path, String configName, ConfigChangeListener listener){
		String listenPath = getConfigPath(path, configName);
		// Register listener to zk.
		getConfigData(path, configName, "");
		
		List<ConfigChangeListener> listeners = configListeners.get(listenPath);
		if (listeners == null) {
			synchronized(this){
				listeners = configListeners.get(listenPath);
				if (listeners == null) {
					listeners = new ArrayList<ConfigChangeListener>();
					listeners.add(listener);
					configListeners.put(listenPath, listeners);
				}
			}
		}
	}
	
	public void removeConfigChangeListener(String path, String configName, ConfigChangeListener listener){
		String listenPath = getConfigPath(path, configName);
		List<ConfigChangeListener> listeners = configListeners.get(listenPath);
		if (listeners != null) {
			listeners.remove(listener);
		}
	}
	
	private synchronized void checkConnect(){
		if(connectCount<5){
			connectCount++;
			zkClient.connect();		
		}
	}
	
	private synchronized void checkZkPathExists(String configZkPath) throws InterruptedException{
		if (zkClient.getNode(configZkPath, null) == null) {
			log.info("Init zk path: " + configZkPath);
			zkClient.createPersistentNode(configZkPath);
		}
	}
	
	private synchronized void checkZkPathAndSetData(String configZkPath, String data) throws Exception{
		if (zkClient.getNode(configZkPath, null) == null) {
			log.info("Init zk path: " + configZkPath + ", data=" + data);
			zkClient.createPersistentNode(configZkPath);
			if (data != null) {
				zkClient.setOrCreatePersistentNode(configZkPath, data.getBytes());
			}
		}
	}
	
	private synchronized String getConfigPath(String configZkPath, String configName) {
		configZkPath = deleteHeadTailSlash(configZkPath);
		if(StringUtils.isBlank(configZkPath)) {
			return zk_path_prefix + configName;
		}
		if(StringUtils.isBlank(configName)) {
			return zk_path_prefix + configZkPath;
		}
		
		return zk_path_prefix + configZkPath + "/" + configName;
	}
	
	private synchronized String deleteHeadTailSlash(String path){
		if(StringUtils.isBlank(path) || path.equals("/"))
			return "";
		
		if (path.startsWith("/")) {
			path = path.substring(1, path.length());
		}
		
		if (path.lastIndexOf('/') == path.length() - 1) {
			 path = path.substring(0, path.length() - 1);
		}
		
		return path;
	}
	
	private boolean backup(Object obj, String path) {
		try {
			String jsonFile = gson.toJson(obj);
			boolean succ =  FileUtils.updateFile(path, jsonFile);
			log.info("backup data to " + path + " over, data=" + obj);
			return succ;
		} catch (Exception e) {
			log.error("backup config.", e);
			return false;
		}
	} 
	
	private Object recovery(Class<?> Obj, String path) {
		String fileContent = null;
		try {
			fileContent = FileUtils.readFileContent(path);
		} catch (IOException e) {
			log.warn("recovery config.", e);
			return null;
		}
		
		if (StringUtils.isEmpty(fileContent)) {
			return null;
		}
		Object data =  gson.fromJson(fileContent, Obj);
		log.info("recovery data from " + path + " over, data=" + data);
		return data;
	}

	private void delete(String path) {
		try {
			FileUtils.deleteFile(new File(path));
			log.info("delete file, path= " + path);
		} catch (Exception e) {
			log.error("backup config.", e);
		}
	} 
	
	
	public interface ConfigChangeListener{
		public void onConfigChange(String configValue);
	}
}
