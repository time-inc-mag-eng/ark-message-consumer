/**
 * Utility DAO class to get data from Ark database.
 * This utility uses connection to MQCLIENT DB, and runs StoredProc in it to access Ark data from 
 * Ark database.
 */
package com.timeinc.messaging.db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.timeinc.messaging.model.ArkDPSInfo;


/**
 * @author apradhan1271
 *
 */
public class ArkDAO {

	private static Logger log = Logger.getLogger(ArkDAO.class);
	
	private static final String PROC_ARTICLES_BY_FOLIOID = "{call GetArticlesByFolioId(?)}";
	private static final String PROC_FOLIOID_FOR_PRODUCTID_APPNAME = "{call GetFolioIdByReferenceId(?,?,?)}";
	private static final String PROC_DPSINFO_FOR_PRODUCTID = "{call GetDPSInfoByReferenceId(?)}";
	private static final String PROC_APPNAME_FOR_FOLIOID = "{call GetAppnameByFolioId(?,?)}";
		
	/**
	 * 
	 */
	public ArkDAO() {}
	

	/**
	 * Executes PROCEDURE GetArticlesByFolioId(IN FolioId VARCHAR(255))
	 * @param folioId
	 * @return Map
	 * @throws SQLException
	 */
	public Map<String, String> getDossierArticleMap(String folioId) throws SQLException {
		log.info("Getting list of Articles for FolioId:" + folioId);
		Map<String, String> m = new HashMap<String, String>();
		Connection con = DBPooledConnection.getConnection();
		CallableStatement cs = con.prepareCall(PROC_ARTICLES_BY_FOLIOID);
		cs.setString(1, folioId);
		ResultSet rs = cs.executeQuery();
		while (rs.next()) {
			m.put(rs.getString(1), rs.getString(2));
		}
		DBPooledConnection.attemptClose(cs);
		DBPooledConnection.attemptClose(con);
		return m;
	}
	
	
	/**
	 * Executes PROCEDURE GetFolioIdByReferenceId(IN ApplicationName VARCHAR(255), 
	 * 												IN ReferenceId VARCHAR(100),
     *               							 OUT FolioId VARCHAR(255))
     * @param productId
	 * @param productId
	 * @return String
	 * @throws SQLException
	 */
	public String getFolioId(String applicationName, String productId) throws SQLException {
		String folioId = null;
		Connection con = DBPooledConnection.getConnection();
		CallableStatement cs = con.prepareCall(PROC_FOLIOID_FOR_PRODUCTID_APPNAME);
		cs.setString(1, applicationName);
		cs.setString(2, productId);
		cs.registerOutParameter(3, java.sql.Types.VARCHAR);
		cs.executeUpdate();
		folioId = cs.getString(3);
		log.info(productId + ": " + folioId);
		DBPooledConnection.attemptClose(cs);
		DBPooledConnection.attemptClose(con);
		log.info("Got foliosId for " + productId + ": " + folioId);
		return folioId;
	} 
	
	/**
	 * @param productId
	 * @return ArkDPSInfo
	 * @throws SQLException 
	 */
	public ArkDPSInfo getDPSInfo(String productId) throws SQLException {
		ArkDPSInfo dpsinfo = null;
		Connection con = DBPooledConnection.getConnection();
		CallableStatement cs = con.prepareCall(PROC_DPSINFO_FOR_PRODUCTID);
		cs.setString(1, productId);
		ResultSet rs = cs.executeQuery();
		if (rs.next()) {
			dpsinfo = new ArkDPSInfo(rs.getString("Address"),
										rs.getString("UserName"),
										rs.getString("Password"),
										rs.getString("ConsumerSecret"),
										rs.getString("ConsumerKey"));
							}
		return dpsinfo;
	}
	
	
	/**
	 * @param folioId
	 * @return String
	 * @throws SQLException
	 */
	public String getAppname(String folioId) throws SQLException {
		String appname = null;
		Connection con = DBPooledConnection.getConnection();
		CallableStatement cs = con.prepareCall(PROC_APPNAME_FOR_FOLIOID);
		cs.setString(1, folioId);
		cs.registerOutParameter(2, java.sql.Types.VARCHAR);
		cs.executeUpdate();
		appname = cs.getString(2);
		log.info(folioId + ": " + appname);
		DBPooledConnection.attemptClose(cs);
		DBPooledConnection.attemptClose(con);
		log.info("Got AppName for " + folioId + ": " + appname);
		return appname;
	}
	

}
