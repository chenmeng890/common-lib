package com.yihaodian.common.util;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class NetUtil {

	public static String getLocalIP() {
		String local = null;
		try {
			InetAddress ip = InetAddress.getLocalHost();
			local = ip.getHostAddress();

			if ("127.0.0.1".equals(local)) {
				Socket s = new Socket("www.yihaodian.com", 80);
				ip = s.getLocalAddress();
				s.close();

				local = ip.getHostAddress();
			}
		} catch (Exception e) {
			e.printStackTrace();

		}

		return local;
	}
	

	public static String localAddress() {
		InetAddress address;
		try {
			address = InetAddress.getLocalHost();
			return address.getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		return "127.0.0.1";
	}

}
