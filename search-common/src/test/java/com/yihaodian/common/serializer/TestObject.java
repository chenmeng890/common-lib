package com.yihaodian.common.serializer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class TestObject {
// 
//	/**
//	 * Case 1: 增加了基本类型，由于长度较短，不可能是类名， Kryo会跳过解析。
//	 */
//	public byte b = 0x1;
//	/**
//	 * Case 2: 增加了基本类型，Kryo会尝试按照对象去解析这个数据，肯定会失败，失败会被忽略。
//	 */
//	public Long l = 123L;
//	/**
//	 * Case 3: 引入了未知类
//	 */
//	public Set<EO> aaobjectCollection;
//	/**
//	 * case 4: 同样的类型在之前加入一个字段，后面的字段会引用这个类型
//	 */
//	public List<Long> addCollectionBefore;
	public List<Long> ids;
	/**
	 * 【FAILED】
	 * case 5: 未知类EO的某个Field引用了一个类型Map，而且在顶层类的后面Field也使用了Map。
	 * 由于未知类没有进行内部解析，Field的类型没有加载进来，解析的时候就会出现顶层类的Map无法解析的情形。
	 */
//	public Map<Long, Long> zzMap;
	public TestObject() {}


//	public static class EO {
//		String name;
//		Map<String, String> map; 
//		public EO() {
//			name = "dfsfd";
//			map = new HashMap<String, String>();
//			map.put("a", "b");
//		}
//	}
//	
	public TestObject(Long id) {
		ids = new  ArrayList<Long>();
		ids.add(id);
		
//		zzMap = new HashMap<Long, Long>();
//		zzMap.put(id, id);
//		
//		addCollectionBefore = new ArrayList<Long>();
//		addCollectionBefore.add(id);
//		aaobjectCollection = new HashSet<EO>();
//		aaobjectCollection.add(new EO());
	}

	@Override
	public String toString() {
		return "TestObject [ids=" + ids + "]";
//		return "TestObject [array=" + addCollectionBefore + ", ids=" + ids + "]";
	}

	
}
