package com.yihaodian.search.nlp.dao.impl;

import java.util.List;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.yihaodian.search.nlp.dao.DictWordDao;
import com.yihaodian.search.nlp.model.DictWord;

public class DictWordDaoImpl extends SqlMapClientDaoSupport implements DictWordDao{

	@SuppressWarnings("unchecked")
	public List<DictWord> getDictWords(int pageNum) {
		return this.getSqlMapClientTemplate().queryForList("getDictWord", pageNum);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DictWord> getDictWordsByVersion(String version) {
		return this.getSqlMapClientTemplate().queryForList("getDictWordsByVersion", version);
	}

	@Override
	public void bakupDictData(String version) {
		this.getSqlMapClientTemplate().delete("dropDictTemp");
		this.getSqlMapClientTemplate().insert("insertDictTemp", version);
	}

}
