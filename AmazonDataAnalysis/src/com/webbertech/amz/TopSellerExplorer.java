package com.webbertech.amz;
import java.io.UnsupportedEncodingException;
import org.jsoup.nodes.Document;

public class TopSellerExplorer extends Crawler {
	// Refer to the following url to make the code work,
	// "https://www.amazon.com/s/ref=nb_sb_noss?url=search-alias%3Daps&field-keywords=best+selling+bike+computer";
	                 //                 https://www.amazon.com/s/ref=nb_sb_noss?url=search-alias=aps&field-keywords=best%2Bselling%2Bbike%2Bcomputer
	private final String baseURL  = "https://www.amazon.com/s/ref=nb_sb_noss?url=search-alias=aps&field-keywords=";
	private String topSellingURL;
 	
	public void setEntryURL(String keyword) {
		try {
			this.topSellingURL  = this.baseURL+ java.net.URLEncoder.encode(keyword.replaceAll(" ", "\\+"),"UTF-8");
			this.topSellingURL = java.net.URLDecoder.decode(this.topSellingURL, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
		}
	}
	
	public TopSellerExplorer(String keyword) {
		super();
		setEntryURL(keyword);
	    System.out.println(this.topSellingURL);
	}

	public void setTopSellingURL () {
		this.topSellingURL = "";
	}
	
	public String getTopSellingURL() {
		return this.topSellingURL;
	}
	
	//TODO need to implement a setDocument thing, this is better to be a method in an interface
	public static void main(String[] args) {
		TopSellerExplorer t = new TopSellerExplorer("best selling bike computer");

	}
}