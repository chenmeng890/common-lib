package com.yihaodian.search.dao.impl;

import java.util.List;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.yihaodian.search.dao.ProductTrainerDao;
import com.yihaodian.search.model.ProductTrainer;

public class ProductTrainerDaoImpl extends SqlMapClientDaoSupport implements ProductTrainerDao {
	
	public void insertProductTrainer(ProductTrainer obj){
		try{
			getSqlMapClientTemplate().insert("insert-producttrainer",obj);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void updateProductTrainer(ProductTrainer obj) {
		try{
			getSqlMapClientTemplate().update("update-producttrainer",obj);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void deleteProductTrainer(ProductTrainer obj) {
		try{
			getSqlMapClientTemplate().delete("delete-producttrainer",obj);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public List<ProductTrainer> getListProductTrainer(ProductTrainer obj) {
		try{
			List<ProductTrainer> list = getSqlMapClientTemplate().queryForList("select-producttrainer-by-obj", obj);
			return list;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public Long getAcountByObj(ProductTrainer obj){
		try{
			return (Long)getSqlMapClientTemplate().queryForObject("select-count-by-obj", obj);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public void insertExecuteBatch(List<ProductTrainer> list){
		try{
			SqlMapClient smc = getSqlMapClientTemplate().getSqlMapClient();
//			smc.startTransaction();
			smc.startBatch();
			for(ProductTrainer pt :list){
				smc.insert("insert-producttrainer",pt);
			}
			smc.executeBatch();
//			smc.endTransaction();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void updateExecuteBatch(List<ProductTrainer> list){
		try{
			SqlMapClient smc = getSqlMapClientTemplate().getSqlMapClient();
//			smc.startTransaction();
			smc.startBatch();
			for(ProductTrainer pt :list){
				smc.update("update-producttrainer",pt);
			}
			smc.executeBatch();
//			smc.endTransaction();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
