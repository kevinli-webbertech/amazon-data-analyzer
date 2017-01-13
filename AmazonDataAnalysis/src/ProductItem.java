public class ProductItem {
	
	String productURL;   	  // URL to access each product page
	String imageURLs;
	String asin;  	  // ASIN/UPC number, which is uniquely identify a product
	float rating; 	  // How many stars
	int reviewNumber; // How many people rate the star
	int bsr; 		  // Best seller rank

	
    public String getImageURLs() {
		return imageURLs;
	}

	public void setImageURLs(String imageURLs) {
		this.imageURLs = imageURLs;
	}

	public void setProductURL(String url) {
    	this.productURL = url;
    }
	
    public String getProductURL() {
    	return this.productURL;
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