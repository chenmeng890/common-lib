package com.yihaodian.search.catwordr.crf;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Vector;


import iitb.CRF.CRF;
import iitb.CRF.DataSequence;
import iitb.CRF.FeatureGenerator;
import iitb.Model.Model;

import com.yihaodian.crf.BoolFeatureCalculator;
import com.yihaodian.crf.EndFeatureCalculator;
import com.yihaodian.crf.EnumFeatureCalculator;
import com.yihaodian.crf.EnumsFeatureGenerator;
import com.yihaodian.crf.FeatureCalculator;
import com.yihaodian.crf.SimpleEdgeFeatureCalculator;
import com.yihaodian.crf.StartFeatureCalculator;
import com.yihaodian.crf.WordFeatureCalculator;
import com.yihaodian.search.catwordr.CsvProductsReader;
import com.yihaodian.search.catwordr.ProductTextImpl;
import com.yihaodian.search.catwordr.Recognizer;
import com.yihaodian.search.model.ProductTrainer;
import com.yihaodian.search.nlp.model.Dictionary;
import com.yihaodian.search.nlp.segment.Segmenter;
import com.yihaodian.search.service.ProductTrainerService;
import com.yihaodian.search.util.Constant;

/**
 * 类别词的CRF识别器(单一模型)
 */
public class RecognizerCRF implements Recognizer, java.io.Externalizable {
	
	private static final long serialVersionUID = -7259306459901147892L;
	
	public RecognizerCRF() {
	}
	
	final static HashMap<String, FeatureCalculator> allCalculators = new HashMap<String, FeatureCalculator>();
	static {
	  	allCalculators.put("Start", new StartFeatureCalculator());
	  	allCalculators.put("Edge", new SimpleEdgeFeatureCalculator());
	  	allCalculators.put("End", new EndFeatureCalculator());
	  	allCalculators.put("W", new WordFeatureCalculator());
	  	allCalculators.put("LC", new LastCharFeatureCalculator());
	  	allCalculators.put("IsBrand", new IsBrandWordFeatCalc());
	  	allCalculators.put("IsCat", new IsCatWordFeatCalc());
	  	allCalculators.put("IsNameCat", new IsNameCatWordFeatCalc());
	  	allCalculators.put("LikeNameCat", new LikeNameCatWordFeatCalc());
	  	allCalculators.put("IsSingleChar", new IsSingleCharFeatCalc());
	  	allCalculators.put("NoAnother", new NoAnotherCatWordsFeatCalc());
	  	allCalculators.put("IsMeasure", new IsMeasureWordFeatCalc());
	  	allCalculators.put("IsSong", new IsSongFeatCalc());
	  	
	  	allCalculators.put("ZengPin", new IsZengPinFeatCalc());
	  	allCalculators.put("FirstBrand", new IsFirstBrandWordFeatCalc());
	  	allCalculators.put("LastCat", new IsLastCatWordFeatCalc());
	  	allCalculators.put("IsBrandName", new IsBrandNameWordFeatCalc());
	  	allCalculators.put("IsInBracket", new IsInBracketFeatCalc());
	}
	
	public void init(Segmenter segmenter, Dictionary dict) {
		init(segmenter, dict, null);
	}
	
	@SuppressWarnings("unchecked")
	public void init(Segmenter segmenter, Dictionary dict, final ByCatNamePattern pattern) {
		this.segmenter = segmenter;
		this.dict = dict;
	  	
		if (featureGen == null) {
			if (pattern != null) {
				fcByCat = new ByCatFeatureCalculator();
				fcByCat.pattern = pattern;
			}
			HashMap<String, FeatureCalculator> calculators = (HashMap<String, FeatureCalculator>)allCalculators.clone();
			if (fcByCat != null)
				calculators.put("ByCat", fcByCat);
			
			featureGen = new EnumsFeatureGenerator();
			featureGen.init(calculators);
			initCRF(featureGen);
		}
	}
	
	private void initCRF(FeatureGenerator featureGen) {
	  	try {
			Model crfModel = Model.getNewModel(3, "Naive");
			Properties options = new Properties();
			crf = new CRF(crfModel.numStates(),featureGen,options);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private Segmenter segmenter;
	private Dictionary dict;
	
	private ByCatFeatureCalculator fcByCat;
	private CRF crf;
	private EnumsFeatureGenerator featureGen;

	public void train(String trainCsvFile) {
		// 样本数据读取
		CsvProductsReader reader = new CsvProductsReader(trainCsvFile, true);
		Vector<CatWordDataSequence> datas = new Vector<CatWordDataSequence>();
		ProductTextImpl p = reader.next();
		while (p != null) {
			CatWordDataSequence seq = new CatWordDataSequence(p, true, segmenter, dict);
			datas.add(seq);
			p = reader.next();
		}

		// 样本特征计算
		CatWordDataIter trainData = new CatWordDataIter(datas);
	    featureGen.trainPrepare(trainData);
	    
	    /*
	    System.out.println("*** featureNames ***");
	    for (int i=0; i < featureGen.numFeatures(); i++)
	    	System.out.println(featureGen.featureName(i));
		for (trainData.startScan(); trainData.hasNext();) {
		    DataSequence data = trainData.next();
		    printFtGenerator(data, featureGen);
		}
	    */
	    
		// 样本训练
	    crf.train(trainData);
	}
	
	public void train(ProductTrainerService ptService) {
		
		long count = ptService.getAcountByObj(null);
		if(count>0){
			long shang = count/Constant.SELECT_SIZE;
			long mod = count%Constant.SELECT_SIZE;
			if(mod!=0){
				shang++;
			}

			// 样本数据读取
			Vector<CatWordDataSequence> datas = new Vector<CatWordDataSequence>();
			ProductTrainer ptCheck = null;
			for(int i = 0;i<shang;i++){
				ptCheck = new ProductTrainer();
				//分页
				ptCheck.setStartPage(i*Constant.SELECT_SIZE);
				ptCheck.setpAccount(Constant.SELECT_SIZE);
				
				List<ProductTrainer> listPt = ptService.getListProductTrainer(ptCheck);
				if(listPt!=null && listPt.size()>0){
					ProductTextImpl p = null;
					for (ProductTrainer pt: listPt) {
						p = new ProductTextImpl();
						p.setBrandName(pt.getBrandName());
						p.setBrandWords(pt.getBrandWords());
						p.setCategoryName(pt.getCategoryName());
						p.setCategoryWords(pt.getCategoryWords());
						p.setProductCode(pt.getProductCode());
						p.setProductName(pt.getProductName());
//						p.setProductTag(pt.getP);
						p.setSplitedWords(pt.getSplitedWords());
						p.setNameSubtitle(pt.getProductSubTitle());
						CatWordDataSequence seq = new CatWordDataSequence(p, true, segmenter, dict);
						datas.add(seq);
					}
				}
			}

			// 样本特征计算
			CatWordDataIter trainData = new CatWordDataIter(datas);
		    featureGen.trainPrepare(trainData);
		    
		    /*
		    System.out.println("*** featureNames ***");
		    for (int i=0; i < featureGen.numFeatures(); i++)
		    	System.out.println(featureGen.featureName(i));
			for (trainData.startScan(); trainData.hasNext();) {
			    DataSequence data = trainData.next();
			    printFtGenerator(data, featureGen);
			}
		    */
		    
			// 样本训练
		    crf.train(trainData);
			
		}
	}
	
	@Override
	public void recognize(ProductTextImpl p) {
		// 类别匹配为no的,不进行词性识别
		if (fcByCat != null) {
			if ("no".equals(fcByCat.calc(p.getCategoryName()))) {
				return;
			}
		}
		
		// 进行CRF识别
		CatWordDataSequence seq = new CatWordDataSequence(p, false, segmenter, dict);
		crf.apply(seq);
		
		// 转换CRF识别的结果
		StringBuilder strWords = new StringBuilder();
		StringBuilder strCatWords = new StringBuilder();
		StringBuilder strBrandWords = new StringBuilder();
		for (int i=0; i < seq.allWords.length; i++) {
			if (strWords.length() > 0)
				strWords.append(',');
			strWords.append(seq.allWords[i]);

			if (seq.labels[i] == CatWordDataSequence.LABEL_catWord) {
				if (strCatWords.length() > 0)
					strCatWords.append(',');
				strCatWords.append(seq.allWords[i]);
			}

			if (seq.labels[i] == CatWordDataSequence.LABEL_brandWord) {
				if (strBrandWords.length() > 0)
					strBrandWords.append(',');
				strBrandWords.append(seq.allWords[i]);
			}
		}
		p.setSplitedWords(strWords.toString());
		p.setCategoryWords(strCatWords.toString());
		p.setBrandWords(strBrandWords.toString());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		if (fcByCat != null)
			out.writeObject(fcByCat.pattern);
		else
			out.writeObject(null);
		
		HashSet<String> calculatorNames = new HashSet<String>();
		calculatorNames.addAll(featureGen.getCalculatorNames());
		out.writeObject(calculatorNames);
		
		out.writeObject(featureGen);

		double[] featureWeights = crf.getLearntWeights();
	    out.writeInt(featureWeights.length);
	    for (int i=0; i < featureWeights.length; i++)
	    	out.writeDouble(featureWeights[i]);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		Object objByCatPattern = in.readObject();
		if (objByCatPattern != null) {
			fcByCat = new ByCatFeatureCalculator();
			fcByCat.pattern = (ByCatNamePattern)objByCatPattern;
		}
		else fcByCat = null;
		
		HashSet<String> calculatorNames = (HashSet<String>)in.readObject();
		HashMap<String, FeatureCalculator> namedCalculators = new HashMap<String, FeatureCalculator>();
		for (String calculatorName : calculatorNames) {
			FeatureCalculator calculator;
			if ("ByCat".equals(calculatorName))
				calculator = fcByCat;
			else
				calculator = allCalculators.get(calculatorName);
			if (calculator == null)
				throw new RuntimeException("Unkown calculators: " + calculatorName);
			namedCalculators.put(calculatorName, calculator);
		}
		
		featureGen = (EnumsFeatureGenerator)in.readObject();
		featureGen.init(namedCalculators);
		initCRF(featureGen);

		int weightsLength = in.readInt();
		double[] featureWeights = new double[weightsLength];
	    for (int i=0; i < weightsLength; i++)
	    	featureWeights[i] = in.readDouble();
	    crf.setLearntWeights(featureWeights);
	}
}

//可能是品牌词(根据词典中的词性数据判断)
class IsBrandWordFeatCalc implements BoolFeatureCalculator {

	@Override
	public boolean calc(DataSequence data, int pos) {
		return ((CatWordDataSequence)data).isBrandWords[pos];
	}
	
}

//可能是类别词(根据词典中的词性数据判断)
class IsCatWordFeatCalc implements BoolFeatureCalculator {

	@Override
	public boolean calc(DataSequence data, int pos) {
		return ((CatWordDataSequence)data).isCatWords[pos];
	}
	
}

//可能品牌词中的第一个
class IsFirstBrandWordFeatCalc implements BoolFeatureCalculator {

	@Override
	public boolean calc(DataSequence data, int pos) {
		return ((CatWordDataSequence)data).isFirstBrandWords[pos];
	}
	
}

//可能类别词中的最后一个
class IsLastCatWordFeatCalc implements BoolFeatureCalculator {

	@Override
	public boolean calc(DataSequence data, int pos) {
		return ((CatWordDataSequence)data).isLastCatWords[pos];
	}
	
}

//类别名(CategoryName)中包含的词
class IsNameCatWordFeatCalc implements BoolFeatureCalculator {

	@Override
	public boolean calc(DataSequence data, int pos) {
		return ((CatWordDataSequence)data).isCatNameWords[pos];
	}
	
}

// 类别名(CategoryName)中类似的词
class LikeNameCatWordFeatCalc implements BoolFeatureCalculator {

	@Override
	public boolean calc(DataSequence data, int pos) {
		return ((CatWordDataSequence)data).likeCatNameWords[pos];
	}
	
}

//品牌名(BrandName)中包含的词
class IsBrandNameWordFeatCalc implements BoolFeatureCalculator {

	@Override
	public boolean calc(DataSequence data, int pos) {
		return ((CatWordDataSequence)data).isBrandNameWords[pos];
	}
	
}

// 可能是量词
class IsMeasureWordFeatCalc implements BoolFeatureCalculator {

	@Override
	public boolean calc(DataSequence data, int pos) {
		if (pos == 0) return false;
		CatWordDataSequence seq = (CatWordDataSequence)data;
		if (seq.allWords[pos].length()==1) {
			if (seq.allWords[pos-1].equals("/"))
				return true;
			
			boolean isNumber = true;
			String ts = seq.allWords[pos-1];
			for (int i=0; i < ts.length()-1; i++) {
				char tc = ts.charAt(i);
				if (((tc < '0')||(tc > '9')) && (tc != '.')) {
					isNumber = false;
					break;
				}
			}
			if (isNumber) return true;
		}
		return false;
	}
	
}

// 附加 '赠送'
class IsSongFeatCalc implements BoolFeatureCalculator {

	@Override
	public boolean calc(DataSequence data, int pos) {
		if (pos == 0) return false;
		CatWordDataSequence seq = (CatWordDataSequence)data;
		for (int i=pos-1; i >= 0; i--) {
			String preWord = seq.allWords[i];
			if (preWord.equals("赠送"))
				return true;
			if (preWord.equals("赠"))
				return true;
			if (preWord.equals("送"))
				return true;
		}
		return false;
	}
	
}

// 在括号之内
class IsInBracketFeatCalc implements BoolFeatureCalculator {
	final static HashSet<String> bracketLeftChars = new HashSet<String>();
	final static HashSet<String> bracketRightChars = new HashSet<String>();
	static {
		bracketLeftChars.add("(");
		bracketRightChars.add(")");
		bracketLeftChars.add("（");
		bracketRightChars.add("）");
		bracketLeftChars.add("【");
		bracketRightChars.add("】");
	}

	@Override
	public boolean calc(DataSequence data, int pos) {
		if (pos == 0) return false;
		if (pos == data.length()-1) return false;
		CatWordDataSequence seq = (CatWordDataSequence)data;

		boolean inLeft = false, inRight = false;
		for (int i=pos-1; i >= 0; i--) {
			String word = seq.allWords[i];
			if (bracketLeftChars.contains(word)) {
				inLeft = true;
				break;
			}
			if (bracketRightChars.contains(word)) {
				break;
			}
		}
		for (int i=pos+1; i < data.length(); i++) {
			String word = seq.allWords[i];
			if (bracketRightChars.contains(word)) {
				inRight = true;
				break;
			}
			if (bracketLeftChars.contains(word)) {
				break;
			}
		}
		return (inLeft && inRight);
	}
}

// '赠品'之前 (xx赠品)
class IsZengPinFeatCalc implements BoolFeatureCalculator {

	@Override
	public boolean calc(DataSequence data, int pos) {
		if (pos == data.length()-1) return false;
		CatWordDataSequence seq = (CatWordDataSequence)data;

		String nextWord = seq.allWords[pos+1];
		if (nextWord.equals("赠品"))
			return true;
		return false;
	}
	
}

// 是否单字
class IsSingleCharFeatCalc implements BoolFeatureCalculator/*, EdgeFeatureCalculator*/ {

	@Override
	public boolean calc(DataSequence data, int pos) {
		return ((CatWordDataSequence)data).allWords[pos].length() == 1;
	}
	
}

// 是否有其它类别词
class NoAnotherCatWordsFeatCalc implements BoolFeatureCalculator {

	@Override
	public boolean calc(DataSequence data, int pos) {
		String[] allWords = ((CatWordDataSequence)data).allWords;
		boolean[] isCatWords = ((CatWordDataSequence)data).isCatWords;
		String currWord = allWords[pos];
		for (int i=0; i < isCatWords.length; i++) {
			if (! isCatWords[i]) continue;
			if (! allWords[i].equals(currWord))
				return false;
		}
		return true;
	}
	
}

// 词的最后一个字符作为特征
class LastCharFeatureCalculator implements EnumFeatureCalculator {

	@Override
	public String calc(DataSequence data, int pos) {
		String ts = (String)data.x(pos);
		return String.valueOf(ts.charAt(ts.length()-1));
	}

}

/**
 * 根据正则表达式匹配CategoryName计算出产品所属的大分类
 */
class ByCatFeatureCalculator implements EnumFeatureCalculator {
	public ByCatNamePattern pattern;
	
	public String calc(String catSearchName) {
		if (pattern == null) return null;
		return pattern.match(catSearchName);
	}
	
	@Override
	public String calc(DataSequence data, int pos) {
		if (pattern == null) return null;
		return pattern.match(((CatWordDataSequence)data).catSearchName);
	}
};
