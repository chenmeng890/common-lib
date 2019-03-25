package com.yihaodian.nlp;

import java.util.Vector;

import iitb.CRF.DataIter;
import iitb.CRF.DataSequence;

public class LineTrainData implements DataIter {
  Vector trainRecs;
  int pos;
  
  LineTrainData(Vector trs) {
  	trainRecs = trs;
  }

  public void startScan() {
  	pos = 0;
  }

  public boolean hasNext() {
  	return (pos < trainRecs.size());
  }

  public DataSequence next() {
  	return (LineDataSequence)trainRecs.elementAt(pos++);
  }

}
