/**
 * 
 */
package com.timeinc.messaging.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

/**
 * @author apradhan1271
 *
 */
public class PublishLockDAO {

	private static final String INSERT_QUERY = "INSERT INTO PUBLISH_LOCK (ProductId) VALUES(?)";
	private static final String DELETE_QUERY = "DELETE FROM PUBLISH_LOCK WHERE ProductId=?";
	private static final String GET_LOCKED_ISSUE_QUERY = "SELECT * FROM PUBLISH_LOCK WHERE ProductId=?";
	
	private static final Logger log = Logger.getLogger(PublishLockDAO.class);
	
	/**
	 * 
	 */
	public PublishLockDAO() {}

	/**
	 * @param IssueId
	 * @throws SQLException
	 */
	public void createPublishLock(String productId) throws SQLException {
		Connection connection = DBPooledConnection.getConnection();
		PreparedStatement stmt = connection.prepareStatement(GET_LOCKED_ISSUE_QUERY);
		stmt.setString(1, productId);
		if (!stmt.executeQuery().next()) { // Lock does not exist, create a new Lock
			stmt = connection.prepareStatement(INSERT_QUERY);
			stmt.setString(1, productId);
			stmt.execute();
		}		
		DBPooledConnection.attemptClose(stmt);
		DBPooledConnection.attemptClose(connection);
		log.info("Created Lock for: " + productId);
	}
	
	/**
	 * @param IssueId
	 * @throws SQLException
	 */
	public void removePublishLock(String productId) throws SQLException {
		Connection connection = DBPooledConnection.getConnection();
		PreparedStatement stmt = connection.prepareStatement(DELETE_QUERY);
		stmt.setString(1, productId);
		stmt.execute();
		DBPooledConnection.attemptClose(stmt);
		DBPooledConnection.attemptClose(connection);
		log.info("Removed Lock for: " + productId);
	}
	
	/**
	 * @param IssueId
	 * @return boolean
	 * @throws SQLException
	 */
	public boolean isIssueLocked(String productId) throws SQLException {
		boolean locked = false;
		Connection connection = DBPooledConnection.getConnection();
		PreparedStatement stmt = connection.prepareStatement(GET_LOCKED_ISSUE_QUERY);
		stmt.setString(1, productId);
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			locked = true;
		}
		DBPooledConnection.attemptClose(stmt);
		DBPooledConnection.attemptClose(connection);
		return locked;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			PublishLockDAO dao = new PublishLockDAO();
			String issueId = "com.timeinc.time.ipad.inapp.10132012";
			System.out.println("Not locked yet: " + dao.isIssueLocked(issueId));
			dao.createPublishLock(issueId);
			System.out.println("Should be Locked: " + dao.isIssueLocked(issueId));
			dao.removePublishLock(issueId);
			System.out.println("Not locked anymore: " + dao.isIssueLocked(issueId));			
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}
