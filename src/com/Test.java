/**
 * 
 */
package com;

import com.timeInc.dps.publish.PublishClient;
import com.timeInc.dps.publish.PublishClients;
import com.timeInc.dps.publish.request.config.LoginConfig;
import com.timeInc.dps.publish.request.config.TicketConfig;
import com.timeInc.dps.publish.response.Credential;
import com.timeInc.dps.publish.response.PublishingStatus;
import com.timeinc.messaging.utils.Constants;
import com.timeinc.messaging.utils.PropertyManager;

/**
 * @author apradhan1271
 *
 * Nov 11, 2013
 */
public class Test implements Constants {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		String folioId = "1XhhVo2cUkWdSaRWtrR0Rw";
		String requestId = "41039617-2b9d-4544-baf1-955977356cd9";		
		PublishClient client = PublishClients.getSingleThreadedClient(PropertyManager.getPropertyValue(PROP_DPS_ORIGIN_URL));
		Credential cred = client.signIn(new LoginConfig("EWS_Generic_Apple@timeinc.com", "sceIQM^sQ92A"));
		PublishingStatus status = client.getPublishStatus(new TicketConfig(cred.getTicket(), requestId));
		System.out.println(status.getMessage());
	}

}
