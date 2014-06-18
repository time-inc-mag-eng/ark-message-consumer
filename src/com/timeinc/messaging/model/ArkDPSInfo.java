/**
 * Bean that holds DPS user (login) information
 */
package com.timeinc.messaging.model;

/**
 * @author apradhan1271
 *
 */
public class ArkDPSInfo {

	private String address;
	private String userName;
	private String password;
	private String consumerSecret;
	private String consumerKey;
	
	
	/**
	 * @param address
	 * @param userName
	 * @param password
	 * @param consumerSecret
	 * @param consumerKey
	 */
	public ArkDPSInfo(String address, String userName, String password,
			String consumerSecret, String consumerKey) {
		this.address = address;
		this.userName = userName;
		this.password = password;
		this.consumerSecret = consumerSecret;
		this.consumerKey = consumerKey;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the consumerSecret
	 */
	public String getConsumerSecret() {
		return consumerSecret;
	}

	/**
	 * @param consumerSecret the consumerSecret to set
	 */
	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}

	/**
	 * @return the consumerKey
	 */
	public String getConsumerKey() {
		return consumerKey;
	}

	/**
	 * @param consumerKey the consumerKey to set
	 */
	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}
		

}
