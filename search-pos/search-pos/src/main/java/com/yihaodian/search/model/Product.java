package com.yihaodian.search.model;

public class Product implements java.io.Serializable {
	
	private static final long serialVersionUID = 8377987318026722502L;

	private long id;
	
	private String code;
	
	private String name;
	
	private String productCode;
	
	private String productName;
	
	private String categoryName;
	
	private String brandName;
	
	private String productTag;
	
	private String productSubTitle;
	
	private String brandWords;
	
	private String categoryWords;
	
	private String splitedWords;

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

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getBrandName() {
		return brandName;
	}

	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}

	public String getProductTag() {
		return productTag;
	}

	public void setProductTag(String productTag) {
		this.productTag = productTag;
	}

	public String getProductSubTitle() {
		return productSubTitle;
	}

	public void setProductSubTitle(String productSubTitle) {
		this.productSubTitle = productSubTitle;
	}
	
	
}
