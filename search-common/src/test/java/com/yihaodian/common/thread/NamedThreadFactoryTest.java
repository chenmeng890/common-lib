package com.yihaodian.common.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import junit.framework.Assert;

import org.junit.Test;


public class NamedThreadFactoryTest {
	
	@Test
	public void testName() {
		final String prefix = "test";
		ExecutorService pool = Executors.newFixedThreadPool(2, new NamedThreadFactory(prefix));
		Future<Boolean> f = pool.submit(new Callable<Boolean>(){

	
			@Override
			public Boolean call() throws Exception {
				String name = Thread.currentThread().getName();
				System.out.println(name);
				Assert.assertTrue(name.startsWith(prefix));
				return true;
			}
			
		});


		try {
			Assert.assertTrue(f.get());
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {

			e.printStackTrace();
		}

	
	}
}
