package yooso.analyzer;

import java.io.*;
import java.util.*;

import org.apache.lucene.analysis.*;

public class ReverseAnalyzer extends Analyzer {
  public ReverseAnalyzer() {
    Dictionary dictionary = new Dictionary(true);
    try {
		InputStream dictStream = ReverseAnalyzer.class.getResourceAsStream("Base.dict");
		if (dictStream == null)
			throw new RuntimeException("Base.dict词典丢失,无法加载.");
		dictionary.AddDict(dictStream, false);

		dictStream = ReverseAnalyzer.class.getResourceAsStream("BaseChilds.dict");
		if (dictStream == null)
			throw new RuntimeException("BaseChilds.dict词典丢失,无法加载.");
		dictionary.AddDict_Child(dictStream, false);

		dictStream = ReverseAnalyzer.class.getResourceAsStream("XNum.dict");
		if (dictStream == null)
			throw new RuntimeException("XNum.dict词典丢失,无法加载.");
		dictionary.AddDict(dictStream, true);

		dictStream = ReverseAnalyzer.class.getResourceAsStream("XNum.dict");
		if (dictStream == null)
			throw new RuntimeException("XNum.dict词典丢失,无法加载.");
		dictionary.AddDict_Child(dictStream, true);
	} catch (Exception e) {
		throw new RuntimeException(e);
	}
    this.defaultSplitor = new ReverseSplitor(dictionary, false, false);
  }
  
  public ReverseAnalyzer(Dictionary dictionary) {
	this.defaultSplitor = new ReverseSplitor(dictionary, false, false);
  }
  
  public ReverseAnalyzer(ReverseSplitor defaultSplitor) {
  	this.defaultSplitor = defaultSplitor;
  }
  
  public ReverseAnalyzer(ReverseSplitor defaultSplitor, HashMap<String, ReverseSplitor> fieldSplitors) {
  	this.defaultSplitor = defaultSplitor;
  	this.fieldSplitors = fieldSplitors;
  }
  
  ReverseSplitor defaultSplitor;
  HashMap<String, ReverseSplitor> fieldSplitors;
  
  public final TokenStream tokenStream(String fieldName, Reader reader) {
  	ReverseSplitor splitor = defaultSplitor;
  	if (fieldSplitors != null)
  		if (fieldSplitors.containsKey(fieldName))
  			splitor = fieldSplitors.get(fieldName);
  	return new ReverseTokenStream(reader, splitor);
  }
}
