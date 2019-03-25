package com.yihaodian.search.service;

import java.util.List;
import java.util.Map;

import com.yihaodian.search.model.Product;

public interface ProductService {
	
	public void setDictPath(String dictPath);
	
	public void setBasePath(String basePath);
	
	public String getBasePath();
	
	public List<Product> getListProductByProductCodes(Map obj);
	
	public void exportWordDict()throws Exception;
}
