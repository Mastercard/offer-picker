package com.mc.saas.offer.picker.utils;
/**
 * @author suqiang.song
 *
 */
public class OfferMetrics implements Comparable {


	public OfferMetrics(int offerid, int quota, int pickerUserCount, int maxScore, int minScore, long maxValue,
			long totalValue, int totalWeight, double offerpriority) {
		super();
		this.offerid = offerid;
		this.quota = quota;
		this.pickerUserCount = pickerUserCount;
		this.maxScore = maxScore;
		this.minScore = minScore;
		this.maxValue = maxValue;
		this.totalValue = totalValue;
		this.totalWeight = totalWeight;
		this.offerpriority = offerpriority;
	}

	private int offerid;
	private int quota;
	private int pickerUserCount;
	private int maxScore;
	private int minScore;
	private long maxValue;
	private long totalValue;
	private int totalWeight;
	private double offerpriority;

	public int getOfferid() {
		return offerid;
	}

	public void setOfferid(int offerid) {
		this.offerid = offerid;
	}

	public int getQuota() {
		return quota;
	}

	public void setQuota(int quota) {
		this.quota = quota;
	}

	public int getPickerUserCount() {
		return pickerUserCount;
	}

	public void setPickerUserCount(int pickerUserCount) {
		this.pickerUserCount = pickerUserCount;
	}

	public int getMaxScore() {
		return maxScore;
	}

	public void setMaxScore(int maxScore) {
		this.maxScore = maxScore;
	}

	public int getMinScore() {
		return minScore;
	}

	public void setMinScore(int minScore) {
		this.minScore = minScore;
	}

	public int getTotalWeight() {
		return totalWeight;
	}

	public void setTotalWeight(int totalWeight) {
		this.totalWeight = totalWeight;
	}

	public int compareTo(Object o) {
		double comparevalue = ((OfferMetrics) o).getOfferpriority();
		/* For Decending order */
		return (int) (comparevalue - this.offerpriority);
	}

	public double getOfferpriority() {
		return offerpriority;
	}

	public void setOfferpriority(double offerpriority) {
		this.offerpriority = offerpriority;
	}

	public long getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(long maxValue) {
		this.maxValue = maxValue;
	}


	public static String[] getColumns() {
		String[] columns = new String[] { "offerid", "quota", "pickerUserCount", "maxScore", "minScore", "maxValue",
				"totalValue","totalWeight", "offerpriority" };
		return columns;
	}

	public long getTotalValue() {
		return totalValue;
	}

	public void setTotalValue(long totalValue) {
		this.totalValue = totalValue;
	}

	@Override
	public String toString() {
		return "OfferMetrics [offerid=" + offerid + ", quota=" + quota + ", pickerUserCount=" + pickerUserCount
				+ ", maxScore=" + maxScore + ", minScore=" + minScore + ", maxValue=" + maxValue + ", totalValue="
				+ totalValue + ", totalWeight=" + totalWeight + ", offerpriority=" + offerpriority + "]";
	}

}
