package com.yihaodian.wordlabel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Properties;

import iitb.CRF.CRF;
import iitb.CRF.DataSequence;
import iitb.CRF.Feature;
import iitb.CRF.FeatureGenerator;
import iitb.Model.Model;

import au.com.bytecode.opencsv.CSVWriter;

import com.yihaodian.crf.BoolFeatureCalculator;
import com.yihaodian.crf.EdgeFeatureCalculator;
import com.yihaodian.crf.EndFeatureCalculator;
import com.yihaodian.crf.EnumFeatureCalculator;
import com.yihaodian.crf.EnumsFeatureGenerator;
import com.yihaodian.crf.FeatureCalculator;
import com.yihaodian.crf.SimpleEdgeFeatureCalculator;
import com.yihaodian.crf.StartFeatureCalculator;
import com.yihaodian.crf.WordFeatureCalculator;
import com.yihaodian.nlp.LineDataSequence;

public class CategoryLabelTest {

  Properties options = new Properties();
  CRF crfModel;
  EnumsFeatureGenerator featureGen;
  
  String baseDir = "D:/workspace/study/search-analyzer/data";

  /**
	 * @param args
	 */
  public static void main(String[] args) throws Exception {
	  CategoryLabelTest crf = new CategoryLabelTest();
	  //crf.dotest("服装鞋帽");
	  //crf.dotest("办公用品");
	  
	  //crf.dotest("图书音像");
	  //crf.dotest("玩具");
	  
	  //crf.dotest("食品饮料");
	  crf.dotest("数码家电");
  }


  public void dotest(String catFileName) throws Exception {
	HashMap<String, FeatureCalculator> namedCalculators = new HashMap<String, FeatureCalculator>();
  	namedCalculators.put("Start", new StartFeatureCalculator());
  	namedCalculators.put("Edge", new SimpleEdgeFeatureCalculator());
  	namedCalculators.put("End", new EndFeatureCalculator());
  	namedCalculators.put("W", new WordFeatureCalculator());
  	namedCalculators.put("LC", new LastCharFeatureCalculator());
  	namedCalculators.put("IsBrand", new IsBrandWordFeatCalc());
  	namedCalculators.put("IsCat", new IsCatWordFeatCalc());
  	namedCalculators.put("IsNameCat", new IsNameCatWordFeatCalc());
  	namedCalculators.put("LikeNameCat", new LikeNameCatWordFeatCalc());
  	namedCalculators.put("IsSingleChar", new IsSingleCharFeatCalc());
  	namedCalculators.put("NoAnother", new NoAnotherCatWordsFeatCalc());
  	namedCalculators.put("IsMeasure", new IsMeasureWordFeatCalc());
  	namedCalculators.put("IsSong", new IsSongFeatCalc());
  	//namedCalculators = null;
  	
  	Model model = Model.getNewModel(3, "Naive");
    featureGen = new EnumsFeatureGenerator(namedCalculators);
    crfModel=new CRF(model.numStates(),featureGen,options);

  	//** train
    CsvDataIter trainData = new CsvDataIter(new File(baseDir, catFileName+"_train.csv"), new CategoryCsvSeqFactory());
    featureGen.trainPrepare(trainData);
    
    System.out.println("*** featureNames ***");
    for (int i=0; i < featureGen.numFeatures(); i++)
    	System.out.println(featureGen.featureName(i));
    
	for (trainData.startScan(); trainData.hasNext();) {
	    DataSequence data = trainData.next();
	    printFtGenerator(data, featureGen);
	}
    
    double featureWts[] = crfModel.train(trainData);

    //** test
    CsvDataIter testData = new CsvDataIter(new File(baseDir, catFileName+".csv"), new CategoryCsvSeqFactory());
	File outFile = new File(baseDir, catFileName+"_tested.csv");
	CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(outFile), "GBK"));
	boolean writedHeader = false;
	
	System.out.println("****** test **************");
    while(testData.hasNext()) { 
    	CategoryCsvDataSequence testRecord = (CategoryCsvDataSequence)testData.next();
    	if (! writedHeader) {
    	  	writer.writeNext(testRecord.headers);
    		writedHeader = true;
    	}
    	for (int i=0; i < testRecord.length(); i++)
    		testRecord.set_y(i, 1);
    	
		crfModel.apply(testRecord);
		//featureGen.mapStatesToLabels(testRecord);
		
		testRecord.calcLabeledCatWords();
	  	writer.writeNext(testRecord.nextLine);
    }
    writer.close();
  }
  
  private void printFtGenerator(DataSequence seq, FeatureGenerator ftGenerator) {
	    System.out.println("#######################################");
	  	System.out.println("###  "+((CategoryCsvDataSequence)seq).toLine()+" ###");
	    for (int l = 0; l < seq.length(); l++) {
	    	ftGenerator.startScanFeaturesAt(seq, l);
	    	System.out.println("**** l="+l+"   num="+ftGenerator.numFeatures()+" ***********************");
	      while (ftGenerator.hasNext()) {
	      	Feature feature = ftGenerator.next();
	      	System.out.print(feature.index());
	      	System.out.print(" *n=");
	      	System.out.print(ftGenerator.featureName(feature.index()));
	      	System.out.print(" *y=");
	      	System.out.print(feature.y());
	      	System.out.print(" *v=");
	      	System.out.print(feature.value());
	      	System.out.print(" *yprev=");
	      	System.out.print(feature.yprev());
	      	System.out.print(" *yprevArray=");
	      	System.out.print(feature.yprevArray());
	      	System.out.println();
	      }
	    }  	
  }
	  
}

class IsBrandWordFeatCalc implements BoolFeatureCalculator {

	@Override
	public boolean calc(DataSequence data, int pos) {
		return ((CategoryCsvDataSequence)data).isBrandWords[pos];
	}
	
}

class IsCatWordFeatCalc implements BoolFeatureCalculator {

	@Override
	public boolean calc(DataSequence data, int pos) {
		return ((CategoryCsvDataSequence)data).isCatWords[pos];
	}
	
}

class IsNameCatWordFeatCalc implements BoolFeatureCalculator {

	@Override
	public boolean calc(DataSequence data, int pos) {
		return ((CategoryCsvDataSequence)data).isCatNameWords[pos];
	}
	
}

class LikeNameCatWordFeatCalc implements BoolFeatureCalculator {

	@Override
	public boolean calc(DataSequence data, int pos) {
		return ((CategoryCsvDataSequence)data).likeCatNameWords[pos];
	}
	
}

// 可能是量词
class IsMeasureWordFeatCalc implements BoolFeatureCalculator {

	@Override
	public boolean calc(DataSequence data, int pos) {
		if (pos == 0) return false;
		CategoryCsvDataSequence seq = (CategoryCsvDataSequence)data;
		if (seq.allWords[pos].length()==1) {
			if (seq.allWords[pos-1].equals("/"))
				return true;
			
			boolean isNumber = true;
			String ts = seq.allWords[pos-1];
			for (int i=0; i < ts.length()-1; i++) {
				char tc = ts.charAt(i);
				if ((tc < '0')||(tc > '9')) {
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
		CategoryCsvDataSequence seq = (CategoryCsvDataSequence)data;
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


class IsSingleCharFeatCalc implements BoolFeatureCalculator/*, EdgeFeatureCalculator*/ {

	@Override
	public boolean calc(DataSequence data, int pos) {
		return ((CategoryCsvDataSequence)data).allWords[pos].length() == 1;
	}
	
}

class NoAnotherCatWordsFeatCalc implements BoolFeatureCalculator {

	@Override
	public boolean calc(DataSequence data, int pos) {
		String[] allWords = ((CategoryCsvDataSequence)data).allWords;
		boolean[] isCatWords = ((CategoryCsvDataSequence)data).isCatWords;
		String currWord = allWords[pos];
		for (int i=0; i < isCatWords.length; i++) {
			if (! isCatWords[i]) continue;
			if (! allWords[i].equals(currWord))
				return false;
		}
		return true;
	}
	
}

class LastCharFeatureCalculator implements EnumFeatureCalculator {

	private static final long serialVersionUID = 4596043013261411598L;

	@Override
	public String calc(DataSequence data, int pos) {
		String ts = (String)data.x(pos);
		return String.valueOf(ts.charAt(ts.length()-1));
	}

}

