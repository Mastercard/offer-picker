package com.mc.saas.offer.picker;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mc.saas.offer.picker.utils.MetricProcessor;
import com.mc.saas.offer.picker.utils.WorkerMetrics;
/**
 * @author suqiang.song
 *
 */
class OfferPickingCalculator implements Callable<String>{

	private int offerId;
	private int totalWeight;
	private Knapsack[] bags;
	/** best values matrix with top N */
	private int[][] bestValues;
	private static final Logger log = LoggerFactory.getLogger(OfferPickingCalculator.class);
	
	public OfferPickingCalculator(int offerId, int userCount,int totalWeight,
			Knapsack[] bags,int[][] bestValues) {
		super();
		this.offerId = offerId;
		this.totalWeight = totalWeight;
		this.bags = bags;
		this.bestValues = bestValues;
	}

	
	public synchronized String offerPickingCalculation() {
		long start = System.currentTimeMillis();
		OfferPickerByDynamicPrograming kp = new OfferPickerByDynamicPrograming(offerId,bags, totalWeight);
		if (bestValues == null) {
			bestValues = new int[this.bags.length + 1][totalWeight + 1];
		}
		log.info(" --------start: offerPickingCalculation for offer id: --------- "+offerId);
		kp.calculate(this.bestValues);
		log.info(" --------end: offerPickingCalculation for offer id: --------- "+offerId);
		long end = System.currentTimeMillis();
		MetricProcessor.getInstance().addMetrics(new WorkerMetrics("offerPickingCalculation:"+Thread.currentThread().getName(),(end-start)));
		return "offerPickingCalculation Finished for offerid:"+offerId;
	}
	

	public String call() throws Exception {
		return offerPickingCalculation();
	}


	public int[][] getBestValues() {
		return bestValues;
	}

	public void setBestValues(int[][] bestValues) {
		this.bestValues = bestValues;
	}
}