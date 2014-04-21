/**
 * 
 */
package com.timeinc.messaging.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.mchange.v2.c3p0.DataSources;
import com.timeinc.messaging.utils.Constants;
import com.timeinc.messaging.utils.PropertyManager;

/**
 * @author apradhan1271
 *
 */
public class DBPooledConnection implements Constants{
	
	private static Logger log = Logger.getLogger(DBPooledConnection.class);
	
	private static DataSource dataSource;
	//private static DataSource arkDataSource;
	
	static {
		try {			
			dataSource = setupDataSource();
//			arkDataSource = setupArkDataSource();
		} catch (SQLException e) {
			log.error(e);
		}
	}
	

	/**
	 * @return Connection
	 * @throws SQLException
	 */
	public static Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}
	
	
	/**
	 * @return Connection
	 * @throws SQLException
	 
	public static Connection getArkConnection() throws SQLException {
		return arkDataSource.getConnection();
	}*/
	
	/**
	 * @return DataSource
	 * @throws SQLException 
	 
	private static DataSource setupArkDataSource() throws SQLException {
		/* set up db connection properties
		Map<String, String> overrides = new HashMap<String, String>();
		overrides.put("maxIdleTime", "60");
		overrides.put("idleConnectionTestPeriod", "55");
		DataSource unpooled = DataSources.unpooledDataSource(PropertyManager.getPropertyValue(PROP_ARKDB_URL), 
				PropertyManager.getPropertyValue(PROP_ARKDB_USERNAME), PropertyManager.getPropertyValue(PROP_ARKDB_PASSWORD));
		DataSource pooled = DataSources.pooledDataSource(unpooled, overrides);
		return pooled;
	}*/

	/**
	 * @return DataSource
	 * @throws SQLException
	 */
	private static DataSource setupDataSource() throws SQLException {
		/* set up db connection properties */
		Map<String, String> overrides = new HashMap<String, String>();
		overrides.put("maxIdleTime", "60");
		overrides.put("idleConnectionTestPeriod", "55");
		DataSource unpooled = DataSources.unpooledDataSource(PropertyManager.getPropertyValue(PROP_DB_URL), 
				PropertyManager.getPropertyValue(PROP_DB_USERNAME), PropertyManager.getPropertyValue(PROP_DB_PASSWORD));
		DataSource pooled = DataSources.pooledDataSource(unpooled, overrides);
		return pooled;
	}
	
	/**
	 * @param connection
	 */
	public static void attemptClose(Connection connection) {
		try { 
			if (connection != null) {
				connection.close();
			}
		} catch (Exception e) { 
			log.error(e);
		}
	}

	/**
	 * @param stmt
	 */
	public static void attemptClose(Statement stmt) {
		try { 
			if (stmt != null) stmt.close();
		} catch (Exception e) { 
			log.error(e);
		}		
	}

	/**
	 * @param rs
	 */
	public static void attemptClose(ResultSet rs) {
		try { 
			if (rs != null) rs.close();
		} catch (Exception e) { 
			log.error(e);
		}		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Connection connection = null ;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			connection = DBPooledConnection.getConnection();
					stmt = connection.createStatement();
			rs = stmt.executeQuery("Select * from PUBLISH_FOLIO_QUEUE");
			if (rs.next()) {
				System.out.println(rs.getString(1) + " " + rs.getString(2));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			attemptClose(rs);
			attemptClose(stmt);
			attemptClose(connection);
		}

	}

}
