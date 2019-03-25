package yooso.analyzer;

import java.io.*;
import java.util.*;

public class Dictionary extends CharDictMap {
  public Dictionary(boolean forReverse) {
    this.forReverse = forReverse;
  }
  private boolean forReverse; // 本词典是否用于逆向分词
  public int wordsMaxLen = 2; // 词典中最长的词的长度

  /** 添加基本词到分词词库 */
  public CharDictInfo AddWord_Base(String currWord) {
    return AddWord_I(currWord);
  }

  /** 添加(x)扩展词到分词词库 */
  public ArrayList<CharDictInfo> AddWord_XNum(String currWord) {
      ArrayList<String> expandedWords = Expand_XNumWord(currWord);

      //取其中一个样例,用于计算各个(x)在CharDictInfo.currWord中的位置
      char[] currChars = ((String) expandedWords.get(0)).toCharArray();
      ArrayList<Integer> xnumOffsets = new ArrayList<Integer>();
      for (int i=0; i < currChars.length; i++) {
        if (currChars[i] <= ' ')
          xnumOffsets.add(i);
      }
      int[] wordXNum_offset = new int[xnumOffsets.size()];
      for (int i=0; i < wordXNum_offset.length; i++)
        wordXNum_offset[i] = xnumOffsets.get(i);

      //扩展后的词,逐个添加到分词词库
      ArrayList<CharDictInfo> retList = new ArrayList<CharDictInfo>();
      for (int n=0; n < expandedWords.size(); n++) {
        CharDictInfo charInfo = AddWord_I((String) expandedWords.get(n));
        charInfo.wordXNum_offset = wordXNum_offset;
        retList.add(charInfo);
      }
      return retList;
  }

  private CharDictInfo AddWord_I(String currWord) {
    if (currWord.length() < 2)
      throw new RuntimeException("词库中的词长度至少为2.   " + currWord);
    char[] currChars = (forReverse ? (new StringBuffer(currWord)).reverse().toString().toCharArray()
                        : currWord.toCharArray());
    if (currChars.length > wordsMaxLen)
      wordsMaxLen = currWord.length();

    // 按树形结构逐字逐层插入到词库
    CharDictInfo atomPreInfo = null;
    int atomStartOff = -1;
    CharDictInfo preCharInfo = null;
    for (int k = 0; k < currChars.length; k++) {
      char currChar = currChars[k];
      if ((currChar>='A')&&(currChar<='Z'))
      	   currChar += 'a'-'A';
      CharDictMap currDictMap;
      if (k == 0)
        currDictMap = this;
      else {
        if (preCharInfo.nextWords == null)
          preCharInfo.nextWords = new CharDictMap();
        currDictMap = preCharInfo.nextWords;
      }

      CharDictInfo currCharInfo = currDictMap.get(currChar);
      if (currCharInfo == null) {
        currCharInfo = new CharDictInfo();
        currCharInfo.currChar = currChar;
        currCharInfo.preCharInfo = preCharInfo;
        currDictMap.put(currChar, currCharInfo);
      }

      //连续纯英文/连续纯阿拉伯部分的特殊处理
      boolean isAtomEnd;
      if ((currChar >= '0') && (currChar <= '9')) {
        if (atomStartOff == -1) {
          atomPreInfo = preCharInfo;
          atomStartOff = k;
        }

        if (k == currChars.length -1)
          isAtomEnd = true;
        else {
          char nextChar = currChars[k+1];
          isAtomEnd = ! ((nextChar >= '0') && (nextChar <= '9'));
        }
      }
      else if ( ((currChar>='a')&&(currChar<='z')) || ((currChar>='A')&&(currChar<='Z')) ) {
        if (atomStartOff == -1) {
          atomPreInfo = preCharInfo;
          atomStartOff = k;
        }

        if (k == currChars.length -1)
          isAtomEnd = true;
        else {
          char nextChar = currChars[k+1];
          isAtomEnd = ! ( ((nextChar>='a')&&(nextChar<='z')) || ((nextChar>='A')&&(nextChar<='Z')) );
        }
      }
      else isAtomEnd = false;

      if (isAtomEnd) {
        currCharInfo.preCharInfo = atomPreInfo;
        currCharInfo.atomLength = k - atomStartOff +1;
        atomPreInfo = null;
        atomStartOff = -1;
      }
      
      preCharInfo = currCharInfo;
    }

    preCharInfo.currWord = currWord;
    return preCharInfo;
  }

  /** 添加词的细粒度全切分信息(即指定一个较长词,再如何切分为多个词)到分词词库 */
  public void AddWord_Child(String currWord, boolean isXNumWord, String[] childWords) {
      for (int n=0; n < childWords.length; n++) {
        String childWord = childWords[n];
        int childWordOffset = currWord.indexOf(childWord);
        if (childWordOffset < 0)
          throw new RuntimeException("词{" + currWord + "}的细分词{" + childWord + "}无效");
      }

      // 非扩展词的处理
      if (! isXNumWord) {
        CharDictInfo currWordInfo = findCharInfo(currWord);
        if (currWordInfo == null)
          throw new RuntimeException("词{"+currWord+")在词典中未定义");

        CharDictInfo.ChildWord[] childWordsInfo = new CharDictInfo.ChildWord[childWords.length];
        for (int n=0; n < childWords.length; n++) {
          String childWord = childWords[n];
          int childWordOffset = currWord.indexOf(childWord);
          if (childWordOffset < 0)
            throw new RuntimeException("词{"+currWord+"}的细分词{"+childWord+"}无效");

          childWordsInfo[n] = new CharDictInfo.ChildWord();
          childWordsInfo[n].offset = childWordOffset;
          childWordsInfo[n].length = childWord.length();
          if (childWordsInfo[n].length != 1) {
            CharDictInfo childWordInfo = findCharInfo(childWord);
            if (childWordInfo == null)
              throw new RuntimeException("词{"+currWord+"}的细分词{"+childWord+")在词典中未定义");
            childWordsInfo[n].wordInfo = childWordInfo;
          }
        }
        currWordInfo.wordChilds = childWordsInfo;
      }
      // 扩展词的处理
      else {
        ArrayList/*String*/ currWordExpands = Expand_XNumWord(currWord);
        ArrayList/*String*/[] childWordsExpands = new ArrayList[childWords.length];
        CharDictInfo.ChildWord[] childWordsInfo = new CharDictInfo.ChildWord[childWords.length];

        // 子串不含扩展的处理
        String currWordFix = (String) currWordExpands.get(0); //用于无扩展子串,仅为其中一个样例
        for (int n=0; n < childWords.length; n++) {
          String childWord = childWords[n];
          if (childWord.indexOf('(') < 0) {
            childWordsInfo[n] = new CharDictInfo.ChildWord();
            childWordsInfo[n].offset = currWordFix.indexOf(childWord);
            childWordsInfo[n].length = childWord.length();
            if (childWordsInfo[n].length != 1) {
              CharDictInfo childWordInfo = findCharInfo(childWord);
              if (childWordInfo == null)
                throw new RuntimeException("词{"+currWord+"}的细分词{"+childWord+"}在词典中未定义");
              childWordsInfo[n].wordInfo = childWordInfo;
            }

            int leftXNumCount = 0;
            for (int x=childWordsInfo[n].offset-1; x >= 0; x--) {
            	char xChar = currWordFix.charAt(x);
            	if (xChar <= ' ') leftXNumCount++;
            }
            childWordsInfo[n].leftXNumCount = leftXNumCount;
          }
          else childWordsExpands[n] = Expand_XNumWord(childWord);
        }

        // 子串含扩展的处理
        for (int k=0; k < currWordExpands.size(); k++) {
          String currWordExp = (String)currWordExpands.get(k);
          CharDictInfo currWordInfo = findCharInfo(currWordExp);
          if (currWordInfo == null)
            throw new RuntimeException("词{"+currWord+"}在词典中未定义");

          for (int n=0; n < childWords.length; n++) {
            if (childWordsExpands[n] == null) continue;

            String childWordExp = null;
            for (int m=0; m < childWordsExpands[n].size(); m++) {
              childWordExp = (String) childWordsExpands[n].get(m);
              if (currWordExp.indexOf(childWordExp) >= 0) break;
            }

            childWordsInfo[n] = new CharDictInfo.ChildWord();
            childWordsInfo[n].offset = currWordExp.indexOf(childWordExp);
            childWordsInfo[n].length = childWordExp.length();
            if (childWordsInfo[n].length != 1) {
              CharDictInfo childWordInfo = findCharInfo(childWordExp);
              if (childWordInfo == null)
                throw new RuntimeException("词{"+currWord+"}的细分词{"+childWords[n]+"}在词典中未定义");
              childWordsInfo[n].wordInfo = childWordInfo;

              int leftXNumCount = 0;
              for (int x=childWordsInfo[n].offset-1; x >= 0; x--) {
              	char xChar = currWordExp.charAt(x);
              	if (xChar <= ' ') leftXNumCount++;
              }
              childWordsInfo[n].leftXNumCount = leftXNumCount;
            }
          }
          currWordInfo.wordChilds = childWordsInfo;
        }
      }
  }

  /** 将带(x)的词转换为词库中的格式(一个将扩展为多个)
   * (n) 纯阿拉伯数字
   * (N) 纯中文数字
   * (M) 纯阿拉伯数字、纯中文数字
   * (x) 阿拉伯数字(可含小数点)
   * (X) 阿拉伯数字(可含小数点)、纯中文数字
   **/
  public ArrayList<String> Expand_XNumWord(String currWord) {
    int tnPos0 = currWord.indexOf('(');
    if (tnPos0 < 0)
      throw new RuntimeException("XNumWord词缺少左括号");
    int tnPos1 = currWord.indexOf(')', tnPos0+1);
    if (tnPos1 < 0)
      throw new RuntimeException("XNumWord词缺少右括号");
    if (tnPos1 - tnPos0 != 2)
      throw new RuntimeException("XNumWord词括号之间必须仅单个字符");
    char xChar = currWord.charAt(tnPos0+1);

    char[] specialChars;
    switch (xChar) {
      case 'n':
        specialChars = new char[]{CharDictInfo.CHARS_digital};
        break;
      case 'N':
        specialChars = new char[]{CharDictInfo.CHARS_cnNumber};
        break;
      case 'M':
        specialChars = new char[]{CharDictInfo.CHARS_digital, CharDictInfo.CHARS_cnNumber};
        break;
      case 'x':
        specialChars = new char[]{CharDictInfo.CHARS_digital, CharDictInfo.CHARS_number};
        break;
      case 'X':
        specialChars = new char[]{CharDictInfo.CHARS_digital, CharDictInfo.CHARS_number, CharDictInfo.CHARS_cnNumber};
        break;
      default:
        throw new RuntimeException("XNumWord词括号之间字符非法 " + xChar);
    }

    StringBuffer bufCurrWord = new StringBuffer(currWord);
    bufCurrWord.delete(tnPos0, tnPos1);

    ArrayList<String> retList = new ArrayList<String>();
    boolean hasNextXNum = (currWord.indexOf('(', tnPos1+1) >= 0);
    for (int i=0; i < specialChars.length; i++) {
      bufCurrWord.setCharAt(tnPos0, specialChars[i]);
      if (hasNextXNum) {
        ArrayList<String> expandedWords = Expand_XNumWord(bufCurrWord.toString());
        retList.addAll(expandedWords);
      }
      else
        retList.add(bufCurrWord.toString());
    }
    return retList;
  }

  /** 添加词典库(无附加属性,无细粒度切分标识) */
  public void AddDict(InputStream dictStream, boolean isXNumWord) throws IOException {
    String[] dictWords = LoadFileLines(dictStream);
    for (int i = 0; i < dictWords.length; i++) {
      String strWord = dictWords[i];
      int tn = strWord.indexOf(' ');
      if (tn > 0)
        strWord = strWord.substring(0, tn);
      else if (tn == 0)
        throw new RuntimeException("词库的每行左边不能有空格");
      
      if (strWord.length() < 2)
          throw new RuntimeException("词库中的词长度至少为2.   " + dictWords[i]);

      if (isXNumWord)
        AddWord_XNum(strWord);
      else
        AddWord_Base(strWord);
    }
  }

  /** 加载词典的细粒度全切分信息(即指定一个较长词,再如何切分为多个词)
   *  词典格式为每行一个长词, 然后为单空格隔开的多个短词
   *  此调用必须在loadDict_Base之后进行
   **/
  public void AddDict_Child(InputStream dictStream, boolean isXNumWord) throws IOException {
    String[] dictWords = LoadFileLines(dictStream);
    for (int i = 0; i < dictWords.length; i++) {
      String strLine = dictWords[i];
      int tn = strLine.indexOf(' ');
      if (tn < 0) continue;
      if (tn == 0)
        throw new RuntimeException("词库的每行左边不能有空格");

      String currWord = strLine.substring(0, tn);
      String[] childWords = strLine.substring(tn+1).split(" ");
      AddWord_Child(currWord, isXNumWord, childWords);
    }
  }

  /** 查找词在词典中的信息 */
  public CharDictInfo findCharInfo(String currWord) {
    char[] currChars = currWord.toCharArray();
    return findCharInfo(null, currChars, this.forReverse ? currChars.length-1 : 0, currChars.length);
  }

  /** 在之前字符(preCharInfo)基础上,继续查找多个字符(startOffset为查找起点,按this.forReverse方向进行) */
  public CharDictInfo findCharInfo(CharDictInfo preCharInfo, char[] chars, int startOffset, int length) {
    CharDictMap dictMap = (preCharInfo == null ? this : preCharInfo.nextWords);
    if (this.forReverse) {
      for (int i=0; i < length; i++) {
        if (dictMap == null) return null;
        char currChar = chars[startOffset];
        if ((currChar>='A')&&(currChar<='Z'))
        	currChar += 'a'-'A'; 
        preCharInfo = dictMap.get(currChar);
        if (preCharInfo == null) return null;
        dictMap = preCharInfo.nextWords;
        startOffset--;
      }
    }
    else {
      for (int i=0; i < length; i++) {
        if (dictMap == null) return null;
        char currChar = chars[startOffset];
        if ((currChar>='A')&&(currChar<='Z'))
        	currChar += 'a'-'A'; 
        preCharInfo = dictMap.get(currChar);
        if (preCharInfo == null) return null;
        dictMap = preCharInfo.nextWords;
        startOffset++;
      }
    }
    return preCharInfo;
  }

  // 注意: 该函数的传入参数为xx.class.getResourceAsStream时,有bug. 请改用LoadFileLines
  static String LoadFileText(InputStream stream) throws IOException {
    byte[] binData = new byte[stream.available()];
    stream.read(binData);
    
    int offset = 0;
    if (binData.length > 3)
    	if ((binData[0] == -17) && (binData[1] == -69) && (binData[2] ==-65)) //UTF-8编码前缀
    		offset = 3;
    return new String(binData, offset, binData.length-offset, "UTF-8");
  }

  static String[] LoadFileLines(InputStream stream) throws IOException {
	ArrayList<String> list = new ArrayList<String>(stream.available() / 8);
	BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
	String strWord = reader.readLine();
	while(strWord != null) {
		list.add(strWord);
		strWord = reader.readLine();
	}
	String[] arrayWords = new String[list.size()];
	list.toArray(arrayWords);
	return arrayWords;
  }
  
  // 初始化特殊字符库
  public static CharDictMap cnDigitalChars = new CharDictMap();
  public static CharDictMap mixtureChars = new CharDictMap();
  static {
    for (char tc='0'; tc <= '9'; tc++)
      mixtureChars.put(tc, null);
    for (char tc='a'; tc <= 'z'; tc++)
      mixtureChars.put(tc, null);
    for (char tc='A'; tc <= 'Z'; tc++)
      mixtureChars.put(tc, null);
    mixtureChars.put('.', null);
    mixtureChars.put('?', null);
    mixtureChars.put('/', null);
    mixtureChars.put('%', null);
    mixtureChars.put('-', null);
    mixtureChars.put('_', null);
    mixtureChars.put('+', null);
    //mixtureChars.put('*', null);
    mixtureChars.put('#', null);
    mixtureChars.put('&', null);
    mixtureChars.put('@', null);
    /*mixtureChars.put('(', null);
    mixtureChars.put(')', null);*/
    mixtureChars.put('[', null);
    mixtureChars.put(']', null);
    mixtureChars.put('<', null);
    mixtureChars.put('>', null);

    cnDigitalChars.put('零', null);
    cnDigitalChars.put('〇', null);
    cnDigitalChars.put('Ｏ', null);
    cnDigitalChars.put('两', null);

    cnDigitalChars.put('一', null);
    cnDigitalChars.put('二', null);
    cnDigitalChars.put('三', null);
    cnDigitalChars.put('四', null);
    cnDigitalChars.put('五', null);
    cnDigitalChars.put('六', null);
    cnDigitalChars.put('七', null);
    cnDigitalChars.put('八', null);
    cnDigitalChars.put('九', null);

    cnDigitalChars.put('十', null);
    cnDigitalChars.put('百', null);
    cnDigitalChars.put('千', null);
    cnDigitalChars.put('万', null);
    cnDigitalChars.put('亿', null);

    cnDigitalChars.put('壹', null);
    cnDigitalChars.put('贰', null);
    cnDigitalChars.put('叄', null);
    cnDigitalChars.put('肆', null);
    cnDigitalChars.put('伍', null);
    cnDigitalChars.put('陆', null);
    cnDigitalChars.put('柒', null);
    cnDigitalChars.put('捌', null);
    cnDigitalChars.put('玖', null);

    cnDigitalChars.put('拾', null);
    cnDigitalChars.put('佰', null);
    cnDigitalChars.put('仟', null);
    cnDigitalChars.put('萬', null);

    cnDigitalChars.put('０', null);
    cnDigitalChars.put('１', null);
    cnDigitalChars.put('２', null);
    cnDigitalChars.put('３', null);
    cnDigitalChars.put('４', null);
    cnDigitalChars.put('５', null);
    cnDigitalChars.put('６', null);
    cnDigitalChars.put('７', null);
    cnDigitalChars.put('８', null);
    cnDigitalChars.put('９', null);
  }

  //***  以下为测试代码 *******************************
  static void test_loadDictionary() throws Exception {
    Dictionary dictionary = new Dictionary(true);

    //dictionary.AddDict(new FileInputStream("D:/EclipseWork/testdata/Test_Base.dict"), false);
    //dictionary.debugPrint();

    dictionary.AddWord_Base("现在");
    dictionary.AddDict(new FileInputStream("D:/EclipseWork/testdata/Test_XNum.dict"), true);
    dictionary.AddDict_Child(new FileInputStream("D:/EclipseWork/testdata/Test_XNum.dict"), true);
    dictionary.debugPrint();
  }

 public void debugPrint() {
   System.out.println("*** Dictionary debugPrint ************");
   debugPrintDictMap(this, 0);
 }

 public static void printObjPtr(Object obj) {
   if (obj == null) {
     System.out.print("null");
     return;
   }
   String ts = obj.toString();
   int tn = ts.lastIndexOf('@');
   if (tn > 0)
     ts = ts.substring(tn);
   System.out.print(ts);
 }

 static void debugPrintDictMap(CharDictMap dictMap, int treeLevel) {
   CharDictMap.Iterator iterator = dictMap.getIterator();
   while (iterator.next()) {
     char currChar = iterator.getCurrentKey();
     CharDictInfo currCharInfo = iterator.getCurrentValue();

     for (int i=0; i < treeLevel; i++)
       System.out.print("  ");
     if (currChar <= ' ')
       System.out.print("?"+(int)currChar);
     else
       System.out.print(currChar);

     if (currCharInfo.currWord != null) {
       StringBuffer tsCurrWord = new StringBuffer(currCharInfo.currWord);
       for (int n=tsCurrWord.length()-1; n >= 0; n--) {
         char tnChar = tsCurrWord.charAt(n);
         if (tnChar <= ' ')
           tsCurrWord.replace(n, n+1, "["+(int)tnChar+"]");
       }
       System.out.print("  currWord=" + tsCurrWord);
       if (currCharInfo.wordProp != null)
    	   System.out.print("  currWordProp=" + currCharInfo.wordProp);
     }
     if (currCharInfo.atomLength != 0)
  	   System.out.print("  atomLength=" + currCharInfo.atomLength);

     System.out.print("  nextWords=");
     if (currCharInfo.nextWords == null)
       System.out.print("null");
     else
       System.out.print(currCharInfo.nextWords.size());

     if (currCharInfo.wordChilds != null) {
       System.out.print("  currWordChilds=");
       for (int n=0; n < currCharInfo.wordChilds.length; n++) {
         CharDictInfo.ChildWord childWord = currCharInfo.wordChilds[n];
         if (n > 0)
           System.out.print(",");
         System.out.print(childWord.length==1 ?
                          currCharInfo.currWord.substring(childWord.offset, childWord.offset+1)
                          : childWord.wordInfo.currWord
         );
       }
     }

     /*
     System.out.print("  this=");
     printObjPtr(currCharInfo);
     System.out.print("  preCharInfo=");
     printObjPtr(currCharInfo.preCharInfo);
     */

     if (currCharInfo.wordXNum_offset != null) {
       System.out.print("  wordXNum_offset=");
       for (int n=0; n < currCharInfo.wordXNum_offset.length; n++) {
         if (n > 0)
           System.out.print(",");
         System.out.print(currCharInfo.wordXNum_offset[n]);
       }
     }

     System.out.println();
     if (currCharInfo.nextWords != null)
       debugPrintDictMap(currCharInfo.nextWords, treeLevel+1);
   }
 }

  public static void main(String[] args) {
    try {
      test_loadDictionary();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
