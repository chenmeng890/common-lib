package com.yihaodian.search.nlp.segment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.yihaodian.search.nlp.model.Dictionary;

public class SegmenterFactory {
   
	private  static Dictionary dict=null;
	
	/**
	 * 逆向最大匹配+ N-gram
	 * @param dictPath
	 * @return
	 * @throws IOException
	 */
	public  static Segmenter newDefaultSegmenter(String dictPath) throws IOException{
		dict=new Dictionary(dictPath, true);
		ReverseSegmenter seg = new ReverseSegmenter(dict,
				ReverseSegmenter.TYPE_LATIN_MIXED,false,false);
		WordSliceSegmenter segmenter=new WordSliceSegmenter(seg);
		return segmenter;
	}
	
	public  static Segmenter mixSegmenter(String dictPath,List<Segmenter> segmenters,boolean isNeedMiniSeg){
		dict=new Dictionary(dictPath, true);
		MixSegmenter ms = new MixSegmenter(dict, segmenters, isNeedMiniSeg);
		return ms;
	}
	
	public  static Segmenter mixSegmenter(String dictPath,Dictionary dict,int segType,boolean isNeedMiniSeg){
		if(dict==null){
			dict=new Dictionary(dictPath, true);
		}
		List<Segmenter> segmenters = new ArrayList<Segmenter>();
	    ReverseSegmenter rSeg= new ReverseSegmenter(dict, ReverseSegmenter.TYPE_LATIN_MIXED, true,false,true);
		ReverseSegmenter rSeg_rp= new ReverseSegmenter(dict, ReverseSegmenter.TYPE_LATIN_ORIGINAL, true, true);
		ForwardSegmenter fSeg =new ForwardSegmenter(dict, ReverseSegmenter.TYPE_LATIN_ORIGINAL, true,false);
		WordSliceSegmenter rsegmenter=new WordSliceSegmenter(rSeg);
		WordSliceSegmenter fsegmenter=new WordSliceSegmenter(fSeg);
		switch(segType){
		   case MixSegmenter.FORWARD_SEG:
			   segmenters.add(fSeg);
			   break;
		   case MixSegmenter.REVERSE_SEG:
			   segmenters.add(rSeg);
			   break;
		   case MixSegmenter.FORWORD_REVERSE_SEG:
			   segmenters.add(fSeg);
			   segmenters.add(rSeg);
			   break;
		   case MixSegmenter.SLICE_FORWARD_SEG:
			   segmenters.add(fsegmenter);
			   break;
		   case MixSegmenter.SLICE_REVERSE_SEG:
			   segmenters.add(rsegmenter);
			   break;
		   case MixSegmenter.REVERSE_SEG_RP:
			   segmenters.add(rSeg_rp);
			   break;
		   default:
		}
		MixSegmenter ms = new MixSegmenter(dict, segmenters, isNeedMiniSeg);
		return ms;
	}

	/**
	 * 最小粒度+正向最大+逆向最大切分
	 * @param dictPath
	 * @param dict
	 * @return
	 */
	public  static Segmenter defaultMixSegmenter(String dictPath,Dictionary dict){
		return mixSegmenter(dictPath,dict, MixSegmenter.FORWORD_REVERSE_SEG, true);
	}

	public  static Segmenter newQuerySegmenter(String dictPath){
		return newQuerySegmenter(dictPath,ReverseSegmenter.TYPE_LATIN_SPLIT);
	}
	
	public  static Segmenter newQuerySegmenter(String dictPath , int ReverseSegmenterType){
		dict=new Dictionary(dictPath, true);
		ReverseSegmenter seg = new ReverseSegmenter(dict,ReverseSegmenterType,false,false,true);
		return seg;
	}
	
	public static Segmenter newQuerySegmenter(Dictionary dict){
		return new ReverseSegmenter(dict,ReverseSegmenter.TYPE_LATIN_SPLIT,false,false,true);
	}
	
	public static Segmenter newQuerySegmenter(Dictionary dict,int ReverseSegmenterType ){
		return new ReverseSegmenter(dict,ReverseSegmenterType,false,false,true);
	}

}
