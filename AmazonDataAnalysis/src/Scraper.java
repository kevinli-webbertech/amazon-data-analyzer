import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * This is an amazon scraper, the problem is that, we are going to do the pagination of 
 * amazon searched items, and pull each page to collect the data we want. 
 * 
 * Problem: 
 * 
 * Due to the network response time and request scraping of a large number of pages,
 * it results in a huge number of computation. 
 * The total number of the searched item is always changing, so before we 
 * iterate through all the pages, the total number of pages might change.
 * 
 * Solution: distributing the computing by using multiple machines to do the data collection
 * and merge the result back from ASCII file.
 * 
 * Jsoup lib: 
 * 
 * the selector pattern refers to: 
 * https://jsoup.org/cookbook/extracting-data/selector-syntax
 * 
 * For example, div.xx and div#xx syntax.
 * 
 *  @author xiaofeng li  xlics05@gmail.com 
 * */

public class Scraper {
	private Document document;
	private static Scraper scraper = new Scraper();
	final static String URL = "https://www.amazon.com/s/ref=nb_sb_noss?url=search-alias%3Daps&field-keywords=daypack";
	public static Logger logger = Logger.getLogger(Scraper.class);
	
	/**
	 * This is compulsory to run otherwise the Document object will be null.
	 * 
	 * Set timeout value so the IOException will be casted.
	 * 
	 * TODO need the log4j to hook up so that the exception handling can log stuff.
	 * rename this method, as setDocument right now is a very bad name
	 * */
	public void setDocument(String url) {
		try {
			document = Jsoup.connect(URL)
					.userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) "
							+ "Chrome/19.0.1042.0 Safari/535.21").timeout(10000)
					.get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("IOException");
		}
	}

	private Scraper() {}
	
	public static Scraper getInstance() {
		return scraper;
	} 
	
	/* TODO what is the better name to replace the Summary?
	  It is the string on top of each product search page such as:
	  "17-32 of 213,094 results for "Daypack""
	*/
	private String getSummaryText(Document document) {
		Element totalCount = document.select("div.s-first-column").first();
		System.out.println("summary text is:" + totalCount.text());
		return totalCount.text();	
	}
	
	/**
	 *  @return total number of searched items
	 *  Example is: "17-32 of 213,094 results for "Daypack""
	 * 
	 * 1/ The apache common lang String lib can help get the number string between two string which is
	 * easier for development.
	 * 
	 * 2/ The div that contains the info was found by browser's inspector, and this is subject to change.
	 * Make this a configurable constant.
	 * */
	public Integer getTotalCountOfItems(String summaryText) {
		return Integer.valueOf(StringUtils.substringBetween(summaryText, "of", "results").trim().replaceAll(",+", ""));
	}

	/**
	 * @return number of items that will be shown in each page
	 * 
	 * Example is: "17-32 of 213,094 results for "Daypack""
	 * */
	public Integer getItemsCountsPerPage(Document document) {
		return Integer.valueOf(StringUtils.substringBetween(getSummaryText(document), "-", " "));
	}
	
	//For example 11/2 should return 6 pages
	public double getTotalPages(int totalPage, int itemsPerPage) {
		return Math.ceil(totalPage/itemsPerPage);
	}
	
	/** 
	 * @return Document object which is connected by jsoup
	 * 
	 * Hopefully just connect to the url and set document once, 
	 * and we can reuse the document object
	   as the connect(url) operation is costly.
	*/
	public Document getDocument() {
		return document;
	}
	
	/**
	 * @return a list of product item in each page
	 *  To parse the dom using jsoup, the div.xxx, div#xxx
		. is used for class and # is for id.
		In each page
		
		related html tags are : div#atfResults (also found only 4 items under ul tag)
		Issue: for some reason ul#s-results-list-atf is only showing 4 items, but there are 20 items, there 
		are 16 are good ones, others a sponsored links, which we need to filtered them out.
	*/
	public List<ProductItem> getItemsPerPage(Document document) {
		List<ProductItem> products = new ArrayList<>();
		/* In the web browser inspector, we rely on the data-* attribute to find out the patterns 
		 * 
		 * <li id="result_0" data-asin="B01IFVL7VG" class="s-result-item celwidget">
		 * */
		Elements middleColumn = document.select("li[^data-]"); 
		
		//the following is a util function for testing
		//it output 20 items per page including 4 sponsored links.
//		printElements(middleColumn);
		
		//TODO need to parse each li tag and see if it is a "sponsor" item, if not add it to List.
		/*
			//print out only sponsored item, this is not working yet
			if (item.select("h:contains(Sponsored)")!=null && !item.select("h:contains(Sponsored)").isEmpty()) {
				System.out.println(item.attr("id")+ " " + item.attr("data-asin"));
			}
		*/
		for(int i=0; i<middleColumn.size(); i++){
			Element ele = middleColumn.get(i);
			ProductItem product = new ProductItem();
			if(isSponsoredProduct(ele)) {
				System.out.println("%%%%%%isSponsoredProduct");
				continue;
			}
			else if(isShopByCategory(ele)) {
				System.out.println("isShopByCategory");
				continue;
			}
			else{
				product.setProductURL(ele);
				product.setAsin(ele);
				System.out.println(product.getProductURL());
				System.out.println(product.getAsin());
				product.setDocument();
				product.setRating();
				product.setBsr();
				product.setReviewNumber();
				product.setImageURLs();
				System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&" + "\n");
			}
		}
		
		return products;
	}
	
	public void printElements(Elements elements) {
		//for (int i=0;i<elements.size();i++) {
		for (int i=0;i<elements.size();i++) {
			Element item = elements.get(i);
//			if(isSponsoredProduct(item)) {
//				System.out.println("Sponsored" + " " + item.attr("data-asin"));
//			} else {
//				System.out.println(item.attr("data-asin"));
//			}
				
			getURL(item);
//			System.out.println(item.attr("data-asin"));
		}
	}
	
	/**
	 * @param Element of <li></li> tag
	 * @return a string of url
	 * */
	public String getURL(Element ele) {
		/*
		 * I've this HTML code:

 		<td class="topic starter"><a href="http://www.test.com">Title</a></td>
		I want to extract "Title" and the URL, so I did this:

 		Elements titleUrl = doc.getElementsByAttributeValue("class", "topic starter");
 
 		String title = titleUrl.text();
		And this works for the title, for the URL I tried the following:
		 * 
		 * 
		 * element link = doc.select("td.topic.starter > a");
			String url = link.attr("href");
		*/
		
		Element link = ele.select("a").first();
		// this link 's ChildNode has all the images I need, could be sperated with a comma and store in a String type.
		// need to make another field in ProductItem.
		//URL is also accessible from the href, the problem is that we have to decode it.
		/*
		 * For example, what I got is: 
		 * 
		 * https%3A%2F%2Fwww.amazon.com%2FGobago-Foldable-Backpack-Lightweight-Packable%2Fdp%2FB01HO4BAW6%2Fref%3Dsr_1_1%2F154-4658085-6387846%3Fie%3DUTF8%26qid%3D1484287411%26sr%3D8-1-spons%26keywords%3Ddaypack%26psc%3D1&amp;qualifier=1484287411&amp;id=3003244927772836&amp;widgetName=sp_atf
		 * 
		 * */
		link.childNode(0).toString();
		String url = null;
		try {
			url = java.net.URLDecoder.decode(link.attr("abs:href"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block, log this to log file
			e.printStackTrace();
		}
		String title = link.text();
		System.out.println("URL is: " + url);
		System.out.println("Title is:" + title);
		return url;
	}
	
	// ele is <li> tag for instance
	/*TODO: refactor this*/
	private boolean isSponsoredProduct(Element ele) {
		String str = null;
		if(ele.select("h5") != null && ele.select("h5").first() != null) {
			str = ele.select("h5").first().text();
//			System.out.println("^^^^^^^^" + str);
			if(str.equals("Sponsored")) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isShopByCategory(Element ele){
		if(ele.getElementsByClass("acs-mn2-midWidgetHeader").text().equals("Shop by Category")) {
//			System.out.println("Shop by Category");
			return true;
		}
		else {
			return false;
		}
	}
	
	/*TODO start service, implement this to make sure that it only 
	 * starts once and check the document is set. 
	*/
	public void startService() {}
	
    /*TODO move all the methods to the util class, 
     * such a large file is hard to read.
	*/
	public static void main(String[] args) throws Exception {
		Scraper s = Scraper.getInstance();
        s.setDocument(Scraper.URL);
        Document document = s.getDocument();
		System.out.println(s.getTotalCountOfItems(s.getSummaryText(document)));
		System.out.println(s.getItemsCountsPerPage(document));
	    List<ProductItem> products = s.getItemsPerPage(document);
	    
	    PropertyConfigurator.configure("log4j.properties");
        // 记录debug级别的信息  
//        logger.debug("This is debug message.");  
//        // 记录info级别的信息  
//        logger.info("This is info message.");  
//        // 记录error级别的信息  
//        logger.error("This is error message.");

    }
}