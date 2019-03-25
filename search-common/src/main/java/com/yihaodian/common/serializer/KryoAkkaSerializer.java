package com.yihaodian.common.serializer;

import org.apache.log4j.Logger;

import com.yihaodian.common.serializer.impl.ThreadLocalKyroSerializer;

import akka.serialization.JSerializer;

/**
 * KryoAkkaSerializer 对akka的序列化做采用kryo的方式
 * @author zengfenghua
 *
 */
public class KryoAkkaSerializer extends JSerializer{
	
	private static Logger log = Logger.getLogger(KryoAkkaSerializer.class);
	
	YSerializer yserializer=ThreadLocalKyroSerializer.kyroSerializer;

	@Override
	public int identifier() {
		return 33333;
	}

	@Override
	public boolean includeManifest() {
		return false;
	}

	@Override
	public byte[] toBinary(Object obj) {
		return yserializer.toBytes(obj);
	}

	@Override
	public Object fromBinaryJava(byte[] bytes, Class<?> arg1) {
		return yserializer.fromBytes(arg1, bytes);
	}

}
