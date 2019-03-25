package com.yihaodian.search.catwordr;

import java.util.ArrayList;

/**
 * 字符串辅助函数
 */
public class StringUtil {
	/**
	 * 字符串分拆
	 * @param str
	 * @param splitChar 分隔符
	 * @return
	 */
	public static String[] split(String str, char splitChar) {
		ArrayList<String> result = new ArrayList<String>();
		int posBegin = 0;
		while (true) {
			int posCurr = str.indexOf(splitChar, posBegin);
			if (posCurr < 0) {
				result.add(str.substring(posBegin, str.length()));
				break;
			}
			result.add(str.substring(posBegin, posCurr));
			posBegin = posCurr +1;
		}
		String[] aResult = new String[result.size()];
		result.toArray(aResult);
		return aResult;
	}
	
	/**
	 * 判断strChild是否为strParent的子串(比较时忽略大小写)
	 * @param strParent
	 * @param strChild
	 * @return
	 */
	public static boolean findSubIgnoreCase(String strParent, String strChild) {
		return strParent.toLowerCase().indexOf(strChild.toLowerCase()) >= 0;
	}
}
