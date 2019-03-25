package com.yihaodian.common.zk;

import java.io.IOException;

import org.apache.zookeeper.ZooKeeper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RedoSessionStateListenerTest {

	public String zkQuorums = "10.161.166.210:2181,10.161.166.211:2181,10.161.166.212:2181";
	ZkClient zkClient;
	
	@Before
	public void setup() throws InterruptedException, SecurityException, NoSuchMethodException {
		zkClient = new ZkClientService(zkQuorums);
		zkClient.connect();
	}
	
	@Test
	public void testRedo() throws InterruptedException, IOException, SecurityException, NoSuchMethodException {
		creatNode();
		Thread.sleep(1000 * 5);
		long oldSessionId = zkClient.getZooKeeper().getSessionId();
		
		// close the old connection, make the seesion expired
		ZooKeeper zooKeeper = new ZooKeeper(zkQuorums, 30000, null, oldSessionId,
				zkClient.getZooKeeper().getSessionPasswd());
		Thread.sleep(1000 * 5);
		zooKeeper.close();
		

		Thread.sleep(1000 * 20);
		byte[] data = zkClient.getNode("/search/test/testRedo", null);
		System.out.println(new String(data));
		Assert.assertEquals("/search/test/testRedo", new String(data));
		zkClient.close();
	}
	

	public void creatNode() throws InterruptedException, SecurityException, NoSuchMethodException {
		zkClient.setOrCreateTransientNode("/search/test/testRedo",
				"/search/test/testRedo".getBytes());
	}
}
