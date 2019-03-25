package com.yihaodian.common.serializer;

import org.apache.log4j.Logger;

import com.yhd.arch.photon.codec.HedwigCompatibleCodecProcessor;
import com.yhd.arch.photon.core.RemoteResponse;

import com.yihaodian.common.serializer.impl.ThreadLocalKyroSerializer;

public class KryoCodecProcessor extends HedwigCompatibleCodecProcessor {

	private static Logger log = Logger.getLogger(KryoCodecProcessor.class);
	
	public final static String name="kryoCodec";

	YSerializer yserializer = ThreadLocalKyroSerializer.kyroSerializer;

	@Override
	public byte[] respones2binary(RemoteResponse obj) {
		return yserializer.toBytes(obj);
	}

	@Override
	public RemoteResponse binary2respones(byte[] array) throws Throwable {
		return yserializer.fromBytes(RemoteResponse.class,array);
	}

}
