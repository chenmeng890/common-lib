package com.yihaodian.common.bdb;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;

import com.sleepycat.je.CheckpointConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Durability;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentStats;
import com.sleepycat.je.StatsConfig;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.util.DbVerify;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.persist.evolve.Mutations;

/**
 * Base class for BDB based key-value store
 * 
 * @author zhouhang
 * 
 * @param <K>
 * @param <V>
 */
public abstract class BDBStore<K, V> implements BDBOperation<K, V> {

	private static Logger log = Logger.getLogger(BDBStore.class);

	private File envHome;
	private boolean isReadOnly;
	private Integer maxMemoryPercent;
	protected EntityStore store;
	private Environment env;
	private final String dbName;
	private DbVerify verifier;

	protected PrimaryIndex primaryIndex;

	/**
	 * 
	 * @param dbPath
	 *            : full path of the database
	 * @param dbName
	 *            : descriptive database name
	 * @param isReadOnly
	 * @param maxMemoryPercent
	 */
	public BDBStore(String dbPath, String dbName, boolean isReadOnly,
			int maxMemoryPercent) {
		this(dbPath, dbName, isReadOnly, maxMemoryPercent, null);
	}

	/**
	 * 
	 * @param dbPath
	 *            : full path of the database
	 * @param dbName
	 *            : descriptive database name
	 * @param isReadOnly
	 * @param maxMemoryPercent
	 * @param mutations
	 *            : mutations if need to convert the data
	 */
	public BDBStore(String dbPath, String dbName, boolean isReadOnly,
			int maxMemoryPercent, Mutations mutations) {
		this.dbName = dbName;
		this.envHome = new File(dbPath);
		this.isReadOnly = isReadOnly;
		this.maxMemoryPercent = maxMemoryPercent;

		init(mutations);
		initPrimaryIndex();

		log.info("Opened database, path: " + dbPath + ", object count: "
				+ size());
	}

	protected abstract void initPrimaryIndex();

	private void init(Mutations mutations) {
		/* Open a Berkeley DB engine environment. */
		EnvironmentConfig envConfig = new EnvironmentConfig();
		envConfig.setAllowCreate(!isReadOnly);
		envConfig.setTransactional(!isReadOnly);
		envConfig.setCachePercent(maxMemoryPercent);
		envConfig.setReadOnly(isReadOnly);

		// Use the most conservative configuration
		Durability durability = new Durability(
				Durability.SyncPolicy.WRITE_NO_SYNC,
				Durability.SyncPolicy.NO_SYNC, Durability.ReplicaAckPolicy.NONE);
		envConfig.setDurability(durability);

		// We don't want any background threads.
		// envConfig.setConfigParam(EnvironmentConfig.ENV_RUN_CHECKPOINTER,
		// "false");
		// envConfig.setConfigParam(EnvironmentConfig.ENV_RUN_CLEANER, "false");
		// envConfig.setConfigParam(EnvironmentConfig.ENV_RUN_EVICTOR, "false");
		// envConfig.setConfigParam(EnvironmentConfig.ENV_RUN_IN_COMPRESSOR,
		// "false");
		envConfig.setConfigParam(EnvironmentConfig.LOG_FILE_MAX, "500000000");
		// envConfig.setConfigParam(EnvironmentConfig.EVICTOR_MAX_THREADS, "1");
		// envConfig.setConfigParam(EnvironmentConfig.TREE_MIN_MEMORY,
		// "51200000");
		// envConfig.setConfigParam(EnvironmentConfig.TREE_COMPACT_MAX_KEY_LENGTH,
		// "128");
		// envConfig.setConfigParam(EnvironmentConfig.NODE_MAX_ENTRIES, "1024");
		/**
		 * BDB JE is built on B-Tree, which looks like:
		 * 
		 * (Root): 					ROOT 
		 * (Level-1): 			IN ... IN 
		 * (Level-...): 	    ... ... ... 
		 * (Level-n-1):     BIN ... ... ... BIN 
		 * Level-n       LN ... ... ... ... ... LN
		 * 
		 * IN: Internal node, could have <=128 children BIN: Bottom Internal
		 * node, could have <=128 LN as children LF: Leaf Node, holding the data
		 * 
		 * If the evication policy is LRU-ONLY, evicator will kick out any IN
		 * (and its children) which loads first.
		 * 
		 * If the evication policy is no-LRU-ONLY, evicator will kick out lowest
		 * level IN in the tree which loads first.
		 */
		envConfig.setConfigParam(EnvironmentConfig.EVICTOR_LRU_ONLY, "false");
		// envConfig.setConfigParam(EnvironmentConfig.EVICTOR_NODES_PER_SCAN,
		// "100");
		// envConfig.setConfigParam(EnvironmentConfig.LOG_FAULT_READ_SIZE,
		// "2048");
		// envConfig.setConfigParam(EnvironmentConfig.LOG_ITERATOR_READ_SIZE,
		// "2048");

		if (!envHome.exists()) {
			envHome.mkdirs();
		}
		env = new Environment(envHome, envConfig);

		/* Open a entity store. */
		StoreConfig storeConfig = new StoreConfig();
		storeConfig.setAllowCreate(!isReadOnly);
		storeConfig.setTransactional(!isReadOnly);
		storeConfig.setReadOnly(isReadOnly);

		if (mutations != null)
			storeConfig.setMutationsVoid(mutations);
		store = new EntityStore(env, dbName, storeConfig);

		verifier = new DbVerify(env, dbName, false);
	}

	/**
	 * Derived class must implement actual put operation with primaryIndex
	 * 
	 * @param txn
	 * @param k
	 * @param v
	 * @return
	 */
	protected abstract V _put(Transaction txn, K k, V v);

	@Override
	public V put(K k, V v) {
		// null key is not allowed.
		if (k == null) {
			return null;
		}

		Transaction txn = env.beginTransaction(null, null);
		try {
			V rv = this._put(txn, k, v);
			if (rv != null) {
				log.debug("Value is updated, key: " + k + ", old value: " + rv
						+ ", new value: " + v);
			}
			txn.commit();
		} catch (DatabaseException e) {
			if (txn != null) {
				txn.abort();
			}
			throw e;
		}

		return v;
	}

	/**
	 * Derived class must implement actual get operation with primaryIndex
	 * 
	 * @param k
	 * @return
	 */
	protected abstract V _get(K k);

	@Override
	public V get(K k) {
		if (k == null) {
			return null;
		}

		return this._get(k);

	}

	@Override
	public boolean containsKey(K k) {
		boolean ret = false;

		if (k == null) {
			return false;
		}

		return primaryIndex.contains(k);

	}

	@Override
	public boolean remove(K k) {

		if (k == null) {
			return true;
		}

		boolean ret = false;

		Transaction txn = env.beginTransaction(null, null);
		try {
			ret = primaryIndex.delete(txn, k);
			txn.commit();
		} catch (DatabaseException e) {
			if (txn != null) {
				txn.abort();
			}
			throw e;
		}

		return ret;
	}

	@Override
	public Set<K> keySet() {
		Set<K> keys = new HashSet<K>();

		EntityCursor<K> cursor = primaryIndex.keys();
		for (K k : cursor) {
			keys.add(k);
		}

		cursor.close();
		return keys;
	}

	@Override
	public long size() {
		return primaryIndex.count();
	}

	@Override
	public boolean verify() {
		boolean valid = false;
		try {
			valid = verifier.verify(System.out);
		} catch (DatabaseException e) {
			log.error(e, e);
		}
		return valid;
	}

	@Override
	public boolean readVerifyThorough(int numThread) {

		boolean valid = false;
		List<Future<Boolean>> results = new ArrayList<Future<Boolean>>();
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors
				.newFixedThreadPool(numThread);

		// Read by multiple threads
		try {
			Set<K> keys = keySet();

			for (int i = 0; i < numThread; ++i) {
				List<K> curList = new ArrayList<K>();
				for (K k : keys) {
					if (k.hashCode() % numThread == i) {
						curList.add(k);
					}
				}
				final List<K> tmpList = new ArrayList<K>(curList.subList(0,
						Math.min(100000, curList.size())));
				Future<Boolean> future = executor
						.submit(new Callable<Boolean>() {
							@Override
							public Boolean call() {
								for (K k : tmpList) {
									try {
										// drop as we read
										get(k);
									} catch (Exception e) {
										log.error("Failed to read data: " + k,
												e);
										return false;
									}
								}
								return true;
							}
						});

				results.add(future);
			} // end for thread

			boolean resultSummary = true;
			for (Future<Boolean> future : results) {
				resultSummary = resultSummary && future.get();
			}
			valid = resultSummary;
		} catch (Exception e) {
			for (Future<Boolean> future : results) {
				if (!future.isDone()) {
					future.cancel(false);
				}
			}
			valid = false;
		} finally {
			executor.shutdown();
		}

		return valid;
	}

	@Override
	public void printStats() {
		StatsConfig statsConfig = new StatsConfig();
		statsConfig.setClear(true);
		EnvironmentStats stats = env.getStats(statsConfig);
		System.out.println(stats.toString());
	}

	@Override
	public void close() {
		if (store != null && env != null)
			log.info("Closing database, path: " + envHome.getAbsolutePath()
					+ ", object count: " + size());

		if (store != null) {
			if (!isReadOnly) {
				store.sync();
			}

			store.close();
			store = null;
		}
		if (env != null) {
			if (!isReadOnly) {
				env.flushLog(true);

				// clean log until nothing to clean
				// while(env.cleanLog() > 0) {}

				CheckpointConfig force = new CheckpointConfig();
				force.setForce(true);
				env.checkpoint(force);
			}
			env.close();
			env = null;
		}
	}

}
