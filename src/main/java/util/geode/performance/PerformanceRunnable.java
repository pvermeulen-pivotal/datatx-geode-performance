package util.geode.performance;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Callable;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientRegionShortcut;

import util.geode.performance.domain.Domain;
import util.geode.performance.domain.Timing;

public class PerformanceRunnable implements Callable {
	private int reads;
	private int writes;
	private Region region;
	private int domainSize;
	private int runTime;
	private long waitTime;
	private String keyHeader;
	private String regionName;
	private ClientCache cache;
	private int lastKey;

	public PerformanceRunnable(ClientCache cache, int reads, int writes, String keyHeader, String regionName,
			int domainSize, int runTime, long waitTime) {
		this.cache = cache;
		this.reads = reads;
		this.writes = writes;
		this.keyHeader = keyHeader;
		this.regionName = regionName;
		this.domainSize = domainSize;
		this.runTime = runTime;
		this.waitTime = waitTime;
	}

	private synchronized int getLastKey() {
		lastKey = lastKey + 1;
		return lastKey;
	}

	private int regionLoad(int size) {
		int lastKey = 0;
		while (lastKey != size) {
			Domain domain = new Domain(keyHeader, lastKey, domainSize);
			region.put(keyHeader + String.format("%010d", lastKey), domain);
			lastKey++;
		}
		return lastKey;
	}

	public Object call() throws Exception {
		region = cache.createClientRegionFactory(ClientRegionShortcut.PROXY).create(regionName);
		int size = 100 * reads;
		lastKey = regionLoad(size);
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.MINUTE, runTime);
		Timing timing = new Timing();
		Random random = new Random();
		while (cal.getTime().getTime() < new Date().getTime()) {
			for (int i = 0; i < reads; i++) {
				int key = random.nextInt(lastKey);
				long startTime = System.currentTimeMillis();
				region.get(keyHeader + String.format("%010d", key));
				long endTime = System.currentTimeMillis();
				timing.setReadTime(timing.getReadTime() + (endTime - startTime));
				timing.setReadCount(timing.getReadCount() + 1);
			}
			for (int i = 0; i < writes; i++) {
				Domain domain = new Domain(keyHeader, getLastKey(), domainSize);
				long startTime = System.currentTimeMillis();
				region.put(keyHeader + String.format("%010d", lastKey), domain);
				long endTime = System.currentTimeMillis();
				timing.setWriteTime(timing.getWriteTime() + (endTime - startTime));
				timing.setWriteCount(timing.getWriteCount() + 1);
			}
			try {
				Thread.sleep(waitTime);
			} catch (InterruptedException e) {
				// do nothing
			}
		}
		return timing;
	}

}
