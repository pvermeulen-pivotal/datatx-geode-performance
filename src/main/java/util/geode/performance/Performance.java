package util.geode.performance;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.geode.performance.domain.Timing;

public class Performance {

	private int numberConnections;
	private int reads;
	private int writes;
	private int domainSize;
	private int locatorPort;
	private int runTime;

	private String regionName;
	private String keyHeader;
	private String locatorHost;

	private long waitTime;

	private ThreadPoolExecutor executor;

	private ClientCache cache;
	
	private Timing[] timing;

	private static final Logger LOG = LoggerFactory.getLogger(Performance.class);

	public Performance(int numberConnections, String regionName, int reads, int writes, long waitTime, String keyHeader,
			int domainSize, String locatorHost, int locatorPort, int runTime) {
		this.numberConnections = numberConnections;
		this.regionName = regionName;
		this.reads = reads;
		this.writes = writes;
		this.waitTime = waitTime;
		this.keyHeader = keyHeader;
		this.domainSize = domainSize;
		this.locatorHost = locatorHost;
		this.locatorPort = locatorPort;
		this.runTime = runTime;
		timing = new Timing[numberConnections];
	}

	@SuppressWarnings("unchecked")
	public void setupPerformance() {
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(this.numberConnections);
		cache = new ClientCacheFactory().addPoolLocator(locatorHost, locatorPort)
				.setPoolMaxConnections(numberConnections).setPoolMinConnections(1).setPoolRetryAttempts(-1)
				.setPoolPRSingleHopEnabled(true).create();
		for (int i = 0; i < numberConnections; i++) {
			PerformanceRunnable pr = new PerformanceRunnable(cache, reads, writes, keyHeader + String.valueOf(i),
					regionName, domainSize, runTime, waitTime);
			timing[i] = (Timing) executor.submit(pr);
		}
		
	}
}
