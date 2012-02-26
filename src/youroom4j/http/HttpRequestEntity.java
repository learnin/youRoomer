/*
 * Copyright 2012 Manabu Inoue
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package youroom4j.http;

import java.util.List;

import youroom4j.KeyValueString;

public class HttpRequestEntity {

	// FIXME Enum化
	public static final int GET = 1;
	public static final int POST = 2;
	public static final int PUT = 3;
	public static final int DELETE = 4;

	private String url;
	private int method;
	private List<KeyValueString> headers;
	private List<KeyValueString> params;
	private String body;

	/**
	 * urlを取得します。
	 *
	 * @return url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * urlを設定します。
	 *
	 * @param url url
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * methodを取得します。
	 *
	 * @return method
	 */
	public int getMethod() {
		return method;
	}

	/**
	 * methodを設定します。
	 *
	 * @param method method
	 */
	public void setMethod(int method) {
		this.method = method;
	}

	/**
	 * headersを取得します。
	 *
	 * @return headers
	 */
	public List<KeyValueString> getHeaders() {
		return headers;
	}

	/**
	 * headersを設定します。
	 *
	 * @param headers headers
	 */
	public void setHeaders(List<KeyValueString> headers) {
		this.headers = headers;
	}

	/**
	 * paramsを取得します。
	 *
	 * @return params
	 */
	public List<KeyValueString> getParams() {
		return params;
	}

	/**
	 * paramsを設定します。
	 *
	 * @param params params
	 */
	public void setParams(List<KeyValueString> params) {
		this.params = params;
	}

	/**
	 * bodyを取得します。
	 *
	 * @return body
	 */
	public String getBody() {
		return body;
	}

	/**
	 * bodyを設定します。
	 *
	 * @param body body
	 */
	public void setBody(String body) {
		this.body = body;
	}
}
