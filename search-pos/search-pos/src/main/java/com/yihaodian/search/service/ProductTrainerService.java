package com.yihaodian.search.service;

import java.util.List;

import com.yihaodian.search.model.ProductTrainer;


public interface ProductTrainerService {
	
	public void insertProductTrainer(ProductTrainer obj);
	
	public void updateProductTrainer(ProductTrainer obj);
	
	public void deleteProductTrainer(ProductTrainer obj);
	
	public List<ProductTrainer> getListProductTrainer(ProductTrainer obj);
	
	public Long getAcountByObj(ProductTrainer obj);
	
	public void insertExecuteBatch(List<ProductTrainer> list);
	
	public void updateExecuteBatch(List<ProductTrainer> list);

}