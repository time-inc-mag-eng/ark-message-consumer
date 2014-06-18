/**
 *  A MQ message consumer that reacts to Ark issue data change event
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
public class ArkIssueDataChangeEventConsumer implements MessageListener, Constants {

	private static Logger log = Logger.getLogger(ArkIssueDataChangeEventConsumer.class);
	
	Session session = null;
	

	public ArkIssueDataChangeEventConsumer() {
		log.info("Consumer started!");
		ActiveMQConnectionFactory connectionFactory;
		Connection connection = null;
		
		Destination destination;
		MessageConsumer consumer = null;
		boolean useTransaction = false;
		connectionFactory = new ActiveMQConnectionFactory(PropertyManager.getPropertyValue(ACTIVEMQ_URL));
         try {
			connection = connectionFactory.createConnection();
			connection.start();
			
			session = connection.createSession(useTransaction, Session.AUTO_ACKNOWLEDGE);
			destination = session.createTopic(PropertyManager.getPropertyValue(ARK_DATACHANGE_TOPIC));
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
			log.info("Table Name: \t" + msg.getStringProperty("tablename"));
			log.info("Record Id: \t" + msg.getStringProperty("recordid"));
		} catch (JMSException e) {
			e.printStackTrace();
		}
		
	}

}
