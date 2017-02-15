package com.webbertech.amz;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.*;
import java.time.LocalDateTime;
import org.apache.log4j.Logger;

/**
 * This is an amazon scraper, the problem is that, we are going to do the
 * pagination of amazon searched items, and pull each page to collect the data
 * we want.
 * 
 * Issues:
 * 
 * 1. Large network IO response time, a distributed architecture will be needed.
 * 2. Amazon has a good team to anti-scraping, certain actions have to be used to address this.
 * 
 * Jsoup lib:
 * 
 * the selector pattern refers to:
 * https://jsoup.org/cookbook/extracting-data/selector-syntax
 * 
 * For example, div.xx and div#xx syntax.
 * 
 * @author xiaofeng li
 *         xlics05@gmail.com
 */

public class Scraper {
	
	// Vars used for control the scrapper
	private final static Scraper scraper = new Scraper();
	private static Logger logger = Logger.getLogger(Scraper.class);
    private final static boolean started = false;
	
    private Document currentDocument;  // Document object from each URL that list a few dozen of products
	private float filterRatio; // Usually it is 5%, read from config file
	private boolean randomReading; //Read from config file
	private boolean randomSleeping; //Read from config file
	
	private int rankThreshold; // Calculated from filterRatio
	private String searchResultSummaryText; //Regex catch
	private int totalCountOfItems; //Regex catch
	private Integer itemCountPerPage; //Regex catch
	private double totalPagesCount; //Calculated based on above info
	
	private String entryURL;      // This is read from config file, nextURL is inferred from this var
    private String nextURL = null; // Now is calculated from the current document source and it is sequential order
                                    // literally the next page url.
	
    private List<ProductItem> store; 
    
    /*
     * The following two files are the data I would be interested currently,
     * and they are pretty raw data for further review to see what other 
     * intelligent things we need to derive.
     * */
    private File recordFile;  // A file that records product URL that is above the threshold I am interested
    private File multipleBSRRecord;  // A file that only records product URL that is above threshold and have more than one bsrs.
    
	private Scraper() {
		store = new ArrayList<>();
	}

	public static Scraper getInstance() {
		return scraper;
	}

	public List<ProductItem> getStore() {
	   return store;	
	}
	
	public void setEntryURL(String url) {
		this.entryURL = url;
	}

	public String getEntryURL() {
		return this.entryURL;
	}
	
	public void setRandomReading(boolean randomReading) {
	    this.randomReading = randomReading;
	}

	public void setRandomSleeping(boolean randomSleeping) {
		this.randomSleeping = randomSleeping;	
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
	public Document setCurrentDocument(String url) {
		try {
			currentDocument = Jsoup.connect(url).userAgent(ScraperUtility.CONNECT_ATTR).timeout(10000).get();
		} catch (IOException e) {
			logger.error("Error in connecting to url: " + url + " at " + LocalDateTime.now() + e.getMessage()+"\n");
		}
		return currentDocument;
	}
	
	/**
	 * @return Document object which is connected by jsoup
	 * 
	 *         Hopefully just connect to the url and set document once, and we
	 *         can reuse the document object as the connect(url) operation is
	 *         costly.
	 */
	public Document getCurrentDocument() {
		return currentDocument;
	}

	public void setFilterRatio(float filterRatio) {
		this.filterRatio = filterRatio;
	}
	
	public float getFilterRatio() {
		return this.filterRatio;
	}
	
    //rankThreshold = totalProducts * filterRatio
	public int setRankThreshold() {
		// TODO need to rename a lot of the methods and make them from get to set
		// this.rankThreshold = Math.round(this.filterRatio * this.g**)
		return 0;
	}
	
	public int getRankThreshold() {
		return this.rankThreshold;
	}
	
	/*
	 * TODO what is the better name to replace the Summary? 
	 * It is the string on
	 * top of each product search page such as:
	 * "17-32 of 213,094 results for "Daypack""
	 */
	public void setSearchResultSummaryText(Document document) {
		Element totalCount = document.select("div.s-first-column").first();
		this.searchResultSummaryText = totalCount.text();
	}

	public String getSearchResultSummaryText() {
		return this.searchResultSummaryText;
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
	public void setTotalCountOfItems(String summaryText) {
		this.totalCountOfItems = Integer.valueOf(StringUtils.substringBetween(summaryText, "of", "results").trim().replaceAll(",+", ""));
	}

	public int getTotalCountOfItems() {
		return this.getTotalCountOfItems();
	}
	
	/**
	 * @return number of items that will be shown in each page
	 * 
	 *         Example is: "17-32 of 213,094 results for "Daypack""
	 */
	public void setItemsCountPerPage(String summaryText) {
		this.itemCountPerPage = Integer.valueOf(StringUtils.substringBetween(summaryText, "-", " "));
	}
	
 	public int getItemCountPerPage () {
 		return this.itemCountPerPage;
 	}

	// For example 11/2 should return 6 pages, how many pages for the search result to display
    public void setTotalPagesCount(int totalPage, int itemsPerPage) {
		this.totalPagesCount = Math.ceil(totalPage / itemsPerPage);
	}

	public double getTotalPagesCount() {
		return this.totalPagesCount;
	}
	
	//TODO rename this method, and introduce the new flags to control the random thing for testing
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
	public void addItemsPerPageToStore(Document curDocument, List<ProductItem> store) {
		/*
		 * In the web browser inspector, we rely on the data-* attribute to find
		 * out the patterns
		 * 
		 * <li id="result_0" data-asin="B01IFVL7VG"
		 * class="s-result-item celwidget">
		 */
		Elements productLists = curDocument.select("li[^data-]");

		//Shuffle this Elements array, and do not iterate in the normal order
		if (randomReading) {
			Collections.shuffle(productLists);
		}
		
		for (int i = 0; i < productLists.size(); i++) {
			// Generate a random number between 1-3 secs, switch it in the config file
			if (randomSleeping) {
				long rand = (int)(Math.random()*2)+1;
				try {
					Thread.sleep(rand);
				} catch (InterruptedException e) {
					logger.error(e.getMessage());
				}
			}
			
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
				
				
				List<BestSellerRank> bsrList = product.getBsr();
				if (bsrList.size()>1) {
					
				}
				
				//the following are for testing
				System.out.println("Product URL:" + product.getProductURL());
				System.out.println("ASIN:" + product.getAsin());
				System.out.println("bsr: " + product.getBsr());
				System.out.println("Rating: " + product.getRating());
				System.out.println("ReviewNumber: " + product.getReviewNumber());
				System.out.println("******End of a Product******" + "\n");
				store.add(product);
			}
		}
	}

	/**
	 * @param Element
	 *            of
	 *            <li></li> tag
	 * @return a string of url
	 */
	public String getCurrentPageURL(Element ele) {
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

	public void setNextPageURL(Element curDocument) {
		Element nextPageEles = curDocument.getElementById("centerBelowMinus").getElementsByClass("pagnLink").first()
				.child(0);
		this.nextURL = "Amazon.com" + nextPageEles.attr("href");
	}

	public String getNextPageURL() {
		return this.nextURL;
	}
	
	// Prevent multithread to start it twice.
	synchronized public void start() throws Exception {
		if (Scraper.started) {
			return;
		}
		if ("".equals(this.getEntryURL())) {
			throw new Exception("No entry URL is set");
		}
		scraper.setCurrentDocument(this.getEntryURL());
		Document currentDocument = scraper.getCurrentDocument();
		scraper.setSearchResultSummaryText(currentDocument);
		String searchResultSummaryText = scraper.getSearchResultSummaryText();
		scraper.setItemsCountPerPage(searchResultSummaryText);
		scraper.setTotalCountOfItems(searchResultSummaryText);
		int totalCountOfItems = scraper.getTotalCountOfItems();
		int itemsCountPerPage = scraper.getItemCountPerPage();
		
		//TODO the following lines will be commented out once it is done
		System.out.println("summary text is:" + searchResultSummaryText);
		System.out.println(totalCountOfItems);
		System.out.println(itemsCountPerPage);
		scraper.addItemsPerPageToStore(currentDocument, store);
		scraper.setNextPageURL(currentDocument);
		scraper.nextURL = scraper.getNextPageURL();
		List<ProductItem> totalProducts = scraper.getStore();
		
		// TODO uncomment these lines, now for debugging, we only do one page interations.
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