package util.geode.performance.domain;

import org.apache.commons.lang.StringUtils;

public class Domain {
	private String keyHeader;
	private int keyValue;
	private int bodySize;
	private String body;

	public Domain() {
	}

	public Domain(String keyHeader, int keyValue, int bodySize) {
		this.keyHeader = keyHeader;
		this.keyValue = keyValue;
		this.bodySize = bodySize;
		body = StringUtils.repeat("1", bodySize);
	}

	public String getKeyHeader() {
		return keyHeader;
	}

	public int getKeyValue() {
		return keyValue;
	}

	public String getBody() {
		if (body == null || body.length() == 0) {
			body = StringUtils.repeat("1", bodySize);
			return body;
		} else {
			return body;
		}
	}
}
