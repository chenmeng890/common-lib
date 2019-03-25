package com.yihaodian.crf;

import iitb.CRF.DataIter;
import iitb.CRF.DataSequence;
import iitb.CRF.Feature;
import iitb.CRF.FeatureGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 特征生成器(仅支持FeatureCalculator特征计算)
 */
public class EnumsFeatureGenerator implements FeatureGenerator {
	
	private static final long serialVersionUID = -9201978793405568394L;

	public EnumsFeatureGenerator(HashMap<String, FeatureCalculator> namedCalculators) {
		if (namedCalculators == null) {
			namedCalculators = new HashMap<String, FeatureCalculator>();
		  	namedCalculators.put("Start", new StartFeatureCalculator());
		  	namedCalculators.put("Edge", new SimpleEdgeFeatureCalculator());
		  	namedCalculators.put("End", new EndFeatureCalculator());
		  	namedCalculators.put("W", new WordFeatureCalculator());
		}
		
		features = new HashMap<String, Integer>();
		featuresName = new HashMap<Integer, String>();
		nolFeatures = new HashMap<String, List<EnumFeature>>();
		this.namedCalculators = namedCalculators;
	}
	
	private HashMap<String, FeatureCalculator> namedCalculators;
	
	private HashMap<String, Integer> features;
	private HashMap<Integer, String> featuresName;
	
	private HashMap<String, List<EnumFeature>> nolFeatures;
	
	private EnumFeature featureToReturn = new EnumFeature();
	private ArrayList<EnumFeature> calculatingFeaturesIndex = new ArrayList<EnumFeature>();
	private Iterator<EnumFeature> calculatingIterator;
	
	/**
	 * 根据训练样本数据,收集样本特征
	 * @param trainData
	 */
	public void trainPrepare(DataIter trainData) {
		for (trainData.startScan(); trainData.hasNext();) {
	    DataSequence data = trainData.next();
	    for (int pos = 0; pos < data.length(); pos++) {
	    	Iterator<Map.Entry<String, FeatureCalculator>> entries = namedCalculators.entrySet().iterator();
	    	while (entries.hasNext()) {
	    		Map.Entry<String, FeatureCalculator> entry = entries.next();
	    		String prefix = entry.getKey();
	    		FeatureCalculator calculator = entry.getValue();
	    		
	    		String vf = calcFeature(calculator, data, pos);
	    		if (vf == null) continue;
	    		
	    		int vl = data.y(pos);
	    		String nolKey = prefix+"."+vf;
	    		String key;
	    		if (calculator instanceof EdgeFeatureCalculator) {
	    			if (pos == 0) continue;
	    			key = nolKey+"."+data.y(pos-1)+"."+vl;
	    		}
	    		else {
	    			key = nolKey+"."+vl;
	    		}

	    		if (! features.containsKey(key)) {
	    			int featureIndex = features.size();
						features.put(key, featureIndex);
						featuresName.put(featureIndex, key);
						
						List<EnumFeature> enumFeatures = nolFeatures.get(nolKey);
						if (enumFeatures == null) {
							enumFeatures = new ArrayList<EnumFeature>();
							nolFeatures.put(nolKey, enumFeatures);
						}
						EnumFeature enumFeature = new EnumFeature();
						enumFeature.featureIndex = featureIndex;
						enumFeature.label = vl;
						if (calculator instanceof EdgeFeatureCalculator) {
		    			//if (pos == 0) continue;
		    			enumFeature.prevLabel = data.y(pos-1);
		    		}
						enumFeatures.add(enumFeature);
	    		}
	    	}
	    }
		}
	}

	@Override
	public int numFeatures() {
		return features.size();
	}

	/**
	 * 样本特征计算
	 */
	@Override
	public void startScanFeaturesAt(DataSequence data, int pos) {
		calculatingFeaturesIndex.clear();
		
  	Iterator<Map.Entry<String, FeatureCalculator>> entries = namedCalculators.entrySet().iterator();
  	while (entries.hasNext()) {
  		Map.Entry<String, FeatureCalculator> entry = entries.next();
  		String prefix = entry.getKey();
  		FeatureCalculator calculator = entry.getValue();

  		String vf = calcFeature(calculator, data, pos);
  		if (vf == null) continue;
  		
  		String nolKey = prefix+"."+vf;
			List<EnumFeature> enumFeatures = nolFeatures.get(nolKey);
			if (enumFeatures != null) {
				calculatingFeaturesIndex.addAll(enumFeatures);
			}
  	}
  	calculatingIterator = calculatingFeaturesIndex.iterator();
	}
	
	private String calcFeature(FeatureCalculator calculator, DataSequence data, int pos) {
		String vf = null;
		if (calculator instanceof BoolFeatureCalculator) {
			if (((BoolFeatureCalculator)calculator).calc(data, pos))
				vf = "!";
		}
		else if (calculator instanceof EnumFeatureCalculator)
			vf = ((EnumFeatureCalculator)calculator).calc(data, pos);
		
		if (vf == null)
			return null;
		
		return vf;
	}
	

	@Override
	public boolean hasNext() {
		return calculatingIterator.hasNext();
	}

	@Override
	public Feature next() {
		EnumFeature enumFeature = calculatingIterator.next();
		featureToReturn.featureIndex = enumFeature.featureIndex;
		featureToReturn.label = enumFeature.label;
		featureToReturn.prevLabel = enumFeature.prevLabel;
		return featureToReturn;
	}

	@Override
	public String featureName(int featureIndex) {
		return featuresName.get(featureIndex);
	}
}

/**
 * 特征值(仅支持值 1)
 */
class EnumFeature implements Feature {

	public EnumFeature() {
	}
	
	public int featureIndex, label;
	public int prevLabel = -1;
	
	@Override
	public int index() {
		return featureIndex;
	}

	@Override
	public int y() {
		return label;
	}

	@Override
	public int yprev() {
		return prevLabel;
	}

	@Override
	public float value() {
		return 1;
	}

	@Override
	public int[] yprevArray() {
		return null;
	}
	
}