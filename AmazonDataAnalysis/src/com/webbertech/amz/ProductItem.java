package com.webbertech.amz;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ProductItem {
	private String productURL; // URL to access each product page
	private Document pageDocument; // Document element of each product URL
	private String asin; // ASIN/UPC number, which is uniquely identify a
							// product
	private float rating; // How many stars
	private int reviewNumber; // How many people rate the star
	private String imageURLs; // imageURLs of specific product
	private List<BestSellerRank> bsr = new ArrayList<>(); // Best seller rank, it can have multiple
	public static Logger logger = Logger.getLogger(ProductItem.class);

	public ProductItem() {
	}

	/**
	 * @param Element
	 *            <li>element
	 * @return
	 */
	public void setProductURL(Element ele) {
		Element link = ele.select("a").first();
		 /* URL is also accessible from the href, the problem is that we have to
		   decode it.
		 * For example, what I got is:
		 * 
		 * https%3A%2F%2Fwww.amazon.com%2FGobago-Foldable-Backpack-Lightweight-
		 * Packable%2Fdp%2FB01HO4BAW6%2Fref%3Dsr_1_1%2F154-4658085-6387846%3Fie%
		 * 3DUTF8%26qid%3D1484287411%26sr%3D8-1-spons%26keywords%3Ddaypack%26psc
		 * %3D1&amp;qualifier=1484287411&amp;id=3003244927772836&amp;widgetName=
		 * sp_atf
		 * 
		 */
		try {
			this.productURL = java.net.URLDecoder.decode(link.attr("abs:href"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("Error in decoding product URL of: " + link.attr("abs:href") + e.getMessage());
		}
	}

	public String getProductURL() {
		return this.productURL;
	}

	/**
	 * @param Element,  <li> element
	 * @return
	 */
	public void setAsin(Element ele) {
		this.asin = ele.attr("data-asin");
	}

	public String getAsin() {
		return this.asin;
	}

	/**
	 * @param String, url of page to be connected
	 * @return
	 */
	public void setPageDocument(String url) {
		try {
			this.pageDocument = Jsoup.connect(url).userAgent(ScraperUtility.CONNECT_ATTR).timeout(10000).get();
		} catch (IOException e) {
			logger.error("error in connecting to the URL" + e.getMessage());
		}
	}

	public Document getPageDocument() {
		return this.pageDocument;
	}

	/**
	 * @param Element, 
	 *            Document constructed from a url
	 * @return
	 */
	//TODO images too small
	public void setImageURLs(Document pageDocument) {
		Elements imagesElements = pageDocument.select("div#altImages > ul > li").select("img");
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < imagesElements.size(); i++) {
			Element imgElement = imagesElements.get(i);
			String url = imgElement.attr("src");
			if (i == imagesElements.size() - 1) {
				str.append(url);
			} else {
				str.append(url).append(",");
			}
		}
		this.imageURLs = str.toString();
	}

	public String getImageURLs() {
		return this.imageURLs;
	}

	/**
	 * @param Document
	 * @return
	 */
	public void setRating(Document pageDocument) {
		/* Some link has no rating at all, for example like the following two links,
		 * https://www.amazon.com/Swedish-Backpack-MERU-Svensk-Ryggsac/dp/B01MFCQ97T/ref=sr_1_12/160-5190271-6159765?ie=UTF8&qid=1485828944&sr=8-12&keywords=daypack
		  https://www.amazon.com/Cycling-Backpack-Sunhiker-Resistant-Lightweight/dp/B00QQBKFCK/ref=sr_1_8/159-3531376-0865326?ie=UTF8&qid=1485831250&sr=8-8&keywords=daypack
		*/
		Elements ratingStr = pageDocument.select("span[title]");
		if(ratingStr == null || ratingStr.size() == 0) {
			logger.warn(this.productURL + " no rating element error"); //this is normal and we can accept that.
			return;
		}
		if(ratingStr.size()>1) {
			String[] strs = ratingStr.get(1).attr("title").split(" ");
			String str = strs[0];
			this.rating = Float.parseFloat(str);
		}
	}

	public float getRating() {
		return this.rating;
	}

	/**
	 * @param Document
	 * @return
	 */
	public void setReviewNumber(Document pageDocument) {
		String reviewNumStrs = pageDocument.select("a:contains(customer reviews)").first().text();
		if (reviewNumStrs==null) {
			logger.error("no review number found for url: " + this.productURL);
			return;
		}
		
		String reviewNumber = StringUtils.substringBetween(reviewNumStrs, "", " cus").trim().replaceAll(",", "");
		
		if (! "".equals(reviewNumber) ) {
			this.reviewNumber = Integer.parseInt(reviewNumber);
		} else {
			logger.error("no review number found for url: " + this.productURL);
		}
	}

	public int getReviewNumber() {
		return this.reviewNumber;
	}

	// =============Need to redo the following two methods===================
	/**
	 * @param Document
	 * @return
	 */

	/*
	 * Taking care of the following senarios 1. comment logging issue 2. some
	 * product has bsr 3. some product does not have bsr
	 * 
	 * Logging format: URL, CurrentPage(index/page 13 of 15,0000/16),
	 * TimeStamp(year-month-day-min-sec) Appending content to the log file
	 * 
	 * Example of multiple ranking: 
	 * 
	 * Product URL:https://www.amazon.com/Outlander-Packable-Lightweight-Backpack-Daypack/dp/B0092ECRLA/ref=sr_1_3/166-4228605-2480935?ie=UTF8&qid=1485819426&sr=8-3&keywords=daypack
		ASIN:B0092ECRLA
		
        Product Details has: 
		Amazon Best Sellers Rank: #222 in Sports & Outdoors (See Top 100 in Sports & Outdoors)
        #3 in Sports & Outdoors > Outdoor Recreation > Camping & Hiking > Backpacks & Bags > Backpacking Packs > Hiking Daypacks

	 */
	public void setBsr(Document pageDocument) {
	
		Elements bsrElements = pageDocument.select("li#SalesRank");
		
		/* ##Bug to fix
		 * 
		 *   <!--
        To discuss automated access to Amazon data please contact api-services-support@amazon.com.
        For information about migrating to our APIs refer to our Marketplace APIs at https://developer.amazonservices.com/ref=rm_c_sv, or our Product Advertising API at https://affiliate-program.amazon.com/gp/advertising/api/detail/main.html/ref=rm_c_ac for advertising use cases.
			--> 
		 * 
		 *  20:16:31,441 [ main ] [ WARN ]:124 - https://www.amazon.com/Outlander-Packable-Lightweight-Backpack-Daypack/dp/B0092ECRLA/ref=sr_1_3/155-1123567-9848723?ie=UTF8&qid=1486775748&sr=8-3&keywords=daypack no rating element error
  20:16:31,444 [ main ] [ ERROR ]:238 - Best seller rank not found. Url is:https://www.amazon.com/Outlander-Packable-Lightweight-Backpack-Daypack/dp/B0092ECRLA/ref=sr_1_3/155-1123567-9848723?ie=UTF8&qid=1486775748&sr=8-3&keywords=daypack
		 * */
		
		//Need to find a URL that has two sales rank
		if (!bsrElements.isEmpty()) {
			
			/*
			 * Raw text to extract from: 
			 * 
			 * Amazon Best Sellers Rank: #89 in Sports & Outdoors (See Top 100 in Sports & Outdoors) #1 in 
			   Sports & Outdoors > Outdoor Recreation > Camping & Hiking > Backpacks & Bags > Backpacking Packs > Hiking Daypacks
		    */
			//String rankingText = StringUtils.substringBetween(bsrElements.text(), "#", " in").trim();
			
			String rankingText = bsrElements.text();
			String regex= "#\\d+\\s+";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(rankingText);
			int count =0;
			//TODO need to get the category text out of the pattern
			while (matcher.find()) {    
               String ranking = matcher.group().replace("#", ""); 
               this.bsr.add(new BestSellerRank(new Integer(count++).toString(),ranking));
            }    
			
			/* int bsrValue = Integer
					.valueOf(rankingText.replaceAll(",", ""));*/
		} else {
			// TODO Missing URL, need to find out what it is, not sure about the following bsr.
			
			logger.error("Best seller rank not found. Url is:" + this.getProductURL());
			System.out.println("********SalesRank not find");
			
			System.out.println("find special product");
			Elements bsrEles = pageDocument.getElementsByClass("a-color-secondary a-size-base prodDetSectionEntry")
					.select(":contains(Best Sellers Rank)");
			Element bsrEle = bsrEles.first();
			String temp = bsrEle.siblingElements().text();
			int bsrValue = Integer.parseInt(StringUtils.substringBetween(temp, "#", " in").trim().replaceAll(",", ""));
		}
	}

	public List<BestSellerRank> getBsr() {
		return bsr;
	}
}