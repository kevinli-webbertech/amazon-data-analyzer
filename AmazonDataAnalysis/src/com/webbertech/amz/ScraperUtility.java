package com.webbertech.amz;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/*
 * A repo class that were put a lot of commonly used methods that does not fit into 
 * other objects.
 * */
public class ScraperUtility {
	public static Logger logger = Logger.getLogger(ScraperUtility.class);
	public static String CONNECT_ATTR = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) "
			+ "Chrome/19.0.1042.0 Safari/535.21" ;
	/*
	 * String msg is 'Error in ** ', extMsg is URL, or other stuff
	 * */
	public String formatString(String msg, String extMsg) {
		return msg + "in: " + extMsg;
	}
	
	// ele is <li> tag for instance
	/* TODO: refactor this */
	public static boolean isSponsoredProduct(Element ele) {
		String str = null;
		if (ele.select("h5") != null && ele.select("h5").first() != null) {
			str = ele.select("h5").first().text();
			if (str.equals("Sponsored")) {
				logger.debug("This product is sponsored, so we can ignore it.");
				return true;
			}
		}
		return false;
	}

	public static boolean isShopByCategory(Element ele) {
		if (ele.getElementsByClass("acs-mn2-midWidgetHeader").text().equals("Shop by Category")) {
			// System.out.println("Shop by Category");
			logger.debug("This item is ShopByCategory, so we can ignore it.");
			return true;
		} else {
			return false;
		}
	}
	
	//TODO complete this if needed
	public static void printElements(Elements elements) {
		for (int i = 0; i < elements.size(); i++) {
			Element item = elements.get(i);
			//getURL(item);
		}
	}
}