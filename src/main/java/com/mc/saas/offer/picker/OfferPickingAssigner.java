package com.mc.saas.offer.picker;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mc.saas.offer.picker.utils.MetricProcessor;
import com.mc.saas.offer.picker.utils.OfferDataSets;
import com.mc.saas.offer.picker.utils.WorkerMetrics;
/**
 * @author suqiang.song
 *
 */
class OfferPickingAssigner implements Callable<OfferDataSets>{
	
	private int offerId;
	private Knapsack[] bags;
	private ConcurrentHashMap<Integer, KnapsackConstraint> userGroupConstraints = new ConcurrentHashMap<Integer, KnapsackConstraint>();
	private ConcurrentHashMap<Integer, KnapsackConstraint> userConstraints = new ConcurrentHashMap<Integer, KnapsackConstraint>();
	private static final Logger log = LoggerFactory.getLogger(OfferPickingAssigner.class);
	
	public OfferPickingAssigner(int offerid,Knapsack[] bags,ConcurrentHashMap<Integer, KnapsackConstraint> userGroupConstraints,ConcurrentHashMap<Integer, KnapsackConstraint> userConstraints) {
		super();
		this.offerId = offerid;
		this.bags = bags;
		this.userGroupConstraints = userGroupConstraints;
		this.userConstraints = userConstraints;
	}

	
	public synchronized OfferDataSets offerPickingAssignment() {
		long start = System.currentTimeMillis();
		log.info(" --------start: offerPickingAssignment for offer id: --------- "+offerId);
		OfferDataSets ods = new OfferDataSets();
		ods.setOfferId(offerId);
		filterUserBags(offerId,bags,userGroupConstraints,userConstraints);
		ods.setUserOfferBags(bags);
		log.info(" --------end: offerPickingAssignment for offer id: --------- "+offerId);
		long end = System.currentTimeMillis();
		MetricProcessor.getInstance().addMetrics(new WorkerMetrics("offerPickingAssignment:"+Thread.currentThread().getName(),(end-start)));
		return ods;
	}
	
	public synchronized boolean isOfferChoose(int offerId, KnapsackConstraint kc) {
		for (Knapsack k : kc.getkList()) {
			if (offerId == k.getOfferId())
				return true;
		}
		return false;
	}
	
	public synchronized boolean isUserBeyondConstraint(int offerId, Knapsack currentBag,KnapsackConstraint userKc) {
		if (userKc.getMaxMeet())
			return true;
		if(userKc.addKnapsack(currentBag))
		{
			return false;
		}
		return true;
	}
	
	public synchronized void filterUserBags(Integer offerId, Knapsack[] userBags,ConcurrentHashMap<Integer, KnapsackConstraint> userGroupConstraints,
			ConcurrentHashMap<Integer, KnapsackConstraint> userConstraints) {
		int removeOfferChooseCount = 0 ;
		int removeUserContraintCount = 0 ;
		for (Knapsack k : userBags) {
			int userGroupId = k.getUserGroupId();
			int userId = k.getUserId();
			// get the user group constraints 
			KnapsackConstraint userGroupKc = userGroupConstraints.get(userGroupId);
			// get the user constraints 
			KnapsackConstraint userKc = userConstraints.get(userId);
			if(null == userKc || null == userGroupKc)
			{
				log.warn("user constraint or user group constraint is null!!");
			}
			else
			{
				// find the offerid ?
				if (!isOfferChoose(offerId, userGroupKc)) {
					// set the unqualified bag is null  
					k.setUserId(-1);
					removeOfferChooseCount++;
					continue;
				}
				// beyond the user constraints ? 
				if (isUserBeyondConstraint(offerId,k,userKc)) {
					// set the unqualified bag is null  
					k.setUserId(-1);
					removeUserContraintCount++;
					continue;
				}
			}
			
			
		}
		log.info("filterUserBags"+" OfferId:"+offerId+" remove ["+removeOfferChooseCount+"] bags for OfferChoose"+" remove ["+removeUserContraintCount+"] bags for UserConstraint" );
	}
	

	public OfferDataSets call() throws Exception {
		return offerPickingAssignment();
	}

}