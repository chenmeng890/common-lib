package com.yihaodian.search.nlp.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.yihaodian.search.nlp.SimpleDictionaryType;
import com.yihaodian.search.nlp.help.ExportDict;

/**
 * 词典管理类
 * 
 * @author yuqian
 * 
 */
public class Dictionary {

	private static final Logger log = Logger.getLogger(Dictionary.class);
	
	public static final String PATH_DIC_QUANTIFIER = "/dict/quantifier.dic";
	public static final String PATH_DIC_STOPWORD = "/dict/stopword.dic";
	
	public static final String DIC_MAIN = "main.dic";
	public static final String DIC_SYSNONMS = "sysnonms.dic";
	
	public static final int WORDTYPE_Category = 1; //词性: 类别
	public static final int WORDTYPE_Brand = 2;    //词性: 品牌
	public static final int WORDTYPE_PROMOTION = 7;  //词性：促销词
	public static final int WORDTYPE_MODEL = 98  ;   //词性：型号词
	public static final int WORDTYPE_MERCHANT_NAME_TYPE = 15  ;   //词性：商家名称
	
	private boolean isExtend=false;// 词典是否加载扩展词
	
	private int maxDepth=0; //词典深度,即最长词的长度

	/**
	 * 加载的词典路径
	 */
	private String dictPath;

	/*
	 * 数据库导出的主词典
	 */
	private Map<String,SimpleDictionary> _MainDictMap;
	/*
	 * 停词词典
	 */
	private SimpleDictionary _StopWordDict;
	
	private static Dictionary dictionary = null;
	
	public synchronized static Dictionary getInstance(String dictPath,boolean isExtend){
		if(dictionary == null) {
			dictionary = new Dictionary(dictPath, isExtend);
		}
		
		return dictionary;
	}


	public Dictionary() {
	}

	public Dictionary(String dictPath,boolean isExtend) {
		log.info("try to load dir=" + dictPath);
		this.dictPath = dictPath;
		this.isExtend = isExtend;
		this._StopWordDict = loadStopWordDict();
		this._MainDictMap = loadMainDict();
	}
	
	public void reloadDict(String dictPath,boolean isExtend) {
		this.dictPath = dictPath;
		this.isExtend = isExtend;
		
		Map<String,SimpleDictionary> oldMainDictMap = _MainDictMap;
		SimpleDictionary oldStopWordDict = _StopWordDict;
		
		try {
			_MainDictMap = loadMainDict();
			_StopWordDict = loadStopWordDict();
		} catch (Exception e) {
			//rool back
			_MainDictMap = oldMainDictMap;
			_StopWordDict = oldStopWordDict;
		}
	}
	
	public boolean isMatchDict(String word,boolean isReservedStopWords){
		if(isStopWord(word)){
			if(isReservedStopWords){
				return true;
			}else{
				return false;
			}
		}
		Lexeme lexeme;
		Iterator<Entry<String,SimpleDictionary>> iter=_MainDictMap.entrySet().iterator();
		while(iter.hasNext()){
			Entry<String,SimpleDictionary> entry=iter.next();
			SimpleDictionary simDict=entry.getValue();
			lexeme=simDict.match(word);
			if(lexeme!=null){
				return true;
			}
		}
		return false;
	}
	
	public List<Lexeme> matchDict(String word){
		return matchDict(word,true);
	}

	public List<Lexeme> matchDict(String word,boolean isReservedStopWords){	
		List<Lexeme> list=new ArrayList<Lexeme>();
		Lexeme lexeme;
		if(_StopWordDict!=null){
			lexeme=_StopWordDict.match(word);
			if(lexeme!=null){
				if(isReservedStopWords){
					list.add(lexeme);
					return list;
				}else{
					return null;
				}
			}
		}
		Iterator<Entry<String,SimpleDictionary>> iter=_MainDictMap.entrySet().iterator();
		while(iter.hasNext()){
			Entry<String,SimpleDictionary> entry=iter.next();
			SimpleDictionary simDict=entry.getValue();
			lexeme=simDict.match(word);
			if(lexeme!=null){
				list.add(lexeme);
			}
		}
		if(!list.isEmpty()){
			return list;
		}
		
		return null;
	}
	
	public Hit matchInMainDict(char[] charArray , int begin, int length){
		Hit hit = null;
		Iterator<Entry<String,SimpleDictionary>> iter=_MainDictMap.entrySet().iterator();
		while(iter.hasNext()){
			Entry<String,SimpleDictionary> entry=iter.next();
			SimpleDictionary simDict=entry.getValue();
			hit=simDict.match(charArray, begin, length);
			if(hit!=null) {break;}			
		}		
		return hit;
	}
	
	

	/**
	 * 扩展词处理.
	 * @param word 待扩展的词
	 * @param wordType 待扩展的词性
	 * @param result 扩展后的词(不含该词本身,含同义词)添加到列表中
	 */
	public void expandWord(String word, int wordType, Collection<String> result) {
		List<Lexeme> lexemes = matchDict(word);
		if (lexemes == null) {return;}
		for (Lexeme lexeme : lexemes) {
			if (lexeme.getType() != wordType) {continue;}
			String[] a = lexeme.getSynonyms();
			if (a != null) {
				for (int i=0; i < a.length; i++){
					result.add(a[i]);
				}
			}
			a = lexeme.getExtendWords();
			if (a != null) {
				for (int i=0; i < a.length; i++){
					result.add(a[i]);
				}
			}
		}
	}
	
	/**
	 * 判断指定的词是否可能为词性
	 * @param word 待判断的词
	 * @param wordType
	 * @return
	 */
	public boolean canBeWordType(String word, int wordType) {
		List<Lexeme> lexemes = matchDict(word);
		if (lexemes == null) {return false;}
		for (Lexeme lexeme : lexemes) {
			if (lexeme.getType() == wordType){
				return true;
			}
		}
		return false;
	}
	
	public Lexeme isWordType(String word,int wordType){
		List<Lexeme> lexemes = matchDict(word);
		if(lexemes == null) {return null;}
		for (Lexeme lexeme : lexemes) {
			if (lexeme.getType() == wordType){
				return lexeme;
			}
		}
		return null;
	}
	
	public boolean isStopWord(String word){
		if(_StopWordDict==null){
			return false;
		}
		Lexeme le=_StopWordDict.match(word);
		if(le==null){
			return false;
		}else{
			return true;
		}
	}

	public boolean isWordInSpecifiedDictionary(String word,String dictionaryType ){
		SimpleDictionary simDict=_MainDictMap.get(dictionaryType);
		if(simDict==null){
			return false;
		}
		Lexeme lexeme=simDict.match(word);
		if(lexeme!=null){
			return true;
		}
		return false;
	}

	private SimpleDictionary loadStopWordDict() {
		// 建立一个停词典实例
		SimpleDictionary _StopWordDict = new SimpleDictionary(SimpleDictionaryType.TYPE_STOPWORDS);
		// 读取停词词典文件
		InputStream is = Dictionary.class
				.getResourceAsStream(Dictionary.PATH_DIC_STOPWORD);
		if (is == null) {
			throw new RuntimeException("StopWord Dictionary not found!!!");
		}
		BufferedReader br =null;
		try {
			br = new BufferedReader(new InputStreamReader(is,
					"UTF-8"), 512);
			String theWord = null;
			do {
				theWord = br.readLine();
				if (theWord != null && !"".equals(theWord.trim())) {
					_StopWordDict.fillSegment(theWord.trim(),null,false);
				}
			} while (theWord != null);

		} catch (IOException ioe) {
			throw new RuntimeException("StopWord Dictionary loading exception.",ioe);

		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				throw new RuntimeException("StopWord Dictionary close stream exception.",e);
			}
			try{
				if(br!=null){
					br.close();
				}
			}catch (Exception e) {
				throw new RuntimeException("StopWord Dictionary close BufferedReader exception.",e);
			}
		}
		
		return _StopWordDict;
	}

	private Map<String,SimpleDictionary> loadMainDict(){
		// 建立主要词典实例
		Map<String,SimpleDictionary> _MainDictMap=new ConcurrentHashMap<String,SimpleDictionary>();
		File dictFile = new File(dictPath);
		if(!dictFile.exists()){
			throw new RuntimeException("mianDict is not exists.");
		}
		if (dictFile.isDirectory()) {
			File[] files = dictFile.listFiles();
			
			for (int i = 0; i < files.length; i++) {
				File cf = files[i];
				if(!cf.getName().endsWith(".dic")){
					continue;
				}

				BufferedReader br = null;
				try{
					 br= new BufferedReader(
							new InputStreamReader(new FileInputStream(cf),
									"UTF-8"), 512);
					String theWord = null;
					int type = 0;
					if(cf.getName().equals(DIC_MAIN)){
						//加载关键词与扩展词词典
						do {
							theWord = br.readLine();
							if (theWord != null && !"".equals(theWord.trim())) {
								theWord = theWord.toLowerCase();
								String[] array=theWord.split(ExportDict.SPILTOR);
								if(array.length<2){
									log.error("ERROR:the length of array <2;--"+theWord);
									continue;
								}
								try{
									type = Integer.parseInt(array[1]);
								}catch (Exception e) {
									type =-1;
									log.error("ERROR WORD:"+theWord + e.getMessage());
								}
								if(type==-1) continue;
								
								SimpleDictionary simDict=_MainDictMap.get(array[1]);
								if(simDict==null){
									simDict = new SimpleDictionary(type);
									_MainDictMap.put(array[1], simDict);
								}
								if(array.length==2){
									simDict.fillSegment(array[0], null, isExtend);
								}
								if(array.length==3){
									String[] extendWords=array[2].split(",");
									simDict.fillSegment(array[0], extendWords, isExtend);
								}
							}
						} while (theWord != null);
					}
					else if(cf.getName().equals(DIC_SYSNONMS)) {
						//加载同义词词典
						do {
							theWord = br.readLine();
							if (theWord != null && !"".equals(theWord.trim())) {
								theWord = theWord.toLowerCase();
								String[] array=theWord.split(ExportDict.SPILTOR);
								if(array.length<2){
									log.error("ERROR:the length of array <2;--"+theWord);
									continue;
								}
								try{
									type = Integer.parseInt(array[0]);
								}catch (Exception e) {
									type =-1;
									log.error("ERROR WORD:"+theWord + e.getMessage());
								}
								if(type==-1) continue;
								
								SimpleDictionary simDict=_MainDictMap.get(array[0]);
								if(simDict==null){
									simDict = new SimpleDictionary(type);
									_MainDictMap.put(array[0], simDict);
								}
								String[] sysnonyms=array[1].split(",");
								simDict.fillSegmentBySysnonyms(sysnonyms);
							}
						} while (theWord != null);
					}
					
				}catch (IOException e) {
					throw new RuntimeException("MainWord Dictionary loading exception.",e);
				}finally{
					try {
						if(br!=null){
							br.close();
						}
					} catch (IOException e) {
						throw new RuntimeException("close mainWord Dictionary exception.",e);
					}
				}
			}
		}else{
			throw new RuntimeException("The path of the dictionary is not a file directory");
		}
		
		if(_MainDictMap.size()==0){
			throw new RuntimeException("Dictionary file does not contain the correct format--.dic!"); 
		}
		
		Iterator<SimpleDictionary> iter=_MainDictMap.values().iterator();
	    while(iter.hasNext()){
	    	SimpleDictionary simDict =iter.next();
	    	if(maxDepth<simDict.getDepth()){
	    		maxDepth=simDict.getDepth();
	    	}
	    }
	    
	    return _MainDictMap;
	}

	public String getDictPath() {
		return dictPath;
	}

	public void setDictPath(String dictPath) {
		this.dictPath = dictPath;
	}

	public int getMaxDepth() {
		return maxDepth;
	}

	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}

	/*public synchronized Map<String, SimpleDictionary> getMainDictMap() {
		Map<String, SimpleDictionary> map=new ConcurrentHashMap<String,SimpleDictionary>();
		map.putAll(_MainDictMap);
		return map;
		
	}*/
	public  Map<String, SimpleDictionary> getMainDictMap() {
		return _MainDictMap;
	}
	
	public void destroy() {
		dictPath = null;
		_MainDictMap.clear();
		_MainDictMap = null;
		_StopWordDict = null;
	}
}
