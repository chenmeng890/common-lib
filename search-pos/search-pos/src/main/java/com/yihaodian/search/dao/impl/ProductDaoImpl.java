package com.yihaodian.search.dao.impl;

import java.util.List;
import java.util.Map;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.yihaodian.search.dao.ProductDao;
import com.yihaodian.search.model.Product;

public class ProductDaoImpl extends SqlMapClientDaoSupport implements ProductDao {

	public List<Product> getListProductByProductCodes(Map obj)throws Exception {
		try{
			List<Product> list = getSqlMapClientTemplate().queryForList("select-product-productcodes", obj);
			return list;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

}
