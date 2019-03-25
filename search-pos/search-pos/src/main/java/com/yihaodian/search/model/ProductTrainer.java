package com.yihaodian.search.model;

import java.util.Date;

public class ProductTrainer {
	
	private String productCode;
	
	private String productName;
	
	private String categoryName;
	
	private String brandName;
	
	private String productTag;
	
	private String productSubTitle;
	
	private String brandWords;
	
	private String splitedWords;
	
	private String categoryWords;
	
	private Date createTime;
	
	private Date updateTime;
	
	private String userId;
	
	private Integer startPage;
	
	private Integer pAccount;

	public Integer getStartPage() {
		return startPage;
	}

	public void setStartPage(Integer startPage) {
		this.startPage = startPage;
	}

	public Integer getpAccount() {
		return pAccount;
	}

	public void setpAccount(Integer pAccount) {
		this.pAccount = pAccount;
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

	public String getBrandWords() {
		return brandWords;
	}

	public void setBrandWords(String brandWords) {
		this.brandWords = brandWords;
	}

	public String getSplitedWords() {
		return splitedWords;
	}

	public void setSplitedWords(String splitedWords) {
		this.splitedWords = splitedWords;
	}

	public String getCategoryWords() {
		return categoryWords;
	}

	public void setCategoryWords(String categoryWords) {
		this.categoryWords = categoryWords;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	
	
	
}
