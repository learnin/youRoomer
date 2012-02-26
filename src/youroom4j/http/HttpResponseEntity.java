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
