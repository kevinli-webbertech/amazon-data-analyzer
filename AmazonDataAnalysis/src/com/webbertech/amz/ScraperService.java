package com.webbertech.amz;

import org.apache.commons.configuration2.Configuration;
import org.apache.log4j.PropertyConfigurator;

/*
 *  Initialize the software configuration class
 *  Initialize the log4j configuration class
 *  Initialize scraper class and set initial values to scraper class that are read from configuration file.
 *  Implemented the method to start the scrawling service. 
 * */
public class ScraperService {
	private Scraper scraper;
	private Configuration config;

	public ScraperService() {
		PropertyConfigurator.configure("log4j.properties");
		config = ScraperConfigurator.getInstance().getConfigurator();
		String entryURL = config.getString("entry.url");
		float filterRatio = config.getFloat("filter.ratio");
		scraper = Scraper.getInstance();
		scraper.setEntryURL(entryURL);
		scraper.setFilterRatio(filterRatio);
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