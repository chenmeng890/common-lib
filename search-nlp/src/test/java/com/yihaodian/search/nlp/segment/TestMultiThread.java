package com.yihaodian.search.nlp.segment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import com.yihaodian.search.nlp.model.Dictionary;
import com.yihaodian.search.nlp.model.Lexeme;
import com.yihaodian.search.nlp.util.PrintSegmenter;

public class TestMultiThread {
	Dictionary dict =null;
	Segmenter seg = null;
	List<String> inputs = new ArrayList<String>();
	@Before
	public void setup(){
//		String dictpath="D:/workspace/main/search-nlp/src/test/resources/dictionary";
		String dictpath ="/var/www/data/mandy/";
		dict = new Dictionary(dictpath,true);
		inputs.add("时尚锁链图案铃铛宝石挂饰V形肩带吊带衫玫红色 6007");
		inputs.add("言若Ringnor糖果色抹胸 BD803005 多色入");
		inputs.add("言若ringnor 2011春装新品韩版女装条纹纯棉打底小背心BD990210");
		inputs.add("达思彼 钉珠深V领不规则细肩带吊带上衣黑色 062#");
		inputs.add("康妮雅家居服 女圆领长袖T01072360米白");
		inputs.add("BSX 女装 拼缝横间长袖T恤 04320700001");
		inputs.add("CORONA珂罗娜新款V领字母印花休闲T恤1913509");
		inputs.add("【亿品】新款棉质圆领长袖T恤女宽松休闲7369");
		inputs.add("【亿品】新款棉质圆领长袖T恤女韩版可爱7342百搭");
		inputs.add("【亿品】新款棉质长袖圆领T恤女白色花边修身打底衫7370");
		inputs.add("【亿品】新款棉质长袖堆堆领T恤女个性拼接袖百褶边7377");
		inputs.add("【亿品】新款雪纺长袖圆领T恤女胸前褶皱设计7290");
		inputs.add("ESPRIT   女装 字母印花长袖女T恤 YG9333-688-9");
		inputs.add("ESPRIT  女装 休闲系列水钻字母长袖T恤 ZC1668M-001");
		inputs.add("ESPRIT  女装 休闲系列水钻字母长袖T恤 ZC1668M-100");
		inputs.add("ESPRIT  女装 休闲系列水钻字母长袖T恤 ZC1668M-557");
		inputs.add("ESPRIT  女装 休闲系列水钻字母长袖T恤 ZC1668M-664");
	}
    @Test
	public void testForwardSegmenterThread() {
		seg = new ForwardSegmenter(dict);
		for (int i = 0; i < 20; i++) {
			int index = new Random().nextInt(inputs.size());
			String input = inputs.get(index);
			SegmenterThread thread = new SegmenterThread(seg, input);
			thread.start();
		}
	}
    @Test
    public void testReverseSegmenterThread(){
		seg = new ReverseSegmenter(dict);
		for (int i = 0; i < 20; i++) {
			int index = new Random().nextInt(inputs.size());
			String input = inputs.get(index);
			SegmenterThread thread = new SegmenterThread(seg, input);
			thread.start();
		}
    }

	public static void main(String[] args) {
		TestMultiThread test = new TestMultiThread();
		test.testForwardSegmenterThread();
		test.testReverseSegmenterThread();
	}

	public class SegmenterThread extends Thread {
		Segmenter seg;
		String input;
		int index;

		public SegmenterThread(Segmenter seg, String input) {
			this.seg = seg;
			this.input = input;
		}

		public void run() {
			List<Lexeme> les;

			les = seg.segmentComplex(input);
			PrintSegmenter.printLexemes(" ", les);
//			System.out.println(index + " 原文：" + input + "-------切词:" + words);

		}
	}
}
