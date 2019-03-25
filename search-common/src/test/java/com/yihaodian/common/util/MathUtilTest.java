package com.yihaodian.common.util;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.Test;

public class MathUtilTest {

	@Test
	public void testisPowOf2() {
		Assert.assertEquals(true, MathUtil.isPowOf2(1));
		Assert.assertEquals(true, MathUtil.isPowOf2(2));
		Assert.assertEquals(true, MathUtil.isPowOf2(2048));
		Assert.assertEquals(false, MathUtil.isPowOf2(123));
	}
	
	@Test
	public void testnextPowOf2() {
		Assert.assertEquals(128, MathUtil.nextPowOf2(123));
		Assert.assertEquals(128, MathUtil.nextPowOf2(67));
	}

}
