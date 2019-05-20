package com.mc.saas.offer.picker;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mc.saas.offer.picker.utils.GlobalDataSetsHelper;
import com.mc.saas.offer.picker.utils.MetricProcessor;
import com.mc.saas.offer.picker.utils.OfferDataSets;
import com.mc.saas.offer.picker.utils.WorkerMetrics;
/**
 * @author suqiang.song
 *
 */
class OfferPickingConstructor implements Callable<OfferDataSets> {

	private int offerId;
	private int offerCount;
	private int userCount;
	private int userGroupCount;
	private double diverseWeight = 0.5d;
	private Knapsack[] userGroupOfferBags;
	private Knapsack[] userOfferBags;
	private Double offerPriority;
	private int offerQuota;
	private static final Logger log = LoggerFactory.getLogger(OfferPickingConstructor.class);

	public OfferPickingConstructor(int offerId, int offerCount, int userCount, int userGroupCount,
			double diverseWeight) {
		super();
		this.offerId = offerId;
		this.offerCount = offerCount;
		this.userCount = userCount;
		this.userGroupCount = userGroupCount;
		this.diverseWeight = diverseWeight;
		userGroupOfferBags = new Knapsack[userGroupCount];
		userOfferBags = GlobalDataSetsHelper.getInstance().getUserOfferBags().get(offerId);
	}

	public synchronized OfferDataSets offerPickingConstruct() {
		long start = System.currentTimeMillis();
		OfferDataSets ret = new OfferDataSets();
		log.info(" --------start: offerPickingConstruct for offer id: --------- " + offerId);
		constructPickingDataSetsByOffer();
		log.info(" --------end: offerPickingConstruct for offer id: --------- " + offerId);
		long end = System.currentTimeMillis();
		MetricProcessor.getInstance().addMetrics(
				new WorkerMetrics("offerPickingConstruct:" + Thread.currentThread().getName(), (end - start)));
		ret.setOfferId(offerId);
		ret.setOfferPriority(getOfferPriority());
		ret.setOfferQuota(getOfferQuota());
		ret.setUserGroupOfferBags(getUserGroupOfferBags());
		ret.setUserOfferBags(getUserOfferBags());
		return ret;
	}

	public synchronized void constructPickingDataSetsByOffer() {
		long startTime = System.currentTimeMillis();

		// partition all bags
		partitionBags();
		// constuct priority 
		constructPriority();
		// constuct Offer Quota
		constructQuota();

		long endTime = System.currentTimeMillis();
		MetricProcessor.getInstance()
				.addMetrics(new WorkerMetrics("simulateAndInitOffer(offerId:" + offerId + ")", (endTime - startTime)));
	}
	
	public void constructPriority() {
		offerPriority = 1 - sigmoid(offerId * 0.1, diverseWeight);
	}

	public void constructQuota() {
		int quota = ThreadLocalRandom.current().nextInt((int) userGroupCount / 10,
				(int) (userGroupCount * (1 + offerPriority)));
		offerQuota = quota <= userGroupCount ? quota : userGroupCount;
		if(offerQuota == 0)
		{
			log.warn("ZERO OfferQuota");
		}
	}


	public synchronized void partitionBags() {
		long startTime = System.currentTimeMillis();
		log.info("starting partitioning the bags belongs to OfferId :" + offerId + " to " + userGroupCount
				+ " partitions");
		Knapsack[] bags = userOfferBags;
		if(null == bags || bags.length==0)
		{
			log.error("Offer user bags are null! OfferId is "+offerId);
			return ;
		}
		int step = bags.length % userGroupCount == 0 ? (int) Math.ceil(bags.length / userGroupCount)
				: (int) Math.ceil(bags.length / userGroupCount);
		int group = userGroupCount - 1;
		for (int start = 0; start < bags.length; start += step) {
			int end = Math.min(start + step, bags.length);
			for (int j = start; j < end; j++) {
				bags[j].setUserGroupId(group);
			}
			if (group == userGroupCount - 1) {
				log.trace("current group:" + group + " current end:" + end + " total bag size:" + bags.length);
				userGroupOfferBags[userGroupCount - 1] = new Knapsack(userGroupCount - 1, userGroupCount - 1,
						bags[end - 1].getOfferId(), bags[end - 1].getMerchant(), offerCount, bags[end - 1].getValue());
			}
			group--;

			// add the last one knapsack to groups
			if (group >= 0) {
				log.trace("current group:" + group + " current end:" + end + " total bag size:" + bags.length);
				userGroupOfferBags[group] = new Knapsack(group, group, bags[end - 1].getOfferId(), bags[end].getMerchant(),
						offerCount, bags[end - 1].getValue());
			}
			if (group < 0 || start >= bags.length) {
				break;
			}
		}
		log.info("final group now is :" + (group + 1) + " bags size is " + bags.length);
		Arrays.sort(userGroupOfferBags);
		log.info("partitioned the bags belongs to OfferId :" + offerId);
		long endTime = System.currentTimeMillis();
		MetricProcessor.getInstance()
				.addMetrics(new WorkerMetrics("partitionBags(offerId:" + offerId + ")", (endTime - startTime)));
	}

	public static double sigmoid(double x, double diverseWeight) {
		return (1 / (1 + Math.pow(Math.E, (-1 * x)))) - diverseWeight;
	}

	public OfferDataSets call() throws Exception {
		return offerPickingConstruct();
	}

	public synchronized Knapsack[] getUserGroupOfferBags() {
		return userGroupOfferBags;
	}

	public synchronized void setUserGroupOfferBags(Knapsack[] userGroupOfferBags) {
		this.userGroupOfferBags = userGroupOfferBags;
	}

	public synchronized Knapsack[] getUserOfferBags() {
		return userOfferBags;
	}

	public synchronized void setUserOfferBags(Knapsack[] userOfferBags) {
		this.userOfferBags = userOfferBags;
	}

	public synchronized Double getOfferPriority() {
		return offerPriority;
	}

	public synchronized void setOfferPriority(Double offerPriority) {
		this.offerPriority = offerPriority;
	}

	public synchronized int getOfferQuota() {
		return offerQuota;
	}

	public synchronized void setOfferQuota(int offerQuota) {
		this.offerQuota = offerQuota;
	}

}