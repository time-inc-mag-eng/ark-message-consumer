/**
 * Utility DAO used when Folio publishing is requested to keep status of each requests.
 */
package com.timeinc.messaging.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.timeinc.messaging.model.Folio;
import com.timeinc.messaging.utils.DesEncrypter;

/**
 * @author apradhan1271
 *
 */
public class PublishFolioDAO {

	private static Logger log = Logger.getLogger(PublishFolioDAO.class);
	
	private static final String INSERT_QUERY = "INSERT INTO PUBLISH_FOLIO_QUEUE " +
				"(Status, Email, Password, FolioId, ProductId, Retail, RequestId, Requestor, IssueName) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String UPDATE_FOLIO = "UPDATE PUBLISH_FOLIO_QUEUE SET Status=?, Retail=?, RequestId=?, Requestor=? WHERE ID=?";
	private static final String SELECT_BY_FOLIOID = "SELECT * from PUBLISH_FOLIO_QUEUE WHERE FolioId=?";
	private static final String SELECT_BY_PRODUCTID = "SELECT * from PUBLISH_FOLIO_QUEUE WHERE ProductId=?";
	private static final String SELECT_BY_ID = "SELECT * from PUBLISH_FOLIO_QUEUE WHERE ID=?";
	private static final String GET_PENDING_FOLIOS = "SELECT * from PUBLISH_FOLIO_QUEUE WHERE Email=? and Status='pending'";
	
	/* column names from table */
	private static enum Column {
						ID("ID"), 
						STATUS("Status"), 
						LAST_UPDATED("Last_Updated"),
						EMAIL("Email"), 
						PASSWORD("Password"), 
						FOLIOID("FolioId"), 
						PRODUCTID("ProductId"), 
						RETAIL("Retail"), 
						REQUESTID("RequestId"), 
						REQUESTOR("Requestor"), 
						ISSUENAME("IssueName");
		
		private String columnName;
		
		private Column(String name) {
			columnName = name;
		}
		
		public String getColumnName() {
			return columnName;
		}
	}
	

	public PublishFolioDAO() {
		
	}

	/**
	 * Insert record to keep track of publish requests 
	 * @param status
	 * @param email
	 * @param password
	 * @param folioId
	 * @param productId
	 * @param retail
	 * @param requestId
	 * @param requestor
	 * @param issueName
	 * @return Folio
	 * @throws SQLException
	 */
	public Folio createPublishFolioQueue(String status, String email, String password, String folioId, 
										String productId, boolean retail, String requestId, String requestor, 
										String issueName) throws SQLException {
		Connection connection = DBPooledConnection.getConnection();
		PreparedStatement stmt = connection.prepareStatement(INSERT_QUERY);
		stmt.setString(1, status);
		stmt.setString(2, email);
		stmt.setString(3, encrypt(password));
		stmt.setString(4, folioId);
		stmt.setString(5, productId);
		stmt.setBoolean(6, retail);
		stmt.setString(7, requestId);
		stmt.setString(8, requestor);
		stmt.setString(9, issueName);
		stmt.execute();
		DBPooledConnection.attemptClose(stmt);
		DBPooledConnection.attemptClose(connection);
		return getFolioByFolioId(folioId);
	}
	
	
	/**
	 * @param id
	 * @return Folio
	 * @throws SQLException
	 */
	public Folio getFolioById(int id) throws SQLException {
		Folio folio = null;
		Connection connection = DBPooledConnection.getConnection();
		PreparedStatement stmt = connection.prepareStatement(SELECT_BY_ID);
		stmt.setInt(1, id);
		ResultSet rs = stmt.executeQuery();
		if (rs != null && rs.next()) {
			folio = activate(rs);
		}
		DBPooledConnection.attemptClose(stmt);
		DBPooledConnection.attemptClose(connection);
		return folio;
	}
	

	/**
	 * @param folioId
	 * @return Folio
	 * @throws SQLException
	 */
	public Folio getFolioByFolioId(String folioId) throws SQLException {
		Folio folio = null;
		Connection connection = DBPooledConnection.getConnection();
		PreparedStatement stmt = connection.prepareStatement(SELECT_BY_FOLIOID);
		stmt.setString(1, folioId);
		ResultSet rs = stmt.executeQuery();
		if (rs != null && rs.next()) {
			folio = activate(rs);
		}
		DBPooledConnection.attemptClose(stmt);
		DBPooledConnection.attemptClose(connection);
		return folio;
	}
	
	

	/**
	 * @param productId
	 * @return Folio
	 * @throws SQLException
	 */
	public Folio getFolioByProductId(String productId) throws SQLException {
		Folio folio = null;
		Connection connection = DBPooledConnection.getConnection();
		PreparedStatement stmt = connection.prepareStatement(SELECT_BY_PRODUCTID);
		stmt.setString(1, productId);
		ResultSet rs = stmt.executeQuery();
		if (rs != null && rs.next()) {
			folio = activate(rs);
		}
		DBPooledConnection.attemptClose(stmt);
		DBPooledConnection.attemptClose(connection);
		return folio;
	}
	
	/**
	 * @param password
	 * @return String encrypted
	 */
	private String encrypt(String string) {
		return new DesEncrypter().encrypt(string);
	}
	
	/**
	 * @param string
	 * @return String decrypted
	 */
	private String decrypt(String string) {
		return new DesEncrypter().decrypt(string);
	}
	

	/**
	 * Active a Folio bean
	 * @param resultset
	 * @return Folio
	 */
	private Folio activate(ResultSet rs) {
		Folio folio = null;
		try {
			if (rs != null) {
				folio = new Folio(rs.getInt(Column.ID.getColumnName()), 
									rs.getString(Column.STATUS.getColumnName()), 
									rs.getTimestamp(Column.LAST_UPDATED.getColumnName()), 
									rs.getString(Column.EMAIL.getColumnName()), 
									decrypt(rs.getString(Column.PASSWORD.getColumnName())), 
									rs.getString(Column.FOLIOID.getColumnName()), 
									rs.getString(Column.PRODUCTID.getColumnName()), 
									rs.getString(Column.REQUESTID.getColumnName()), 
									rs.getString(Column.REQUESTOR.getColumnName()), 
									rs.getBoolean(Column.RETAIL.getColumnName()),
									rs.getString(Column.ISSUENAME.getColumnName()));
			}
		} catch (SQLException e) {
			log.error(e);
		}
		return folio;
	}
	
	
	/**
	 * @param rs
	 * @return List
	 */
	private List<Folio> activateMore(ResultSet rs) {
		List<Folio> l = new ArrayList<Folio>();
		try {
			while (rs.next()) {
				l.add(activate(rs));
			}
		} catch (SQLException e) {
			log.error(e);
		}
		return l;
	}

	/**
	 * Status=?, Retail=?, RequestId=?, Requestor=?";
	 * @param Folio
	 * @return Folio
	 * @throws SQLException 
	 */
	public Folio updateFolio(Folio folio) throws SQLException {
		Connection connection = DBPooledConnection.getConnection();
		PreparedStatement stmt = connection.prepareStatement(UPDATE_FOLIO);
		stmt.setString(1, folio.getStatus());
		stmt.setBoolean(2, folio.isRetail());
		stmt.setString(3, folio.getRequestId());
		stmt.setString(4, folio.getRequestor());
		stmt.setInt(5, folio.getId());
		stmt.executeUpdate();
		DBPooledConnection.attemptClose(stmt);
		DBPooledConnection.attemptClose(connection);
		return this.getFolioById(folio.getId());
	}


	/**
	 * @param email
	 * @return List
	 * @throws SQLException 
	 */
	public List<Folio> getPendingFoliosByEmail(
			String email) throws SQLException {
		Connection connection = DBPooledConnection.getConnection();
		PreparedStatement stmt = connection.prepareStatement(GET_PENDING_FOLIOS);
		stmt.setString(1, email);
		ResultSet rs = stmt.executeQuery();
		List<Folio> l = activateMore(rs);
		DBPooledConnection.attemptClose(stmt);
		DBPooledConnection.attemptClose(connection);
		return l;
	}


}
