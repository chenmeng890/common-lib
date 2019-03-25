package com.yihaodian.search.nlp.segment;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class TestLatinSplitor {

	@Test
	public void testSplitLatinString(){
		String input = "KIT EF-S 18-55mm";
		LatinSplitor splitor =new LatinSplitor(input);
		Set<String> set = new HashSet<String>();
		set.add("KIT");
		set.add("EF-S");
		set.add("18");
		set.add("-55");
		set.add("mm");
		Set<String> ret =splitor.splitLatinString();
		assertEquals(set,ret);
	}
}
 