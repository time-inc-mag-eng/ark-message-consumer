/**
 * 
 */
package com.timeinc.messaging.utils;

/**
 * @author apradhan1271
 *
 */
public interface Constants {
	String PROPFILEPATH = "/appdata/message-consumers/config/message-consumers.properties";
	String PROP_DPS_ORIGIN_URL = "adobe-origin-url";
	
	String ACTIVEMQ_URL = "activemq-url";
	String PUBLISH_QUEUE = "publish-queue";
	String ARK_DATACHANGE_TOPIC = "ark.datachange.topic";
	String ARK_CONTENTUPLOAD_TOPIC = "ark.contentupload.topic";
	String ARK_PREVIEWUPLOAD_TOPIC = "ark.previewupload.topic";
	
	String PROP_DB_URL = "db.url";
	String PROP_DB_USERNAME = "db.username";
	String PROP_DB_PASSWORD = "db.password";
	
	String PROP_ARKDB_URL = "db.ark.url";
	String PROP_ARKDB_USERNAME = "db.ark.username";
	String PROP_ARKDB_PASSWORD = "db.ark.password";
	
	String PROP_SMTP_SERVER = "smtp.server";
	String PROP_EMAIL_FROM = "email.from";
	
	String PROP_STATUS_EMAIL_SUBJECT = "request.status.email.subject";
	String PROP_STATUS_EMAIL_BODY = "request.status.email.body";
	
	String PROP_REQUEST_ERROR_MULTIPLE_EMAIL_SUBJECT = "request.error.multiple.email.subject";
	String PROP_REQUEST_ERROR_MULTIPLE_EMAIL_BODY = "request.error.multiple.email.body";
	
	String PROP_SUPPORT_EMAIL_ADDRESSES = "support.email.addresses";
	
	String PROP_ADMIN_EMAIL = "admin.email.address";
	String PROP_ADMIN_SUBJECT = "admin.email.subject";
	String PROP_ADMIN_BODY = "admin.email.body";
	
	String PROP_TEMP_DIR = "temp-directory";
	
	String ACCOUNTS_FILE = "/appdata/message-consumers/config/accounts.list";
	
	String HTML_MIME_TYPE = "text/html";
	
}
