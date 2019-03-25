package com.yihaodian.search.nlp;

import java.util.List;

public interface ProductText {
	String getProductName();   //产品名称
	String getBrandName();     //品牌名称
	String getCategoryName();   //类别名称(category_search_name字段内容格式)
	String getNameSubtitle();  //产品附标题
	String getProductCode();   //产品编码
	List<String> getAttributeValues();   //产品属性值(中文值,每个属性值之间空格间隔)
	String getProductTag();
}
