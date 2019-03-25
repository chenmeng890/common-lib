package yooso.analyzer;

public class CharDictInfo {
  public final static char CHARS_digital = (char)12; //纯阿拉伯数字串(无小数点且无负号)
  public final static char CHARS_cnNumber = (char)13; //纯中文数字串
  public final static char CHARS_number = (char)14; //阿拉伯数字串(有小数点或负号)

  public char currChar;
  public CharDictInfo preCharInfo; //上一节点指针. 当本节点为纯连续部分的终点时,该指针指向起点的上一节点(跳过中间节点)
  public CharDictMap nextWords; //下一节点(子节点)词汇
  public int atomLength; //当本节点为纯连续部分的终点时,保存纯连续部分的长度. 当为可变节点时,分词时临时保存可变部分的长度
  public Object numValue; //当为可变节点时,分词时临时保存可变部分的数值(Integer/Double)

  // 以下成员仅用于成词节点(即本节点已形成一个完整的词).
  public String currWord; //!=null为成词节点. 该值为AddWord时传入的表示,(x)已替换为单个内码
  public int[] wordXNum_offset; //为(x)扩展词时, 各个(x)在currWord中的位置
  public Object wordProp; //词的附加属性
  public ChildWord[] wordChilds; //细粒度全切分时,该词包含的细粒度词

  public static class ChildWord {
    public CharDictInfo wordInfo; //该细分词的词典信息.单字此项为null
    public int offset, length; //该细分词在长词中相对currWord的位置
    public int leftXNumCount; //该细分词在长词中位置左边的(x)扩展个数
  }
}
