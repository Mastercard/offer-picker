package com.mc.saas.offer.picker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mc.saas.offer.picker.utils.GlobalDataSetsHelper;
import com.mc.saas.offer.picker.utils.MetricProcessor;
import com.mc.saas.offer.picker.utils.OfferDataSets;
import com.mc.saas.offer.picker.utils.PersistHelper;
import com.mc.saas.offer.picker.utils.WorkerMetrics;
/**
 * @author suqiang.song
 *
 */
class OfferPickingSaver implements Callable<String>{
	
	private int offerId;
	private OfferDataSets ods;
	private boolean persist = false;
	private static final Logger log = LoggerFactory.getLogger(OfferPickingSaver.class);
	
	public OfferPickingSaver(OfferDataSets ods,boolean persist) {
		super();
		this.offerId = ods.getOfferId();
		this.ods = ods;
		this.persist = persist;
	}
	
	public synchronized String offerPickingSave() {
		long start = System.currentTimeMillis();
		log.info(" --------start: offerPickingSave for offer id: --------- "+offerId);
		doSave();
		log.info(" --------end: offerPickingSave for offer id: --------- "+offerId);
		long end = System.currentTimeMillis();
		MetricProcessor.getInstance().addMetrics(new WorkerMetrics("offerPickingAssignment:"+Thread.currentThread().getName(),(end-start)));
		return "offerPickingSave Finished for offerid:"+offerId;
	}
	
	public synchronized void doSave()
	{
		long start = System.currentTimeMillis();
		if (persist) {
			saveOfferResultToDisk(ods.getOfferId(),ods.getUserOfferBags());
		
		} else {
			log.info("Final results are:--------------");
			for(Knapsack k:ods.getUserOfferBags())
			{
				log.debug(k.toString());
			}
		}
		long end = System.currentTimeMillis();
		MetricProcessor.getInstance().addMetrics(new WorkerMetrics("saveFinalResults", (end - start)));
	}
	
	public synchronized void saveOfferResultToDisk(int offerId,Knapsack[] bags)
	{
		//remove all the user id ==-1 element 
		log.debug("OfferId:"+offerId+" bags size before remove unqualified elements:"+bags.length);
		ArrayList<Knapsack> resultBags = new ArrayList<Knapsack>();
        for(Knapsack k:bags)
        {
        	if(k.getUserId()==-1)
        	{
        		continue;
        	}
        	resultBags.add(k);
        }
        // don't forget resort the result list
        Collections.sort(resultBags);
		log.debug("OfferId:"+offerId+" bags size after remove unqualified elements:"+resultBags.size());
		//OfferResult
		PersistHelper.getInstance().writePickerResults("OfferResult"+offerId, Knapsack.getColumns(), resultBags);
		resultBags.clear();
	}
	

	public String call() throws Exception {
		return offerPickingSave();
	}

}