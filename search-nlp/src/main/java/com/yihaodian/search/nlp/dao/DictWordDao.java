package com.yihaodian.search.nlp.dao;

import java.util.List;

import com.yihaodian.search.nlp.model.DictWord;

public interface DictWordDao {

	/**
	 * 根据ibatis分页从数据库读取
	 * @param pageNum
	 * @return
	 */
	public List<DictWord> getDictWords(int pageNum);
	
	/**
	 * 从KEYWORD_DICT_TEMP中读取
	 * @param version
	 * @return
	 */
	public List<DictWord> getDictWordsByVersion(String version);
	
	/**
	 * 将KEYWORD_DICT表数据导入到KEYWORD_DICT_TEMP表中
	 * @return version
	 */
	public void bakupDictData(String version);
}
