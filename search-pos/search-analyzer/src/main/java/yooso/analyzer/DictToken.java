package yooso.analyzer;

public class DictToken {
  public final static int TYPE_word = 1; //词库中的词(含固定词和(x)扩展词)
  public final static int TYPE_char = 2; //各种单个字符或符号(英文\阿拉伯数字\中文数字除外)

  public final static int TYPE_enLetter = 11; //纯英文字母
  public final static int TYPE_digital = 12; //纯阿拉伯数字串(无小数点且无负号)
  public final static int TYPE_cnNumber = 13; //纯中文数字串
  public final static int TYPE_number = 14; //阿拉伯数字串(有小数点或负号)
  public final static int TYPE_mixture = 20; //连续混合(数字\英文\-等)字符串.包括email\电话\产品型号等

  public DictToken(int offset, int length, CharDictInfo wordDictInfo) {
    this.offset = offset;
    this.length = length;
    this.type = TYPE_word;
    this.wordDictInfo = wordDictInfo;
  }

  public DictToken(int offset, int length, int type) {
    this.offset = offset;
    this.length = length;
    this.type = type;
    this.wordDictInfo = null;
  }

  public int offset, length, type;
  public String tokenStr; //一般为null, 仅当结果与原文有差异的情况有值(英文首字母大写、(x)扩展词正规化)
  public CharDictInfo wordDictInfo;
  public int[] wordXNum_length; //为(x)扩展词时, 各个(x)实际匹配内容的length
  public Object[] wordXNum_val; //为(x)扩展词时, 各个(x)实际匹配内容的值(Integer/Double)
}
