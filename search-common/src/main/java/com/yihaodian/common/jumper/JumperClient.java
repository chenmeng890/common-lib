package com.yihaodian.common.jumper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.yihaodian.architecture.jumper.common.consumer.ConsumerType;
import com.yihaodian.architecture.jumper.common.consumer.MessageFilter;
import com.yihaodian.architecture.jumper.common.message.Destination;
import com.yihaodian.architecture.jumper.consumer.Consumer;
import com.yihaodian.architecture.jumper.consumer.ConsumerConfig;
import com.yihaodian.architecture.jumper.consumer.ConsumerFactory;
import com.yihaodian.architecture.jumper.consumer.MessageListener;
import com.yihaodian.architecture.jumper.consumer.impl.ConsumerFactoryImpl;
import com.yihaodian.architecture.jumper.producer.Producer;
import com.yihaodian.architecture.jumper.producer.ProducerConfig;
import com.yihaodian.architecture.jumper.producer.SendMode;
import com.yihaodian.architecture.jumper.producer.impl.ProducerFactoryImpl;

/**
 * Jumper client
 * @author zhaowei1
 *
 */
public class JumperClient {
	private static ProducerFactoryImpl producerFactoryImpl = 
			ProducerFactoryImpl.getInstance();
	private static final Logger log = Logger.getLogger(JumperClient.class);
	private static Map<String, Producer> producers = new HashMap<String, Producer>();

	/**
	 * Get publiser
	 * @param topic
	 * @return
	 */
	public static Producer getPublisher(String topic){
		Producer producer = producers.get(topic);
		if (producer == null) {
			synchronized(producerFactoryImpl) {
				producer = producers.get(topic);
				if (producer == null) {
					log.info("Create publisher, topicname: " + topic);
					ProducerConfig config=new ProducerConfig();
					config.setMode(SendMode.SYNC_MODE);
					config.setSyncRetryTimes(3);
					config.setThreadPoolSize(10);
					producer = producerFactoryImpl.createProducer(
							Destination.topic(topic),config);
					
					producers.put(topic, producer);
				}
			}
		}
		
		return producers.get(topic);
	}
	
	public static Consumer getConsumer(String topic, String clientId, 
			String filter,MessageListener listener){
		// Check clientId
		if (clientId.length() > 29) {
			clientId = clientId.substring(clientId.length() - 29);
		}
		
		ConsumerFactory consumerFactory=ConsumerFactoryImpl.getInstance();
		ConsumerConfig config=new ConsumerConfig();
		config.setMaxConnectionCount(20);
		config.setConsumerType(ConsumerType.CLIENT_ACKNOWLEDGE);
		config.setThreadPoolSize(2);
		
		if (StringUtils.isNotEmpty(filter)) {
			Set<String> msgFilter = new HashSet<String>();
			msgFilter.add(filter);
			config.setMessageFilter(MessageFilter.createInSetMessageFilter(msgFilter));
		}
		
		log.info("Create consumer. Client id is --> " + clientId);
		Consumer consumer=consumerFactory.createConsumer(
				Destination.topic(topic), clientId, config);
		consumer.setListener(listener);
		
		return consumer;
	}
}
