package com.mc.saas.offer.picker.utils;
/**
 * @author suqiang.song
 *
 */
public class WorkerMetrics {
	public WorkerMetrics(String workerName, long totalTimings) {
		super();
		this.workerName = workerName;
		this.totalTimings = totalTimings;
	}

	private String workerName;
	private long totalTimings;

	public String getWorkerName() {
		return workerName;
	}

	public void setWorkerName(String workerName) {
		this.workerName = workerName;
	}

	public long getTotalTimings() {
		return totalTimings;
	}

	public void setTotalTimings(long totalTimings) {
		this.totalTimings = totalTimings;
	}

	@Override
	public String toString() {
		return "WorkerMetrics [workerName=" + workerName + ", totalTimings=" + totalTimings + "]";
	}

}
