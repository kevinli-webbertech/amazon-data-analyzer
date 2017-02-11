package com.webbertech.amz;

public class BestSellerRank {
	private String Category;
	private String bsrValue;

	public String getCategory() {
		return Category;
	}

	public void setCategory(String category) {
		Category = category;
	}

	public String getBsrValue() {
		return bsrValue;
	}

	public void setBsrValue(String bsrValue) {
		this.bsrValue = bsrValue;
	}

	public BestSellerRank(String category, String bsrValue) {
		super();
		Category = category;
		this.bsrValue = bsrValue;
	}
}