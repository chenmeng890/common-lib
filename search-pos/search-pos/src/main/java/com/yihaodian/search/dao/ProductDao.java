package com.yihaodian.search.dao;

import java.util.List;
import java.util.Map;

import com.yihaodian.search.model.Product;

public interface ProductDao {
	
	public List<Product> getListProductByProductCodes(Map obj)throws Exception;
}
