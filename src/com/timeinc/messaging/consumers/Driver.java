/**
 * Driver class to run message consumers
 */
package com.timeinc.messaging.consumers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;
import org.apache.log4j.Logger;

import com.timeinc.messaging.utils.Constants;

/**
 * @author apradhan1271
 *
 */
public class Driver implements Constants {

	static final Logger log = Logger.getLogger(Driver.class);
	
	// list of Adobe DPS accounts (email address) one per line
	static final File accountFile = new File(ACCOUNTS_FILE); 
		
	private List<String> accounts = new ArrayList<String>();
	
	
	/**
	 * Set the file change event listener when this Driver is instantiated
	 */
	public Driver() {
		try {
			new FileChangeListener().startListening(accountFile);
		} catch (FileSystemException e) {
			log.error("Cannot instantiate to listen changers in account file" + e);
		}
	}

	/**
	 * Creates consumer from the accounts file
	 */
	public void createConsumers() {
		if (!accountFile.exists() || !accountFile.canRead()) {
			log.error("Cannot read account file: '" + ACCOUNTS_FILE + "'");
			System.exit(0);			
		}
		try {
			BufferedReader buff = new BufferedReader(new FileReader(accountFile));
			String s;
			int count = 0;
			while ((s = buff.readLine()) != null) {
				s.trim();
				if (!accounts.contains(s)) {
					accounts.add(s);
					new FolioPublishingConsumer(s);	// one FolioPublishingConsumer per account is created
					count++;
				}
				
			}
			buff.close();			
			
			log.debug(count + " Publishing Consumers will be started.");

			new ArkContentUploadEventConsumer();
			new ArkIssueDataChangeEventConsumer();
			new ArkPreviewUploadEventConsumer();
			new DPSManagingConsumer();
		} catch (FileNotFoundException e) {
			log.error(e);
		} catch (IOException e) {
			log.error(e);
		} catch (Throwable t) {
			log.error("What happened?", t);
		} 
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		log.info("Starting messaging consumers");
		new Driver().createConsumers();
	}


	/**
	 * @author apradhan1271
	 * Listens to file change, if a new account is added to the account file, new consumer will be started
	 */
	class FileChangeListener implements FileListener {

		DefaultFileMonitor fm;
		
		public void startListening(File f) throws FileSystemException {
			final FileSystemManager fsManager = VFS.getManager();
			final FileObject file = fsManager.toFileObject(f);
			fm = new DefaultFileMonitor(this);
			fm.addFile(file);
			fm.start();
		}
		
		/* (non-Javadoc)
		 * @see org.apache.commons.vfs2.FileListener#fileChanged(org.apache.commons.vfs2.FileChangeEvent)
		 */
		public void fileChanged(FileChangeEvent arg0) throws Exception {
			log.info("Account list file changed, processing!");
			createConsumers();
		}

		/* (non-Javadoc)
		 * @see org.apache.commons.vfs2.FileListener#fileCreated(org.apache.commons.vfs2.FileChangeEvent)
		 */
		public void fileCreated(FileChangeEvent arg0) throws Exception {
			
		}

		/* (non-Javadoc)
		 * @see org.apache.commons.vfs2.FileListener#fileDeleted(org.apache.commons.vfs2.FileChangeEvent)
		 */
		public void fileDeleted(FileChangeEvent arg0) throws Exception {
			
		}
		
	}
}
