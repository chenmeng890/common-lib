package com.yihaodian.search.nlp.help;


import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.yihaodian.search.nlp.dao.DictWordDao;
import com.yihaodian.search.nlp.help.ExportDict;
import com.yihaodian.search.nlp.model.DictWord;
import com.yihaodian.search.nlp.segment.SegmenterFactory;

public class TestExportDict {

	public static void main(String[] args) throws Exception {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext-beans.xml");
		DictWordDao dictWordDao=(DictWordDao)applicationContext.getBean("dictWordDao");
//		long timer = System.currentTimeMillis();
		String dictpath=SegmenterFactory.class.getResource("/").getFile()+"/dictionary";
		ExportDict export = new ExportDict(dictpath);
		if(export.isExist()){
			System.out.println("dict already exist!");
		}else{
			System.out.println("dict not exist!");
		}
		export.prepare();
		List<DictWord> words=dictWordDao.getDictWords(0);
		export.writeDict(words);
		/*for(DictWord dc:words){
			String word=dc.getKeyword().toLowerCase().trim();
			if(!word.equals("")){
				String str=ExportDict.ToDBC(word);
				if(!str.equals(word))
					System.out.println(word +"------"+str);
			}
		}*/
		
	}
	
}
