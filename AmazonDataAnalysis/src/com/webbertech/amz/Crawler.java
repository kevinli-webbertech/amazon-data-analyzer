package com.webbertech.amz;

import java.io.IOException;
import java.time.LocalDateTime;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public abstract class Crawler {

	String entryURL;
	Document document;
	Logger logger ;
	
	public Crawler() {
		setLogger();
	}
		
	void setLogger() {
		String loggerName = this.getClass().getSimpleName()+".class";
		this.logger = Logger.getLogger(loggerName);
	}
	
	/**
	 * @return Document object which is connected by jsoup
	 * 
	 *         Hopefully just connect to the url and set document once, and we
	 *         can reuse the document object as the connect(url) operation is
	 *         costly.
	 */
	public Document getDocument() {
		return document;
	}
	
	public String getEntryURL() {
		return this.entryURL;
	}
	
	/**
	 * This is compulsory to run otherwise the Document object will be null.
	 * It will check if the document is bot check by amz too.
	 * 
	 * Set timeout value so the IOException will be casted.
	 * @param String url, a url to grab 
	 * @return Document
	 */
	public void setDocument(String url) throws Exception {
		try {
			document = Jsoup.connect(url).userAgent(ScraperUtility.CONNECT_ATTR).timeout(10000).get();
		} catch (IOException e) {
			logger.error("Error in connecting to url: " + url + " at " + LocalDateTime.now() + e.getMessage()+"\n");
		}

		if ("Robot Check".equals(document.title())) {
			// TODO send java email
			throw new Exception("Bot Check");
		}
	}
	
	abstract void setEntryURL(String url);

}