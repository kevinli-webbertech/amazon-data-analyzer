import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;


/**
 * This is an amazon scraper, the problem is that, we are going to do the pagination of 
 * amazon searched items, and pull each page to collect the data we want. 
 * 
 * Problem: The total number of the searched item is always changing, so before we 
 * iterate through all the pages, the total number of pages might change.
 * 
 * Solution: distributing the computing by using multiple machines to do the data collection
 * and merge the result back from ASCII file.
 * 
 * Jsoup lib: 
 *    For example it looks like the following, the selector pattern refers to: 
		 * https://jsoup.org/cookbook/extracting-data/selector-syntax
		 * 
		 * 
 *  @author xiaofeng li  xlics05@gmail.com
 * */

public class Scraper {
	private Document document;
	private static Scraper scraper = new Scraper();
	final static String URL = "https://www.amazon.com/s/ref=nb_sb_noss?url=search-alias%3Daps&field-keywords=daypack";

	
	/**
	 * This is compulsory to run otherwise the Document object will be null.
	 * 
	 * Set timeout value so the IOException will be casted.
	 * 
	 * TODO need the log4j to hook up so that the exception handling can log stuff.
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
	public Integer getTotalCountOfItems(Document document) {
		return Integer.valueOf(StringUtils.substringBetween(getSummaryText(document), "of", "results").trim().replaceAll(",+", ""));
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
	public double getTotalPages() {
		return Math.ceil(getTotalCountOfItems(getDocument())/getItemsCountsPerPage(getDocument()));
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
		
		//the following is to output that we get 20 items per page including 4 sponsored links.
		for (int i=0;i<middleColumn.size();i++) {
			Element item = middleColumn.get(i);
			System.out.println(item.attr("data-asin"));
		}
		
		//TODO need to parse each li tag and see if it is a "sponsor" item, if not add it to List.
		/*
			//print out only sponsored item, this is not working yet
			if (item.select("h:contains(Sponsored)")!=null && !item.select("h:contains(Sponsored)").isEmpty()) {
				System.out.println(item.attr("id")+ " " + item.attr("data-asin"));
			}
			*/
		for (int i=0;i<middleColumn.size();i++) {
			Element item = middleColumn.get(i);
			if(isSponsoredProduct(item)) System.out.println("Sponsored" + " " + item.attr("data-asin"));
			else {
				System.out.println(item.attr("data-asin"));
			}
		}
		return products;
	}
	
	// ele is <li> tag for instance
	private boolean isSponsoredProduct(Element ele) {
		String str = null;
		if(ele.select("h5") != null && ele.select("h5").first() != null) {
			str = ele.select("h5").first().text();
			if(str.equals("Sponsored")) return true;
		}
		return false;
	}
	
	
	public static void main(String[] args) throws Exception {
		Scraper s = Scraper.getInstance();
        s.setDocument(Scraper.URL);
        Document document = s.getDocument();
		System.out.println(s.getTotalCountOfItems(document));
		System.out.println(s.getItemsCountsPerPage(document));
	    List<ProductItem> products = s.getItemsPerPage(document);
        
		// this works div.xx, and div#xx syntax.
	
	   // Element middleColumn = document.select("div#resultsCol").first();
		/*
	    Element middleColumn = document.select("ul#s-results-list-atf").first();
		  for (Element li : middleColumn.getAllElements()) {
		  System.out.println(li.toString()); }
		*/
	    
//	    List<ProductItem> list = s.getItemsPerPage(document);
	 
	}
}