package com.mc.saas.offer.picker.utils;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.mc.saas.offer.picker.Knapsack;
import com.mc.saas.offer.picker.KnapsackConstraint;
/**
 * @author suqiang.song
 *
 */
public class GlobalDataSetsHelper {
	private static final GlobalDataSetsHelper INSTANCE = new GlobalDataSetsHelper();
	private  ConcurrentHashMap<Integer, Integer> offerQuotas = new ConcurrentHashMap<Integer, Integer>();
	private  ConcurrentHashMap<Integer, Double> offerPriorities = new ConcurrentHashMap<Integer, Double>();
	private  ConcurrentHashMap<Integer, Knapsack[]> userGroupOfferBags = new ConcurrentHashMap<Integer, Knapsack[]>();
	private  ConcurrentHashMap<Integer, Knapsack[]> userOfferBags = new ConcurrentHashMap<Integer, Knapsack[]>();
	private  ConcurrentHashMap<Integer, int[][]> offerMatrixsMap = new ConcurrentHashMap<Integer, int[][]>();
	private  ConcurrentHashMap<Integer, KnapsackConstraint> userGroupConstraints = new ConcurrentHashMap<Integer, KnapsackConstraint>();
	private  ConcurrentHashMap<Integer, KnapsackConstraint> userConstraints = new ConcurrentHashMap<Integer, KnapsackConstraint>();
	private  ArrayList<OfferMetrics> omList = new ArrayList<OfferMetrics>();

	private GlobalDataSetsHelper() {
	}

	public static GlobalDataSetsHelper getInstance() {
		return GlobalDataSetsHelper.INSTANCE;
	}

	public ConcurrentHashMap<Integer, Integer> getOfferQuotas() {
		return offerQuotas;
	}

	public void setOfferQuotas(ConcurrentHashMap<Integer, Integer> offerQuotas) {
		this.offerQuotas = offerQuotas;
	}

	public ConcurrentHashMap<Integer, Double> getOfferPriorities() {
		return offerPriorities;
	}

	public void setOfferPriorities(ConcurrentHashMap<Integer, Double> offerPriorities) {
		this.offerPriorities = offerPriorities;
	}

	public ConcurrentHashMap<Integer, Knapsack[]> getUserGroupOfferBags() {
		return userGroupOfferBags;
	}

	public void setUserGroupOfferBags(ConcurrentHashMap<Integer, Knapsack[]> userGroupOfferBags) {
		this.userGroupOfferBags = userGroupOfferBags;
	}

	public ConcurrentHashMap<Integer, Knapsack[]> getUserOfferBags() {
		return userOfferBags;
	}

	public void setUserOfferBags(ConcurrentHashMap<Integer, Knapsack[]> userOfferBags) {
		this.userOfferBags = userOfferBags;
	}

	public ConcurrentHashMap<Integer, int[][]> getOfferMatrixsMap() {
		return offerMatrixsMap;
	}

	public void setOfferMatrixsMap(ConcurrentHashMap<Integer, int[][]> offerMatrixsMap) {
		this.offerMatrixsMap = offerMatrixsMap;
	}

	public ConcurrentHashMap<Integer, KnapsackConstraint> getUserConstraints() {
		return userConstraints;
	}

	public void setUserConstraints(ConcurrentHashMap<Integer, KnapsackConstraint> userConstraints) {
		this.userConstraints = userConstraints;
	}

	public ArrayList<OfferMetrics> getOmList() {
		return omList;
	}

	public void setOmList(ArrayList<OfferMetrics> omList) {
		this.omList = omList;
	}

	public void clean() {
		offerQuotas.clear();
		offerPriorities.clear();
		userGroupOfferBags.clear();
		userOfferBags.clear();
		offerMatrixsMap.clear();
		omList.clear();
		userConstraints.clear();
	}

	public ConcurrentHashMap<Integer, KnapsackConstraint> getUserGroupConstraints() {
		return userGroupConstraints;
	}

	public void setUserGroupConstraints(ConcurrentHashMap<Integer, KnapsackConstraint> userGroupConstraints) {
		this.userGroupConstraints = userGroupConstraints;
	}


}
