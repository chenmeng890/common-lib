package com.yihaodian.store.zk;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.data.ACL;

/**
 * 
 * @author zengfenghua
 *
 */
public class ZKUtil implements Watcher {

	static Logger log = Logger.getLogger(ZKUtil.class);
	public static final String quorums_dev_wh = "192.168.129.107:2181,192.168.129.108:2181,192.168.129.109:2181";
	public static final String quorums_dev = "192.168.130.107:2181,192.168.130.108:2181,192.168.130.109:2181";
	public static final String quorums_stg = "10.63.0.154:2181,10.63.0.155:2181,10.63.0.156:2181";
	public static final String quorums_prod = "10.3.0.66:2181,10.3.0.68:2181,10.2.1.126:2181";

	private int session_timeout = 180000;

	private volatile CountDownLatch latch;

	private ZooKeeper zk = null;
	
	private String address;

	private Watcher watcher;
	
	private Object obj=new Object();
	
	// 创建一个Zookeeper实例，第一个参数为目标服务器地址和端口，第二个参数为Session超时时间，
	// 第三个为节点变化时的回调方法
	public ZKUtil(String address) throws IOException, InterruptedException {
		this.address=address;
		this.watcher=this;
	    checkAndConnect();
	}

	public ZKUtil(String address, int sessionTimeout) throws IOException,
			InterruptedException {
		this.address=address;
		this.session_timeout=sessionTimeout;
		this.watcher=this;
		checkAndConnect();
	}

    public ZKUtil(String address,int sessionTimeout,Watcher watcher) throws IOException, InterruptedException {
		this.address = address;
		this.session_timeout=sessionTimeout;
		this.watcher = watcher;
		checkAndConnect();
    }
    
    public void connect(){
    	ZooKeeper old_zk = this.zk;
		try {	
			log.info("Connecting to zookeeper : " + address + ", timeout: " + session_timeout);
			latch = new CountDownLatch(1);
			this.zk = new ZooKeeper(address, session_timeout, watcher);	
			latch.await(session_timeout, TimeUnit.MILLISECONDS);
		} catch (Exception e1) {
			log.error("Failed to connected to zookeeper : " + address, e1);
		}finally{
			if(old_zk!=null){
				try {
					old_zk.close();
				} catch (InterruptedException e) {
					log.error("Failed to old_zk.close() : ", e);
				}
			}
				
		}
    	
    }

     
	public void destroy() throws InterruptedException {
		if (zk != null) {
			zk.close();
		}
	}

	public String createNode(String path) throws Exception{
		return createNode(path,false);
	}
	
    public String createNode(String path,boolean isCreateParent) throws Exception {
    	return createNode(path,null,isCreateParent);
	}
    
    public String createNode(String path, byte[] data) throws Exception {
		return createNode(path,data,false);
	} 
    
	public String createNode(String path, byte[] data,boolean isCreateParent) throws Exception {
		return createNode(path,data,Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT,isCreateParent);
	}

	public String createNode(String path, byte[] data, List<ACL> acl,
			CreateMode mode,boolean isCreateParent) throws Exception{


		String realPath=null;
		if(isCreateParent){
			checkPath(path);
			String parentPath=path.substring(0,path.lastIndexOf("/"));
			if(!parentPath.equals("") && !parentPath.equals("/") && !this.exist(parentPath))
			    createNode(parentPath,null,acl,mode,isCreateParent);
		}
		checkAndConnect();
		if (zk != null) {
			realPath=zk.create(path, data, acl, mode);
		}
		this.sync(realPath);
		return realPath;
	}

	public List<String> getChilds(String path) throws KeeperException,
			InterruptedException {
		checkAndConnect();
		if (zk != null) {
			return zk.getChildren(path, true);
		}
		return null;
	}

	public byte[] getData(String path) throws KeeperException,
			InterruptedException {
		checkAndConnect();
		if (zk != null) {
			// 取得/root/childone节点下的数据,返回byte[]
			byte[] b = zk.getData(path, true, null);
			return b;
		}
		return null;
	}
	
	public byte[] getData(final String path, Watcher watcher) throws KeeperException, InterruptedException
	{   
		checkAndConnect();
		if (zk != null) {
			// 取得/root/childone节点下的数据,返回byte[]
			byte[] b = zk.getData(path, watcher, null);
			return b;
		}
		
		return null;
	}

	public void changeData(String path, byte[] data) throws Exception {
		checkAndConnect(); 
		if (zk != null) {
			// 修改节点/root/childone下的数据，第三个参数为版本，如果是-1，
			// 那会无视被修改的数据版本，直接改掉
//			this.delNode(path);
//			this.createNode(path, data);
			zk.setData(path, data, -1);
			this.sync(path);
		}
	}

	public void delData(String path) throws Exception {
		if (zk != null) {
			// 删除/root/childone这个节点，第二个参数为版本，－1的话直接删除，无视版本
			//zk.delete(path, -1);
			changeData(path,null);
		}
	}

	public void delNode(String path) throws InterruptedException,
			KeeperException {
		checkAndConnect();
		if (zk != null) {
			zk.delete(path, -1);
		}
	}

	public void delNodeRecursive(String path) throws InterruptedException,
	KeeperException {
		checkAndConnect();
		
		if(!exist(path)){
			return;
		}
		
		if (zk != null) {
			List<String> children = getChilds(path);
			if(children != null && !children.isEmpty()){
				for(String child : children){
					delNodeRecursive(path + "/" + child);
				}
			}
			zk.delete(path, -1);
		}
	}
	
	public boolean exist(String path) throws KeeperException,
			InterruptedException {
		checkAndConnect();
		if (zk != null) {
			return zk.exists(path, true) != null;
		}
		return false;
	}

	public void process(WatchedEvent event) {
		if (event.getState() == KeeperState.SyncConnected) {
			if(latch != null) {
				latch.countDown();
			}
		} else if(event.getState() == KeeperState.Expired){
			// reconnect
			log.info("Zookeeper session expired, reconnecting ...");
			checkAndConnect(); 
		} else if(event.getState() == KeeperState.Disconnected){
			// reconnect
			log.info("Zookeeper disconnected, reconnecting ...");
			checkAndConnect();
		}
	}

	public void sync(String path) {
		checkAndConnect();
		if (zk != null)
			zk.sync(path, null, null);
	}
	
	public void checkPath(String path) throws Exception{
		if(path==null || !path.startsWith("/") || path.endsWith("/"))
			throw new Exception("path should not be null and must start with /," +
					"and should not end with /");
	}
	
	public States getZkState(){
		return zk.getState();
	}
	
	public boolean isConnectioned(){
		if(zk==null || zk.getState()==null)
			return false;
		return getZkState()==States.CONNECTED;
	}
	
	public void checkAndConnect(){
		if(!isConnectioned()){
			synchronized(obj){
				if(!isConnectioned())
					connect();
			}
		}		
	}
	
	public static void main(String[] args) throws Exception{
		ZKUtil zk=new ZKUtil("hadoopcm8,hadoopcm9,hadoopcm10");
	    //zk.createNode("/search_pref/test/config/cache/prod");
	    String data=new String(zk.getData("/search/test/test/config/cache/prod"));
	    System.out.println(data);
	   // zk.changeData("/search_pref/test/config/cache/prod", data.getBytes());
	    data=new String(zk.getData("/search_pref/test/test/config/cache/prod"));
	    System.out.println(data);
	}
	
}
