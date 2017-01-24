import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ProductItem {

	private String productURL; // URL to access each product page
	private String asin; // ASIN/UPC number, which is uniquely identify a
							// product
	private Document document; // Document element of the specific product URL
	private String imageURLs; // imageURLs of specific product
	private float rating; // How many stars
	private int reviewNumber; // How many people rate the star
	private int bsr; // Best seller rank
	public static Logger logger = Logger.getLogger(ProductItem.class);
	
	public ProductItem() {
	}

	public void setProductURL(Element ele) {
		Element link = ele.select("a").first();
		// System.out.println(link);
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
		link.childNode(0).toString();
		try {
			this.productURL = java.net.URLDecoder.decode(link.attr("abs:href"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block, log this to log file
			logger.error("if the product URL has not been found");;
			e.printStackTrace();
		}
	}

	public String getProductURL() {
		return this.productURL;
	}

	public void setAsin(Element ele) {
		this.asin = ele.attr("data-asin");
	}

	public String getAsin() {
		return this.asin;
	}

	public void setDocument(String url) {
		try {
			this.document = Jsoup.connect(url)
					.userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) "
							+ "Chrome/19.0.1042.0 Safari/535.21")
					.timeout(10000).get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("if a problem happend while parsing the URL");
			e.printStackTrace();
		}
	}

	public Document getDocument() {
		return this.document;
	}

	public void setImageURLs() {
		// Elements images = document.getElementsByClass("imgTagWrapper");
		// System.out.println(images);
		//// System.out.println(images.select("img[src].class"));
		// this.imageURLs = imageURLs;
	}

	public String getImageURLs() {
		return this.imageURLs;
	}

	public void setRating(Document document) {
		Elements ratingStr = document.select("span[title]");
		String[] strs = ratingStr.get(1).attr("title").split(" ");
		String str = strs[0];
		this.rating = Float.parseFloat(str);
	}

	public float getRating() {
		return this.rating;
	}

	public void setReviewNumber(Document document) {
		String reviewNumStrs = document.select("a:contains(customer reviews)").first().text();
		this.reviewNumber = Integer
				.parseInt(StringUtils.substringBetween(reviewNumStrs, "", " cus").trim().replaceAll(",", ""));
	}

	public int getReviewNumber() {
		return this.reviewNumber;
	}

	public void setBsr(Document document) {
		if (document.select("#SalesRank").isEmpty()) {
			//TODO, need to log this, 
			logger.debug("SalesRank identifier not found");
			//Logging format: URL, CurrentPage(index/page 13 of 15,0000/16), TimeStamp(year-month-day-min-sec)
			//Appending content to the log file
			System.out.println("********SalesRank not find");
		}
		if (!document.select("#SalesRank").isEmpty()) {
			Elements bsrEle = document.select("#SalesRank");
			this.bsr = Integer.valueOf(StringUtils.substringBetween(bsrEle.text(), "#", " in").trim().replaceAll(",", ""));
		} else {
			System.out.println("find special product");
			Elements bsrEles = document.getElementsByClass("a-color-secondary a-size-base prodDetSectionEntry")
					.select(":contains(Best Sellers Rank)");
			Element bsrEle = bsrEles.first();
			String temp = bsrEle.siblingElements().text();
			this.bsr = Integer.parseInt(StringUtils.substringBetween(temp, "#", " in").trim().replaceAll(",", ""));
		}
	}

	public int getBsr() {
		return bsr;
	}
}