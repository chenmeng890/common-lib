package com.yihaodian.search.nlp.help;

import java.util.HashMap;
import java.util.Map;

/**
 * Latin文字帮助类
 * @author yuqian
 *
 */
public class Latin {
	public static final char LATIN_START = '\u0020';

	public static final char LATIN_END = '\u007E';

	public static final char FULLWIDTH_OFFSET = '\uFF00' - LATIN_START;

	public static final char DIGIT_ZERO = '\u0030';

	public static final char DIGIT_NINE = '\u0039';

	public static final char LATIN_CAPITAL_LETTER_A = '\u0041';

	public static final char LATIN_CAPITAL_LETTER_Z = '\u005A';

	public static final char LATIN_SMALL_LETTER_A = '\u0061';

	public static final char LATIN_SMALL_LETTER_Z = '\u007A';

	public static boolean isLatin(int c) {
		return c >= LATIN_START && c <= LATIN_END;
	}

	public static boolean isLatin1(char c) {
		return (c >= '\u00C0' && c <= '\u00FF');
	}

	public static boolean isFullwidth(int c) {
		c -= FULLWIDTH_OFFSET;
		return isLatin(c);
	}

	public static boolean isDigit(int c) {
		return c >= DIGIT_ZERO && c <= DIGIT_NINE;
	}

	public static boolean isFullwidthDigit(int c) {
		c -= FULLWIDTH_OFFSET;
		return isDigit(c);
	}

	public static boolean isInteger(String s) {
		for (int i = 0; i < s.length(); i++) {
			if (!isDigit(s.charAt(i))){
				return false;
			}
		}
		return true;
	}

	public static boolean isFullwidthInteger(String s) {
		for (int i = 0; i < s.length(); i++) {
			if (!isFullwidthDigit(s.charAt(i))){
				return false;
			}
		}
		return true;
	}

	public static boolean isLetter(int c) {
		return (c >= LATIN_CAPITAL_LETTER_A && c <= LATIN_CAPITAL_LETTER_Z)
				|| (c >= LATIN_SMALL_LETTER_A && c <= LATIN_SMALL_LETTER_Z);
	}

	public static boolean isFullwidthLetter(int c) {
		c -= FULLWIDTH_OFFSET;
		return isLetter(c);
	}

	public static boolean isLatinString(String s, boolean includeFullwidth) {
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (isLatin(c) || (includeFullwidth && isFullwidth(c))){
				continue;
			}
			return false;
		}
		return true;
	}

	public static Map<Character, Character> charMap() {
		Map<Character, Character> ret = new HashMap<Character, Character>();
		for (int c = LATIN_START; c <= LATIN_END; c++){
			ret.put((char) (c + FULLWIDTH_OFFSET), (char) c);
		}
		return ret;
	}
	
	/**
	 * 判断是否CJK字符
	 * @param input
	 * @return
	 */
	public static boolean isCJKCharacter(char input){
		return Character.UnicodeBlock.of(input) 
					== Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS;
	}
	
	/**
	 * 进行字符规格化（全角转半角，大写转小写处理）
	 * @param input
	 * @return char
	 */
	public static char regularize(char input){
        if (input == 12288) {
            input = (char) 32;
            
        }else if (input > 65280 && input < 65375) {
            input = (char) (input - 65248);
            
        }else if (input >= 'A' && input <= 'Z') {
        	input += 32;
		}
        
        return input;
	}
	/**
	 * 全角转半角
	 * @param input
	 * @return
	 */
	public static String toDBC(String input) {
	     char c[] = input.toCharArray();
	          for (int i = 0; i < c.length; i++) {
	            if (c[i] == '\u3000') {
	              c[i] = ' ';
	            } else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
	              c[i] = (char) (c[i] - 65248);
	            }
	          }
	          return new String(c);
	 }
	
}