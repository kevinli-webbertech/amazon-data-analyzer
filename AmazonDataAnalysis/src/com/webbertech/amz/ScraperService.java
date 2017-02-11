package com.webbertech.amz;

import org.apache.commons.configuration2.Configuration;
import org.apache.log4j.PropertyConfigurator;

/*
 * class to start the service in a cleaner way
 * */
public class ScraperService {
	private Scraper scraper;
	private Configuration config;

	public ScraperService() {
		PropertyConfigurator.configure("log4j.properties");
		config = ScraperConfigurator.getInstance().getConfigurator();
		String entryURL = config.getString("entry.url");
		scraper = Scraper.getInstance();
		scraper.setEntryURL(entryURL);
	}

	public void startCrawlingService() throws Exception {
		if (scraper == null) {
			throw new Exception("scrapper object is null");
		} else if ("".equals(scraper.getEntryURL())) {
			throw new Exception("scrapper entryURL is empty");
		}
		scraper.start();
	}
}