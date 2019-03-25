package yooso.analyzer;

/* 逆向最大匹配分词器
 * 可配置参数: 是否进行细粒度全切分、是否将(x)扩展部分正规化(即一律以阿拉伯数字替换)
 * 
 * TODO: 
 *  *纯英文切词的优化. 
 *  *英文首字母大写的处理. 
 *  *(x)扩展部分正规化的处理，
 *  *混合字符串的细粒度全切分. 
 *  *细粒度全切分词库的自动生成
 *  待完善函数 getCnNumberChars(..)
 */

import java.io.*;
import java.util.*;

import yooso.analyzer.CharDictInfo.ChildWord;

public class ReverseSplitor {
  /** 初始化逆向最大匹配分词器. 
   * bSplitChilds 表示是否进行细粒度全切分
   * bXNumNormal  表示是否将(x)扩展部分正规化(即一律以阿拉伯数字替换)
   **/
  public ReverseSplitor(Dictionary dictionary, boolean bSplitChilds, boolean bXNumNormal) {
    this.dictionary = dictionary;
    this.bSplitChilds = bSplitChilds;
    this.bXNumNormal = bXNumNormal;
  }
  private Dictionary dictionary;
  private boolean bSplitChilds;
  private boolean bXNumNormal;

  /** 初始化测试分词器 */
  public ReverseSplitor(char[] value) {
    this.textChars = value;
  }
  
  /** 进行分词, 返回分词结果(逆向顺序) */
  public ArrayList<DictToken> doSplit(char[] value, int offset, int count) {
    this.textChars = value;

    int initialCapacity = count / 4;
  	if (initialCapacity < 10) initialCapacity = 10;
  	dictTokens = new ArrayList<DictToken>(initialCapacity);
  	
    split(offset + count -1, offset);
    return dictTokens;
  }
  
  private char[] textChars;
  private ArrayList<DictToken> dictTokens;

  /** 获取中文数字串(可能含小数点,负号).返回串终点,并将数字值存入numValue(Integer/Double) */
  public int getCnNumberChars(int firstOffset, int leftOffset, Object[] numValue) {
		if (cnDigitals == null) {
			cnDigitals = new HashMap<Integer, Integer>();
		  cnDigitals.put((int)'零', 0);
		  cnDigitals.put((int)'〇', 0);
		  cnDigitals.put((int)'Ｏ', 0);
		  cnDigitals.put((int)'两', 2);

		  cnDigitals.put((int)'一', 1);
		  cnDigitals.put((int)'二', 2);
		  cnDigitals.put((int)'三', 3);
		  cnDigitals.put((int)'四', 4);
		  cnDigitals.put((int)'五', 5);
		  cnDigitals.put((int)'六', 6);
		  cnDigitals.put((int)'七', 7);
		  cnDigitals.put((int)'八', 8);
		  cnDigitals.put((int)'九', 9);
		
		  cnDigitals.put((int)'壹', 1);
		  cnDigitals.put((int)'贰', 2);
		  cnDigitals.put((int)'叄', 3);
		  cnDigitals.put((int)'肆', 4);
		  cnDigitals.put((int)'伍', 5);
		  cnDigitals.put((int)'陆', 6);
		  cnDigitals.put((int)'柒', 7);
		  cnDigitals.put((int)'捌', 8);
		  cnDigitals.put((int)'玖', 9);
		
		  cnDigitals.put((int)'０', 0);
		  cnDigitals.put((int)'１', 1);
		  cnDigitals.put((int)'２', 2);
		  cnDigitals.put((int)'３', 3);
		  cnDigitals.put((int)'４', 4);
		  cnDigitals.put((int)'５', 5);
		  cnDigitals.put((int)'６', 6);
		  cnDigitals.put((int)'７', 7);
		  cnDigitals.put((int)'８', 8);
		  cnDigitals.put((int)'９', 9);
		}
	
    int n = firstOffset;
    while (n >= leftOffset) {
      char currChar = textChars[n];
      if (Dictionary.cnDigitalChars.containsKey(currChar)) {
        n--;
        continue;
      }
      break;
    }
    
    StringBuffer ts = new StringBuffer();
    ts.append(textChars, n+1, firstOffset-n);
    for (int i=ts.length()-1; i >= 0; i--) {
    	int currChar = ts.charAt(i);
    	Integer tnVal = cnDigitals.get(currChar);
    	if (tnVal == null)
    		ts.deleteCharAt(i);
    	else
    		ts.setCharAt(i, tnVal.toString().charAt(0));
    }
    numValue[0] = new Integer(ts.toString());
    return n+1;
  }
  private static HashMap<Integer, Integer> cnDigitals;

  /** 获取阿拉伯数字串(可能含小数点,负号).返回串终点,并将数字值存入numValue(Integer/Double) */
  public int getNumberChars(int firstOffset, int leftOffset, Object[] numValue) {
    boolean hasPoint = false;
    boolean hasMinus = false; //负号
    int n = firstOffset;
    while (n >= leftOffset) {
      char currChar = textChars[n];
      if ((currChar >= '0') && (currChar <= '9')) {
        n--;
        continue;
      }

      if (currChar == '.') {
        if (! hasPoint) {
          hasPoint = true;
          n--;
          continue;
        }
      }
      else if (currChar == '-') {
        hasMinus = true;
        n--;
      }
      break;
    }
    String ts = new String(textChars, n+1, firstOffset-n);
    if (hasPoint || hasMinus || ts.length()>9) //有负号一律作Double处理
      numValue[0] = new Double(ts);
    else
      numValue[0] = new Integer(ts);
    return n+1;
  }

  /** 逆向最大匹配词库中的词,未匹配上返回null**/
  private DictToken matchToken(int firstOffset, int leftOffset, CharDictInfo preInfo) {
    if (preInfo != null)
      if (preInfo.nextWords == null)
        leftOffset = 0x7fffffff; //该节点无后续节点,跳过循环匹配

    // 尽可能最大匹配
    while (firstOffset >= leftOffset) {
      char currChar = textChars[firstOffset];
      if (currChar <= ' ') break; //遇控制符,停止匹配

      CharDictMap dictMap = (preInfo == null ? dictionary : preInfo.nextWords);
      if ((currChar>='A')&&(currChar<='Z'))
   	   currChar += 'a'-'A';
      CharDictInfo currInfo_Base = dictMap.get(currChar);
      CharDictInfo currInfo_XNum = null;
      if (currChar < 128) {
        if (currInfo_Base != null) {
          // 非扩展词,连续纯阿拉伯部分的特殊处理
          if ((currChar >= '0') && (currChar <= '9')) {
            for (int n = firstOffset - 1; n >= leftOffset; n--) {
               char currChar2 = textChars[n];
               if (! ((currChar2 >= '0') && (currChar2 <= '9')) ) break;

               if (currInfo_Base.nextWords == null) {
                 currInfo_Base = null;
                 break;
               }
               currInfo_Base = currInfo_Base.nextWords.get(textChars[n]);
               if (currInfo_Base == null)break;
            }
            if (currInfo_Base != null)
              if (currInfo_Base.atomLength == 0) //未完全匹配词的连续纯数字部分
                currInfo_Base = null;
          }
          // 非扩展词,连续纯英文部分的特殊处理
          else if ( ((currChar>='a')&&(currChar<='z')) || ((currChar>='A')&&(currChar<='Z')) )
          {
            for (int n = firstOffset - 1; n >= leftOffset; n--) {
               char currChar2 = textChars[n];
               if (! ( ((currChar2>='a')&&(currChar2<='z')) || ((currChar2>='A')&&(currChar2<='Z')) ) ) break;

               if (currInfo_Base.nextWords == null) {
                 currInfo_Base = null;
                 break;
               }
               if ((currChar2>='A')&&(currChar2<='Z'))
            	   currChar2 += 'a'-'A';
               currInfo_Base = currInfo_Base.nextWords.get(currChar2);
               if (currInfo_Base == null)break;
            }
            if (currInfo_Base != null)
              if (currInfo_Base.atomLength == 0) //未完全匹配词的连续纯英文部分
                currInfo_Base = null;
          }
        }

        // 阿拉伯数字的(x)扩展匹配
        if ((currChar >= '0') && (currChar <= '9')) {
          // (x)扩展词按数值类型匹配
          Object[] numValues = new Object[1];
          int numOffset = getNumberChars(firstOffset, leftOffset, numValues);
          if (numValues[0] instanceof Double)
            currInfo_XNum = dictMap.get(CharDictInfo.CHARS_number);
          else
            currInfo_XNum = dictMap.get(CharDictInfo.CHARS_digital);
          if (currInfo_XNum != null) {
            currInfo_XNum.atomLength = firstOffset-numOffset+1;
            currInfo_XNum.numValue = numValues[0];
          }
        }
      }
      else {
        // 中文数字的(x)扩展匹配
        if (Dictionary.cnDigitalChars.containsKey(currChar)) {
          currInfo_XNum = dictMap.get(CharDictInfo.CHARS_cnNumber);
          if (currInfo_XNum != null) {
            Object[] numValues = new Object[1];
            int numOffset = getCnNumberChars(firstOffset, leftOffset, numValues);
            currInfo_XNum.atomLength = firstOffset-numOffset+1;
            currInfo_XNum.numValue = numValues[0];
          }
        }
      }

      // 此处未匹配上词,停止匹配
      if ((currInfo_Base == null) && (currInfo_XNum == null)) break;

      // 此处未匹配上(x)扩展词和非扩展词中的一种情况
      if ((currInfo_Base == null) || (currInfo_XNum == null)) {
        preInfo = (currInfo_Base != null ? currInfo_Base : currInfo_XNum);
        firstOffset -= (preInfo.atomLength==0 ? 1 : preInfo.atomLength);
        if (preInfo.nextWords == null) break; //该节点无后续节点,停止匹配
      }
      // 此处匹配上(x)扩展词和非扩展词两种情况, 分两路进行继续匹配, 以较长匹配作为结果返回
      else {
        int nextOffBase = firstOffset - (currInfo_Base.atomLength>0 ? currInfo_Base.atomLength : 1);
        DictToken tokenBase = matchToken(nextOffBase, leftOffset, currInfo_Base);
        int nextOffXNum = firstOffset - currInfo_XNum.atomLength;
        DictToken tokenXNum = matchToken(nextOffXNum, leftOffset, currInfo_XNum);

        if ((tokenBase != null) && (tokenXNum != null))
          return tokenBase.offset <= tokenXNum.offset ? tokenBase : tokenXNum;
        else
          return tokenBase != null ? tokenBase : tokenXNum;
      }
    }

    // 停止匹配后,进行回溯,已查找实际匹配上的词
    if (preInfo == null) return null;
    firstOffset += 1;
    while (preInfo.currWord == null) {
      firstOffset += (preInfo.atomLength > 0 ? preInfo.atomLength : 1);
      preInfo = preInfo.preCharInfo;
      if (preInfo == null) return null;
    }

    // 匹配上非扩展词,直接返回
    if (preInfo.wordXNum_offset == null)
      return new DictToken(firstOffset, preInfo.currWord.length(), preInfo);

    // 匹配上(x)扩展词,回溯计算各(x)的长度
    int[] wordXNum_length = new int[preInfo.wordXNum_offset.length];
    Object[] wordXNum_val = new Object[preInfo.wordXNum_offset.length];
    int x_index = 0, wordLength = 0;
    CharDictInfo tmpPreInfo = preInfo;
    while (tmpPreInfo != null) {
      wordLength += (tmpPreInfo.atomLength > 0 ? tmpPreInfo.atomLength : 1);
      if (tmpPreInfo.currChar <= ' ') {
        wordXNum_length[x_index] = tmpPreInfo.atomLength;
        wordXNum_val[x_index] = tmpPreInfo.numValue;
        x_index++;
      }
      tmpPreInfo = tmpPreInfo.preCharInfo;
    }
    DictToken retToken = new DictToken(firstOffset, wordLength, preInfo);
    retToken.wordXNum_length = wordXNum_length;
    retToken.wordXNum_val = wordXNum_val;
    return retToken;
  }



  /** 逆向获取连续的字符串(含符号字符, 例如:email、电话、产品型号等),返回串 */
  public DictToken getToken_cnNumber(int firstOffset, int leftOffset) {
    for (int i=firstOffset-1; i >= leftOffset; i--) {
      char currChar = textChars[i];
      if (Dictionary.cnDigitalChars.containsKey(currChar)) continue;
      return new DictToken(i+1, firstOffset - i, DictToken.TYPE_cnNumber);
    }
    return new DictToken(leftOffset, firstOffset - leftOffset +1, DictToken.TYPE_cnNumber);
  }

  /** 逆向获取连续的字符串(含符号字符, 例如:email、电话、产品型号等),返回串 */
  public DictToken getToken_en(int firstOffset, int leftOffset) {
    for (int i=firstOffset-1; i >= leftOffset; i--) {
      char currChar = textChars[i];
      if ( ((currChar>='a')&&(currChar<='z')) || ((currChar>='A')&&(currChar<='Z')) ) continue;

      if ((currChar <= ' ') || (currChar > 127))
        return new DictToken(i+1, firstOffset - i, DictToken.TYPE_enLetter);
      else if ((currChar >= '0') && (currChar <= '9'))
        return getMixtureTokenR(i-1, leftOffset, firstOffset);
      else if ((currChar == '.') || (currChar == '?')) {
        int wordLen = firstOffset - i;
        if (wordLen >= 3)
          if ( (textChars[i+1]>='A')&&(textChars[i+1]<='Z')
            && (textChars[i+2]>='a')&&(textChars[i+2]<='z')
            && (textChars[i+3]>='a')&&(textChars[i+3]<='z') )
           return new DictToken(i+1, firstOffset - i, DictToken.TYPE_enLetter);
        return getMixtureTokenR(i-1, leftOffset, firstOffset);
      }
      else if (Dictionary.mixtureChars.containsKey(currChar))
        return getMixtureTokenR(i-1, leftOffset, firstOffset);
      else
        return new DictToken(i+1, firstOffset - i, DictToken.TYPE_enLetter);
    }
    return new DictToken(leftOffset, firstOffset - leftOffset +1, DictToken.TYPE_enLetter);
  }

  /** 逆向获取连续的字符串(含符号字符, 例如:email、电话、产品型号等),返回串 */
  public DictToken getToken_digit(int firstOffset, int leftOffset) {
    for (int i=firstOffset-1; i >= leftOffset; i--) {
      char currChar = textChars[i];
      if ((currChar >= '0') && (currChar <= '9')) continue;

      if ((currChar <= ' ') || (currChar > 127))
        return new DictToken(i+1, firstOffset - i, DictToken.TYPE_digital);
      else if (currChar == '.') {
        if (i == leftOffset)
          return new DictToken(i, firstOffset - i+1, DictToken.TYPE_number);
        currChar = textChars[i-1];
        if ((currChar >= '0') && (currChar <= '9'))
          return getNumberTokenR(i-1, leftOffset, firstOffset);
        else
          return getMixtureTokenR(i-1, leftOffset, firstOffset);
      }
      else if (currChar == '-') {
        if (i == leftOffset)
          return new DictToken(i, firstOffset - i+1, DictToken.TYPE_number);
        currChar = textChars[i-1];
        if (Dictionary.mixtureChars.containsKey(currChar))
          return getMixtureTokenR(i-1, leftOffset, firstOffset);
        else
          return new DictToken(i, firstOffset - i+1, DictToken.TYPE_number);
      }
      else if (Dictionary.mixtureChars.containsKey(currChar))
        return getMixtureTokenR(i-1, leftOffset, firstOffset);
      else
        return new DictToken(i+1, firstOffset - i, DictToken.TYPE_digital);
    }
    return new DictToken(leftOffset, firstOffset - leftOffset +1, DictToken.TYPE_digital);
  }


  /** 逆向获取连续混合(数字\英文\-等)字符串.包括email\电话\产品型号等,返回串 */
  public DictToken getMixtureTokenR(int firstOffset, int leftOffset, int tokenRightOffset) {
    for (int i=firstOffset; i >= leftOffset; i--) {
      char currChar = textChars[i];
      if (Dictionary.mixtureChars.containsKey(currChar)) continue;
      if (currChar == ':') if (textChars[i+1] == '/') continue;

      return new DictToken(i+1, tokenRightOffset - i, DictToken.TYPE_mixture);
    }
    return new DictToken(leftOffset, tokenRightOffset - leftOffset +1, DictToken.TYPE_mixture);
  }

  /** 逆向获取连续的数字字串 */
  public DictToken getNumberTokenR(int firstOffset, int leftOffset, int tokenRightOffset) {
    for (int i=firstOffset-1; i >= leftOffset; i--) {
      char currChar = textChars[i];
      if ((currChar >= '0') && (currChar <= '9')) continue;

      if (currChar == '-')
        return new DictToken(i, tokenRightOffset - i +1, DictToken.TYPE_number);
      return new DictToken(i+1, tokenRightOffset - i, DictToken.TYPE_number);
    }
    return new DictToken(leftOffset, tokenRightOffset - leftOffset +1, DictToken.TYPE_number);
  }
  
  private final String CalcXNumToken(DictToken wordToken) {
  	CharDictInfo wordDictInfo = wordToken.wordDictInfo;
  	StringBuffer tsVal = new StringBuffer(wordDictInfo.currWord);
  	for (int i=wordDictInfo.wordXNum_offset.length-1; i >= 0; i--) {
  		int tn = wordDictInfo.wordXNum_offset[i];
  		String ts = wordToken.wordXNum_val[i].toString();
  		tsVal.replace(tn, tn+1, ts);
  	}
  	return tsVal.toString();
  }
  
  private final void addToken_Word(DictToken wordToken) {
  	dictTokens.add(wordToken);
  	CharDictInfo wordDictInfo = wordToken.wordDictInfo;
  	// (x)扩展词的正规化
  	if (bXNumNormal && (wordDictInfo.wordXNum_offset != null))
  		wordToken.tokenStr = CalcXNumToken(wordToken);
  	

    // 细粒度全切分的处理
    if (bSplitChilds && (wordDictInfo.wordChilds != null)) {
    	ChildWord[] wordChilds = wordDictInfo.wordChilds;
    	// 非扩展词
    	if (wordDictInfo.wordXNum_offset == null) {
    		for (int i=0; i < wordChilds.length; i++) {
    			DictToken tokenChild = new DictToken(wordToken.offset+wordChilds[i].offset, wordChilds[i].length, wordChilds[i].wordInfo);
        	dictTokens.add(tokenChild);
    		}
    	}
    	// (x)扩展词
    	else {
    		for (int i=0; i < wordChilds.length; i++) {
    			int childOffset = wordToken.offset + wordChilds[i].offset;
    			for (int k=0; k < wordChilds[i].leftXNumCount; k++)
  					childOffset += wordToken.wordXNum_length[k] -1;

    			DictToken tokenChild;
    			if (wordChilds[i].wordInfo == null)
      			tokenChild = new DictToken(childOffset, 1, DictToken.TYPE_char);
  				else if (wordChilds[i].wordInfo.wordXNum_offset == null)
      			tokenChild = new DictToken(childOffset, wordChilds[i].length, wordChilds[i].wordInfo);
    			else {
          	CharDictInfo childInfo = wordChilds[i].wordInfo;
      			int childLength = wordChilds[i].length;
      		  int[] wordXNum_length = new int[childInfo.wordXNum_offset.length];
      		  Object[] wordXNum_val = new Object[childInfo.wordXNum_offset.length];
      			for (int k=0; k < childInfo.wordXNum_offset.length; k++) {
      				int tnIndex = wordChilds[i].leftXNumCount + k;
    					childLength += wordToken.wordXNum_length[tnIndex] -1;
    					wordXNum_length[k] = wordToken.wordXNum_length[tnIndex];
    					wordXNum_val[k] = wordToken.wordXNum_val[tnIndex];
      			}
      			tokenChild = new DictToken(childOffset, childLength, wordChilds[i].wordInfo);
      			tokenChild.wordXNum_length = wordXNum_length;
      			tokenChild.wordXNum_val = wordXNum_val;

      	  	if (bXNumNormal && (wordChilds[i].wordInfo.wordXNum_offset != null))
      	  		tokenChild.tokenStr = CalcXNumToken(tokenChild);
    			}
        	dictTokens.add(tokenChild);
    		}
    	}
    }
  }
  
  // 添加混合字符串,含全切分的处理
  private final void addToken_mixture(DictToken token) {
  	int firstOffset = token.offset + token.length -1;
  	while (firstOffset >= token.offset) {
      char currChar = textChars[firstOffset];
      if ( ((currChar>='a')&&(currChar<='z')) || ((currChar>='A')&&(currChar<='Z')) ) {
      	int endOffset = firstOffset-1;
      	for (; endOffset >= token.offset; endOffset--) {
      		currChar = textChars[endOffset];
      		if (!( ((currChar>='a')&&(currChar<='z')) || ((currChar>='A')&&(currChar<='Z')) ))
      			break;
      	}
      	dictTokens.add(new DictToken(endOffset+1, firstOffset-endOffset, DictToken.TYPE_enLetter));
      	firstOffset = endOffset;
      }
      else firstOffset--;
  	}
  }
  
  // 添加英文词,含首字母大写的转换处理
  private final void addToken_EnLetter(DictToken token, int upperCharCount) {
    if ((upperCharCount == 1) && (token.length > 1)) {
    	char firstChar = textChars[token.offset];
    	if (firstChar>='A') //首字母大写的处理
    	{
    		StringBuffer tokenStr = new StringBuffer(token.length);
    		tokenStr.append((char)(firstChar+upperAddToLower));
    		tokenStr.append(textChars, token.offset+1, token.length-1);
    		token.tokenStr = tokenStr.toString();
    	}
    }
    dictTokens.add(token);
  }
  static final int upperAddToLower = 'a' - 'A';
  
  private void split(int firstOffset, int leftOffset) {
    while (firstOffset >= leftOffset) {
      char currChar = textChars[firstOffset];

      // 跳过控制符
      if (currChar <= ' ') {
        firstOffset--;
        continue;
      }

      // 纯英文词的优化
      if ((currChar < 128) && ((currChar < '0') || (currChar > '9'))) {
      	int symbolOffset = -1; //两个单词间的标点符号位置
      	int upperCharCount=0, upperCharCount2=0;
      	boolean isEnWord = true; //此连续字符串是否为纯英文单词(含一个标点符号的也算)
      	int n = firstOffset;
        for (; n >= leftOffset; n--) {
          char currChar2 = textChars[n];
          if ((currChar2>='a')&&(currChar2<='z'))
          	continue;
          else if ((currChar2>='A')&&(currChar2<='Z')) {
          	if (symbolOffset < 0)
          		upperCharCount++;
          	else
          		upperCharCount2++;
          	continue;
          }
          else if (currChar2 <= ' ')
          	break;
          else if ( (symbolOffset < 0) && (currChar2 < 128) 
          		&& ((currChar2<'0') || (currChar2>'9')) ) {
          	symbolOffset = n;
          	continue;
          }
          else {
          	isEnWord = false;
          	break;
          }
        }

        if (isEnWord) {
        	if (symbolOffset < 0) {
	            DictToken token = new DictToken(n+1, firstOffset-n, DictToken.TYPE_enLetter);
	            addToken_EnLetter(token, upperCharCount);
        	}
        	else {
        		// 匹配词库中的词
        	  CharDictInfo wordDictInfo = dictionary.findCharInfo(null, textChars, firstOffset, firstOffset-n);
        	  if ( (wordDictInfo != null) && (wordDictInfo.currWord != null) ) {
        	  	DictToken wordToken = new DictToken(n+1, firstOffset-n, wordDictInfo);
        	  	addToken_Word(wordToken);
        	  	firstOffset = n;
        	  	continue;
        	  }
        	  
        	  // 中间含混合串连接字符,全切分时作为一种结果
        	  if (bSplitChilds && (symbolOffset > n+1) && (symbolOffset < firstOffset))
        	  	if (Dictionary.mixtureChars.containsKey(textChars[symbolOffset]))
                dictTokens.add(new DictToken(n+1, firstOffset-n, DictToken.TYPE_mixture));

        	  // 添加英文词和符号
        	  if (symbolOffset < firstOffset)
        	  	addToken_EnLetter(new DictToken(symbolOffset+1, firstOffset-symbolOffset, DictToken.TYPE_enLetter), upperCharCount);
        	  dictTokens.add(new DictToken(symbolOffset, 1, DictToken.TYPE_char));
        	  if (symbolOffset > n+1)
        	  	addToken_EnLetter(new DictToken(n+1, symbolOffset-1-n, DictToken.TYPE_enLetter), upperCharCount2);
        	}

          firstOffset = n;
          continue;
        }
      }

      // 词库匹配
      DictToken wordToken = matchToken(firstOffset, leftOffset, null);
      if (wordToken != null) {
      	addToken_Word(wordToken);
        firstOffset = wordToken.offset -1;
        continue;
      }

      // 单字/英文/数字的处理
      if (currChar < 128) {
        if ((currChar >= '0') && (currChar <= '9')) {
          DictToken token = getToken_digit(firstOffset, leftOffset);
          dictTokens.add(token);
          if (bSplitChilds && (token.type == DictToken.TYPE_mixture))
          	addToken_mixture(token);
          firstOffset = token.offset -1;
        }
        else if ( ((currChar>='a')&&(currChar<='z')) || ((currChar>='A')&&(currChar<='Z')) ) {
          DictToken token = getToken_en(firstOffset, leftOffset);
          dictTokens.add(token);
          if (bSplitChilds && (token.type == DictToken.TYPE_mixture))
          	addToken_mixture(token);
          firstOffset = token.offset -1;
        }
        else {
          DictToken tokenChar = new DictToken(firstOffset, 1, DictToken.TYPE_char);
          dictTokens.add(tokenChar);
          firstOffset--;
        }
      }
      else {
        if (Dictionary.cnDigitalChars.containsKey(currChar)) {
          DictToken tokenCnNumber = getToken_cnNumber(firstOffset, leftOffset);
          dictTokens.add(tokenCnNumber);
          firstOffset = tokenCnNumber.offset -1;
        }
        else {
          DictToken tokenChar = new DictToken(firstOffset, 1, DictToken.TYPE_char);
          dictTokens.add(tokenChar);
          firstOffset--;
        }
      }
    }
  }


  static void test_getContinueTokenR() throws Exception {
    String[] testData = new String[] {
       "it automatically creates the desktop window.The",
       "Nokia K750",
       "email:dgwingsky@163.com",
       "tel:(010)3344568-12",
       "abc0.35",
       "asdf.00cba",
       "国标GB2312"
    };
    for (int i=0; i < testData.length; i++) {
      String testStr = testData[i];
      char[] testChars = testStr.toCharArray();

      int nowCharType;
      char currChar = testChars[testChars.length-1];
      if ((currChar >= '0') && (currChar <= '9'))
        nowCharType = CharDictInfo.CHARS_digital;
      else if ( ((currChar>='a')&&(currChar<='z')) || ((currChar>='A')&&(currChar<='Z')) )
        nowCharType = CharDictInfo.CHARS_number;
      else if (Dictionary.cnDigitalChars.containsKey(currChar))
        nowCharType = CharDictInfo.CHARS_cnNumber;
      else
        nowCharType = 0;

      DictToken testToken = null;//getAdvanceTokenR(testChars, testChars.length-1, 0, (char)nowCharType);

      System.out.println("STR: " + testStr);
      System.out.println("Token: type=" + testToken.type +"    value=" + new String(testChars, testToken.offset, testToken.length));
    }
  }


/*
  public ReverseTokenStream(Dictionary dictionary, Reader textReader) throws IOException {
    this(dictionary, readerToBuffer(textReader));
  }

  public ReverseTokenStream(Dictionary dictionary, String text) {
    this(dictionary, text.toCharArray());
  }
*/
  public static char[] readerToBuffer(Reader textReader) throws IOException {
    StringBuffer strBuffer = new StringBuffer();
    char[] bufChars = new char[1024];
    int readLen = textReader.read(bufChars);
    while (readLen > 0) {
      strBuffer.append(bufChars, 0, readLen);
      readLen = textReader.read(bufChars);
    }
    char[] strChars = new char[strBuffer.length()];
    strBuffer.getChars(0, strChars.length, strChars, 0);
    return strChars;
  }

  //***  以下为测试代码 *******************************
  static void test_split_file(String fileName, ReverseSplitor splitor) throws Exception {
    String testStr = Dictionary.LoadFileText(new FileInputStream(fileName));
    char[] testChars = testStr.toCharArray();

    long time0 = System.currentTimeMillis();
    ArrayList<DictToken> dictTokens = splitor.doSplit(testChars, 0, testChars.length);
    long time1 = System.currentTimeMillis();

    System.out.println("******* FILE: "+fileName+" *************************************");
    System.out.println("*** CharsCount=" + testChars.length);
    System.out.println("*** TokensCount=" + dictTokens.size());
    System.out.println("*** Time=" + (time1-time0) +" ms");
    if ((time1-time0) > 0) {
      long tnRate = testChars.length * (long)1000 / (time1-time0);
      StringBuffer tsRate = new StringBuffer((new Long(tnRate)).toString());
      if (tsRate.length() > 4)
      	tsRate.insert(tsRate.length()-4, ',');
      System.out.println("*** Rate=" + tsRate +" char/s");
    }

    if (testChars.length > 1024 * 5) return;
    System.out.println("******* TOKENS ***********************************************");
    for (int i=dictTokens.size()-1; i >= 0; i--) {
      DictToken dictToken = (DictToken) dictTokens.get(i);
      String tokenStr;
      if (dictToken.tokenStr != null)
      	tokenStr = dictToken.tokenStr;
      else
      	tokenStr = new String(testChars, dictToken.offset, dictToken.length);

      System.out.print(tokenStr);
      System.out.print("    TokenType=" + dictToken.type);
      System.out.print("    offset=[" + dictToken.offset +' '+ (dictToken.offset+dictToken.length-1) +']');
      if (dictToken.wordXNum_length != null) {
        System.out.print("    XNum_len=");
        for (int k=0; k < dictToken.wordXNum_length.length; k++) {
          if (k > 0) System.out.print(",");
          System.out.print(dictToken.wordXNum_length[k]);
        }
      }
      if (dictToken.wordXNum_val != null) {
        System.out.print("    XNum_val=");
        for (int k=0; k < dictToken.wordXNum_val.length; k++) {
          if (k > 0) System.out.print(",");
          System.out.print(dictToken.wordXNum_val[k]);
        }
      }
      System.out.println();
    }
  }
  static void test_split() throws Exception {
    Dictionary dictionary = new Dictionary(true);
    dictionary.AddDict(new FileInputStream("D:/EclipseWork/testdata/Test_Base.dict"), false);
    dictionary.AddDict(new FileInputStream("D:/EclipseWork/testdata/Test_XNum.dict"), true);
    dictionary.AddDict_Child(new FileInputStream("D:/EclipseWork/testdata/Test_Base.dict"), false);
    dictionary.AddDict_Child(new FileInputStream("D:/EclipseWork/testdata/Test_XNum.dict"), true);
    ReverseSplitor splitor = new ReverseSplitor(dictionary, true, true);

    //test_split_file("D:/EclipseWork/testdata/Test_file_en.text", splitor);
    test_split_file("D:/EclipseWork/testdata/Test_file_enLarge.txt", splitor);
    //test_split_file("D:/EclipseWork/testdata/Test_file_cn.text", splitor);
    test_split_file("D:/EclipseWork/testdata/Test_file_cnLarge.text", splitor);
  }

  // 全切分词库自动生成
  static void GernerateChildsDict_Filter(String[] dictWords, HashMap<String, String> waitingWords, int filterLen) {
    for (int i = 0; i < dictWords.length; i++) {
      String strWord = dictWords[i];
      int tn = strWord.indexOf(' ');
      if (tn > 0)
        strWord = strWord.substring(0, tn);
      else if (tn == 0)
        throw new RuntimeException("词库的每行左边不能有空格");

      if (strWord.length() == filterLen)
      	waitingWords.put(strWord, "");
    }
  }
  static void GernerateChildsDict(String inputFile, String outputFile, boolean includeCharToken) throws Exception {
    Dictionary dictionary = new Dictionary(true);
    dictionary.AddDict(new FileInputStream(inputFile), false);
    ReverseSplitor splitor = new ReverseSplitor(dictionary, false, false);
    
    String[] dictWords = Dictionary.LoadFileLines(new FileInputStream(inputFile));
    HashMap<String, String> waitingWords = new HashMap<String, String>();
    for (int i = 3; i <= dictionary.wordsMaxLen; i++)
    	GernerateChildsDict_Filter(dictWords, waitingWords, i);
    Iterator<String> itWords = waitingWords.keySet().iterator();
    while (itWords.hasNext()) {
    	String tsWord = itWords.next();
    	CharDictInfo wordDictInfo = dictionary.findCharInfo(tsWord);
    	if (wordDictInfo == null)
    		throw new RuntimeException("词(" + tsWord + ")未在词典中.");
    	if (wordDictInfo.currWord == null)
    		throw new RuntimeException("词(" + tsWord + ")未在词典中.");
    	if (wordDictInfo.wordChilds != null)
    		throw new RuntimeException("词(" + tsWord + ")已有全切分信息.");
    	
    	//计算全切分信息
    	wordDictInfo.currWord = null; //临时去除该词
    	char[] wordChars = tsWord.toCharArray();
    	ArrayList<DictToken> wordTokens = splitor.doSplit(wordChars, 0, wordChars.length);
    	ArrayList<CharDictInfo.ChildWord> wordChilds = new ArrayList<CharDictInfo.ChildWord>();
    	boolean hasWordToken = false;
    	for (int i=wordTokens.size()-1; i >= 0; i--) {
    		DictToken wordToken = wordTokens.get(i);
    		if (wordToken.type == DictToken.TYPE_word) {
    			hasWordToken = true;
    			CharDictInfo.ChildWord wordChild = new CharDictInfo.ChildWord();
    			wordChild.wordInfo = wordToken.wordDictInfo;
    			wordChild.offset = wordToken.offset;
    			wordChild.length = wordToken.length;
    			wordChilds.add(wordChild);
    			
    			ChildWord[] childChilds = wordToken.wordDictInfo.wordChilds;
    			if (childChilds != null) {
    				for (int k=0; k < childChilds.length; k++) {
    					wordChild = new CharDictInfo.ChildWord();
        			wordChild.wordInfo = childChilds[k].wordInfo;
        			wordChild.offset = wordToken.offset + childChilds[k].offset;
        			wordChild.length = childChilds[k].length;
        			wordChilds.add(wordChild);
    				}
    			}
    		}
    		else if (includeCharToken && (wordToken.length == 1)) {
    			CharDictInfo.ChildWord wordChild = new CharDictInfo.ChildWord();
    			wordChild.wordInfo = null;
    			wordChild.offset = wordToken.offset;
    			wordChild.length = 1;
    			wordChilds.add(wordChild);
    		}
    	}
    	if (hasWordToken) {
      	ChildWord[] aWordChilds = new ChildWord[wordChilds.size()];
      	wordChilds.toArray(aWordChilds);
      	wordDictInfo.wordChilds = aWordChilds;
    	}
    	
    	wordDictInfo.currWord = tsWord;
    }

    // 写结果到文件
    StringBuffer fileData = new StringBuffer();
    itWords = waitingWords.keySet().iterator();
    while (itWords.hasNext()) {
    	String tsWord = itWords.next();
    	CharDictInfo wordDictInfo = dictionary.findCharInfo(tsWord);
    	if (wordDictInfo.wordChilds != null) {
      	fileData.append(tsWord);
      	char[] wordChars = tsWord.toCharArray();
      	for (int i=0; i < wordDictInfo.wordChilds.length; i++) {
      		CharDictInfo.ChildWord wordChild = wordDictInfo.wordChilds[i];
      		fileData.append(' ');
      		fileData.append(new String(wordChars, wordChild.offset, wordChild.length));
      	}
      	fileData.append("\r\n");
    	}
    }
    OutputStream outputStream = new FileOutputStream(outputFile);
    byte[] fileBinData = fileData.toString().getBytes("UTF-8");
    outputStream.write(fileBinData);
    outputStream.close();
  }
  
  static void test_getNumberChars() throws Exception {
    String[] tsa = new String[]{".12", "-.12", "3.55", "2.3.55", "a.12", "-b.12", "3.c-55", "2.3.d55"};
    for (int i=0; i < tsa.length; i++) {
      char[] tcs = tsa[i].toCharArray();
      ReverseSplitor sp = new ReverseSplitor(tcs);
      Object[] val = new Object[1];
      int offset = sp.getNumberChars(tcs.length-1, 0, val);

      System.out.print(tsa[i]);
      System.out.print("    offset=" + offset);
      System.out.print("    valType=" + val[0].getClass().getName());
      System.out.print("    val=" + val[0]);
      System.out.println();
    }
  }

  static void test_getCnNumberChars() throws Exception {
    String[] tsa = new String[]{"一九七八", "三千二百四十五", "二十二万", "这是五十三"};
    for (int i=0; i < tsa.length; i++) {
      char[] tcs = tsa[i].toCharArray();
      ReverseSplitor sp = new ReverseSplitor(tcs);
      Object[] val = new Object[1];
      int offset = sp.getCnNumberChars(tcs.length-1, 0, val);

      System.out.print(tsa[i]);
      System.out.print("    offset=" + offset);
      System.out.print("    valType=" + val[0].getClass().getName());
      System.out.print("    val=" + val[0]);
      System.out.println();
    }
  }

  public static void main(String[] args) {
    try {
      //test_getNumberChars();
    	//test_getCnNumberChars();
      //test_getContinueTokenR();
      test_split();
    	//GernerateChildsDict("D:/EclipseWork/testdata/Test_Base.dict", "D:/EclipseWork/testdata/Test_BaseChilds.dict", true);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
