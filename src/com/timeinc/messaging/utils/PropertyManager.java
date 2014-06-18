/**
 * Utility to read properties file and give back property values
 */
package com.timeinc.messaging.utils;

import java.io.File;
import java.util.NoSuchElementException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.log4j.Logger;

/**
 * @author apradhan1271
 *
 */
public class PropertyManager implements Constants {

	static Logger log = Logger.getLogger(PropertyManager.class);
	private static PropertiesConfiguration properties;
	
	static {
		new PropertyManager();
	}
	
	
	private PropertyManager() {
		File f = new File(PROPFILEPATH);
		if (f.exists()) {
			log.debug("Loading properties from '" + PROPFILEPATH + "'");
				try {
					properties = new PropertiesConfiguration(f);
				} catch (ConfigurationException e) {
					log.error("Could not load properties file: " + PROPFILEPATH, e);
				}
 			/* auto load the properties file when it changes */
			properties.setReloadingStrategy(new FileChangedReloadingStrategy()); 
		}
		if (properties == null) {
			log.error("Could not load properties file: '" + PROPFILEPATH + "'");
			System.exit(0);
		}
		
	}

	
	/**
	 * @param property
	 * @return String
	 */
	public static String getPropertyValue(String property) {
		String val = "";
		properties.setThrowExceptionOnMissing(true);
		try {
			val = properties.getString(property);
		} catch (NoSuchElementException e) { 
			/* do nothing, we will just return empty string if property key not found */
		}
		return val;
	}
	
	
	/**
	 * @param property
	 * @return String 
	 */
	public static String[] getPropertyValues(String property) {
		String vals[] = null;
		try {
			vals = properties.getStringArray(property);
		} catch (NoSuchElementException e) { 
			/* do nothing, we will just return empty string if property key not found */
		}
		return vals;
	}

}
