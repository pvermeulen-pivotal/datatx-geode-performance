package util.geode.performance;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Callable;

import org.apache.geode.cache.Region;
import org.apache.log4j.Logger;

import util.geode.performance.domain.Domain;
import util.geode.performance.domain.Timing;

@SuppressWarnings("rawtypes")
public class PerformanceCallable implements Callable<Timing> {
	private int reads;
	private int writes;
	private Region region;
	private int domainSize;
	private int runTime;
	private long waitTime;
	private String keyHeader;
	private int lastKey;
	private Logger LOG;

	public PerformanceCallable(int reads, int writes, String keyHeader, Region region, int domainSize, int runTime,
			long waitTime, Logger log) {
		this.reads = reads;
		this.writes = writes;
		this.keyHeader = keyHeader;
		this.region = region;
		this.domainSize = domainSize;
		this.runTime = runTime;
		this.waitTime = waitTime;
		this.LOG = log;
	}

	private synchronized int getLastKey() {
		lastKey = lastKey + 1;
		return lastKey;
	}

	private int regionLoad(int size) {
		int lastKey = 0;
		while (lastKey != size) {
			region.put(keyHeader + String.format("%010d", lastKey), new Domain(keyHeader, lastKey, domainSize));
			lastKey++;
		}
		return lastKey;
	}

	public Timing call() throws Exception {
		int size = reads + (reads / 2);
		LOG.info("Starting region load using keyHeader " + keyHeader + " records to be written = " + size);
		lastKey = regionLoad(size);
		LOG.info("Completed region load using keyHeader " + keyHeader);
		Timing timing = new Timing(keyHeader, region.getName(), domainSize);
		Random random = new Random();
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.MINUTE, runTime);
		long duration = new Date().getTime();
		Domain domain;
		LOG.info("Executing performance test");
		while (duration < cal.getTime().getTime()) {
			for (int i = 0; i < reads; i++) {
				try {
					int key = random.nextInt(lastKey);
					long startTime = System.currentTimeMillis();
					domain = (Domain) region.get(keyHeader + String.format("%010d", key));
					long endTime = System.currentTimeMillis();
					timing.setReadTime(timing.getReadTime() + (endTime - startTime));
					timing.setReadCount(timing.getReadCount() + 1);
					domain = null;
				} catch (Exception e) {
					LOG.error("Error performing region get operation - exception: " + e.getMessage());
				}
			}
			for (int i = 0; i < writes; i++) {
				try {
					domain = new Domain(keyHeader, getLastKey(), domainSize);
					long startTime = System.currentTimeMillis();
					region.put(keyHeader + String.format("%010d", lastKey), domain);
					long endTime = System.currentTimeMillis();
					timing.setWriteTime(timing.getWriteTime() + (endTime - startTime));
					timing.setWriteCount(timing.getWriteCount() + 1);
					domain = null;
				} catch (Exception e) {
					LOG.error("Error performing region write operation - exception: " + e.getMessage());
				}
			}
			try {
				Thread.sleep(waitTime);
				duration = new Date().getTime();
			} catch (InterruptedException e) {
				// do nothing
			}
		}
		LOG.info("Thread completed keyHeader=" + keyHeader);
		return timing;
	}
}
