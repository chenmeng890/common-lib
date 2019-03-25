package com.yihaodian.search.service.impl;

import java.util.List;

import com.yihaodian.search.dao.ProductTrainerDao;
import com.yihaodian.search.model.ProductTrainer;
import com.yihaodian.search.service.ProductTrainerService;
import com.yihaodian.search.util.Constant;

public class ProductTrainerServiceImpl implements ProductTrainerService {
	
	private ProductTrainerDao productTrainerDao;
	
	@Override
	public void insertProductTrainer(ProductTrainer obj) {
		try{
			productTrainerDao.insertProductTrainer(obj);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void updateProductTrainer(ProductTrainer obj) {
		try{
			productTrainerDao.updateProductTrainer(obj);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void deleteProductTrainer(ProductTrainer obj) {
		try{
			productTrainerDao.deleteProductTrainer(obj);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public List<ProductTrainer> getListProductTrainer(ProductTrainer obj) {
		try{
			return productTrainerDao.getListProductTrainer(obj);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public Long getAcountByObj(ProductTrainer obj){
		try{
			return productTrainerDao.getAcountByObj(obj);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public void insertExecuteBatch(List<ProductTrainer> list){
		try{
			if(list!=null && list.size()>0){
				int count = list.size();
				int shang = count/Constant.pageSize;
				int mod = count%Constant.pageSize;
				if(mod!=0){
					shang++;
				}
				for(int i = 0; i<shang;i++){
					int start = i*Constant.pageSize;
					int end = (i+1)*Constant.pageSize;
					if(end>count){
						end = count;
					}
					List<ProductTrainer> listNew = list.subList(start, end);
					productTrainerDao.insertExecuteBatch(listNew);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void updateExecuteBatch(List<ProductTrainer> list){
		try{
			if(list!=null && list.size()>0){
				int count = list.size();
				int shang = count/Constant.pageSize;
				int mod = count%Constant.pageSize;
				if(mod!=0){
					shang++;
				}
				for(int i = 0; i<shang;i++){
					int start = i*Constant.pageSize;
					int end = (i+1)*Constant.pageSize;
					if(end>count){
						end = count;
					}
					List<ProductTrainer> listNew = list.subList(start, end);
					productTrainerDao.updateExecuteBatch(listNew);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public ProductTrainerDao getProductTrainerDao() {
		return productTrainerDao;
	}

	public void setProductTrainerDao(ProductTrainerDao productTrainerDao) {
		this.productTrainerDao = productTrainerDao;
	}
	
	

}
