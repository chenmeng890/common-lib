package com.yihaodian.store.hbasestore.json;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSON;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class HbaseRecordSerializer implements 
	JsonSerializer<Map<byte[], Map<byte[], byte[]>>>, 
	JsonDeserializer<Map<byte[], Map<byte[], byte[]>>>{
	
	@Override
	public JsonElement serialize(Map<byte[], Map<byte[], byte[]>> src,
			Type typeOfSrc, JsonSerializationContext context) {
		JsonElement elem = null;
		if(src != null) {
			elem = new JsonObject();
			JsonObject elemObj = (JsonObject)elem;
			for (Entry<byte[], Map<byte[], byte[]>> entry : src.entrySet()) {
				JsonObject family = new JsonObject();
				for (Entry<byte[], byte[]> qualifEntry : entry.getValue().entrySet()){
					JsonArray qualifierBytes = new JsonArray();
					for (byte b : qualifEntry.getValue()){
						qualifierBytes.add(new JsonPrimitive(b));
					}
					family.add(new String(qualifEntry.getKey()), qualifierBytes);
				}
				elemObj.add(new String(entry.getKey()), family);
			}
		} else {
			elem = JsonNull.INSTANCE;
		}
		return elem;
	}
	
	@Override
	public Map<byte[], Map<byte[], byte[]>> deserialize(JsonElement json,
			Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		Map<byte[], Map<byte[], byte[]>> map = null;
		if (json != null) {
			map = new HashMap<byte[], Map<byte[],byte[]>>();
			JsonObject datas = json.getAsJsonObject();
			for (Entry<String, JsonElement> entry: datas.entrySet()) {
				byte[] familyName = entry.getKey().getBytes();
				Map<byte[], byte[]> qulifiers = new HashMap<byte[], byte[]>();
				for (Entry<String, JsonElement> qulifEntry : 
					entry.getValue().getAsJsonObject().entrySet()) {
					JsonArray byteArray = qulifEntry.getValue().getAsJsonArray();
					byte[] qulifBytes = new byte[byteArray.size()];
					for (int i = 0; i < byteArray.size(); i++) {
						qulifBytes[i] = byteArray.get(i).getAsByte();
					}
					byteArray.size();
					qulifiers.put(qulifEntry.getKey().getBytes(), qulifBytes);
							
				}
				map.put(familyName, qulifiers);
			}
		}
		
		return map;
	}
}
