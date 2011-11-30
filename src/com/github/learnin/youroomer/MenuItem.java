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
