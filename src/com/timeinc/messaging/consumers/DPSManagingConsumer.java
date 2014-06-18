/**
 * A message consumer that reacts to DPS Folio un-publishing requests. This MQ consumer mimics 
 * a synchronous response 
 */
package com.timeinc.messaging.consumers;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import com.timeInc.dps.publish.PublishClient;
import com.timeInc.dps.publish.PublishClients;
import com.timeInc.dps.publish.request.config.LoginConfig;
import com.timeInc.dps.publish.request.config.UnpublishConfig;
import com.timeInc.dps.publish.response.Credential;
import com.timeInc.dps.publish.response.Result;
import com.timeinc.messaging.utils.Constants;
import com.timeinc.messaging.utils.PropertyManager;

/**
 * @author Ashim_Pradhan@timeinc.com
 *
 * May 5, 2014
 */
public class DPSManagingConsumer implements MessageListener, Constants{

	static final Logger log = Logger.getLogger(DPSManagingConsumer.class);
	
	private Session session = null;
	private PublishClient client = null;
	private MessageProducer replyProducer;
	
	public DPSManagingConsumer() {
		ActiveMQConnectionFactory connectionFactory;
		Connection connection = null;
		
		Destination destination;
		MessageConsumer consumer = null;
		boolean useTransaction = false;
		
		client = PublishClients.getSingleThreadedClient(PropertyManager.getPropertyValue(PROP_DPS_ORIGIN_URL));
		
        try {
            connectionFactory = new ActiveMQConnectionFactory(PropertyManager.getPropertyValue(ACTIVEMQ_URL));
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(useTransaction, Session.AUTO_ACKNOWLEDGE);
            destination = session.createQueue(PropertyManager.getPropertyValue(DPSMANAGING_QUEUE));
            
            this.replyProducer = this.session.createProducer(null);
            this.replyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            
            consumer = session.createConsumer(destination);
//            consumer.receiveNoWait();
            consumer.setMessageListener(this);
        } catch (Throwable e) {
               log.error("", e);
        } finally {
        	
        }
	}
	/* (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message message) {
		MapMessage jmsmessage = (MapMessage) message;
		String folioId = null;
		String appName = null;
		String productId = null;
		try {
			folioId = jmsmessage.getString("folioId");
			productId = jmsmessage.getString("productId");
			appName = productId = jmsmessage.getString("appName");
			String email = jmsmessage.getString("email");
			String password = jmsmessage.getString("password");
			log.info("Unpublishing " + appName + " " + productId);
			Credential cred = client.signIn(new LoginConfig(email, password));
			Result result = client.unpublishFolio(new UnpublishConfig(cred.getTicket(), folioId));
//			let's send response to the producer
			TextMessage response = this.session.createTextMessage();
			response.setText(result.getMessage());
			response.setJMSCorrelationID(message.getJMSCorrelationID());
			this.replyProducer.send(message.getJMSReplyTo(), response);
		} catch (JMSException e) {
			log.error("Error from message, folioId: " + folioId, e);
		}
	}

}
