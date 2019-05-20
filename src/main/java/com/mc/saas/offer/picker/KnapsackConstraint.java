package com.mc.saas.offer.picker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * @author suqiang.song
 *
 */
public class KnapsackConstraint implements Comparable {

	public KnapsackConstraint(int max, int min) {
		super();
		this.max = max;
		this.min = min;
		this.totalValue=0;
		this.totalWeight=0;
	}

	private List<Knapsack> kList = new ArrayList<Knapsack>();
	private int max;
	private int min;
	private int totalWeight;
	private int totalValue;
	private int currentSize;
	private Boolean maxMeet=false;
	private Boolean minMeet=false;

	private synchronized void checkBoundConstraint() {
		if (kList.size() >= min)
			minMeet = true;
		if (kList.size() == max)
			maxMeet = true;
		
	}
	
	private synchronized boolean duplicateMerchant(Knapsack k)
	{
		//one user only can pick up one offer from one merchant
		for(Knapsack tk:kList)
		{
			if(tk.getMerchant().equals(k.getMerchant())) return true;
		}
		return false;
	}

	public synchronized boolean addKnapsack(Knapsack k) {
		boolean ret=false;
		if (!maxMeet) {
			if(!duplicateMerchant(k))
			{
				kList.add(k);
				currentSize=kList.size();
				totalWeight+=k.getWeight();
				totalValue+=k.getValue();
				checkBoundConstraint();
				ret = true;
			}

		}
		return ret;
	}

	public synchronized int compareTo(Object o) {
		int comparevalue=((KnapsackConstraint) o).getCurrentSize();
        /* For Decending order*/
        return comparevalue -this.currentSize;
	}

	public int getCurrentSize() {
		return currentSize;
	}

	public void setCurrentSize(int currentSize) {
		this.currentSize = currentSize;
	}

	public Boolean getMaxMeet() {
		return maxMeet;
	}

	public void setMaxMeet(Boolean maxMeet) {
		this.maxMeet = maxMeet;
	}

	public Boolean getMinMeet() {
		return minMeet;
	}

	public void setMinMeet(Boolean minMeet) {
		this.minMeet = minMeet;
	}

	public List<Knapsack> getkList() {
		return kList;
	}

	public void setkList(List<Knapsack> kList) {
		this.kList = kList;
	}

	@Override
	public String toString() {
		return "KnapsackConstraint [kList=" + kList + ", max=" + max + ", min=" + min + ", totalWeight=" + totalWeight
				+ ", totalValue=" + totalValue + ", currentSize=" + currentSize + ", maxMeet=" + maxMeet + ", minMeet="
				+ minMeet + "]";
	}
	
	public String getMetrics()
	{
		return totalWeight+","+totalValue+","+currentSize+","+maxMeet+","+minMeet+","+getOfferList();
	}
	
	public String getOfferList()
	{
		if(this.kList.isEmpty())
		{
			return "NULL";
		}
		StringBuffer sb = new StringBuffer();
		Collections.sort(this.kList);
		for(Knapsack k:this.kList)
		{
			sb.append(k.getOfferId()+":"+k.getWeight()+":"+k.getValue()).append("|");
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getTotalWeight() {
		return totalWeight;
	}

	public void setTotalWeight(int totalWeight) {
		this.totalWeight = totalWeight;
	}

	public int getTotalValue() {
		return totalValue;
	}

	public void setTotalValue(int totalValue) {
		this.totalValue = totalValue;
	}
}
