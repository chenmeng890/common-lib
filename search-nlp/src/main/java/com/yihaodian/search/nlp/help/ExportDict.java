package com.yihaodian.search.nlp.help;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import com.yihaodian.search.nlp.model.DictWord;

/**
 * 从数据库中导出词典，生成字典文件
 * @author yuqian
 *
 */
public class ExportDict {
	
	public static final String DIC_MAIN = "main.dic";
	public static final String DIC_SYSNONMS = "sysnonms.dic";
	public static final String SPILTOR="<>";
	private String dictPath; //词典目录路径
	
	public ExportDict(String dictPath){
		this.dictPath=dictPath;
	}
	public String getDictPath() {
		return dictPath;
	}
	public void setDictPath(String dictPath) {
		this.dictPath = dictPath;
	}

	public boolean isExist(){
		if(this.dictPath==null){
			return false;
		}
		File dictFile = new File(this.dictPath);
		if(dictFile.exists()&&dictFile.isDirectory()){
			File[] files=dictFile.listFiles();
			if(files.length!=2){
				return false;
			}
			for(File f:files){
				String fileName=f.getName();
				if(!DIC_MAIN.equals(fileName)&&
						!DIC_SYSNONMS.equals(fileName)){
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	public void prepare()throws Exception{
		File dictFile = new File(this.dictPath);
		if(dictFile.exists()&& dictFile.isDirectory()){
			File[] files=dictFile.listFiles();
			for(File f:files){
				try{
					f.delete();
				}catch (Exception e) {
					throw new RuntimeException("delete dictionary file error!",e);
				}
			}
		}else{
			if(dictFile.mkdir()){
				System.out.println("Create Folder successfully!");
			}
		}
	}
	public void writeDict(List<DictWord> words)throws Exception{
		File tempFile1,tempFile2;
		OutputStreamWriter mainWriter = null,sysWriter = null;
		
		tempFile1=new File(dictPath+"/"+DIC_MAIN);
		tempFile2 = new File(dictPath+"/"+DIC_SYSNONMS); 
		try{
			tempFile1.createNewFile();
			tempFile2.createNewFile(); 
			mainWriter = new OutputStreamWriter(new FileOutputStream(tempFile1), "UTF-8");
			sysWriter = new OutputStreamWriter(new FileOutputStream(tempFile2), "UTF-8");
				
			String mainStr,sysnonmStr = null;
			for(DictWord dc:words){
				if(dc==null) continue;
				String word=dc.getKeyword().toLowerCase().trim();
				String extendWords=dc.getExtend().toLowerCase().trim();
				String synonyms=dc.getSynonyms().toLowerCase().trim();
				if(extendWords.equals("")){
					mainStr=Latin.toDBC(word)+SPILTOR+dc.getType();
				}else{
					mainStr=Latin.toDBC(word)+SPILTOR+dc.getType()+SPILTOR+Latin.toDBC(extendWords);
				}
				mainWriter.write(mainStr);
				mainWriter.write("\n");
				
				if(!synonyms.equals("")){
					sysnonmStr=dc.getType()+SPILTOR+Latin.toDBC(word)+","+Latin.toDBC(synonyms);
					sysWriter.write(sysnonmStr);
					sysWriter.write("\n");
				}
			}
		}catch (IOException e) {
			throw new RuntimeException("export dict error! ",e);
		}finally{
			try{
				if(mainWriter!=null){
					mainWriter.close();
				}
			}catch (Exception e) {
				// TODO: handle exception
			}
			try{
				if(sysWriter!=null){
					sysWriter.close();
				}
			}catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
	
	
}
