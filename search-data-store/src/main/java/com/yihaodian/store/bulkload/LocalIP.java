package com.yihaodian.store.bulkload;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

public class LocalIP {
	
	
	private static final String ip=getLocalIP();
	
	
	private static String getLocalIP(){
		Collection<InetAddress> colInetAddress =getAllHostAddress();
		  
		  for (InetAddress address : colInetAddress) {   
		    if (!address.isLoopbackAddress()) 
//		     System.out.println("IP:"+address.getHostAddress());
		        return address.getHostAddress();       
		        }
		return null;
	}

	private static Collection<InetAddress> getAllHostAddress() {   
        try {   
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();   
            Collection<InetAddress> addresses = new ArrayList<InetAddress>();   
               
            while (networkInterfaces.hasMoreElements()) {   
                NetworkInterface networkInterface = networkInterfaces.nextElement();   
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();   
                while (inetAddresses.hasMoreElements()) {   
                    InetAddress inetAddress = inetAddresses.nextElement();   
                    addresses.add(inetAddress);   
                }   
            }   
               
            return addresses;   
        } catch (SocketException e) {   
            throw new RuntimeException(e.getMessage(), e);   
        }   
    } 
	
	public static String get(){
		return ip;
	}
	
	public static void main(String[] args){
		System.out.println(ip);
		System.out.println(System.currentTimeMillis());
		double d=Math.random();
		System.out.println(d);
		d=(d*100000);
		System.out.println(d);
		System.out.println((int)d);
		System.out.println(System.currentTimeMillis()+"_"+
				((int)(Math.random()*100000)));
	}
}
