package com.yihaodian.search.nlp.model;

import java.util.HashMap;
import java.util.Map;


/**
 * 字典细胞
 * @author yuqian
 *
 */
public class DictCell {
	//字典首字符表
    private static final Map<Character,Character> firstCharMap = new HashMap<Character,Character>();
    //数组大小上限
    private static final int ARRAY_LENGTH_LIMIT = 3;
    
    private Character nodeChar;
    
    private Map<Character,DictCell> childrenMap;
    
   private DictCell[] childrenArray;
    //当前节点对应的同义词组
   /*  private String[] synonyms;

	//当前节点对应的扩展词
    private String[] extendWords;*/
  //当前节点对应的词元
    private Lexeme lexeme;
    //当前节点存储的Cell数目
	//storeSize <=ARRAY_LENGTH_LIMIT ，使用数组存储， storeSize >ARRAY_LENGTH_LIMIT ,则使用Map存储
	private int storeSize = 0;
	
	//当前DictCell状态 ,默认 0 , 1表示从根节点到当前节点的路径表示一个词
	private int nodeState = 0;
	
	public DictCell(Character nodeChar){
		if(nodeChar == null){
			throw new IllegalArgumentException("参数为空异常，字符不能为空");
		}
		this.nodeChar = nodeChar;
	}

	public Character getNodeChar() {
		return nodeChar;
	}
	
	/*
	 * 判断是否有下一个节点
	 */
	public boolean hasNextNode(){
		return  this.storeSize > 0;
	}
	
	/**
	 * 查找词典中是否有该词
	 * @param charArray
	 * @param begin
	 * @param length
	 * @return
	 */
	public Lexeme match(char[] charArray,int begin,int length){
		Character ch = Character.valueOf(charArray[begin]);

		DictCell cell=null;
		
		DictCell[] cellArr=this.childrenArray;
		Map<Character,DictCell> cellMap=this.childrenMap;
		
		if(cellArr!=null){
			//在数组中查找
			for(DictCell dc:cellArr){
				if(dc!=null&&dc.nodeChar.equals(ch)){
					cell=dc;
					break;
				}
			}
		}else if(cellMap!=null){
			//在map中查找
			cell=cellMap.get(ch);
		}
		//找到DictCell，判断词的匹配状态，是否继续递归还是返回结果
		if(cell!=null){
			if(length>1){
				//词未匹配完，继续往下搜索
				return cell.match(charArray, begin+1, length-1);
			}
			else if(length==1){
				if(cell.nodeState==1){
					return cell.lexeme;
				}
			}
		}
		return null;
	}
	
	/**
	 * 匹配词段
	 * @param charArray
	 * @param begin
	 * @param length
	 * @param searchHit
	 * @return Hit 
	 */
	public Hit match(char[] charArray , int begin , int length , Hit searchHit){
		
		if(searchHit == null){
			//如果hit为空，新建
			searchHit= new Hit();
			//设置hit的其实文本位置
			searchHit.setBegin(begin);
		}else{
			//否则要将HIT状态重置
			searchHit.setUnmatch();
		}
		//设置hit的当前处理位置
		searchHit.setEnd(begin);
		
		Character keyChar = Character.valueOf(charArray[begin]);
		DictCell ds = null;
		
		//引用实例变量为本地变量，避免查询时遇到更新的同步问题
		DictCell[] segmentArray = this.childrenArray;
		Map<Character , DictCell> segmentMap = this.childrenMap;		
		
		//STEP1 在节点中查找keyChar对应的DictSegment
		if(segmentArray != null){
			//在数组中查找
			for(DictCell seg : segmentArray){
				if(seg != null && seg.nodeChar.equals(keyChar)){
					//找到匹配的段
					ds = seg;
				}
			}
		}else if(segmentMap != null){
			//在map中查找
			ds = (DictCell)segmentMap.get(keyChar);
		}
		
		//STEP2 找到DictSegment，判断词的匹配状态，是否继续递归，还是返回结果
		if(ds != null){			
			if(length > 1){
				//词未匹配完，继续往下搜索
				return ds.match(charArray, begin + 1 , length - 1 , searchHit);
			}else if (length == 1){
				
				//搜索最后一个char
				if(ds.nodeState == 1){
					//添加HIT状态为完全匹配
					searchHit.setMatch();
				}
				if(ds.hasNextNode()){
					//添加HIT状态为前缀匹配
					searchHit.setPrefix();
					//记录当前位置的DictSegment
					searchHit.setMatchedDictCell(ds);
				}
				return searchHit;
			}
			
		}
		//STEP3 没有找到DictSegment， 将HIT设置为不匹配
		return searchHit;		
	}	
	
	/**
	 * 往词典中添加一个带有扩展词的词组
	 * @param charArray 
	 * @param synonyms 
	 * @param extendWords
	 */
	public void fillSegment(String keyword,String[]extendWords,boolean isExtend){
		this.fillSegment(keyword.toCharArray(), 0, keyword.length(),null, extendWords);
		if(isExtend){
			if(extendWords!=null&&extendWords.length>0){
				for(String extend:extendWords){
					this.fillSegment(extend);	
				}
			}
		}
	}
	
	/**
	 * 往词典中添加一组同义词
	 * @param sysnonyms
	 */
	public void fillSegmentBySynonyms(String sysword,String[] synonyms){
		this.fillSegment(sysword.toCharArray(),0,sysword.length(),synonyms,null);
	}
	
	/**
	 * 往词典中添加一个词
	 * @param word
	 */
	public void fillSegment(String word){
		this.fillSegment(word.toCharArray(),0, word.length(), null, null);
	}
	
	/**
	 * 往词典里添加词
	 * @param charArray
	 * @param begin  字符串的初始位置
	 * @param length  字符串的结束位置
	 * @param synonyms  该词的同义词
	 * @param extendWords  该词的扩展词
	 */
	public synchronized void fillSegment(char[] charArray , int begin , int length,
			String[]synonyms,String[] extendWords){
		Character beginChar = Character.valueOf(charArray[begin]);
		Character keyChar = firstCharMap.get(beginChar);
		//字典中没有该字，则将其添加入字典
		if(keyChar == null){
			firstCharMap.put(beginChar, beginChar);
			keyChar = beginChar;
		}
		
		//搜索当前节点的存储，查询对应keyChar的keyChar，如果没有则创建
		DictCell dc = lookforCell(keyChar);
		//处理keyChar对应的segment
		if(length > 1){
			//词元还没有完全加入词典树
			dc.fillSegment(charArray, begin + 1, length - 1,synonyms,extendWords);
		}else if (length == 1){
			//已经是词元的最后一个char,设置当前节点状态为1，表明一个完整的词
			dc.nodeState = 1;
			if(dc.lexeme==null){
				dc.lexeme=new Lexeme();
			}
			if(extendWords!=null){
				dc.lexeme.setExtendWords(extendWords);
			}
			if(synonyms!=null){
				dc.lexeme.setSynonyms(synonyms);
			}
		}
	}
	
	/**
	 * 查找包含某个字符的字典细胞
	 * @param keychar
	 * @return
	 */
	private DictCell lookforCell(Character keychar){
		DictCell dc=null;
		
		if(this.storeSize<=ARRAY_LENGTH_LIMIT){
			DictCell[] cellArray = getChildrenArray();
			for(DictCell cell:cellArray){
				if(cell!=null&&cell.nodeChar.equals(keychar)){
					dc=cell;
					break;
				}
			}
			if(dc==null){
				dc=new DictCell(keychar);
				if(this.storeSize<ARRAY_LENGTH_LIMIT){
					cellArray[this.storeSize]=dc;
				}else{
					Map<Character , DictCell> cellMap=getChildrenMap();
					mergeChildren(cellArray,cellMap);
					cellMap.put(keychar, dc);
					this.childrenArray = null;
				}
				this.storeSize++;
			}
		}else{
			Map<Character , DictCell> cellMap=getChildrenMap();
			dc = (DictCell)cellMap.get(keychar);
			if(dc ==null){
				dc=new DictCell(keychar);
				cellMap.put(keychar, dc);
				this.storeSize++;
			}
		}
		return dc;
	}
	
	/**
	 * 获取数组容器
	 * 线程同步方法
	 */
	private DictCell[] getChildrenArray(){
			synchronized(this){
				if(this.childrenArray == null){
					this.childrenArray = new DictCell[ARRAY_LENGTH_LIMIT];
				}
			}
		return this.childrenArray;
	}
	
	/**
	 * 获取Map容器
	 * 线程同步方法
	 */	
	private Map<Character , DictCell> getChildrenMap(){
			synchronized(this){
				if(this.childrenMap == null){
					this.childrenMap = new HashMap<Character , DictCell>(ARRAY_LENGTH_LIMIT * 2,0.8f);
				}
			}
		return this.childrenMap;
	}
	
	private void mergeChildren(DictCell[] cellArray,Map<Character , DictCell> cellMap){
		for(DictCell cell:cellArray){
			if(cellMap!=null){
				cellMap.put(cell.nodeChar,cell);
			}
		}
	}
	
	/**
	 * 合并该词元的同义词
	 * @param cell
	 * @param synonyms
	 *//*
	private void fillSynonyms(DictCell cell,String[] synonyms){
		if(synonyms==null||synonyms.length==0)
			return;
		if(cell.synonyms==null||cell.synonyms.length==0)
			cell.synonyms=synonyms;
		else{
			List<String> list = new ArrayList<String>(Arrays.asList(cell.synonyms));  
			for(String ex:synonyms){
				if(!list.contains(ex))
					list.add(ex);
			}
			cell.synonyms=list.toArray(new String[list.size()]);
		}
	}
	

	public void setExtendWords(String[] extendWords) {
		this.extendWords = extendWords;
	}

	public String[] getExtendWords() {
		return extendWords;
	}
	
	public String[] getSynonyms() {
		return synonyms;
	}

	public void setSynonyms(String[] synonyms) {
		this.synonyms = synonyms;
	}*/
}
