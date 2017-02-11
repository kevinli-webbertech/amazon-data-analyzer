package com.webbertech.amz;


import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.time.LocalDateTime;
import org.apache.log4j.Logger;


/**
 * This is an amazon scraper, the problem is that, we are going to do the
 * pagination of amazon searched items, and pull each page to collect the data
 * we want.
 * 
 * Problem:
 * 
 * Due to the network response time and request scraping of a large number of
 * pages, it results in a huge number of computation. The total number of the
 * searched item is always changing, so before we iterate through all the pages,
 * the total number of pages might change.
 * 
 * Solution: distributing the computing by using multiple machines to do the
 * data collection and merge the result back from ASCII file.
 * 
 * Jsoup lib:
 * 
 * the selector pattern refers to:
 * https://jsoup.org/cookbook/extracting-data/selector-syntax
 * 
 * For example, div.xx and div#xx syntax.
 * 
 * @author xiaofeng li xlics05@gmail.com
 */

public class Scraper {
	public Document document;
	private final static Scraper scraper = new Scraper();
	public String nextURL = null;
	public static Logger logger = Logger.getLogger(Scraper.class);
    final static boolean started = false;

	private Scraper() {
	}

	public static Scraper getInstance() {
		return scraper;
	}

	private String entryURL;

	public void setEntryURL(String url) {
		this.entryURL = url;
	}

	public String getEntryURL() {
		return this.entryURL;
	}

	/**
	 * This is compulsory to run otherwise the Document object will be null.
	 * 
	 * Set timeout value so the IOException will be casted.
	 * 
	 * TODO need the log4j to hook up so that the exception handling can log
	 * stuff. rename this method, as setDocument right now is a very bad name
	 * 
	 * @return
	 */
	public Document setDocument(String url) {
		try {
			document = Jsoup.connect(url).userAgent(ScraperUtility.CONNECT_ATTR).timeout(10000).get();
		} catch (IOException e) {
			logger.error("Error in connecting to url: " + url + " at " + LocalDateTime.now() + e.getMessage()+"\n");
		}
		return document;
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

	/*
	 * TODO what is the better name to replace the Summary? 
	 * It is the string on
	 * top of each product search page such as:
	 * "17-32 of 213,094 results for "Daypack""
	 */
	public String getSearchResultSummaryText(Document document) {
		Element totalCount = document.select("div.s-first-column").first();
		return totalCount.text();
	}

	/**
	 * @return total number of searched items Example is: "17-32 of 213,094
	 *         results for "Daypack""
	 * 
	 *         1/ The apache common lang String lib can help get the number
	 *         string between two string which is easier for development.
	 * 
	 *         2/ The div that contains the info was found by browser's
	 *         inspector, and this is subject to change. Make this a
	 *         configurable constant.
	 */
	public Integer getTotalCountOfItems(String summaryText) {
		return Integer.valueOf(StringUtils.substringBetween(summaryText, "of", "results").trim().replaceAll(",+", ""));
	}

	/**
	 * @return number of items that will be shown in each page
	 * 
	 *         Example is: "17-32 of 213,094 results for "Daypack""
	 */
	public Integer getItemsCountsPerPage(String summaryText) {
		return Integer.valueOf(StringUtils.substringBetween(summaryText, "-", " "));
	}

	// For example 11/2 should return 6 pages, how many pages for the search result to display
	public double getTotalPages(int totalPage, int itemsPerPage) {
		return Math.ceil(totalPage / itemsPerPage);
	}

	/**
	 * @return a list of product item in each page To parse the dom using jsoup,
	 *         the div.xxx, div#xxx . is used for class and # is for id. In each
	 *         page
	 * 
	 *         related html tags are : div#atfResults (also found only 4 items
	 *         under ul tag) Issue: for some reason ul#s-results-list-atf is
	 *         only showing 4 items, but there are 20 items, there are 16 are
	 *         good ones, others a sponsored links, which we need to filtered
	 *         them out.
	 */
	public List<ProductItem> getItemsPerPage(Document document) {
		List<ProductItem> products = new ArrayList<>();
		/*
		 * In the web browser inspector, we rely on the data-* attribute to find
		 * out the patterns
		 * 
		 * <li id="result_0" data-asin="B01IFVL7VG"
		 * class="s-result-item celwidget">
		 */
		Elements productLists = document.select("li[^data-]");

		//TODO need to shuffle this Elements array, and do not iterate in the normal order
		//TODO need to make a flag to turn this feature on and off, make a class called ScraperProperty.java and read param from a property file
		
		//Turn this on when the flag is possible, and when I finish the bsr implementation
		//Collections.shuffle(middleColumn);
		
		for (int i = 0; i < productLists.size(); i++) {
			
			/*Turn this on if the ip is still banned, it will greatly slow down the single thread though.
			 * Generate a random number between 1-3 secs
			
			
			long rand = (int)(Math.random()*2)+1;
			try {
				Thread.sleep(rand);
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			}
			*/
			
			//TODO missing URL to explain this if logics
			Element ele = productLists.get(i);
			ProductItem product = new ProductItem();
			if (ScraperUtility.isSponsoredProduct(ele)) {
				continue;
			} else if (ScraperUtility.isShopByCategory(ele)) {
				continue;
			} else {
				product.setProductURL(ele);
				product.setAsin(ele);
				product.setPageDocument(product.getProductURL());
				product.setRating(product.getPageDocument());
				product.setBsr(product.getPageDocument());
				product.setReviewNumber(product.getPageDocument());
				product.setImageURLs(product.getPageDocument());
				
				//the following are for testing
				System.out.println("Product URL:" + product.getProductURL());
				System.out.println("ASIN:" + product.getAsin());
				System.out.println("bsr: " + product.getBsr());
				System.out.println("Rating: " + product.getRating());
				System.out.println("ReviewNumber: " + product.getReviewNumber());
				System.out.println("******End of a Product******" + "\n");
				
				products.add(product);
			}
		}
		return products;
	}

	/**
	 * @param Element
	 *            of
	 *            <li></li> tag
	 * @return a string of url
	 */
	public String getURL(Element ele) {
		/*
		 * I've this HTML code:
		 * 
		 * <td class="topic starter"><a
		 * href="http://www.test.com">Title</a></td> I want to extract "Title"
		 * and the URL, so I did this:
		 * 
		 * Elements titleUrl = doc.getElementsByAttributeValue("class",
		 * "topic starter");
		 * 
		 * String title = titleUrl.text(); And this works for the title, for the
		 * URL I tried the following:
		 * 
		 * 
		 * element link = doc.select("td.topic.starter > a"); String url =
		 * link.attr("href");
		 */

		Element link = ele.select("a").first();
		// this link 's ChildNode has all the images I need, could be sperated
		// with a comma and store in a String type.
		// need to make another field in ProductItem.
		// URL is also accessible from the href, the problem is that we have to
		// decode it.
		/*
		 * For example, what I got is:
		 * 
		 * https%3A%2F%2Fwww.amazon.com%2FGobago-Foldable-Backpack-Lightweight-
		 * Packable%2Fdp%2FB01HO4BAW6%2Fref%3Dsr_1_1%2F154-4658085-6387846%3Fie%
		 * 3DUTF8%26qid%3D1484287411%26sr%3D8-1-spons%26keywords%3Ddaypack%26psc
		 * %3D1&amp;qualifier=1484287411&amp;id=3003244927772836&amp;widgetName=
		 * sp_atf
		 * 
		 */
		String url = null;
		try {
			url = java.net.URLDecoder.decode(link.attr("abs:href"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("Error happens in decoding URL of hyperlink object: " + link + " " + e.getMessage());

		}
		String title = link.text();
		System.out.println("URL is: " + url);
		System.out.println("Title is:" + title);
		return url;
	}


	public String getNextPage(Element ele) {
		Element nextPageEles = document.getElementById("centerBelowMinus").getElementsByClass("pagnLink").first()
				.child(0);
		String nextPageLink = "Amazon.com" + nextPageEles.attr("href");
		return nextPageLink;
	}

	// Prevent multithread to start it twice.
	synchronized public void start() throws Exception {
		if (Scraper.started) {
			return;
		}
		if ("".equals(this.getEntryURL())) {
			throw new Exception("No entry URL is set");
		}
		scraper.setDocument(this.entryURL);
		Document document = scraper.getDocument();
		String researchResultText = scraper.getSearchResultSummaryText(document);
		int totalCountOfItems = scraper.getTotalCountOfItems(researchResultText);
		int itemsCountsPerPage = scraper.getItemsCountsPerPage(researchResultText);
		
		//TODO the following lines will be commented out once it is done
		System.out.println("summary text is:" + researchResultText);
		System.out.println(totalCountOfItems);
		System.out.println(itemsCountsPerPage);
		
		
		List<ProductItem> totalProducts = scraper.getItemsPerPage(document);
		scraper.nextURL = scraper.getNextPage(document);
 
		// TODO uncomment these lines, now for debugging, we only
		// do one page interations.
		/*
		 * for(int i=1; i<3;
		 * //Math.ceil(totalCountOfItems/itemsCountsPerPage)-1; i++) {
		 * s.setDocument(s.nextURL); s.document = s.getDocument();
		 * totalProducts.addAll(s.getItemsPerPage(s.document)); s.nextURL =
		 * s.getNextPage(s.document); System.out.println("*****" +
		 * totalProducts.size() + "******"); }
		 */
	}
}