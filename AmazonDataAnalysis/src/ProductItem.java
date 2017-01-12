public class ProductItem {
	String asin; // UPC number, which is uniquely identify a product
	float rating; // how many stars
	int reviewNumber; // how many people rate the star
	int bsr; // best seller rank

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
