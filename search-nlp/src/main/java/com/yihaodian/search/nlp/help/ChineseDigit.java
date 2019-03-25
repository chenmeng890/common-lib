package com.yihaodian.search.nlp.help;

import java.util.HashMap;

/**
 * 中文数字转化器
 * @author yuqian
 *
 */
public class ChineseDigit {
	
	
	public static final HashMap<Character,Integer> CHINESE_DIDIT_MAP  = new HashMap<Character,Integer>();
	
	public static final HashMap<Character,Integer> CHINESE_DIDIT_MAP_1  = new HashMap<Character,Integer>();
	
	static {
		CHINESE_DIDIT_MAP_1.put('O', 0);
		CHINESE_DIDIT_MAP_1.put('零', 0);
		CHINESE_DIDIT_MAP_1.put('一', 1);
		CHINESE_DIDIT_MAP_1.put('壹', 1);
		CHINESE_DIDIT_MAP_1.put('二', 2);
		CHINESE_DIDIT_MAP_1.put('贰', 2);
		CHINESE_DIDIT_MAP_1.put('两', 2);
		CHINESE_DIDIT_MAP_1.put('三', 3);
		CHINESE_DIDIT_MAP_1.put('叁', 3);
		CHINESE_DIDIT_MAP_1.put('四', 4);
		CHINESE_DIDIT_MAP_1.put('肆', 4);
		CHINESE_DIDIT_MAP_1.put('五', 5);
		CHINESE_DIDIT_MAP_1.put('伍', 5);
		CHINESE_DIDIT_MAP_1.put('六', 6);
		CHINESE_DIDIT_MAP_1.put('陆', 6);
		CHINESE_DIDIT_MAP_1.put('七', 7);
		CHINESE_DIDIT_MAP_1.put('柒', 7);
		CHINESE_DIDIT_MAP_1.put('八', 8);
		CHINESE_DIDIT_MAP_1.put('捌', 8);
		CHINESE_DIDIT_MAP_1.put('九', 9);
		CHINESE_DIDIT_MAP_1.put('玖', 9);
		CHINESE_DIDIT_MAP_1.put('十', 10);
		CHINESE_DIDIT_MAP_1.put('拾', 10);
	}
	
	static {
		CHINESE_DIDIT_MAP.put('O', 0);
		CHINESE_DIDIT_MAP.put('零', 0);
		CHINESE_DIDIT_MAP.put('一', 1);
		CHINESE_DIDIT_MAP.put('壹', 1);
		CHINESE_DIDIT_MAP.put('二', 2);
		CHINESE_DIDIT_MAP.put('贰', 2);
		CHINESE_DIDIT_MAP.put('两', 2);
		CHINESE_DIDIT_MAP.put('三', 3);
		CHINESE_DIDIT_MAP.put('叁', 3);
		CHINESE_DIDIT_MAP.put('四', 4);
		CHINESE_DIDIT_MAP.put('肆', 4);
		CHINESE_DIDIT_MAP.put('五', 5);
		CHINESE_DIDIT_MAP.put('伍', 5);
		CHINESE_DIDIT_MAP.put('六', 6);
		CHINESE_DIDIT_MAP.put('陆', 6);
		CHINESE_DIDIT_MAP.put('七', 7);
		CHINESE_DIDIT_MAP.put('柒', 7);
		CHINESE_DIDIT_MAP.put('八', 8);
		CHINESE_DIDIT_MAP.put('捌', 8);
		CHINESE_DIDIT_MAP.put('九', 9);
		CHINESE_DIDIT_MAP.put('玖', 9);
		CHINESE_DIDIT_MAP.put('十', 10);
		CHINESE_DIDIT_MAP.put('拾', 10);
		CHINESE_DIDIT_MAP.put('百', 100);
		CHINESE_DIDIT_MAP.put('佰', 100);
		CHINESE_DIDIT_MAP.put('千', 1000);
		CHINESE_DIDIT_MAP.put('仟', 1000);
		CHINESE_DIDIT_MAP.put('万', 10000);
		CHINESE_DIDIT_MAP.put('萬', 10000);
	}
	
	/**
	 * 判断是否中文数字
	 * @param c
	 * @return
	 */
	public static boolean isDigit(char c) {
		return CHINESE_DIDIT_MAP.containsKey(c);
	}
	
	/**
	 * 将一个字符串转为阿拉伯数字
	 * @param buf
	 * @param length
	 * @return
	 */
	public static int parseDigit(char[] buf,int length) {
		if(length == 1) {
			Integer c = CHINESE_DIDIT_MAP_1.get(buf[0]);
			if(c!=null) {return c;}
			else {return 0;}
		}
		int d = 0;
		int d3 = 0;
		int d0 = 0;
		for(int i=0;i<length;i++) {
			Integer c = CHINESE_DIDIT_MAP.get(buf[i]);
			if(c==null){
				continue;
			}
			if(c<10) {
				d0 = d0*10 + c;
			}
			else if(c<10000){
				d3 += c * (d0==0?1:d0);
				d0 = 0;
			} else {
				d = (d3+d0) * c;
				d3 = 0;
				d0 = 0;
			}
		}
		return d+d3+d0;
	}
}
