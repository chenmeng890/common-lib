package com.yihaodian.common.bdb;

import com.sleepycat.je.Transaction;


public class MockStore extends BDBStore<String, String> {

	public MockStore(String dbPath, String dbName, boolean isReadOnly,
			int maxMemoryPercent) {
		super(dbPath, dbName, isReadOnly, maxMemoryPercent);
	}


	@Override
	protected void initPrimaryIndex() {
		primaryIndex =  store.getPrimaryIndex(String.class, MockEntity.class);
	}


	@Override
	protected String _put(Transaction txn, String k, String v) {
		MockEntity entity = new MockEntity(k, v);
		MockEntity re = (MockEntity)primaryIndex.put(txn, entity);
		if(re == null)
			return null;
		return re.getV();
	}


	@Override
	protected String _get(String k) {

		MockEntity entity = (MockEntity)primaryIndex.get(k);

		if (entity == null) {
			return null;
		} else {
			return entity.getV();
		}

	}

}
