package com.yihaodian.nlp;

import iitb.CRF.CRF;
import iitb.CRF.DataSequence;
import iitb.CRF.Feature;
import iitb.CRF.FeatureGenerator;
import iitb.Model.Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Properties;
import java.util.Vector;

import com.yihaodian.crf.EnumsFeatureGenerator;

public class TestEnumsFeatureGenerator {
  Properties options = new Properties();
  CRF crfModel;
  EnumsFeatureGenerator featureGen;
  
  String baseDir = "D:/workspace/study/StudyCRF/samples/mysimple";
  //String baseDir = "D:/workspace/study/StudyCRF/samples/verysimple";
  
  public TestEnumsFeatureGenerator() {
  	
  }
  
  public static void main(String argv[]) throws Exception {
  	TestEnumsFeatureGenerator crf = new TestEnumsFeatureGenerator();
  	crf.dotest();
  }

  private void printFtGenerator(DataSequence seq, FeatureGenerator ftGenerator) {
    System.out.println("#######################################");
  	System.out.println("###  "+((LineDataSequence)seq).toLine()+" ###");
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
  

  public void dotest() throws Exception {
  	Model model = Model.getNewModel(3, "Naive");
    featureGen = new EnumsFeatureGenerator(null);
    crfModel=new CRF(model.numStates(),featureGen,options);

  	//** train
  	LineTrainData trainData = readLineData("train.txt");
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
  	LineTrainData testData = readLineData("test.txt");
  	
		System.out.println("****** test **************");
    while(testData.hasNext()) { 
    	LineDataSequence testRecord = (LineDataSequence)testData.next();
    	for (int i=0; i < testRecord.length(); i++)
    		testRecord.set_y(i, 1);
    	
			crfModel.apply(testRecord);
			//featureGen.mapStatesToLabels(testRecord);
			
			System.out.println(testRecord.toLine());
    }
  }

  LineTrainData readLineData(String fileName) throws Exception {
    BufferedReader reader = new BufferedReader(new FileReader(new File(baseDir, fileName)));
    Vector vect = new Vector();
    String readLine = reader.readLine();
    while (readLine != null) {
    	String[] words = readLine.split(", ");
    	vect.add(new LineDataSequence(words));
    	readLine = reader.readLine();
    }
  	return new LineTrainData(vect);
  }
  
}
