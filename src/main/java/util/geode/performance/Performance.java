package util.geode.performance;

import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.pdx.ReflectionBasedAutoSerializer;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import util.geode.performance.domain.Timing;
import util.geode.performance.PerformanceCallable;

@SuppressWarnings("rawtypes")
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
	private Region region;
	private HashMap<Integer, Future<Timing>> threadsFuture = new HashMap<Integer, Future<Timing>>();
	private Logger LOG;

	public Performance() {
	}

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
	}

	private void createLogAppender() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource("log4j.properties");
		PropertyConfigurator.configure(url);
		LOG = org.apache.log4j.Logger.getLogger("file");
	}

	public void setupPerformance() {
		createLogAppender();
		LOG.info("Setting up performance run " + new Date());
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numberConnections);
		cache = new ClientCacheFactory().addPoolLocator(locatorHost, locatorPort)
				.setPoolMaxConnections(numberConnections).setPoolMinConnections(1).setPoolRetryAttempts(-1)
				.setPoolPRSingleHopEnabled(true).setPoolReadTimeout(15000)
				.setPdxSerializer(new ReflectionBasedAutoSerializer("util.geode.performance.domain.Domain"))
				.set("log-level", "CONFIG").set("log-file", "logs/performance-gemfire.log").create();
		region = cache.createClientRegionFactory(ClientRegionShortcut.PROXY).create(regionName);
		submitThreads();
		checkThreads();
		Timing totTiming = new Timing();
		totTiming.setRegionName(regionName);
		totTiming = printResults(totTiming);
		printTotalResults(totTiming);
		executor.shutdown();
		LOG.info("Performance run complete " + new Date());
	}

	private void submitThreads() {
		LOG.info("Starting threads");
		for (int i = 0; i < numberConnections; i++) {
			PerformanceCallable pr = new PerformanceCallable(reads, writes, keyHeader + String.valueOf(i) + "-", region,
					domainSize, runTime, waitTime, LOG);
			LOG.info("Starting thread: reads=" + reads + " writes=" + writes + " keyheader=" + keyHeader
					+ String.valueOf(i) + " region=" + regionName + " domainsize=" + domainSize + " runtime=" + runTime
					+ " mins waittime=" + waitTime + "ms");
			threadsFuture.put(i, executor.submit(pr));
		}
	}

	private void checkThreads() {
		LOG.info("Checking threads");
		boolean complete = false;
		int outstandingThreads = 0;
		Set<Integer> keys = threadsFuture.keySet();
		while (!complete) {
			for (Integer key : keys) {
				Future<Timing> ft = threadsFuture.get(key);
				if (!ft.isDone()) {
					outstandingThreads++;
				}
			}
			if (outstandingThreads > 0) {
				try {
					Thread.sleep(5000);
					outstandingThreads = 0;
				} catch (InterruptedException e) {
					// do nothing
				}
			} else {
				complete = true;
			}
		}
	}

	private Timing printResults(Timing totTiming) {
		Set<Integer> keys = threadsFuture.keySet();
		for (Integer key : keys) {
			try {
				Future<Timing> f = threadsFuture.get(key);
				Timing timing = f.get();
				if (timing == null)
					break;
				totTiming.setReadCount(totTiming.getReadCount() + timing.getReadCount());
				totTiming.setReadTime(totTiming.getReadTime() + timing.getReadTime());
				totTiming.setWriteCount(totTiming.getWriteCount() + timing.getWriteCount());
				totTiming.setWriteTime(totTiming.getWriteTime() + timing.getWriteTime());
				LOG.info("");
				LOG.info("Performance Timing Region=" + timing.getRegionName() + " KeyHeader=" + timing.getKeyHeader()
						+ " DomainSize=" + domainSize + " ReadRate:" + reads + " WriteRate:" + writes + " TestDuration:"
						+ runTime + " secs");
				LOG.info("     ReadCount=" + timing.getReadCount() + " AverageReadTime="
						+ timing.getReadTime() / timing.getReadCount() + "ms TotalReadTime=" + timing.getReadTime()
						+ "ms");
				LOG.info("     WriteCount=" + timing.getWriteCount() + " AverageWriteTime="
						+ timing.getWriteTime() / timing.getWriteCount() + "ms TotalWriteTime=" + timing.getWriteTime()
						+ "ms");
				LOG.info("     Read IOPS=" + (timing.getReadCount() / (timing.getReadTime() / 1000)));
				totTiming
						.setIopsRead(totTiming.getIopsRead() + (timing.getReadCount() / (timing.getReadTime() / 1000)));
				LOG.info("     Write IOPS=" + (timing.getWriteCount() / (timing.getWriteTime() / 1000)));
				totTiming.setIopsWrite(
						totTiming.getIopsWrite() + (timing.getWriteCount() / (timing.getWriteTime() / 1000)));
				double ioread = (timing.getReadCount() / (timing.getReadTime() / 1000));
				double iowrite = (timing.getWriteCount() / (timing.getWriteTime() / 1000));
				LOG.info("     Total IOPS=" + (ioread + iowrite));
				totTiming.setIopsTotal(totTiming.getIopsTotal() + (ioread + iowrite));
			} catch (Exception e) {
				LOG.error("Error printing results for key " + key + " exception: " + e.getMessage());
			}
		}
		return totTiming;
	}

	private void printTotalResults(Timing totTiming) {
		LOG.info("");
		LOG.info("Performance Timing Total Region=" + totTiming.getRegionName());
		LOG.info("     ReadCount=" + totTiming.getReadCount() + " AverageReadTime="
				+ totTiming.getReadTime() / totTiming.getReadCount() + "ms TotalReadTime=" + totTiming.getReadTime()
				+ "ms");
		LOG.info("     WriteCount=" + totTiming.getWriteCount() + " AverageWriteTime="
				+ totTiming.getWriteTime() / totTiming.getWriteCount() + "ms TotalWriteTime=" + totTiming.getWriteTime()
				+ "ms");
		LOG.info("     Total Read IOPS=" + totTiming.getIopsRead());
		LOG.info("     Total Write IOPS=" + totTiming.getIopsWrite());
		LOG.info("     Total IOPS=" + (totTiming.getIopsRead() + totTiming.getIopsWrite()));
	}

	public int getNumberConnections() {
		return numberConnections;
	}

	public void setNumberConnections(int numberConnections) {
		this.numberConnections = numberConnections;
	}

	public int getReads() {
		return reads;
	}

	public void setReads(int reads) {
		this.reads = reads;
	}

	public int getWrites() {
		return writes;
	}

	public void setWrites(int writes) {
		this.writes = writes;
	}

	public int getDomainSize() {
		return domainSize;
	}

	public void setDomainSize(int domainSize) {
		this.domainSize = domainSize;
	}

	public int getLocatorPort() {
		return locatorPort;
	}

	public void setLocatorPort(int locatorPort) {
		this.locatorPort = locatorPort;
	}

	public int getRunTime() {
		return runTime;
	}

	public void setRunTime(int runTime) {
		this.runTime = runTime;
	}

	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	public String getKeyHeader() {
		return keyHeader;
	}

	public void setKeyHeader(String keyHeader) {
		this.keyHeader = keyHeader;
	}

	public String getLocatorHost() {
		return locatorHost;
	}

	public void setLocatorHost(String locatorHost) {
		this.locatorHost = locatorHost;
	}

	public long getWaitTime() {
		return waitTime;
	}

	public void setWaitTime(long waitTime) {
		this.waitTime = waitTime;
	}

	static public void main(String[] args) throws Exception {
		if (args == null || args.length == 0 || args.length != 20) {
			usage();
			return;
		}
		Performance perf = new Performance();
		processArgs(args, perf);
		String logFile = System.getProperty("logfile-name");
		if (logFile == null || logFile.length() == 0) {
			String dir = System.getProperty("user.dir");
			DateFormat df = new SimpleDateFormat("MM-dd-yyyy_HH-mm-ss");
			System.setProperty("logfile-name",
					dir + File.separator + "logs/performance-" + df.format(new Date()) + ".log");
		}
		perf.setupPerformance();
	}

	static private void usage() {
		System.out.println("");
		System.out.println("-C Number Connections");
		System.out.println("-N Region Name");
		System.out.println("-R Number of Reads");
		System.out.println("-W Number of Writes");
		System.out.println("-I Idle/Wait Time");
		System.out.println("-K Key Header");
		System.out.println("-S Domain Size");
		System.out.println("-H Locator Host Name/IP");
		System.out.println("-P Locator Port");
		System.out.println("-E Execution/Run Time");
		System.out.println("");
		System.out.println("All arguments must be present since no defaults are assumed");
		System.out.println("");
	}

	static private void processArgs(String[] args, Performance perf) {
		String lastCommand = new String();
		for (String arg : args) {
			if (arg.toUpperCase().equals("-C") || arg.toUpperCase().equals("-N") || arg.toUpperCase().equals("-R")
					|| arg.toUpperCase().equals("-W") || arg.toUpperCase().equals("-I")
					|| arg.toUpperCase().equals("-K") || arg.toUpperCase().equals("-S")
					|| arg.toUpperCase().equals("-H") || arg.toUpperCase().equals("-P")
					|| arg.toUpperCase().equals("-E")) {
				lastCommand = arg.toUpperCase();
			} else {
				if (lastCommand.equals("-C")) {
					perf.setNumberConnections(Integer.parseInt(arg));
				} else if (lastCommand.equals("-N")) {
					perf.setRegionName(arg);
				} else if (lastCommand.equals("-R")) {
					perf.setReads(Integer.parseInt(arg));
				} else if (lastCommand.equals("-W")) {
					perf.setWrites(Integer.parseInt(arg));
				} else if (lastCommand.equals("-I")) {
					perf.setWaitTime(Long.parseLong(arg));
				} else if (lastCommand.equals("-K")) {
					perf.setKeyHeader(arg);
				} else if (lastCommand.equals("-S")) {
					perf.setDomainSize(Integer.parseInt(arg));
				} else if (lastCommand.equals("-H")) {
					perf.setLocatorHost(arg);
				} else if (lastCommand.equals("-P")) {
					perf.setLocatorPort(Integer.parseInt(arg));
				} else if (lastCommand.equals("-E")) {
					perf.setRunTime(Integer.parseInt(arg));
				}
			}
		}
	}
}
