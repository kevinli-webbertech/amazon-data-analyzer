public class ProductItem {
	
	String url;   	  // URL to access each product page
	String asin;  	  // UPC number, which is uniquely identify a product
	float rating; 	  // How many stars
	int reviewNumber; // How many people rate the star
	int bsr; 		  // Best seller rank

	
	public void setURL(String url) {
		this.url = url;
	}
	
	public String getURL() {
		return this.url;
	}
	
	public String getAsin() {
		return asin;
	}

	public void setAsin(String asin) {
		this.asin = asin;
	}

	public float getRating() {
		return rating;
	}

	public void setRating(float rating) {
		this.rating = rating;
	}

	public int getReviewNumber() {
		return reviewNumber;
	}

	public void setReviewNumber(int reviewNumber) {
		this.reviewNumber = reviewNumber;
	}

	public int getBsr() {
		return bsr;
	}

	public void setBsr(int bsr) {
		this.bsr = bsr;
	}
}