package com.yihaodian.common.thread;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {

	private String prefix;
	
	private ThreadFactory threadFactory;
	
	private final AtomicInteger id = new AtomicInteger(1);
	
	public NamedThreadFactory(String prefix) {
		this.prefix = prefix;
		threadFactory=Executors.defaultThreadFactory();
	}
	
	@Override
	public Thread newThread(Runnable r) {
		Thread t = threadFactory.newThread(r);		
		t.setName(prefix + "-" + id.getAndIncrement());
		return t;
	}

}
