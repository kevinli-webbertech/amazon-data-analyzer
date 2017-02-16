package com.webbertech.amz;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.Logger;
/**
 * Configure the property file for the software.
 * */
public class ScraperConfigurator {
	private Configuration config;

	final static ScraperConfigurator  configurator= new ScraperConfigurator();
	public static Logger logger = Logger.getLogger(ScraperConfigurator.class);
	
	public Configuration getConfigurator() {
       return config;	
    }
	
    public static ScraperConfigurator getInstance() {
    	return configurator;
    }
    
    private ScraperConfigurator() {
		Parameters params = new Parameters();
		FileBasedConfigurationBuilder<FileBasedConfiguration> builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(
				PropertiesConfiguration.class).configure(params.properties().setFileName("config.properties"));
		try {
			 config = builder.getConfiguration();
		} catch (ConfigurationException cex) {
			// loading of the configuration file failed了；
		   logger.error(cex);
		}
	}
}