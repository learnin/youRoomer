package youroom4j.http;

import java.io.InputStream;

public class HttpResponseEntity {

	private int statusCode;
	private InputStream content;

	/**
	 * statusCodeを取得します。
	 *
	 * @return statusCode
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * statusCodeを設定します。
	 *
	 * @param statusCode statusCode
	 */
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	/**
	 * contentを取得します。
	 *
	 * @return content
	 */
	public InputStream getContent() {
		return content;
	}

	/**
	 * contentを設定します。
	 *
	 * @param content content
	 */
	public void setContent(InputStream content) {
		this.content = content;
	}
}
