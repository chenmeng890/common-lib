package com.yihaodian.search;

import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;

import com.ibatis.sqlmap.client.SqlMapClient;

public class DemoServiceImpl implements DemoService {
	private SqlMapClient sqlMapClient;

	protected SqlMapClient getSqlMapClient() {
		return sqlMapClient;
	}

	public void setSqlMapClient(SqlMapClient sqlMapClient) {
		this.sqlMapClient = sqlMapClient;
	}
	
	@Override
	public int maxBrandId() {
		try {
			return ((Integer)getSqlMapClient().queryForObject("maxBrandId")).intValue();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String getDataSourceClass() {
		return getSqlMapClient().getDataSource().getClass().getName();
	}

}
