package com.yihaodian.common.util;

public class MathUtil {
	
	/**
	 * Whether a positive number is power of 2.
	 * @param n
	 * @return
	 */
	public static boolean isPowOf2(int n){
		return (n & (n -1)) == 0;
	}
	
	/**
	 * Next nearest power of 2.
	 * @param n
	 * @return
	 */
	public static int nextPowOf2(int n) {
		if(isPowOf2(n)) {
			return n;
		} 
		
		while(!isPowOf2(n)) {
			n = n & (n - 1);
		}
		n = n << 1;	
		return n;
	}

}
