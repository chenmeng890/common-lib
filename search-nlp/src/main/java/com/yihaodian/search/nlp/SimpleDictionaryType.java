package com.yihaodian.search.nlp;

/**
 * 词元Type常量
 * @author yuqian
 *
 */
public class SimpleDictionaryType {

	/**
	 *  1	分类
	  	2	品牌
		3	属性
		4	型号
		5	规格
		6	组合词
		8	人群
		7	促销词
		9	包装
		10	比例
		11	部位
		12	材质
		13	产地
		14	成份
		15	尺寸
		16	功率
		17	季节
		18	年龄
		19	容积
		20	时间
		21	速度
		22	图案
		23	外观
		24	线长
		25	形状
		26	颜色
		27	语言
		28	直径
		29	重量
		99	其他
 */
	public static final int TYPE_BRAND = 0;
	
    public static final int TYPE_CATEGORY = 1;
    
    //阿拉伯数字
    public static final int TYPE_NUM = 2;
    //英文
    public static final int TYPE_LETTER = 3;
    //量词
    public static final int TYPE_QUANTIFIER = 90; 
    //停词
    public static final int TYPE_STOPWORDS = 91;
    //敏感词
    public static final int TYPE_ILLEGAL = 92;
}
