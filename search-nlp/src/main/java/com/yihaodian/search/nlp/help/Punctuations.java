package com.yihaodian.search.nlp.help;

import static java.lang.Character.UnicodeBlock.*;

import java.lang.Character.UnicodeBlock;

/**
 * 标点帮助类
 * @author yuqian
 *
 */
public class Punctuations {
	

	public static boolean isASCIIPunctuation(int c) {
		return Latin.isLatin(c) && (!Latin.isDigit(c) && !Latin.isLetter(c));
	}

	public static boolean isLatin1Punctuation(int c) {
		return (c >= '\u00A0' && c <= '\u00BF');
	}

	public static boolean isGeneralPunctuation(UnicodeBlock b) {
		return GENERAL_PUNCTUATION == b;
	}

	public static boolean isSupplementalPunctuation(int c) {
		return (c >= '\u2E00' && c <= '\u2E7F');
	}

	public static boolean isCJKPunctuation(UnicodeBlock b) {
		return CJK_SYMBOLS_AND_PUNCTUATION == b;
	}

	public static boolean isFullwidthASCIIPunctuation(UnicodeBlock b) {
		return HALFWIDTH_AND_FULLWIDTH_FORMS == b;
	}

	public static boolean isVerticalForm(int c) {
		return (c >= '\uFE10' && c <= '\uFE1F');
	}

	public static boolean isPunctuation(int c) {
		if (isASCIIPunctuation(c) || isLatin1Punctuation(c)
				|| isSupplementalPunctuation(c) || isVerticalForm(c)){
			return true;
		}
		UnicodeBlock b = UnicodeBlock.of(c);
		return isGeneralPunctuation(b) || isCJKPunctuation(b)
				|| isFullwidthASCIIPunctuation(b);
	}


	public static boolean isPunctuation(String s) {
		return s.length() == 1 && isPunctuation(s.charAt(0));
	}
}
