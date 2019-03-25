/**
 * 
 */
package com.yihaodian.search.nlp.help;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * 中文拼音帮助类
 * @author norman
 * 
 */
public class PinYinHelper {

	private Map<Character, LinkedList<String>> charMap;

	private Map<String, String> multiMap;

	private static PinYinHelper instance = new PinYinHelper();

	public PinYinHelper() {
		charMap = new LinkedHashMap<Character, LinkedList<String>>();
		multiMap = new HashMap<String, String>();
		try {
			reload();
		} catch (Exception e) {
			throw new RuntimeException("load spell dictionary exception.",e);
		}
	}

	public static PinYinHelper getInstance() {
		return instance;
	}

	/**
	 * 加载拼音对照表及多音字词组
	 * @throws Exception
	 */
	public synchronized void reload() throws Exception {
		InputStream f1 = PinYinHelper.class.getResourceAsStream("/spellDict/pinyin.txt");
		if (f1 == null){
			throw new IllegalArgumentException("/spellDict/pinyin.txt not found.");
		}
		InputStream f2 = PinYinHelper.class.getResourceAsStream("/spellDict/duoYinZi_ZuCi.txt");
		if (f2 == null){
			throw new IllegalArgumentException("/spellDict/duoYinZi_ZuCi.txt not found.");
		}
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(f1,
					"utf-8"));
			String input = "";
			LinkedList<String> spell;
			while ((input = br.readLine()) != null) {
				char ch = input.charAt(0);
				String s = input.substring(2, input.length() - 1);
				if (!charMap.containsKey(ch)) {
					spell = new LinkedList<String>();
					spell.add(s);
					charMap.put(ch, spell);
				} else {
					spell = charMap.get(ch);
					if (!spell.contains(s)){
						spell.add(s);
					}
				}
			}
			br.close();

			BufferedReader in = new BufferedReader(new InputStreamReader(f2,
					"utf-8"));
			String s;
			String ch;
			int t = 0;
			while ((s = in.readLine()) != null) {
				ch = s.substring(0, 1);
				String[] arr = s.substring(4).split("/");
				for (String temp : arr) {
					t = temp.indexOf("]");
					if (t > 0) {
						String py = ch + temp.substring(1, t);
						String word = temp.substring(t + 1).trim();
						if (word.length() > 0){
							multiMap.put(py, word);
						}
					}
				}
			}
			in.close();// 关闭输入流;
		} catch (Exception e) {
			throw new RuntimeException("load spell dictionary exception.",e);
		} finally {
			try{
				if(null!=f1){			
					f1.close();
				}
			}catch (Exception e) {
				throw new RuntimeException("close duoYinZi InputStream exception.",e);
			}
			try{
				if(null!=f2){			
					f2.close();
				}
			}catch (Exception e) {
				throw new RuntimeException("close pinyin InputStream exception.",e);
			}
		}
	}

	public String[] cn2Spell(String str) {
		int len = str.length();
		String py = "";
		int i;
		List<String> pinyin = new LinkedList<String>();

		for (i = 0; i < len; i++) {
			Character ch = str.charAt(i);
			if (charMap.get(ch) == null) {
				py += ch;
			} else {
				if (py.length() > 0) {
					pinyin.add(py);
					py = "";
				}
				LinkedList<String> L = charMap.get(ch);
				if (L.size() == 1) {
					pinyin.add(L.get(0));
				} else if (L.size() > 1) {
					int j;
					for (j = 0; j < L.size(); j++) {
						String s1 = L.get(j);
						String multiWords = multiMap.get(ch + s1);
						if (multiWords != null) {
							// 在多音字组词表中找匹配的组词
							if (((i + 2) <= str.length() && multiWords
									.contains(str.substring(i, i + 2)))
									|| ((i >= 1) && multiWords.contains(str
											.substring(i - 1, i + 1)))) {
								pinyin.add(s1);
								break;
							}
						}
					}
					if (j == L.size()) {
						pinyin.add(L.get(0));
					}
				}
			}

		}
		if (i == len && py.length() > 0) {
			pinyin.add(py);
		}
		return pinyin.toArray(new String[pinyin.size()]);
	}

	public static String fuzzyTranslate(String spell){
		StringBuffer str=new StringBuffer(spell);
		if(spell.endsWith("en")||spell.endsWith("in")){
			str.append("-g");
		}
		else if(spell.endsWith("eng")||spell.endsWith("ing")){
			str.insert(spell.length()-2, "-");
		}
	    if(spell.startsWith("ch")||spell.startsWith("sh")||spell.startsWith("zh")){
			str.insert(1, "-");
	    }
	    else if(spell.startsWith("c")||spell.startsWith("s")||spell.startsWith("z")){
			str.insert(1, "-h");
	    }
		return str.toString();
	}
	
	public String cnToSpell(String str){
		String[] spells = cn2Spell(str);
		StringBuilder sb=new StringBuilder();
		for(String s:spells){
			sb.append(s);
		}
		return sb.toString();
	}

	public String cnToFirstSpell(String str){
		String[] spells = cn2Spell(str);
		StringBuilder sb=new StringBuilder();
		for(String s:spells){
			sb.append(s.substring(0, 1));
		}
		return sb.toString();
	}
	
	
}
