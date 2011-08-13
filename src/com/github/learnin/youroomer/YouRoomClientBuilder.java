package com.github.learnin.youroomer;

import youroom4j.YouRoomClient;

public class YouRoomClientBuilder {

	private static final String CONSUMER_KEY = "";
	private static final String CONSUMER_SECRET = "";

	public static YouRoomClient createYouRoomClient() {
		return new YouRoomClient(CONSUMER_KEY, CONSUMER_SECRET);
	}
}
