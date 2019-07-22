package util.geode.performance.domain;

public class Timing {
	private long readTime = 0;
	private long readCount = 0;
	private long writeTime = 0;
	private long writeCount = 0;
	
	public Timing() {}
	
	public Timing(long readTime, long readCount, long writeTime, long writeCount) {
		this.readTime=readTime;
		this.readCount=readCount;
		this.writeTime = writeTime;
		this.writeCount = writeCount;
	}

	public long getReadTime() {
		return readTime;
	}

	public void setReadTime(long readTime) {
		this.readTime = readTime;
	}

	public long getReadCount() {
		return readCount;
	}

	public void setReadCount(long readCount) {
		this.readCount = readCount;
	}

	public long getWriteTime() {
		return writeTime;
	}

	public void setWriteTime(long writeTime) {
		this.writeTime = writeTime;
	}

	public long getWriteCount() {
		return writeCount;
	}

	public void setWriteCount(long writeCount) {
		this.writeCount = writeCount;
	}
}
