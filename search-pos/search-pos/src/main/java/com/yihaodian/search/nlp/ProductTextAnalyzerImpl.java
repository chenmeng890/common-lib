package com.yihaodian.search.nlp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.TokenStream;

import com.yihaodian.search.catwordr.ProductTextImpl;
import com.yihaodian.search.catwordr.Recognizer;
import com.yihaodian.search.catwordr.crf.RecognizerCRF;
import com.yihaodian.search.nlp.model.Dictionary;
import com.yihaodian.search.nlp.model.Lexeme;
import com.yihaodian.search.nlp.segment.ReverseSegmenter;
import com.yihaodian.search.nlp.segment.Segmenter;
import com.yihaodian.search.nlp.segment.WordSliceSegmenter;
import com.yihaodian.search.util.UtilFuction;

public class ProductTextAnalyzerImpl implements ProductTextAnalyzer {
	
	public ProductTextAnalyzerImpl(String dictPath,String trainModelFile){
		UtilFuction.createDirectory(dictPath);
		this.dictPath = dictPath;
		
		UtilFuction.createDirectory(trainModelFile);
		this.trainModelFile = trainModelFile+"all.model";
	}
	
	public ProductTextAnalyzerImpl() {
	}

	final Pattern PAT_categoryName = Pattern.compile("^\\d+[0-9\\-]*:.*$");
	
	@Override
	public String analyze(ProductText product) {
		ensureInited();
		ProductTextImpl pi = new ProductTextImpl(product);
		recognizer.recognize(pi);
		return pi.getCategoryWords();
	}
	
	@Override
	public void analyze(ProductText product, boolean doPOS) {
		ensureInited();

		String categoryName,leafCateName = null;
		if (PAT_categoryName.matcher(product.getCategoryName()).matches()) {
			int tn = product.getCategoryName().indexOf(':');
			categoryName = product.getCategoryName().substring(tn+1);
		}
		else {
			categoryName = product.getCategoryName();
		}
		if (doPOS) {
			ProductTextImpl pi = new ProductTextImpl(product);
			recognizer.recognize(pi);
			strBrandWords = pi.getBrandWords();
			strCategoryWords = pi.getCategoryWords();
			
			// 识别出的品牌词/类别词, 加入扩展词
//			brandWords = expandRecgnWords(strBrandWords, Dictionary.WORDTYPE_Brand);
			categoryWords = expandRecgnWords(strCategoryWords, Dictionary.WORDTYPE_Category);
			
			// 品牌词: 附加品牌名称(brandName)且为品牌词的词(不再扩展)
			String brandName = product.getBrandName();
			if (brandName != null) {
				List<Lexeme> lexemes=segForWord.segmentComplex(brandName.trim());
				List<String> ret=new ArrayList<String>();
				if(lexemes!=null){
					for(Lexeme le:lexemes){
						if(le.getType() != Dictionary.WORDTYPE_Brand) continue;
						ret.add(le.getText());
						/*modify by yuqian at 2012-03-12*/
						String[] a = le.getSynonyms();
						if (a != null) {
							for (int i=0; i < a.length; i++){
								if(!ret.contains(a[i]))
									ret.add(a[i]);
							}
						}
						a = le.getExtendWords();
						if (a != null) {
							for (int i=0; i < a.length; i++){
								if(!ret.contains(a[i]))
									ret.add(a[i]);
							}
						}
					}
				}
//				strBrandWords="";
//				for(String bw:ret)
//					strBrandWords+=bw+",";
				brandWords=ret;
				
			}else{
				//brandWords = expandRecgnWords(strBrandWords, Dictionary.WORDTYPE_Brand);
				brandWords = null;
			}
			
			// 类别词: 附加类别名称(categoryName)分段后且为类别词的词(不再扩展)
			String[] categoryNameWords = categoryName.split("-");
			leafCateName=categoryNameWords[categoryNameWords.length-1];
			for (int i=0; i < categoryNameWords.length; i++) {
				String tsWord = categoryNameWords[i].trim();
				Lexeme lexeme = dict.isWordType(tsWord, Dictionary.WORDTYPE_Category);
				if(lexeme!=null){
//				if (dict.canBeWordType(tsWord, Dictionary.WORDTYPE_Category)) {
					if (categoryWords == null)
						categoryWords = new ArrayList<String>();
					categoryWords.add(tsWord);
					/*modify by yuqian at 2012-03-12*/
					String[] synonyms = lexeme.getSynonyms();
					if(synonyms!=null && synonyms.length>0){
						for(String syWord:synonyms)
							categoryWords.add(syWord);
					}
					String[] extendWords = lexeme.getExtendWords();
					if(extendWords!=null && extendWords.length>0){
						for(String exword:extendWords)
							categoryWords.add(exword);
					}
				}
			}
		}
		else {
			brandWords = null;
			categoryWords = null;
		}

		// 其它可搜文本内容合并
		/*StringBuilder tsText = new StringBuilder();
		mergeText(product.getProductName(), tsText);
		mergeText(product.getBrandName(), tsText);
		mergeText(categoryName, tsText);
		mergeText(product.getNameSubtitle(), tsText);
		mergeText(product.getProductCode(), tsText);
		List<String> attributeValues = product.getAttributeValues();
		if (attributeValues != null) {
			for (String attributeValue : attributeValues)
				mergeText(attributeValue, tsText);
		}
		String text = tsText.toString();*/
		String text = product.getProductTag();
		// 其它可搜文本分词. 普通分词/再细切分两种
		words = segForWord.segment(text);
		
		new ListTokenStream(words); //修复null bug
	
		String cateTitle=product.getProductName()+" "+leafCateName;
		List<String> tagChars=segForWord.segment(cateTitle);
		tagChars=andNot(tagChars,brandWords);
		wordsEx = WordSliceSegmenter.sliceSegment(tagChars);
		wordlist = segForRecognizer.segment(text);
	}
	
	private void mergeText(String text, StringBuilder result) {
		if (text == null) return;
		if (text.trim().length() == 0) return;
		result.append(' ');
		result.append(text);
	}
	
	private List<String> expandRecgnWords(String strWords, int wordType) {
		if (strWords == null) return null;
		if (strWords.length() == 0) return null;
		
		List<String> result = new ArrayList<String>();
		String[] aWords = strWords.split(",");
		for (int i=0; i < aWords.length; i++) {
			result.add(aWords[i]);
			dict.expandWord(aWords[i], wordType, result);
		}
		return result;
	}
	
	/**
	 * Returns the String in this list that are not in the other .
	 * @param list
	 * @param other
	 * @return
	 */
	private List<String> andNot(List<String> list,List<String> other){
		if(other==null||other.isEmpty()||list==null)
			return list;
		Iterator<String> iter=list.iterator();
		while(iter.hasNext()){
			String str=iter.next();
			if(other.contains(str))
				iter.remove();
		}
		return list;
	}
	
	private String dictPath; //词典文件所在目录
	private String trainModelFile; //CRF训练后的模型数据
	
	public String getDictPath() {
		return dictPath;
	}

	public void setDictPath(String dictPath) {
		this.dictPath = dictPath;
	}

	public String getTrainModelFile() {
		return trainModelFile;
	}

	public void setTrainModelFile(String trainModelFile) {
		this.trainModelFile = trainModelFile;
	}
	
	private Dictionary dict;
	private Segmenter segForRecognizer, segForWord;
	private Recognizer recognizer;
	
	// 初始化分词器和词性识别器
	private void ensureInited() {
		if (recognizer != null) return;
		
		try {
			this.dict = new Dictionary(dictPath, true);
			this.segForRecognizer = new ReverseSegmenter(dict, ReverseSegmenter.TYPE_LATIN_ORIGINAL, true,true);
			this.segForWord = new ReverseSegmenter(dict, ReverseSegmenter.TYPE_LATIN_ORIGINAL, true,false);
			
			FileInputStream fis = new FileInputStream(trainModelFile);
			ObjectInputStream ois = new ObjectInputStream(fis);
			RecognizerCRF recognizerCRF = (RecognizerCRF)ois.readObject();
			recognizerCRF.init(segForRecognizer, dict);
			ois.close();
			fis.close();
			this.recognizer = recognizerCRF;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	

	public Recognizer getRecognizer() {
		return recognizer;
	}

	public void setRecognizer(Recognizer recognizer) {
		this.recognizer = recognizer;
	}

	private String strBrandWords, strCategoryWords;
	private List<String> brandWords, categoryWords, words, wordsEx,wordlist;

	@Override
	public TokenStream getBrandTokens() {
		return new ListTokenStream(brandWords);
	}

	@Override
	public String printBrandTokens(){
		if(brandWords==null||brandWords.isEmpty())
			return null;
		String str="";
		int i = 0;
		for(String brandword:brandWords){
			if(i>0) str+=" ,";
			str+=brandword;
			i++;
		}
		return str;
	}
	@Override
	public TokenStream getCategoryTokens() {
		return new ListTokenStream(categoryWords);
	}

	@Override
	public String printCategoryTokens(){
		if(categoryWords==null||categoryWords.isEmpty())
			return null;
		String str="";
		int i = 0;
		for(String categoryword:categoryWords){
			if(i>0) str+=" ,";
			str+=categoryword;
			i++;
		}
		return str;
	}
	@Override
	public TokenStream getWordTokens() {
		if(words==null) return null;
		Iterator<String> iter=words.iterator();
		while(iter.hasNext()){
			String word=iter.next();
			if(categoryWords!=null&&categoryWords.contains(word)){
					iter.remove();
					continue;
			}
		    if(brandWords!=null&&brandWords.contains(word)){
		    	iter.remove();
		    	continue;
		    }
		}
		return new ListTokenStream(words);
	}

	@Override
	public TokenStream getWordTokensByTrain(){
		return new ListTokenStream(wordlist);
	}
	@Override
	public TokenStream getWordExTokens() {
		if(wordsEx==null) return null;
		Iterator<String> iter=wordsEx.iterator();
		while(iter.hasNext()){
			String word=iter.next();
			if(categoryWords!=null&&categoryWords.contains(word)){
					iter.remove();
					continue;
			}
		    if(brandWords!=null&&brandWords.contains(word)){
		    	iter.remove();
		    	continue;
		    }
		    if(words!=null&&words.contains(word)){
		    	iter.remove();
		    	continue;
		    }
		}
		return new ListTokenStream(wordsEx);
	}

	@Override
	public String getBrandWords() {
		return strBrandWords;
	}

	@Override
	public String getCategoryWords() {
		return strCategoryWords;
	}
	public void reloadRecognizer(){
		this.recognizer=null;
	}

}
