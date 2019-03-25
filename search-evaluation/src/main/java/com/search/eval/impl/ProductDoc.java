package com.search.eval.impl;

import com.search.eval.SearchDoc;
import com.yihaodian.mandy.model.Product;

/**
 * mandy result
 * 
 */
public class ProductDoc implements SearchDoc {

	/**
	 * target doc
	 */
	Product product;

	public ProductDoc() {

	}

	public ProductDoc(Product product) {
		this.product = product;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	@Override
	public String getId() {
		return "" + product.getPmInfoId();
	}

	@Override
	public String getName() {
		return product.getCnName();
	}

	@Override
	public String toString() {
		return String.format("%-10d %s", product.getPmInfoId(),
				product.getCnName());
	}
}
