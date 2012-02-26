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
package youroom4j;

public class KeyValueString {

	private String key;
	private String value;

	public KeyValueString(String key, String value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * keyを取得します。
	 *
	 * @return key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * keyを設定します。
	 *
	 * @param key key
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * valueを取得します。
	 *
	 * @return value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * valueを設定します。
	 *
	 * @param value value
	 */
	public void setValue(String value) {
		this.value = value;
	}

}
