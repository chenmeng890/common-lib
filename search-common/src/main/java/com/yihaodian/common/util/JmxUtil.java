package com.yihaodian.common.util;

import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class JmxUtil{

	public static void registerMBean(Object beanObject, ObjectName beanName) {
		if (beanName == null) {
			registerMBean(beanObject);
		} else {
			MBeanServer server = ManagementFactory.getPlatformMBeanServer();	
			try {
				if (!server.isRegistered(beanName)){
					server.registerMBean(beanObject, beanName);
				}					
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * @param beanObject
	 */
	public static void registerMBean(Object beanObject, String beanName) {
		assert (beanObject != null) && (beanName != null);
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		try {
			ObjectName objName = new ObjectName("com.yihaodian:service="
					+ beanName);
			if (!server.isRegistered(objName))
				server.registerMBean(beanObject, objName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void registerMBean(Object beanObject) {
		assert beanObject != null;
		registerMBean(beanObject, beanObject.getClass().getSimpleName());
	}
	
	public static void unRegisterDefaultPathMBean(String beanName) throws Exception {
		unRegisterMBean("com.yihaodian:service="+beanName);
	}
	
	public static void unRegisterMBean(String MBeanPath) throws Exception {
		unRegisterMBean(new ObjectName(MBeanPath));
	}
	
	public static void unRegisterMBean(ObjectName objectName) throws Exception {
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		server.unregisterMBean(objectName);
	}

	/**
	 * @param serverIP
	 * @param serverJmxPort
	 * @param beanName
	 * @param methodName
	 * @param params
	 * @param signature
	 * @return
	 */
	public static Object remoteInvoke(String serverIP, int serverJmxPort,
			String beanName, String methodName, Object params[],
			String signature[]) {
		try {
			JMXServiceURL url = new JMXServiceURL(
					"service:jmx:rmi:///jndi/rmi://" + serverIP + ":"
							+ serverJmxPort + "/jmxrmi");
			JMXConnector jmxc = JMXConnectorFactory.connect(url);
			MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
			ObjectName mBeanName = new ObjectName("com.yihaodian:service="
					+ beanName);
			Object result = mbsc.invoke(mBeanName, methodName, params,
					signature);
			jmxc.close();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error at JMX invoke <" + methodName
					+ ">.  " + e.getMessage());
		}
	}

	/**
	 * @param serverIP
	 * @param serverJmxPort
	 * @param beanName
	 * @param methodName
	 */
	public static Object remoteGetAttribute(String serverIP, int serverJmxPort,
			String beanName, String attributeName) {
		try {
			JMXServiceURL url = new JMXServiceURL(
					"service:jmx:rmi:///jndi/rmi://" + serverIP + ":"
							+ serverJmxPort + "/jmxrmi");
			JMXConnector jmxc = JMXConnectorFactory.connect(url);
			MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
			ObjectName mBeanName = new ObjectName("com.yihaodian:service="
					+ beanName);
			Object result = mbsc.getAttribute(mBeanName, attributeName);
			jmxc.close();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error at JMX getAttribute <"
					+ attributeName + ">.  " + e.getMessage());
		}
	}

}
