/**
 * A MQ message consumer that reacts to a content being uploaded from Ark
 */
package com.timeinc.messaging.consumers;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import com.timeinc.messaging.utils.Constants;
import com.timeinc.messaging.utils.PropertyManager;

/**
 * @author apradhan1271
 *
 */
public class ArkContentUploadEventConsumer implements MessageListener,
		Constants {
	private static final Logger log = Logger.getLogger(ArkContentUploadEventConsumer.class);
	
	Session session = null;
	
	/**
	 * 
	 */
	public ArkContentUploadEventConsumer() {
		log.info("Consumer started!");
		ActiveMQConnectionFactory connectionFactory;
		Connection connection = null;
		
		Destination destination;
		MessageConsumer consumer = null;
		boolean useTransaction = false;
		connectionFactory = new ActiveMQConnectionFactory(PropertyManager.getPropertyValue(ACTIVEMQ_URL));
//     	connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
         try {
			connection = connectionFactory.createConnection();
			connection.start();
			
			session = connection.createSession(useTransaction, Session.AUTO_ACKNOWLEDGE);
			destination = session.createTopic(PropertyManager.getPropertyValue(ARK_CONTENTUPLOAD_TOPIC));
			consumer = session.createConsumer(destination);
			consumer.setMessageListener(this);
		} catch (JMSException e) {
			log.error("Error with JMS", e);
		}
	}

	/* (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message msg) {
		log.info("picked up message");
		try {
			log.info("Product Id: \t" + msg.getStringProperty("productid"));
			log.info("Application Name: \t" + msg.getStringProperty("applicationname"));
			log.info("Content Path: \t" + msg.getStringProperty("contentpath"));
//			System.out.println(((TextMessage) msg).getText());
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}


}
