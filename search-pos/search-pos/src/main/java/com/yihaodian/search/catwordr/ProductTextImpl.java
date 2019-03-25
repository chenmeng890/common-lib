package com.yihaodian.search.catwordr;

import java.util.List;

import com.yihaodian.search.nlp.ProductText;

/**
 * 产品文本信息
 */
public class ProductTextImpl implements ProductText {

	private String productName;
	private String brandName;
	private String categoryName;
	private String nameSubtitle;
	private String productCode;
	private List<String> attributeValues;
	
	private String brandWords;
	private String categoryWords;
	private String splitedWords;
	
	private String productTag;
	public ProductTextImpl() {
	}
	
	public void setProductTag(String productTag) {
		this.productTag = productTag;
	}

	public ProductTextImpl(ProductText p) {
		this.productName = p.getProductName();
		this.brandName = p.getBrandName();
		this.categoryName = p.getCategoryName();
		this.nameSubtitle = p.getNameSubtitle();
		this.productCode = p.getProductCode();
		this.attributeValues = p.getAttributeValues();
		
		if (p instanceof ProductTextImpl) {
			ProductTextImpl pi = (ProductTextImpl)p;
			this.brandWords = pi.brandWords;
			this.categoryWords = pi.categoryWords;
			this.splitedWords = pi.splitedWords;
		}
	}
	
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public String getBrandName() {
		return brandName;
	}
	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}
	public String getCategoryName() {
		return categoryName;
	}
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	public String getNameSubtitle() {
		return nameSubtitle;
	}
	public void setNameSubtitle(String nameSubtitle) {
		this.nameSubtitle = nameSubtitle;
	}
	public String getProductCode() {
		return productCode;
	}
	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}
	
	public List<String> getAttributeValues() {
		return attributeValues;
	}
	public void setAttributeValues(List<String> attributeValues) {
		this.attributeValues = attributeValues;
	}
	public String getBrandWords() {
		return brandWords;
	}
	public void setBrandWords(String brandWords) {
		this.brandWords = brandWords;
	}
	public String getCategoryWords() {
		return categoryWords;
	}
	public void setCategoryWords(String categoryWords) {
		this.categoryWords = categoryWords;
	}
	public String getSplitedWords() {
		return splitedWords;
	}
	public void setSplitedWords(String splitedWords) {
		this.splitedWords = splitedWords;
	}
	public String getProductTag() {
		return this.productTag;
	}
	
}
