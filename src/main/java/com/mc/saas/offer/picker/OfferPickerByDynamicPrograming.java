package com.mc.saas.offer.picker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
/**
 * @author suqiang.song
 *
 */
public class OfferPickerByDynamicPrograming {

	private Knapsack[] bags;

	private int offerid;

	private int totalWeight;

	/** count of bags */
	private int n;

	/** best value from TOP N */
	private double bestValue;

	/** TOP N */
	private ArrayList<Knapsack> bestSolution;

	private ConcurrentHashMap<Integer, KnapsackConstraint> userConstraints;

	// for calculate
	public OfferPickerByDynamicPrograming(int offerid, Knapsack[] bags, int totalWeight) {
		this.bags = bags;
		this.offerid = offerid;
		this.totalWeight = totalWeight;
		this.n = bags.length;
	}

	// for solve
	public OfferPickerByDynamicPrograming(int offerid, Knapsack[] bags, int totalWeight,
			ConcurrentHashMap<Integer, KnapsackConstraint> userConstraints) {
		this.bags = bags;
		this.offerid = offerid;
		this.totalWeight = totalWeight;
		this.userConstraints = userConstraints;
		this.n = bags.length;
	}

	public synchronized void calculate(int[][] bestValues) {
		System.out.println("OfferId:" + this.offerid + " totalWeight: " + totalWeight);
		if (bestValues == null) {
			bestValues = new int[n + 1][totalWeight + 1];
		}
		// find the best solve
		for (int j = 0; j <= totalWeight; j++) {
			for (int i = 0; i <= n; i++) {

				if (i == 0 || j == 0) {
					bestValues[i][j] = 0;
				} else {
					// if weight of i > totalWeight , then best solve must be in previous i-1 bag
					if (j < bags[i - 1].getWeight()) {
						bestValues[i][j] = bestValues[i - 1][j];
					} else {
						int iweight = bags[i - 1].getWeight();
						int ivalue = bags[i - 1].getValue();
						Integer iMax = Math.max(bestValues[i - 1][j], ivalue + bestValues[i - 1][j - iweight]);
						bestValues[i][j] = iMax;
					} // else

				} // else
			} // for
		} // for
	}
	
	
	@SuppressWarnings("unchecked")
	public synchronized void solve(int[][] bestValues) {
        
		// find best solution
		if (bestSolution == null) {
			bestSolution = new ArrayList<Knapsack>();
		}
		int tempWeight = totalWeight;
		for (int i = n; i >= 1; i--) {
			if (bestValues[i][tempWeight] > bestValues[i - 1][tempWeight]) {
				// add user limit check condition
				KnapsackConstraint kc = userConstraints.get(bags[i - 1].getUserGroupId());
				if (kc.getMaxMeet())
					continue;
				if(kc.addKnapsack(bags[i - 1]))
				{
					bestSolution.add(bags[i - 1]);
					tempWeight -= bags[i - 1].getWeight();
				}

			}
			if (tempWeight == 0) {
				break;
			}
		}

		Collections.sort(bestSolution);

		bestValue = bestValues[n][totalWeight];
	}


	public synchronized double getBestValue() {
		return bestValue;
	}

	public synchronized ArrayList<Knapsack> getBestSolution(int limit) {

		return bestSolution.size() < limit ? bestSolution
				: new ArrayList<Knapsack>(bestSolution.subList(0, limit));
	}

}
