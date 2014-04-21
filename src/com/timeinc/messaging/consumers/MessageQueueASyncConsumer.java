package com.timeinc.messaging.consumers;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.mail.MessagingException;

import com.timeInc.tiwg.utils.EmailProcessor; 
//import javax.mail.MessagingException;

import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * 
 */

/**
 * @author apradhan1271
 *
 */
public class MessageQueueASyncConsumer implements MessageListener {
	Connection connection = null;
	Session session = null;
	MessageConsumer consumer = null;

	public MessageQueueASyncConsumer() throws JMSException {
		ActiveMQConnectionFactory connectionFactory;
		Destination destination;

        try {
            // Create the connection.
            connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            connection = connectionFactory.createConnection();
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            destination = session.createQueue("EmailQueue");
            consumer = session.createConsumer(destination);
            consumer.setMessageListener(this);
        } catch (Throwable e) {
                e.printStackTrace();
        } 
        System.out.println("MessageQueueASyncConsumer Started");
	}

	/**
	 * @param args
	 * @throws JMSException 
	 */
	public static void main(String[] args) throws JMSException {
		new MessageQueueASyncConsumer();
	}

	/* (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */

	@Override
	public void onMessage(Message m) {
		
		MapMessage message = (MapMessage)m;
//		new EmailProcessor();
		EmailProcessor email = EmailProcessor.getInstance();
		
		try {
			
			//String[] attachments = new String[1];
			//attachments[0] = "C:/test.txt";
			
			String[] noAttachments = new String[0];
			
			String[] addresses = message.getString("addresses").split(",");
			
			for(int i =0; i < addresses.length; i++){
			
			email.sendMail(addresses[i], message.getString("subject"), message.getString("body"), "mailgateway.timeinc.com", message.getString("from"), "text/html", noAttachments);
			
			}
			
			System.out.println("Addresses: " + message.getString("addresses"));
			System.out.println("Sender: " + message.getString("from"));
			System.out.println("Subject: " + message.getString("subject"));
			System.out.println("Body: " + message.getString("body"));
			
		} catch (JMSException e) {
			e.printStackTrace();
		}
		catch (MessagingException u) {
			u.printStackTrace();
		}
		finally{
		try {
			consumer.close();
			session.close();
	    	connection.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
    	
		
		
		
	}


}
