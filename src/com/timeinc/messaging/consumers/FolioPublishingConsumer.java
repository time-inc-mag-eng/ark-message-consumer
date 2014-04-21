/**
 * 
 */
package com.timeinc.messaging.consumers;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import javax.mail.MessagingException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;

import com.timeInc.dps.exception.DpsCommunicationException;
import com.timeInc.dps.publish.PublishClient;
import com.timeInc.dps.publish.PublishClients;
import com.timeInc.dps.publish.request.config.LoginConfig;
import com.timeInc.dps.publish.request.config.PublishConfig;
import com.timeInc.dps.publish.request.config.TicketConfig;
import com.timeInc.dps.publish.response.Credential;
import com.timeInc.dps.publish.response.Publish;
import com.timeInc.dps.publish.response.PublishingStatus;
import com.timeInc.dps.publish.response.PublishingStatus.Response;
import com.timeInc.dps.translator.ResponseHandlerException;
import com.timeInc.tiwg.utils.DateFormatter;
import com.timeInc.tiwg.utils.EmailProcessor;
import com.timeinc.messaging.db.ArkDAO;
import com.timeinc.messaging.db.PublishFolioDAO;
import com.timeinc.messaging.model.Folio;
import com.timeinc.messaging.utils.Constants;
import com.timeinc.messaging.utils.PropertyManager;

/**
 * @author apradhan1271
 *
 */
public class FolioPublishingConsumer implements MessageListener, Constants {

	private static Logger log = Logger.getLogger(FolioPublishingConsumer.class);
	
	Session session = null;
	PublishFolioDAO dao = null;
	PublishClient client = null;
	
	private static final boolean UPDATE_CONTENT = true;
	private static final String INITIAL_STATUS = "submitted";
	private static final String WEB_RENDITION = "WEB";
//	private static final String CONSUMER = "Consumer(";
	private String account;
	private String selector = "accountName='";
	
	ArkDAO arkDAO = null;
	/**
	 * 
	 */
	public FolioPublishingConsumer(String acnt) {
		arkDAO = new ArkDAO();
		this.account = acnt + " ";
		this.selector = selector + acnt + "'";
		log.debug(account + "Started listening to messages");
		dao = new PublishFolioDAO();
		ActiveMQConnectionFactory connectionFactory;
		Connection connection = null;
		
		Destination destination;
		MessageConsumer consumer = null;
		boolean useTransaction = false;
		
		client = PublishClients.getSingleThreadedClient(PropertyManager.getPropertyValue(PROP_DPS_ORIGIN_URL));
		
        try {
            // Create the connection.
            connectionFactory = new ActiveMQConnectionFactory(PropertyManager.getPropertyValue(ACTIVEMQ_URL));
            connection = connectionFactory.createConnection();
            connection.start();
            log.debug(selector);
            session = connection.createSession(useTransaction, Session.AUTO_ACKNOWLEDGE);
            destination = session.createQueue(PropertyManager.getPropertyValue(PUBLISH_QUEUE));
                             
            consumer = session.createConsumer(destination, selector);
            consumer.receiveNoWait();
            consumer.setMessageListener(this);
        
        } catch (Throwable e) {
               log.error("", e);
        } finally {
        	
        }
	}

	/* (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	@Override
	public void onMessage(Message msg) {
		String email = null;
		String loggedInUser = null;
		Folio folioBean = null;
		String message = "success";
		try {
			Publish pub = null;
			//Counting seconds
			MapMessage jmsmessage = (javax.jms.MapMessage) msg;
			email = jmsmessage.getString("email");
			loggedInUser = jmsmessage.getString("userEmail");
			String folioId = jmsmessage.getString("folioId");
			String productId = jmsmessage.getString("productId");
			String password = jmsmessage.getString("password");
			String issueName = jmsmessage.getString("issueName");
			String rendition = jmsmessage.getString("rendition");
			long saleDate = jmsmessage.getLong("saleDate");
//			String contentType = jmsmessage.getString("type"); // preview or content
//			boolean privatePublish = jmsmessage.getBoolean("private");
			boolean retail = jmsmessage.getBoolean("retail");
			
			
			Credential cred = client.signIn(new LoginConfig(email, password));
			
			log.info(account + "Request to publish received for: " + productId);
			
			/* let's see if a pending request for this product already exists */
			folioBean = dao.getFolioByFolioId(folioId);
			if (folioBean != null && !folioBean.getStatus().equalsIgnoreCase(Response.State.COMPLETED.toString())
					&& !folioBean.getStatus().equalsIgnoreCase(Response.State.FAILED.toString())) { 
				try { 
					// pending status exists, let's request recall
					client.cancelPublish(new TicketConfig(cred.getTicket(), folioBean.getRequestId())); // this may fail if status in the server is not "submitted"
					folioBean.setStatus(Response.State.CANCELED.toString());
					folioBean = dao.updateFolio(folioBean);
				} catch (DpsCommunicationException e) {
					log.error(account + "Could not cancel Publishing request", e);
				}
				
			} 
			
			
			
			/*
			 * If web rendition, publish as public
			 * Check PUBLISH_FOLIO_QUEUE to determine if this folio has been request for publish previously
			 * 	if not
			 * 		Publish as Scheduled for saledate if saledate is over 45 mins in the future, otherwise schedule it for 45 mins from now.
			 * 			Folio Producer limitation, allows to schedule only over 30 mins in the future, using 45 mins here as a saftynet.
			 * 		If saledate is in the past Publish as Public
			 * 	if yes
			 * 		Check Folio Producer for the status of the Folio (private or public), if public publish as public, if private and sale date in the future
			 * 		schedule it for saledate taking account the 30 mins limitation.
			 */
			try {
				if (rendition.equalsIgnoreCase(WEB_RENDITION)) {
					pub = client.publish(PublishConfig.getPublicInstance(folioId, cred.getTicket(), productId, UPDATE_CONTENT, retail));
				} else {
					if (folioBean == null) { // first time publishing
						if (saledateInThePast(saleDate)) {
							pub = client.publish(PublishConfig.getPublicInstance(folioId, cred.getTicket(), productId, UPDATE_CONTENT, retail));
						} else {
							pub = client.publish(PublishConfig.getScheduledPublicInstance(folioId, cred.getTicket(), productId, UPDATE_CONTENT, retail, getPublishSchedule(saleDate)));
						}
					} else {
						// get folio status (public or private) if public
						if (getFolioPublishState(folioBean.getRequestId(), cred.getTicket()).equalsIgnoreCase(PublishConfig.State.PUBLIC.toString())) {
							pub = client.publish(PublishConfig.getPublicInstance(folioId, cred.getTicket(), productId, UPDATE_CONTENT, retail));
						} else {
							pub = client.publish(PublishConfig.getScheduledPublicInstance(folioId, cred.getTicket(), productId, UPDATE_CONTENT, retail, getPublishSchedule(saleDate)));
						}
					}
				}
				
				if (pub != null) {
					/* Create folioBean record */
					if (folioBean == null) {
						folioBean = dao.createPublishFolioQueue(INITIAL_STATUS, email, password, folioId, productId, retail, 
																pub.getRequestId(), loggedInUser, issueName);
					} else {
						folioBean.setRequestor(loggedInUser);
						folioBean.setRetail(retail);
						folioBean.setRequestId(pub.getRequestId());
					}
					/* update with then new status request etc */
					folioBean = dao.updateFolio(folioBean);;
				}
			} catch (ResponseHandlerException e) {
				message = e.getMessage();
				log.error(account + "Error with publishing" + e);
				/* could not publish, send error to user */
				sendUserAdminError(message, loggedInUser, issueName, productId);
				
			}
			MessageProducer producer = null;
			try {
				/* try if the temporary queue is not expired */
				producer = session.createProducer(null);
		        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
				TextMessage response = this.session.createTextMessage(message);
				response.setJMSCorrelationID(msg.getJMSCorrelationID());
				producer.send(msg.getJMSReplyTo(), response);
			} catch(JMSException e) { 
				log.error(account+ e.getMessage());
			} finally {
				producer.close();
			}

			/* let's now check status of the publish request until success or failure */
			PublishingStatus status = client.getPublishStatus(new TicketConfig(cred.getTicket(), pub.getRequestId()));
			Response statusResponse = getStatusResponse(status);
			folioBean.setStatus(statusResponse.getJobStatus().toString());
			
			/* update the local db with publishing status */
			folioBean = dao.updateFolio(folioBean);
			while (!statusResponse.getJobStatus().equals(Response.State.COMPLETED) 
					&& !statusResponse.getJobStatus().equals(Response.State.FAILED)) {
				status = client.getPublishStatus(new TicketConfig(cred.getTicket(), folioBean.getRequestId()));
				statusResponse = getStatusResponse(status);
				log.debug(account + "Status of " + productId + ": " + statusResponse.getJobStatus());
				/* if the status is canceled, let's exit out, other process must have cancelled the request */
				folioBean.setStatus(statusResponse.getJobStatus().toString());
				if (statusResponse.getJobStatus().equals(Response.State.CANCELED)) {
					break;
				}
				Thread.sleep(1000 * 15); // sleep for 15 seconds
			}
			dao.updateFolio(folioBean);
						
			/* Publishing request either failed or is successful; send email */
			EmailProcessor.getInstance().sendMail(loggedInUser, 
					getTransformedString(PropertyManager.getPropertyValue(PROP_STATUS_EMAIL_SUBJECT), getValuesMap(folioBean)), 
					getTransformedString(PropertyManager.getPropertyValue(PROP_STATUS_EMAIL_BODY), getValuesMap(folioBean)), 
					PropertyManager.getPropertyValue(PROP_SMTP_SERVER), 
					PropertyManager.getPropertyValue(PROP_EMAIL_FROM), HTML_MIME_TYPE, null);
			log.info(account + "Finished publihsing " + productId);
		} catch (DpsCommunicationException e) {
			log.info(account + e);
			/* probably there is probably one publishing publishing happening already, let's 
			 * email the user to wait for the previous request to finish */
			List<Folio> pendingFolios;
			try {
				pendingFolios = dao.getPendingFoliosByEmail(email);
				if (pendingFolios.size() == 1) { // there should not be more than one pending folios, if there is then something is wrong
					try {
						
						EmailProcessor.getInstance().sendMail(loggedInUser, 
								getTransformedString(PropertyManager.getPropertyValue(PROP_REQUEST_ERROR_MULTIPLE_EMAIL_SUBJECT), getValuesMap(folioBean)), 
								getTransformedString(PropertyManager.getPropertyValue(PROP_REQUEST_ERROR_MULTIPLE_EMAIL_BODY), getValuesMap(folioBean)), 
								PropertyManager.getPropertyValue(PROP_SMTP_SERVER), 
								PropertyManager.getPropertyValue(PROP_EMAIL_FROM), HTML_MIME_TYPE, null);
					} catch (MessagingException e1) {
						log.error(account + e1);
					}
				} else {
					try {
						EmailProcessor.getInstance().sendMail(PropertyManager.getPropertyValue(PROP_ADMIN_EMAIL), 
								getTransformedString(PropertyManager.getPropertyValue(PROP_ADMIN_SUBJECT), getValuesMap(folioBean)), 
								getTransformedString(PropertyManager.getPropertyValue(PROP_ADMIN_BODY), getValuesMap(folioBean)), 
								PropertyManager.getPropertyValue(PROP_SMTP_SERVER), 
								PropertyManager.getPropertyValue(PROP_EMAIL_FROM), HTML_MIME_TYPE, null);
					} catch (MessagingException e1) {
						log.error(account + e1);
					}
				}
			} catch (SQLException e2) {
				log.error("", e2);
			}			
		} catch (JMSException e) {
			log.error(account + "JMSError" ,e);
		} catch (Throwable t) {
			log.error(account + "I do not know what happened :o(", t); // let's not do crap
			try {
				EmailProcessor.getInstance().sendMail(PropertyManager.getPropertyValue(PROP_ADMIN_EMAIL), 
						getTransformedString(PropertyManager.getPropertyValue(PROP_ADMIN_SUBJECT), getValuesMap(folioBean)), 
						message, 
						PropertyManager.getPropertyValue(PROP_SMTP_SERVER), 
						PropertyManager.getPropertyValue(PROP_EMAIL_FROM), HTML_MIME_TYPE, null);
			} catch (MessagingException e1) {
				log.error(account + e1);
			}
		} finally {
			
		}
	}
	

	/**
	 * @param message
	 * @param loggedInUser
	 * @throws MessagingException 
	 */
	private void sendUserAdminError(String message, String loggedInUser, String issueName, String productId) 
			throws MessagingException {
		EmailProcessor.getInstance().sendMail(Arrays.asList(loggedInUser, PropertyManager.getPropertyValue(PROP_ADMIN_EMAIL)), 
				null, // no cc
				"Error: Publishing " + issueName + "(" + productId + ")", 
				message, 
				PropertyManager.getPropertyValue(PROP_SMTP_SERVER), 
				PropertyManager.getPropertyValue(PROP_EMAIL_FROM), HTML_MIME_TYPE, null);
	}

	/**
	 * Get the folio state from DPS, either public or private
	 * @param folioId
	 * @return String
	 */
	private String getFolioPublishState(String requestId, String ticket) {
		PublishingStatus status = client.getPublishStatus(new TicketConfig(ticket, requestId));
		Response resp = getStatusResponse(status);
		return resp.getState().toString();
	}


	/**
	 * @param folioId
	 * @param productId
	 
	private void updateFolioProducerWithWebRendition(String folioId, String productId) {
		try {
			ArkDPSInfo dpsInfo = arkDAO.getDPSInfo(productId);
			ManagedProducer producer = ManagedProducers.getHttpClient(dpsInfo.getAddress(), dpsInfo.getConsumerKey(), dpsInfo.getConsumerSecret());
			producer.open(new OpenSessionConfig(dpsInfo.getUserName(), dpsInfo.getPassword(), false));
			producer.sendRequest(new UpdateFolioRequest(new UpdateFolioConfig.Builder(folioId).withViewer(Viewer.WEB).build()));
			log.debug("Done updating Web Only Redndition");
		} catch (SQLException e) {
			log.error("Could not get DPS INFO for: " + productId + "\n, This issue will not be updated as Web only rendition. Fix it manually.", e);
		}
		
	}
	*/
	
	/**
	 * @param saleDate
	 * @return boolean
	 */
	private boolean saledateInThePast(long saleDate) {
		Date date = new Date(saleDate);
		return date.before(Calendar.getInstance().getTime());
	}

	/**
	 * @param saledate
	 * @return long
	 */
	public long getPublishSchedule(long saledate) {
		long curdate = System.currentTimeMillis();
//		System.out.println("saledate - curdate : " + (saledate - curdate));
		if (saledate - curdate > (45 * 60 * 1000)) { // 45 mins
			return saledate;
		}
		return curdate + 45 * 60 * 1000;
	}

	/**
	 * @param stringProperty
	 * @return boolean
	 */
	public boolean isWebOnlyRendition(String folioId) {
		log.debug("Checking to see if web rendition for folioId: " + folioId);
		try {
			String appName = arkDAO.getAppname(folioId);
			log.debug("App name is: " + appName);
			if (appName != null && appName.toLowerCase().contains("web")) {
				return true;
			}
		} catch (SQLException e) {
			log.error(e);
		}
	
		return false;
	}

	/**
	 * @param folioBean
	 * @return Map
	 */
	private Map<String, String> getValuesMap(Folio f) {
		Map<String, String> vm = new HashMap<String, String>();
		if (f != null) {
			vm.put("status", f.getStatus());
			vm.put("lastUpdated", DateFormatUtils.format(f.getLastUpdated(), "MM/dd/yyyy HH:mm:ss"));
			vm.put("account", f.getEmail());
			vm.put("folioId", f.getFolioId());
			vm.put("productId", f.getProductId());
			vm.put("retail", f.isRetail() ? "true" : "false");
			vm.put("requestId", f.getRequestId());
			vm.put("requestor", f.getRequestor());
			vm.put("issueName", f.getIssueName());
		}
		return vm;
	}

	/**
	 * Gets the status of a publishing request. Output contains the request Id, details, status, and progress for each 
	 * request. You can use the requestId to cancel or delete the publish request. In general, publishing requests are 
	 * long operations and may take several minutes to complete. After submitting the publishing request, it’s best to wait
	 * at least one minute before calling this API, and to wait at least one minute between status requests. 
	 * A jobStatus=complete in the response indicates that publishing is complete. If the ‘onSaleDate’ was specified in 
	 * the publish request, a second <request> will be returned in the response’s list of <requests>. 
	 * The original publish request will have a ‘jobStatus’ of ‘completed’. The new scheduled request will have a jobStatus 
	 * of ‘pending’, then ‘started’, then ‘completed’; the scheduled request will have a ‘parentRequestId’ equal to the 
	 * original request’s requestId. 
	 * @param status
	 * @return Response
	 */
	private Response getStatusResponse(PublishingStatus status) {
		Response r = null;
		if (status.getPublishStatus().size() == 1) {
			r = status.getPublishStatus().get(0);
		} else if ((status.getPublishStatus().size() == 2)) {
			// get the status from response that does not have the parentRequsetId
			if (status.getPublishStatus().get(0).getParentRequestId() == null) {
				r = status.getPublishStatus().get(0);
			} else {
				r = status.getPublishStatus().get(1);
			}
		} else {
			for (Response res : status.getPublishStatus()) {
				if (!res.getJobStatus().equals(Response.State.CANCELED)) {
					r = res;
				}
			}
		}
		return r;
	}

	
	/**
	 * Transform the template with given valuesMap
	 * @param template
	 * @param m
	 * @return String
	 */
	private String getTransformedString(String template, Map<String, String> m) {
		StrSubstitutor sub = new StrSubstitutor(m);
		return sub.replace(template);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*String emails[] = {"EWS_Generic_Apple@timeinc.com"};
		for (String a : emails) {
			new FolioPublishingConsumer(a);
		}
			
			new FolioPublishingConsumer(2);*/
//			System.out.println(new FolioPublishingConsumer("EWS_Generic_Apple@timeinc.com").isWebOnlyRendition("com.timeinc.wallpaper.ipad.inapp.04082011"));
		
//		log.info("FolioPublishingConsumers Started");
		/*Folio f = null;
		try {
			f = new PublishFolioDAO().getFolioById(35);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Map<String, String> m = new HashMap<String, String>();
		FolioPublishingConsumer con = new FolioPublishingConsumer("adfasd");
		con.setValuesMap(m, f);
		System.out.println(con.getTransformedString(PropertyManager.getPropertyValue(PROP_STATUS_EMAIL_BODY), m));
		*/
		Calendar cal = Calendar.getInstance();
		System.out.println("current time: " + DateFormatter.dateToString(cal.getTime(), "dd/MM/yyyy HH:mm"));
		cal.add(Calendar.MINUTE, 20);
		System.out.println("rolled a day time: " + DateFormatter.dateToString(cal.getTime(), "dd/MM/yyyy HH:mm"));
		long l = new FolioPublishingConsumer("test").getPublishSchedule(cal.getTimeInMillis());
		Date d = new Date(l);
		System.out.println(DateFormatter.dateToString(d, "dd/MM/yyyy HH:mm"));
	}

}
