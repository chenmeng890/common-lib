package com.yihaodian.search.nlp.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 分词后的词元*
 * @author yuqian
 *
 */
public class Lexeme implements Serializable,Comparable<Lexeme>{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final int TYPE_DIGIT= 102;
	public static final int TYPE_LETTER= 101;
	public static final int TYPE_CHINESEDIGIT=103;
    //词元文本
	private String text;
	//词元类型
	private int type;
	//词元的同义词
	private String[] synonyms;
	//词元扩展
	private String[] extendWords;
	
	public Lexeme(){
		
	}
	
	public Lexeme(String text,int begin,int end){
		this.text=text.substring(begin,end);
	}
	
	public Lexeme(String text){
		this.text=text;
	}
	
	public Lexeme(String text,int type){
		this.text=text;
		this.type=type;
	}

	public boolean equals(Object o){
		if(o==null){
			return false;
		}
		if(this == o){
			return true;
		}
		if(o instanceof Lexeme){
			Lexeme other = (Lexeme)o;
			if(this.text.equals(other.text) && this.type==other.type
					&&this.extendWords==other.extendWords){
				return true;
			}else{
				return false;
			}
		}
		return false;
	}
	
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + type;
		if(extendWords!=null && extendWords.length>0){
			for(String word:extendWords){
				result = prime * result + word.hashCode();
			}
		}
		return result;
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String[] getSynonyms() {
		if(synonyms==null||synonyms.length==0){
			return null;
		}
		if(this.text==null){
			return synonyms;
		}
		List<String> synon=new ArrayList<String>();
		for(String sy:synonyms){
			if(text.equals(sy)){
				continue;
			}
			synon.add(sy);
		}
		return synon.toArray(new String[synon.size()]);
	}
	
	
	public String[] getAllSynonyms() {
		if(synonyms==null||synonyms.length==0){
			return null;
		}
		if(this.text==null){
			return synonyms;
		}
		return synonyms;
	}
	
	public void setAllSynonyms(String[] synonyms){
		this.synonyms = synonyms;
	}

	public void setSynonyms(String[] synonyms) {
		if(this.synonyms==null||this.synonyms.length==0){
			this.synonyms=synonyms;
		}
		else{
			List<String> list = new ArrayList<String>(Arrays.asList(this.synonyms));  
			for(String ex:synonyms){
				if(!list.contains(ex)){
					list.add(ex);
				}
			}
			this.synonyms=list.toArray(new String[list.size()]);
		}
	}
	
	public String[] getExtendWords() {
		return extendWords;
	}

	public void setExtendWords(String[] extendWords) {
		this.extendWords = extendWords;
	}

	@Override
	public int compareTo(Lexeme o) {
		if(!this.text.equals(o.text)){
			return this.text.compareTo(o.text);
		}else{
			return this.type -o.type;
		}
	}
}
