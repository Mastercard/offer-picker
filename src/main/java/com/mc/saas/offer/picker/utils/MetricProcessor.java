package com.mc.saas.offer.picker.utils;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author suqiang.song
 *
 */
public class MetricProcessor implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(MetricProcessor.class);
	private volatile boolean exit = false;
	ConcurrentLinkedQueue<WorkerMetrics> metrics = new ConcurrentLinkedQueue<WorkerMetrics>();
	private static final MetricProcessor INSTANCE = new MetricProcessor();
	static
	{
		Thread t = new Thread(INSTANCE);
		System.out.println("start MetricProcessor thread!");
		t.start();
	}
	
	public static MetricProcessor getInstance() {
		return MetricProcessor.INSTANCE;
	}
	public boolean addMetrics(WorkerMetrics m) {
		return metrics.offer(m); // This may block, but not for a significant amount of time.
	}

	public void run() {
		while(!exit) {
			WorkerMetrics metric = metrics.poll();
			if(null !=metric)
			{
				log.info(metric.toString());
			}

		}
	}
	
	public void quit()
	{
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		metrics.clear();
		exit = true;
	}
}
