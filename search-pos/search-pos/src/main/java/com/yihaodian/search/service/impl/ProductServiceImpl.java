package com.yihaodian.search.service.impl;

import java.util.List;
import java.util.Map;

import com.yihaodian.search.dao.ProductDao;
import com.yihaodian.search.model.Product;
import com.yihaodian.search.nlp.dao.DictWordDao;
import com.yihaodian.search.nlp.help.ExportDict;
import com.yihaodian.search.nlp.model.DictWord;
import com.yihaodian.search.service.ProductService;
import com.yihaodian.search.util.UtilFuction;

public class ProductServiceImpl implements ProductService {
	
	private ProductDao productDao;
	
	private DictWordDao dictwordDao;
	
	private String dictPath;
	
	private String basePath;
	
	public List<Product> getListProductByProductCodes(Map obj){
		try{
			return productDao.getListProductByProductCodes(obj);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public void exportWordDict() throws Exception{
		List<DictWord> words=dictwordDao.getDictWords(0);
		ExportDict ex= new ExportDict(dictPath);
		ex.prepare();
		ex.writeDict(words);
		
	}

	public ProductDao getProductDao() {
		return productDao;
	}

	public void setProductDao(ProductDao productDao) {
		this.productDao = productDao;
	}

	public DictWordDao getDictwordDao() {
		return dictwordDao;
	}

	public void setDictwordDao(DictWordDao dictwordDao) {
		this.dictwordDao = dictwordDao;
	}

	public String getDictPath() {
		return dictPath;
	}

	public void setDictPath(String dictPath) {
		UtilFuction.createDirectory(dictPath);
		this.dictPath = dictPath;
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}
	
	
}
