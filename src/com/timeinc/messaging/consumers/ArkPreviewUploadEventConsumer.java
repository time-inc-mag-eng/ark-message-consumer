/**
 *  A MQ consumer that reacts to preview images being uploaded from Ark
 *  A proprietary sidecar file is created and uploaded to the SFTP server for any Amazon KPP file uploads.
 */
package com.timeinc.messaging.consumers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.mail.MessagingException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.timeInc.tiwg.utils.DateFormatter;
import com.timeInc.tiwg.utils.EmailProcessor;
import com.timeinc.messaging.utils.Constants;
import com.timeinc.messaging.utils.PropertyManager;

/**
 * @author apradhan1271
 * 
 *         Nov 1, 2013
 */
public class ArkPreviewUploadEventConsumer implements MessageListener,
		Constants {
	private final static Logger log = Logger.getLogger(ArkPreviewUploadEventConsumer.class);

	 /* any application that requires the amazon
	issue-meta.xml file created, the app name needs to match KPP_APPNAME_PREFIX in the message */
	private static final String KPP_APPNAME_PREFIX = "amazon folio ftp";
	private static final String TRUE = "true";
	private static final String ISSUE_META_FILENAME = "issue-meta.xml";
	private static final String COVER_FILE_NAME = "cover.png";
	Session session = null;


	public ArkPreviewUploadEventConsumer() {
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
			destination = session.createTopic(PropertyManager.getPropertyValue(ARK_PREVIEWUPLOAD_TOPIC));
			consumer = session.createConsumer(destination);
			consumer.setMessageListener(this);
		} catch (JMSException e) {
			log.error("Error with JMS", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message msg) {
		try {
			String port =  msg.getStringProperty("port");
			String saledate = msg.getStringProperty("saledate");
			String host = msg.getStringProperty("host");
			String referenceId = msg.getStringProperty("referenceid");
			String newsstand = msg.getStringProperty("newsstand");
			String password = msg.getStringProperty("password");
			String folder = msg.getStringProperty("folder" );
			String issuename = msg.getStringProperty("issuename");
			String price = msg.getStringProperty("price");
			String pubname = msg.getStringProperty("pubname");
			String previewpath = msg.getStringProperty("previewpath");
			String appname = msg.getStringProperty("appname");
			String login = msg.getStringProperty("login");
			String apptype = msg.getStringProperty("apptype");
			String success = msg.getStringProperty("success");
			String shortdate = msg.getStringProperty("shortdate"); 
			String coverstory = msg.getStringProperty("coverstory");
			String contentpath = msg.getStringProperty("contentpath");
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(Long.parseLong(shortdate));
			String sd = DateFormatter.dateToString(c.getTime(), "MM-dd-yyyy");
//			if the request if for Amazon KPP create the xml file and upload it to sftp
			if (pubname != null) {
				pubname = pubname.replaceAll(" ", "");
			}
			if (success.equals(TRUE) && appname.toLowerCase().contains(KPP_APPNAME_PREFIX)) {
				log.info("KPP request received, starting process to upload metadata file to amazon");
				File tf = new File(PropertyManager.getPropertyValue(PROP_TEMP_DIR) + "/" + System.currentTimeMillis() + "/" + ISSUE_META_FILENAME);
				StandardFileSystemManager fsManager = null;
				try {
					if (!tf.getParentFile().exists()) {
						log.debug(tf + " does not exists, creating it.");
						tf.getParentFile().mkdirs();
					}
//					If needed, get sftp password locally to it more secure instead of passing it in the message as done here
					String ftpPath = "sftp://" + login + ":" + password + "@" + host + "/" + pubname;
					FileWriter fw = new FileWriter(tf);
					fw.write("<issue-meta>" +
							"<id>" + referenceId + "</id>" +
							"<title>" + pubname + "</title>" + 
							"<description>" + StringEscapeUtils.escapeXml(coverstory) + "</description>" +
							"<edition_date>" + sd + "</edition_date>" +
							"<cover_location>" + ftpPath + "/" + COVER_FILE_NAME + "</cover_location>" +
							"</issue-meta>");
					fw.close();
					
//					lets now upload the file
					fsManager = new StandardFileSystemManager();
					FileSystemOptions opts = new FileSystemOptions();
				    SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
				    SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);
				    fsManager.init();
				    FileObject ftpFile = fsManager.resolveFile(ftpPath + "/issue-meta.xml", opts);
					ftpFile.copyFrom(fsManager.resolveFile(tf.getAbsolutePath()), Selectors.SELECT_SELF);
					log.info("Successfully uploaded issue-meta.xml for " + referenceId);
				} catch (IOException e) {
					log.error("Error with Amazon issue-meta for: " + referenceId, e);
					try {
						EmailProcessor.getInstance().sendMail(PropertyManager.getPropertyValue(PROP_ADMIN_EMAIL), 
								"Error: uploading issue-meta to amazon " + referenceId, 
								"There was an error uploading or creating issue-meta.xml for " + referenceId + "\n" +
								 "If the file was successfully created locally, it should be at " + tf.getAbsolutePath(), 
								PropertyManager.getPropertyValue(PROP_SMTP_SERVER), 
								PropertyManager.getPropertyValue(PROP_EMAIL_FROM), 
								HTML_MIME_TYPE, 
								null);
					} catch (MessagingException e1) {
						log.error("Error email error: " + e1);
					}
				} finally {
					if (fsManager != null) {
						fsManager.close();
					}
					if (tf.exists()) {
						File parentfolder = tf.getParentFile();
						tf.delete();
						parentfolder.delete();
					}
				}
			}
			
			log.info("port: " + port + ", saledate: " + saledate
							+ ", host: " + host
							+ ", referenceId: " + referenceId
							+ ", newsstand: " + newsstand
				//			+ ", password: " + password
							+ ", folder: " + folder
							+ ", issuename: " + issuename
							+ ", price: " + price
							+ ", pubname: " + pubname
							+ ", previewpath: " + previewpath
							+ ", appname: " + appname
							+ ", login: " + login
							+ ", apptype: " + apptype
							+ ", success: " + success
							+ ", shortdate: " + shortdate 
							+ ", coverstory: " + coverstory
							+ ", Content Path: " + contentpath);
		} catch (JMSException e) {
			log.error("OnMessage error: " + e);
		}
	}

}
