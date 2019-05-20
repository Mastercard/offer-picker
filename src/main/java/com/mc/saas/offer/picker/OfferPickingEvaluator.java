package com.mc.saas.offer.picker;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mc.saas.offer.picker.utils.GlobalDataSetsHelper;
import com.mc.saas.offer.picker.utils.MetricProcessor;
import com.mc.saas.offer.picker.utils.OfferMetrics;
import com.mc.saas.offer.picker.utils.PersistHelper;
import com.mc.saas.offer.picker.utils.WorkerMetrics;
/**
 * @author suqiang.song
 *
 */
class OfferPickingEvaluator{
	
	private static final Logger log = LoggerFactory.getLogger(OfferPickingEvaluator.class);
	
	public OfferPickingEvaluator() {
		super();
	}
	
	public void offerPickingEvaluate() {
		long start = System.currentTimeMillis();
		log.info("Start the evaluating");
		offerEvalute();
		userGroupEvalute();
		userEvalute();
		log.info("Finished the evaluating");
		long end = System.currentTimeMillis();
		MetricProcessor.getInstance().addMetrics(new WorkerMetrics("doEvaluate:"+Thread.currentThread().getName(),(end-start)));
	}

	private void offerEvalute() {
		// evaluate Offers
		Collections.sort(GlobalDataSetsHelper.getInstance().getOmList());
		PersistHelper.getInstance().writeOfferMetrics("OfferEvaluate", OfferMetrics.getColumns(),
				GlobalDataSetsHelper.getInstance().getOmList());
	}

	private void userGroupEvalute() {
		// evaluate UserGroups
		Map<Integer, KnapsackConstraint> map = new TreeMap<Integer, KnapsackConstraint>(
				GlobalDataSetsHelper.getInstance().getUserGroupConstraints());
		log.info("Sorting by user groupid:");
		Set<Entry<Integer, KnapsackConstraint>> set = map.entrySet();
		Iterator<Entry<Integer, KnapsackConstraint>> iterator = set.iterator();
		while (iterator.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry me = (Map.Entry) iterator.next();
			int tmpUgId = (int) me.getKey();
			KnapsackConstraint tmp = (KnapsackConstraint) me.getValue();
			PersistHelper.getInstance().getWriter("UserGroupEvaluate")
					.writeNext((tmpUgId + "," + tmp.getMetrics()).split(","));
		}
		PersistHelper.getInstance().closeWriter("UserGroupEvaluate");
	}

	private void userEvalute() {
		log.info("Saving outputs for all users:");
		Set<Entry<Integer, KnapsackConstraint>> set = GlobalDataSetsHelper.getInstance().getUserConstraints()
				.entrySet();
		Iterator<Entry<Integer, KnapsackConstraint>> iterator = set.iterator();
		while (iterator.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry me = (Map.Entry) iterator.next();
			int tmpUgId = (int) me.getKey();
			KnapsackConstraint tmp = (KnapsackConstraint) me.getValue();
			PersistHelper.getInstance().getWriter("UserEvaluate")
					.writeNext(((int) me.getKey() + "," +tmp.getMetrics()).split(","));
		}
		PersistHelper.getInstance().closeWriter("UserEvaluate");
	}

}