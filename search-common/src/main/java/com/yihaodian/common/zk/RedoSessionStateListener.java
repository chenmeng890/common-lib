package com.yihaodian.common.zk;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.log4j.Logger;

import com.yihaodian.common.zk.ZkClient;
import com.yihaodian.common.zk.ZkClient.SessionStateListener;


public class RedoSessionStateListener implements SessionStateListener{
	
	private static final Logger logger = Logger.getLogger(RedoSessionStateListener.class);

	//最多重新执行的zk操作数目
	private static final int maxRedoOperations = 1000;
	private ConcurrentLinkedQueue<RedoOperation> redoOperations = new ConcurrentLinkedQueue<RedoOperation>();
	private Set<String> operationKeys = new HashSet<String>();
	
	private ZkClient zkClient;
	
	public RedoSessionStateListener(ZkClient zkClient){
		zkClient.addSessionStateListener(this);
		this.zkClient = zkClient;
	}
	
	public void addRedoOperation(RedoOperation redoOperation){
		//判断是否有类似的redo操作已经被加入
		if(redoOperation == null || operationKeys.contains(redoOperation.getUniqueKey()))
			return;
		
		//当redo队列超过最大限制时，从队列头部移除最老的一个zk操作
		if(redoOperations.size() >= maxRedoOperations) {
			RedoOperation removedOperation = redoOperations.poll();
			logger.info("remove redo operation:" + removedOperation);
			if(removedOperation != null)
				operationKeys.remove(removedOperation.getUniqueKey());
		}
		
		operationKeys.add(redoOperation.getUniqueKey());
		redoOperations.add(redoOperation);
		logger.info("add redo operation:" + redoOperation);
	}

	@Override
	public void sessionDisconnected() {
	}

	@Override
	public void sessionConnected() {
	}

	@Override
	public void sessionExpired() {
		while (!zkClient.connected()) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		}
		
		if(redoOperations.size()>0){
			for (RedoOperation redoOperation : redoOperations) {
				try {
					redoOperation.redo();
					logger.info("sessionExpired() redo operation "+redoOperation);
				} catch (Exception e) {
					logger.error("sessionExpired() redo operation error " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}
	
	public static class RedoOperation{
		
		Object obj;
		
		Method method;
		
		Object[] params;
		
		String uniqueKey;

		public RedoOperation(Object obj, Method method, String uniqueKey, Object... params) {
			super();
			this.obj = obj;
			this.method = method;
			this.params = params;
			this.uniqueKey = uniqueKey;
		}
		
		public void redo() throws Exception{
			method.invoke(obj, params);
		}

		@Override
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("obj=").append(obj).append(";method=").append(method)
				.append(";uniqueKey=").append(uniqueKey).append(";params=");
			for (Object param : params) {
				if(param instanceof byte[])
					buffer.append(new String((byte[])param)).append(",");
				else
					buffer.append(param.toString()).append(",");
			}
			return buffer.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((method == null) ? 0 : method.hashCode());
			result = prime * result + ((obj == null) ? 0 : obj.hashCode());
			result = prime * result + Arrays.hashCode(params);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RedoOperation other = (RedoOperation) obj;
			if (method == null) {
				if (other.method != null)
					return false;
			} else if (!method.equals(other.method))
				return false;
			if (this.obj == null) {
				if (other.obj != null)
					return false;
			} else if (!this.obj.equals(other.obj))
				return false;
			if (!Arrays.equals(params, other.params))
				return false;
			return true;
		}

		public String getUniqueKey() {
			return uniqueKey;
		}

	}
	
}