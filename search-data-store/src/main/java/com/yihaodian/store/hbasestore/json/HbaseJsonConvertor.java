package com.yihaodian.store.hbasestore.json;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class HbaseJsonConvertor {
	private static Gson gson = null;

	public static Gson getGson() {
		if (gson == null) {
			synchronized (HbaseJsonConvertor.class) {
				if (gson == null) {
					// GsonBuilder builder = new GsonBuilder();
					GsonBuilder builder = new GsonBuilder();

					builder.registerTypeAdapter(
							new TypeToken<Map<byte[], Map<byte[], byte[]>>>(){}.getType(),
							new HbaseRecordSerializer());
					gson = builder.create();
				}
			}
		}
		return gson;
	}
}
