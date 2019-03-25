package com.yihaodian.search.nlp.segment;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.yihaodian.search.nlp.help.Latin;
import com.yihaodian.search.nlp.help.Punctuations;
import com.yihaodian.search.nlp.model.Lexeme;

/**
 * 对拉丁文字符串进行切分，把英文和阿拉伯数字分开
 * @author yuqian
 *
 */
public class LatinSplitor {

	public static final String Sign_Letter_Connector ="-_.@&'#";//英文连接符号
	public static final Set<Character> englishConnectChars = new HashSet<Character>();
	static{
		char[] ca = Sign_Letter_Connector.toCharArray();
		for(char nChar:ca){
			englishConnectChars.add(nChar);
		}
	}
	
	public static final String Arabic_Num_Pre = "-+$￥";//阿拉伯数词前缀
	public static final String Arabic_Num_Mid = ",.*/:";//阿拉伯数词连接符号
	public static final String Arabic_Num_End = "%‰";//阿拉伯数词后缀
	public static final Set<Character> arabicNumConnectChars = new HashSet<Character>();
	static{
		char[]ca = Arabic_Num_Pre.toCharArray();
		for(char nChar:ca){
			arabicNumConnectChars.add(nChar);
		}
		ca = Arabic_Num_Mid.toCharArray();
		for(char nChar:ca){
			arabicNumConnectChars.add(nChar);
		}
		ca = Arabic_Num_End.toCharArray();
		for(char nChar:ca){
			arabicNumConnectChars.add(nChar);
		}
	}
	
	private String word;
	
	private Set<Lexeme> lexemeSet;
	private Set<String> wordSet;
	//数词起始位置
	private int dStart = -1;
	//数词结束位置
	private int dEnd = -1;
	//当前数词的状态
	private int dStatus = NaN;
	//扑捉到一个数词
//	private boolean bDigit = false;
	//英文起始位置
	private int lStart = -1;
	//英文结束位置
	private int lEnd = -1;
	
	private int index=0;
	
	public LatinSplitor(String word){
		this.word=word;
		this.lexemeSet=new LinkedHashSet<Lexeme>();
		this.wordSet=new LinkedHashSet<String>();
	}
	
	 public Set<Lexeme> splitLatin(){
		 while(index<word.length()){
			 handleNumber();
			 if(lStart==-1){
				 if(dStart==-1){
					 handleLetter();
				 }
			 }else{
				 handleLetter();
			 }
			 index++;
		 }
		 return lexemeSet;		 
	 }
	 
	 public Set<String> splitLatinString(){
		 while(index<word.length()){
			 handleNumber();
			 if(lStart==-1){
				 if(dStart==-1){
					 handleLetter();
				 }
			 }else{
				 handleLetter();
			 }
			 index++;
		 }
		 return wordSet;
	 }
	
	/**
	 * 数词处理
	 */
	private void handleNumber(){
		int inputStatus = nIdentify(word.charAt(index));
		if(NaN == dStatus){
			onNaNStatus(inputStatus);
		}else if(NC_ANP == dStatus){
			onANPStatus(inputStatus);
		}else if(NC_ARABIC == dStatus){
			onARABICStatus(inputStatus);
		}else if(NC_ANM == dStatus){
			onANMStatus(inputStatus);
		}else if(NC_ANE == dStatus){
			onANEStatus(inputStatus);
		}
		if(index==word.length()-1){
			if(dStart!=-1 && dEnd!=-1){
				outputDigitLexeme();
			}
			digitReset();
		}
	}
	
	/**
	 * 英文处理
	 */
	private void handleLetter(){
		char ch = word.charAt(index);
		if(Latin.isLetter(ch)||englishConnectChars.contains(ch)){
			if(lStart==-1){
				lStart = index;
			}
//			dStatus = NaN;
			lEnd = index;
		}else{
			outputLetterLexeme();
			lStart=-1;
			lEnd=-1;
		}
		if(index==word.length()-1){
			outputLetterLexeme();
			lStart=-1;
			lEnd=-1;
		}
	}
	
	/**
	 * 当前为NaN状态时，状态机处理（状态转换）
	 * @param inputStatus
	 */
	private void onNaNStatus(int inputStatus){
		if(NaN == inputStatus){
			return;
		}else if(NC_ANP == inputStatus){
			dStart = index;
			dStatus = inputStatus;
		}else if(NC_ARABIC == inputStatus){
			dStart = index;
			dStatus = inputStatus;
			dEnd = index;
		}
	}
	
	/**
	 * 当前为ANP状态时，状态机处理（状态转换）
	 * @param inputStatus
	 */
	private void onANPStatus(int inputStatus){
		if(NC_ARABIC == inputStatus){
			dStatus = inputStatus;
			dEnd=index;
		}else{
			outputDigitLexeme();
			digitReset();
			onNaNStatus(inputStatus);
		}
	}
	
	/**
	 * 当前为ARABIC状态时，状态机处理（状态转换）
	 * @param inputStatus
	 */
	private void onARABICStatus(int inputStatus){
		if(NC_ARABIC == inputStatus){
			dEnd = index;
		}else if(NC_ANM == inputStatus){
			dStatus = inputStatus;
		}else if(NC_ANE == inputStatus){
			dStatus = inputStatus;
			dEnd = index;
			outputDigitLexeme();
			digitReset();
		}else{
			outputDigitLexeme();
			digitReset();
			onNaNStatus(inputStatus);
		}
	}
	
	/**
	 * 当前为ANM状态时，状态机处理（状态转换）
	 * @param inputStatus
	 */
	private void onANMStatus(int inputStatus){
		if(NC_ARABIC==inputStatus){
			dStatus=inputStatus;
			dEnd=index;
		}else if(NC_ANP==inputStatus){
			dStatus=inputStatus;
		}else{
			outputDigitLexeme();
			digitReset();
			onNaNStatus(inputStatus);
		}
	}
	
	/**
	 * 当前为ANE状态时，状态机处理（状态转换）
	 * @param inputStatus
	 */
	private void onANEStatus(int inputStatus){
		outputDigitLexeme();
		digitReset();
		onNaNStatus(inputStatus);
	}
	
	/**
	 * 添加数词词元到结果集
	 */
	private void outputDigitLexeme(){
		if(dStart>-1&&dEnd>-1){
			Lexeme le = new Lexeme(word,dStart,dEnd+1);
			le.setType(Lexeme.TYPE_DIGIT);
			lexemeSet.add(le);
			wordSet.add(word.substring(dStart,dEnd+1));
//			bDigit = true;
		}
	}
	
	/**
	 * 添加英文词元到结果集
	 */
	private void outputLetterLexeme(){
		if(lStart>-1&&lEnd>-1){
			Lexeme le = new Lexeme(word,lStart,lEnd+1);
			le.setType(Lexeme.TYPE_LETTER);
			lexemeSet.add(le);
			wordSet.add(word.substring(lStart,lEnd+1));
		}
	}
	
	/**
	 * 重置数词的状态
	 */
	private void digitReset(){
		this.dStart = -1;
		this.dEnd = -1;
		this.dStatus = NaN;
	}
	//非数词字符
	public static final int NaN = -99;
	//数词前缀
	public static final int NC_ANP = 01;
	//数词连接符
	public static final int NC_ANM = 02;
	//数词结束符
	public static final int NC_ANE = 03;
	//阿拉伯数字
	public static final int NC_ARABIC=04;
	
	/**
	 * 识别数字字符类型
	 * @param c
	 * @return
	 */
	 private int nIdentify(char c){
		 int type=NaN;
		 if(!arabicNumConnectChars.contains(c)&&
				 !Latin.isDigit(c)){
			 return type;
		 }
		 if(Arabic_Num_Pre.indexOf(c)>=0){
			 type=NC_ANP;
		 }
		 else if(Arabic_Num_Mid.indexOf(c)>=0){
			 type=NC_ANM;
		 }
		 else if(Arabic_Num_End.indexOf(c)>=0){
			 type=NC_ANE;
		 }else if(Latin.isDigit(c)){
			 type=NC_ARABIC;
		 }
		 return type;
	 }
	 
}
