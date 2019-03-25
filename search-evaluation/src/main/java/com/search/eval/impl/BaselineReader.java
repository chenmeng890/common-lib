package com.search.eval.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.search.eval.SearchDoc;
import com.search.eval.SearchQuery;
import com.search.eval.SearchResult;

/**
 * EvalResult reader and writer
 * 
 */
public class BaselineReader {
	static Logger logger = Logger.getLogger(BaselineReader.class);

	Gson gson = new GsonBuilder().enableComplexMapKeySerialization()
			.setPrettyPrinting()
			.registerTypeAdapter(SearchDoc.class, new SearchDocMarshal())
			.create();

	static class ResultHolder {
		Map<SearchQuery, SearchResult> baseline;
	}

	/**
	 * Baseline is saved in xml
	 * 
	 */
	static class SearchDocMarshal implements JsonSerializer<SearchDoc>,
			JsonDeserializer<SearchDoc> {

		@Override
		public SearchDoc deserialize(JsonElement ele, Type type,
				JsonDeserializationContext context) throws JsonParseException {
			return context.deserialize(ele, ProductDoc.class);
		}

		@Override
		public JsonElement serialize(SearchDoc doc, Type type,
				JsonSerializationContext context) {
			return context.serialize((ProductDoc) doc, ProductDoc.class);
		}

	}

	/**
	 * save baseline
	 * 
	 * @param file
	 * @param baseline
	 * @throws IOException
	 */
	public void save(String file, Map<SearchQuery, SearchResult> baseline)
			throws IOException {
		logger.info("write baseline to " + file);

		ResultHolder holder = new ResultHolder();
		holder.baseline = baseline;

		String json = gson.toJson(holder);
		FileUtils.writeStringToFile(new File(file), json);
	}

	/**
	 * read baseline
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public Map<SearchQuery, SearchResult> read(String file) throws IOException {
		logger.info("read baseline from " + file);

		String json = FileUtils.readFileToString(new File(file));
		ResultHolder holder = gson.fromJson(json, ResultHolder.class);

		if (holder.baseline != null) {
			Map<SearchQuery, SearchResult> maps = new TreeMap<SearchQuery, SearchResult>(
					SearchQuery.weightComarator);
			maps.putAll(holder.baseline);
			return maps;
		}

		return null;
	}

}
