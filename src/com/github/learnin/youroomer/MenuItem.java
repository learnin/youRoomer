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
package com.github.learnin.youroomer;

import youroom4j.Entry;

public class MenuItem {

	private int id;
	private String text;
	private Entry entry;

	/**
	 * idを取得します。
	 *
	 * @return id
	 */
	public int getId() {
		return id;
	}

	/**
	 * idを設定します。
	 *
	 * @param id id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * textを取得します。
	 *
	 * @return text
	 */
	public String getText() {
		return text;
	}

	/**
	 * textを設定します。
	 *
	 * @param text text
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * entryを取得します。
	 * @return entry
	 */
	public Entry getEntry() {
	    return entry;
	}

	/**
	 * entryを設定します。
	 * @param entry entry
	 */
	public void setEntry(Entry entry) {
	    this.entry = entry;
	}

}
