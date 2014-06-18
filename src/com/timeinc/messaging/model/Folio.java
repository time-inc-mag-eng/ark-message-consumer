/**
 * DB POJO object for Folio 
 */
package com.timeinc.messaging.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author apradhan1271
 *
 */
public class Folio implements Serializable {

	private int id;
	private String status;
	private Date lastUpdated;
	private String email;
	private String password;
	private String folioId;
	private String productId;
	private String requestId;
	private String requestor;
	private boolean retail;
	private String issueName;
	
	private static final long serialVersionUID = 4857713855560452480L;

	
	public Folio() {}
	

	/**
	 * @param id
	 * @param status
	 * @param lastUpdated
	 * @param email
	 * @param password
	 * @param folioId
	 * @param productId
	 * @param requestId
	 * @param requestor
	 * @param retail
	 * @param issueName
	 */
	public Folio(int id, String status, Date lastUpdated, String email,
			String password, String folioId, String productId,
			String requestId, String requestor, boolean retail, String issueName) {
		super();
		this.id = id;
		this.status = status;
		this.lastUpdated = lastUpdated;
		this.email = email;
		this.password = password;
		this.folioId = folioId;
		this.productId = productId;
		this.requestId = requestId;
		this.requestor = requestor;
		this.retail = retail;
		this.issueName = issueName;
	}


	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}


	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}


	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}


	/**
	 * @return the lastUpdated
	 */
	public Date getLastUpdated() {
		return lastUpdated;
	}


	/**
	 * @param lastUpdated the lastUpdated to set
	 */
	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}


	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}


	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
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
	 * @return the folioId
	 */
	public String getFolioId() {
		return folioId;
	}


	/**
	 * @param folioId the folioId to set
	 */
	public void setFolioId(String folioId) {
		this.folioId = folioId;
	}


	/**
	 * @return the productId
	 */
	public String getProductId() {
		return productId;
	}


	/**
	 * @param productId the productId to set
	 */
	public void setProductId(String productId) {
		this.productId = productId;
	}


	/**
	 * @return the retail
	 */
	public boolean isRetail() {
		return retail;
	}


	/**
	 * @param retail the retail to set
	 */
	public void setRetail(boolean retail) {
		this.retail = retail;
	}


	/**
	 * @return the requestId
	 */
	public String getRequestId() {
		return requestId;
	}


	/**
	 * @param requestId the requestId to set
	 */
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}


	/**
	 * @return the requestor
	 */
	public String getRequestor() {
		return requestor;
	}


	/**
	 * @param requestor the requestor to set
	 */
	public void setRequestor(String requestor) {
		this.requestor = requestor;
	}


	/**
	 * @return the issueName
	 */
	public String getIssueName() {
		return issueName;
	}


	/**
	 * @param issueName the issueName to set
	 */
	public void setIssueName(String issueName) {
		this.issueName = issueName;
	}
	

}
