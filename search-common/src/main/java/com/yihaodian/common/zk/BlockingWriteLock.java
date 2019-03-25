package com.yihaodian.common.zk;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.ACL;

import com.yihaodian.common.zk.lock.LockListener;
import com.yihaodian.common.zk.lock.WriteLock;

public class BlockingWriteLock {
	private static final Logger logger = Logger.getLogger(BlockingWriteLock.class);
	private CountDownLatch signal = new CountDownLatch(1);
	WriteLock writeLock;

	public BlockingWriteLock(String name, ZkClientService zkClientService, String path,
			List<ACL> acls) {
		this.writeLock = new WriteLock(zkClientService, path, acls,
				new SyncLockListener());
	}

	public void lock() throws InterruptedException, KeeperException {
		logger.info("wait for lock, path:" + writeLock.getDir());
//		boolean success = writeLock.lock();
//		logger.info("get lock " + success + ", lock:" + writeLock.getDir());
//		signal.await();
		while (true) {
			if (writeLock.lock()) {
				logger.info("get lock success, lock:" + writeLock.getDir());
				signal.await();
				return;
			}
			
			try {
				Thread.sleep(30 * 1000 + RandomUtils.nextInt(1000));
			} catch (InterruptedException e) {
				logger.debug("Failed to sleep: " + e, e);
			}
		}
	}

	public void unlock() throws InterruptedException {
		writeLock.unlock();
	}

	class SyncLockListener implements LockListener {
		@Override
		public void lockAcquired() {
			//lock already acquire and release, need to release again
			if(signal.getCount() == 0){
				try {
					unlock();
				} catch (InterruptedException e) {
					logger.error("unlock failed, " + e.getMessage());
				}
			}
			
			signal.countDown();
		}

		@Override
		public void lockReleased() {
		}
	}
}
