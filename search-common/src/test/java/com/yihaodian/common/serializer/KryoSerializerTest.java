package com.yihaodian.common.serializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.yihaodian.common.serializer.impl.KryoSerializer;

public class KryoSerializerTest {

	static class TestClass {
		public String text = "something";
		public int moo = 120;
		public long moo2 = 1234120;
	}

	@Test
	public void testSerializer() {
		YSerializer ks = new KryoSerializer();

		TestClass obj = new TestClass();
		byte[] bytes = ks.toBytes(obj);

		System.out.println(bytes.length);
		TestClass obj2 = ks.fromBytes(TestClass.class, bytes);
		assertEqual(obj, obj2);
	}

	@Test
	public void testAddField() throws IOException {
		YSerializer ks = new KryoSerializer();
		TestClass obj = new TestClass();
		byte[] bytes = ks.toBytes(obj);

		String file = System.getProperty("java.io.tmpdir") + "/"
				+ System.currentTimeMillis();
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(bytes);
		fos.close();

		bytes = FileUtils.readFileToByteArray(new File(file));
		System.out.println(bytes.length);
		TestClass obj2 = ks.fromBytes(TestClass.class, bytes);
		assertEqual(obj, obj2);
		new File(file).delete();
	}

	void assertEqual(TestClass obj, TestClass obj2) {
		Assert.assertEquals(obj.text, obj2.text);
		Assert.assertEquals(obj.moo, obj2.moo);
		Assert.assertEquals(obj.moo2, obj2.moo2);
	}
}
