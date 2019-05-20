package com.mc.saas.offer.picker;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.mc.saas.offer.picker.utils.OfferMetrics;
/**
 * @author suqiang.song
 *
 */
class OfferPickingSolver implements Callable<String>{

	private int offerid;
	private int quota;
	private double priority;
	private Knapsack[] bags;
	private ConcurrentHashMap<Integer,KnapsackConstraint> userGroupConstraints;
	/** best values matrix with top N */
	private int[][] bestValues;
	private int totalWeight;
	Gson gson = new Gson();
	private static final Logger log = LoggerFactory.getLogger(OfferPickingSolver.class);
	
	public OfferPickingSolver(int offerid, int userCount,int quota, double priority,
			Knapsack[] bags, ConcurrentHashMap<Integer, KnapsackConstraint> userGroupConstraints, int[][] bestValues,int totalWeight) {
		super();
		this.offerid = offerid;
		this.quota = quota;
		this.priority = priority;
		this.bags = bags;
		this.bestValues = bestValues;
		this.userGroupConstraints = userGroupConstraints;
		this.totalWeight = totalWeight;
	}
	
	public synchronized String offerPickingSolve() {
		OfferPickerByDynamicPrograming kp = new OfferPickerByDynamicPrograming(offerid,bags,totalWeight,userGroupConstraints);
		if (bestValues == null) {
			log.error("the input of bestValues couldn't be null !!");
			return null;
		}
		kp.solve(this.bestValues);
		log.info(" -------- offer id: --------- "+offerid);
		ArrayList<Knapsack> retList = kp.getBestSolution(quota);
		int pickCount = 0;
		int maxScore = 0;
		int minScore = 0;
		int totalValue = 0;
		for(Knapsack k : retList)
		{
			totalValue+=k.getValue();
		}
		if(!retList.isEmpty())
		{
			pickCount = retList.size();
			maxScore = retList.get(0).getValue();
			minScore = retList.get(pickCount-1).getValue();
		}
		
		log.info("quota: " + quota);
		log.info("Best Resolve pick up count : " + pickCount);
		log.info("Best Resolve maxValue : " + kp.getBestValue());

		
		OfferMetrics om = new OfferMetrics(offerid,quota,pickCount,maxScore,minScore,totalValue,(long) kp.getBestValue(),totalWeight,priority);
		return gson.toJson(om);
	}
	

	public String call() throws Exception {
		return offerPickingSolve();
	}
}