package util.geode.performance.domain;

public class Timing {
	private String keyHeader;
	private String regionName;
	private int domainSize;
	private double readTime = 0;
	private double readCount = 0;
	private double writeTime = 0;
	private double writeCount = 0;

	public Timing() {
	}

	public Timing(String keyHeader, String regionName, int domainSize) {
		this.keyHeader = keyHeader;
		this.regionName = regionName;
		this.domainSize = domainSize;
	}

	public String getKeyHeader() {
		return keyHeader;
	}

	public void setKeyHeader(String keyHeader) {
		this.keyHeader = keyHeader;
	}

	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	public int getDomainSize() {
		return domainSize;
	}

	public void setDomainSize(int domainSize) {
		this.domainSize = domainSize;
	}

	public double getReadTime() {
		return readTime;
	}

	public void setReadTime(double readTime) {
		this.readTime = readTime;
	}

	public double getReadCount() {
		return readCount;
	}

	public void setReadCount(double readCount) {
		this.readCount = readCount;
	}

	public double getWriteTime() {
		return writeTime;
	}

	public void setWriteTime(double writeTime) {
		this.writeTime = writeTime;
	}

	public double getWriteCount() {
		return writeCount;
	}

	public void setWriteCount(double writeCount) {
		this.writeCount = writeCount;
	}
}
