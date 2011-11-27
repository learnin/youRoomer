package com.github.learnin.youroomer;

import java.util.LinkedHashMap;
import java.util.Map;

public class LruHashMap<K, V> extends LinkedHashMap<K, V> {

	private static final long serialVersionUID = 1L;

	private int limitSize;

	public LruHashMap(int limitSize) {
		this.limitSize = limitSize;
	}

	public LruHashMap(int limitSize, int initialCapacity) {
		super(initialCapacity);
		this.limitSize = limitSize;
	}

	public LruHashMap(int limitSize, int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
		this.limitSize = limitSize;
	}

	public LruHashMap(int limitSize, int initialCapacity, float loadFactor, boolean accessOrder) {
		super(initialCapacity, loadFactor, accessOrder);
		this.limitSize = limitSize;
	}

	public LruHashMap(int limitSize, Map<? extends K, ? extends V> m) {
		super(m);
		this.limitSize = limitSize;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > limitSize;
	}

}
