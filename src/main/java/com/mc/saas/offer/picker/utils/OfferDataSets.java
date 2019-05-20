package com.mc.saas.offer.picker.utils;

import java.io.Serializable;

import com.mc.saas.offer.picker.Knapsack;
/**
 * @author suqiang.song
 *
 */
public class OfferDataSets implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 2050979626732450337L;
	private int offerId=0;
	private Integer offerQuota;
	private Double offerPriority;
	private Knapsack[] userGroupOfferBags;
	private Knapsack[] userOfferBags;
	private int[][] offerMatrixs;
	
	public int getOfferId() {
		return offerId;
	}
	public void setOfferId(int offerId) {
		this.offerId = offerId;
	}
	public Integer getOfferQuota() {
		return offerQuota;
	}
	public void setOfferQuota(Integer offerQuota) {
		this.offerQuota = offerQuota;
	}
	public Double getOfferPriority() {
		return offerPriority;
	}
	public void setOfferPriority(Double offerPriority) {
		this.offerPriority = offerPriority;
	}
	public Knapsack[] getUserGroupOfferBags() {
		return userGroupOfferBags;
	}
	public void setUserGroupOfferBags(Knapsack[] userGroupOfferBags) {
		this.userGroupOfferBags = userGroupOfferBags;
	}
	public Knapsack[] getUserOfferBags() {
		return userOfferBags;
	}
	public void setUserOfferBags(Knapsack[] userOfferBags) {
		this.userOfferBags = userOfferBags;
	}
	public int[][] getOfferMatrixs() {
		return offerMatrixs;
	}
	public void setOfferMatrixs(int[][] offerMatrixs) {
		this.offerMatrixs = offerMatrixs;
	}
	
}
