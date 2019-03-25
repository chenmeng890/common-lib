package com.yihaodian.search.nlp.help;

import java.io.BufferedReader;
//import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;



/**
 * 繁体转简体
 * 
 * @author E
 *
 */
public class WordConvert {
	
	private static Logger log = Logger.getLogger(WordConvert.class);
	
	private static final String EMPTY = "";
	
	private static final char SPLITER = ',';
	
	/**
	 * 转换表
	 */
	private Map<String, String> map;
	
	/**
	 * 首字检查是否需要转换，提高效率
	 */
	private Map<Character, String> check;

	/**
	 * 最大词长
	 */
	private int maxWordLength;
	
	private static WordConvert instance= new WordConvert(true);
	
	public WordConvert(boolean isInit){
			init();
	}
	public WordConvert(){}
	public static WordConvert getInstance(){
		return instance;
	}
	
	public void init(){
//		long time = System.currentTimeMillis();
		InputStream stream = WordConvert.class.getResourceAsStream("/spellDict/big2cn.txt");
		BufferedReader br = null;
		map = new HashMap<String, String>();
		check = new HashMap<Character, String>();
		maxWordLength = 0;
		try {
			br = new BufferedReader(new InputStreamReader(stream,"UTF-8"));
			String in =null;
			do{
				in = br.readLine();
				if(in==null){
					break;
				}
				in = in.trim();
				if(in.startsWith("//") || in.length() == 0){
					continue;
				}
				final int psp = in.indexOf(SPLITER);
				if(psp > 0 && ! in.contains("?")){
					String big = in.substring(0, psp);
					String cn = in.substring(psp + 1);
					if(big.length() > maxWordLength){
						maxWordLength = big.length();
					}
					map.put(big, cn);
					if(big.length()>1){
						check.put(big.charAt(0), EMPTY);
					}else{
						check.put(big.charAt(0), cn);
					}
				}
			}while(in!=null);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				if(br !=null){
					br.close();
				}
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}
	}
	
	public String big52gb(String sentence) throws Exception {
		int pHead = 0;
		int pTail = maxWordLength;
		StringBuilder ret = new StringBuilder();
		while(pHead < sentence.length()){
			final char ck = sentence.charAt(pHead);

			if(! check.containsKey(ck)){
				ret.append(ck);
				pTail = (++ pHead) + maxWordLength;
				continue;
			}
			final String cn = check.get(ck);
			if(! cn.equals(EMPTY)){
				ret.append(cn);
				pTail = (++ pHead) + maxWordLength;
				continue;
			}
			
			if(pTail > sentence.length()){
				pTail = sentence.length();
			}
			int l = pTail - pHead;
			boolean fMatched = false;
			for(;l > 0; l --){
				String test = sentence.substring(pHead, pHead + l);
				if(map.containsKey(test)){
					ret.append(map.get(test));
					fMatched = true;
					break;
				}
			}
			if(fMatched){
				pHead += l;
			}else{
				ret.append(sentence.charAt(pHead));
				pHead ++;
			}
			pTail = pHead + maxWordLength;
		}

		return ret.toString();
	}

	public static void main(String[] args) throws Exception {
		WordConvert sc = WordConvert.getInstance();
	    String temp=sc.complexToSimple("手機，；電腦");
	    System.out.println(temp);
	}
	/**
	 *此方法封装了繁简转换函数以及全半角转换功能
	 */
	public String complexToSimple(String sentence){
		String simple=sentence;
		try {
			simple=this.big52gb(simple);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("ComplexToSimple is error");
		}
		simple=Latin.toDBC(simple);
		
		return simple;
	}
	
	public Map<String, String> getMap() {
		return map;
	}
   
}
