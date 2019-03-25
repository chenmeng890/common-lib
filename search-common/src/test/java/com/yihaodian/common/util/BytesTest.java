package com.yihaodian.common.util;

import org.junit.Test;

public class BytesTest {
	
	@Test
	public void overSizeTest() {
		Bytes.oversize(1, 3);
	}
	
	@Test
	public void itoaTest() {
		Bytes.itoa(3);
	}
	
	@Test
	public void atoiTest() {
		Bytes.atoi(Bytes.itoa(3));
	}
	
	@Test
	public void ltoaTest() {
		Bytes.ltoa(100L);
	}
	
	@Test
	public void atol() {
		Bytes.atol(Bytes.ltoa(100L));
	}
}
