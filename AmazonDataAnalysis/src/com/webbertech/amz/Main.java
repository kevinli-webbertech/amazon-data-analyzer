package com.webbertech.amz;

public class Main {
	
	// TODO make the entry url into a propety file
	final static String initURL = "https://www.amazon.com/s/ref=nb_sb_noss?url=search-alias%3Daps&field-keywords=daypack";
	
	public static void main(String[] args) throws Exception {
		ScraperService service = new ScraperService(initURL);
		service.startLoggingService();
    }
}
