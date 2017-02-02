package com.webbertech.amz;
import org.apache.log4j.PropertyConfigurator;

/*
 * class to start the service in a cleaner way
 * */
public class ScraperService {
	private Scraper scraper;

	public ScraperService() {
		PropertyConfigurator.configure("log4j.properties");
	}
	
	public ScraperService(String entryURL) {
	   this();
	   scraper = Scraper.getInstance();
	   scraper.setEntryURL(entryURL);
	}

	public void startLoggingService() throws Exception {
		if (scraper == null) {
			throw new Exception("scrapper object is null");
		} else if ("".equals(scraper.getEntryURL())) {
			throw new Exception("scrapper entryURL is empty");
		}
		scraper.start();
	}
}