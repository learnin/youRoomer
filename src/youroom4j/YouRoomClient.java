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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.http.HttpStatus;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import youroom4j.http.HttpRequestClient;
import youroom4j.http.HttpRequestEntity;
import youroom4j.http.HttpResponseHandler;
import youroom4j.http.httpclient.HttpRequestClientImpl;
import youroom4j.oauth.OAuthClient;
import youroom4j.oauth.OAuthTokenCredential;
import android.util.Log;

//TODO 流れるようなインターフェースにしてはどうか？
// FIXME 抽象クラスにして、サブクラスとしてAndroidYouRoomClientをつくる。インスタンス生成はYouRoomClientFactoryで。
// TODO 401エラーが返った場合に、tokenエラーの場合は、OAuth認証フローへ導く動線があった方が良い。このクラス自体で画面遷移させるわけにはいかないので特定の例外をスローするとかする。
// FIXME System.out.printlnの削除とAndroid用とそうでない環境両方で使えるロギング
public class YouRoomClient {

	private static final String REQUEST_TOKEN_URL = "http://youroom.in/oauth/request_token";
	private static final String AUTHORIZATION_URL = "http://youroom.in/oauth/authorize";
	private static final String ACCESS_TOKEN_URL = "https://youroom.in/oauth/access_token";

	private OAuthClient oAuthClient;

	public YouRoomClient(String oauthConsumerKey, String oauthConsumerSecret) {
		oAuthClient =
			new OAuthClient(
				oauthConsumerKey,
				oauthConsumerSecret,
				REQUEST_TOKEN_URL,
				AUTHORIZATION_URL,
				ACCESS_TOKEN_URL);
	}

	public OAuthTokenCredential getOAuthTokenCredential() {
		return oAuthClient.getOAuthTokenCredential();
	}

	public void setOAuthTokenCredential(OAuthTokenCredential oAuthTokenCredential) {
		oAuthClient.setOAuthTokenCredential(oAuthTokenCredential);
	}

	// TODO メソッドシグネチャのthrows句がこれでいいか要検討(他メソッドも)
	public String oAuthRequestTokenRequest(String oauthCallbackUrl) throws YouRoom4JException {
		try {
			return oAuthClient.temporaryCredential(oauthCallbackUrl);
		} catch (IOException e) {
			throw new YouRoom4JException(e);
		}
	}

	public void oAuthAccessTokenRequest(String oauthVerifier) throws YouRoom4JException {
		try {
			oAuthClient.tokenCredential(oauthVerifier);
		} catch (IOException e) {
			throw new YouRoom4JException(e);
		}
	}

	// TODO 各種パラメータ(since, flat, read_state)対応
	public List<Entry> getHomeTimeLine(int page) throws YouRoom4JException {
		List<KeyValueString> paramList = new ArrayList<KeyValueString>();
		paramList.add(new KeyValueString("format", "xml"));
		paramList.add(new KeyValueString("page", String.valueOf(page)));

		String url = "https://www.youroom.in/";
		HttpRequestEntity requestEntity = new HttpRequestEntity();
		requestEntity.setUrl(url);
		requestEntity.setMethod(HttpRequestEntity.GET);
		requestEntity.setParams(paramList);
		oAuthClient.addOAuthTokenCredentialToRequestEntity(requestEntity, url, paramList);

		HttpRequestClient client = new HttpRequestClientImpl(5000, 10000, 0, Charset.forName("UTF-8"));
		try {
			String responseContent = client.execute(requestEntity);
			System.out.println(responseContent);
			return parseEntries(responseContent);
		} catch (IOException e) {
			throw new YouRoom4JException(e);
		}
	}

	// TODO 各種パラメータ(since, search_query, flat, read_state)対応
	public List<Entry> getRoomTimeLine(String groupParam, int page) throws YouRoom4JException {
		List<KeyValueString> paramList = new ArrayList<KeyValueString>();
		paramList.add(new KeyValueString("format", "xml"));
		paramList.add(new KeyValueString("page", String.valueOf(page)));

		String url = "https://www.youroom.in/r/" + groupParam + "/";
		HttpRequestEntity requestEntity = new HttpRequestEntity();
		requestEntity.setUrl(url);
		requestEntity.setMethod(HttpRequestEntity.GET);
		requestEntity.setParams(paramList);
		oAuthClient.addOAuthTokenCredentialToRequestEntity(requestEntity, url, paramList);

		HttpRequestClient client = new HttpRequestClientImpl(5000, 10000, 0, Charset.forName("UTF-8"));
		try {
			String responseContent = client.execute(requestEntity);
			System.out.println(responseContent);
			return parseEntries(responseContent);
		} catch (IOException e) {
			throw new YouRoom4JException(e);
		}
	}

	// TODO Android以外はStAXでパースする
	// FIXME Parserを別クラスに切り出す
	// FIXME 不要なelseif解析はぶくため、continue使う
	private List<Entry> parseEntries(String responseContent) throws YouRoom4JException {
		List<Entry> results = new ArrayList<Entry>();
		ByteArrayInputStream byteArrayInputStream = null;
		try {
			byteArrayInputStream = new ByteArrayInputStream(responseContent.getBytes("UTF-8"));
			XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
			parser.setInput(byteArrayInputStream, "UTF-8");
			int eventType = parser.getEventType();
			String parentTag = null;
			Entry entry = null;
			Attachment attachment = null;
			Participation participation = null;
			Group group = null;
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'Z");
			Stack<Entry> parentStack = new Stack<Entry>();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String tag = null;
				switch (eventType) {
				case XmlPullParser.START_TAG:
					tag = parser.getName();
					if (("entry".equals(tag) && parentTag == null)
						|| ("child".equals(tag) && "entry".equals(parentTag))) {
						entry = new Entry();
						parentTag = "entry";
					} else if (entry != null && "entry".equals(parentTag)) {
						if ("created-at".equals(tag)) {
							entry.setCreatedAt(df.parse(parser.nextText() + "+0000"));
						} else if ("updated-at".equals(tag)) {
							entry.setUpdatedAt(df.parse(parser.nextText() + "+0000"));
						} else if ("root-id".equals(tag)) {
							entry.setRootId(Long.parseLong(parser.nextText()));
						} else if ("id".equals(tag)) {
							entry.setId(Long.parseLong(parser.nextText()));
						} else if ("can-update".equals(tag) && !"true".equals(parser.getAttributeValue(null, "nil"))) {
							entry.setUpdatable(Boolean.parseBoolean(parser.nextText()));
						} else if ("level".equals(tag)) {
							entry.setLevel(Integer.parseInt(parser.nextText()));
						} else if ("parent-id".equals(tag) && !"true".equals(parser.getAttributeValue(null, "nil"))) {
							entry.setParentId(Long.parseLong(parser.nextText()));
						} else if ("content".equals(tag)) {
							entry.setContent(parser.nextText());
						} else if ("has-read".equals(tag)) {
							entry.setHasRead(Boolean.parseBoolean(parser.nextText()));
						} else if ("descendants-count".equals(tag)) {
							int descendantsCount = Integer.parseInt(parser.nextText());
							if (descendantsCount > 0) {
								entry.setDescendantsCount(descendantsCount);
							}
						} else if ("unread-comment-ids".equals(tag)) {
							String unreadCommentIdsString = parser.nextText();
							if (unreadCommentIdsString.length() > 0) {
								String[] unreadCommentIds = unreadCommentIdsString.split(",");
								List<Long> unreadCommentIdList = new ArrayList<Long>();
								for (String unreadCommentId : unreadCommentIds) {
									unreadCommentIdList.add(Long.valueOf(unreadCommentId));
								}
								entry.setUnreadCommentIds(unreadCommentIdList);
							}
						} else if ("attachment".equals(tag)) {
							parentTag = "attachment";
							attachment = new Attachment();
						} else if ("participation".equals(tag)) {
							parentTag = "participation";
							participation = new Participation();
						} else if ("children".equals(tag) && "array".equals(parser.getAttributeValue(null, "type"))) {
							parentStack.push(entry);
						}
					} else if (attachment != null && "attachment".equals(parentTag)) {
						if ("original-filename".equals(tag)) {
							attachment.setOriginalFilename(parser.nextText());
						} else if ("data".equals(tag) && !"true".equals(parser.getAttributeValue(null, "nil"))) {
							// FIXME
						} else if ("content-type".equals(tag)) {
							attachment.setContentType(parser.nextText());
						} else if ("attachment-type".equals(tag)) {
							attachment.setAttachmentType(parser.nextText());
						} else if ("filename".equals(tag)) {
							attachment.setFilename(parser.nextText());
						}
					} else if (participation != null && "participation".equals(parentTag)) {
						if ("name".equals(tag)) {
							participation.setName(parser.nextText());
						} else if ("id".equals(tag)) {
							participation.setId(Long.parseLong(parser.nextText()));
						} else if ("group".equals(tag)) {
							parentTag = "group";
							group = new Group();
						}
					} else if (group != null && "group".equals(parentTag)) {
						if ("name".equals(tag)) {
							group.setName(parser.nextText());
						} else if ("to-param".equals(tag)) {
							group.setToParam(parser.nextText());
						} else if ("categories".equals(tag)) {
							// FIXME categoriesのセット処理
							parentTag = "categories";
						}
					}
					break;
				case XmlPullParser.END_TAG:
					tag = parser.getName();
					if ("categories".equals(tag)) {
						// FIXME categoriesのセット処理
						parentTag = "group";
					} else if ("group".equals(tag)) {
						participation.setGroup(group);
						parentTag = "participation";
					} else if ("participation".equals(tag)) {
						entry.setParticipation(participation);
						parentTag = "entry";
					} else if ("attachment".equals(tag)) {
						entry.setAttachment(attachment);
						parentTag = "entry";
					} else if ("child".equals(tag)) {
						Entry parent = parentStack.peek();
						if (parent != null) {
							if (parent.getChildren() == null) {
								parent.setChildren(new ArrayList<Entry>());
							}
							parent.getChildren().add(entry);
						}
					} else if ("children".equals(tag)) {
						entry = parentStack.pop();
					} else if ("entry".equals(tag)) {
						results.add(entry);
						parentTag = null;
					}
					break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			throw new YouRoom4JException(e);
		} finally {
			if (byteArrayInputStream != null) {
				try {
					byteArrayInputStream.close();
				} catch (IOException e) {
				}
			}
		}
		return results;
	}

	public List<Group> getMyGroups() throws YouRoom4JException {
		List<KeyValueString> paramList = new ArrayList<KeyValueString>();
		paramList.add(new KeyValueString("format", "xml"));

		HttpRequestEntity requestEntity = new HttpRequestEntity();
		requestEntity.setUrl("https://www.youroom.in/groups/my?format=xml");
		requestEntity.setMethod(HttpRequestEntity.GET);
		oAuthClient
			.addOAuthTokenCredentialToRequestEntity(requestEntity, "https://www.youroom.in/groups/my", paramList);

		HttpRequestClient client = new HttpRequestClientImpl(5000, 10000, 0, Charset.forName("UTF-8"));
		ByteArrayInputStream byteArrayInputStream = null;
		List<Group> results = new ArrayList<Group>();

		try {
			String responseContent = client.execute(requestEntity);
			System.out.println(responseContent);
			// FIXME
			// AndroidならXmlPullParser、JDKならStAXでパースしてオブジェクトに詰めた結果を返すので、処理を外出しして切り替えが容易な形にしておく
			XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();

			byteArrayInputStream = new ByteArrayInputStream(responseContent.getBytes("UTF-8"));
			parser.setInput(byteArrayInputStream, "UTF-8");
			int eventType = parser.getEventType();
			Group group = null;
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'Z");
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String tag = null;
				switch (eventType) {
				case XmlPullParser.START_TAG:
					tag = parser.getName();
					if ("group".equals(tag)) {
						group = new Group();
					} else if (group != null) {
						if ("created-at".equals(tag)) {
							group.setCreatedAt(df.parse(parser.nextText() + "+0000"));
						} else if ("updated-at".equals(tag)) {
							group.setUpdatedAt(df.parse(parser.nextText() + "+0000"));
						} else if ("id".equals(tag)) {
							group.setId(Long.parseLong(parser.nextText()));
						} else if ("name".equals(tag)) {
							group.setName(parser.nextText());
						} else if ("to-param".equals(tag)) {
							group.setToParam(parser.nextText());
						} else if ("opened".equals(tag)) {
							group.setOpened(Boolean.parseBoolean(parser.nextText()));
						}
					}
					break;
				case XmlPullParser.END_TAG:
					tag = parser.getName();
					if ("group".equals(tag)) {
						results.add(group);
					}
					break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			throw new YouRoom4JException(e);
		} finally {
			if (byteArrayInputStream != null) {
				try {
					byteArrayInputStream.close();
				} catch (IOException e) {
				}
			}
		}
		return results;
	}

	public <T> T showPicture(String url, HttpResponseHandler<T> httpResponseHandler) throws YouRoom4JException {
		List<KeyValueString> paramList = new ArrayList<KeyValueString>();
		paramList.add(new KeyValueString("format", "image"));

		HttpRequestEntity requestEntity = new HttpRequestEntity();
		requestEntity.setUrl(url);
		requestEntity.setMethod(HttpRequestEntity.GET);
		requestEntity.setParams(paramList);
		oAuthClient.addOAuthTokenCredentialToRequestEntity(requestEntity, url, paramList);

		HttpRequestClient client = new HttpRequestClientImpl(5000, 10000, 0, Charset.forName("UTF-8"));
		try {
			return client.execute(requestEntity, httpResponseHandler);
		} catch (IOException e) {
			throw new YouRoom4JException(e);
		}
	}

	public Entry createEntry(String groupParam, String content, Long parentId) throws YouRoom4JException {
		List<KeyValueString> paramList = new ArrayList<KeyValueString>();
		paramList.add(new KeyValueString("format", "xml"));
		paramList.add(new KeyValueString("entry[content]", content));
		if (parentId != null) {
			paramList.add(new KeyValueString("entry[parent_id]", parentId.toString()));
		}

		String url = "https://www.youroom.in/r/" + groupParam + "/entries";
		HttpRequestEntity requestEntity = new HttpRequestEntity();
		requestEntity.setUrl(url);
		requestEntity.setMethod(HttpRequestEntity.POST);
		requestEntity.setParams(paramList);
		oAuthClient.addOAuthTokenCredentialToRequestEntity(requestEntity, url, paramList);

		HttpRequestClient client = new HttpRequestClientImpl(5000, 10000, 0, Charset.forName("UTF-8"));
		try {
			String responseContent = client.execute(requestEntity, HttpStatus.SC_CREATED);
			System.out.println(responseContent);
			return parseEntries(responseContent).get(0);
		} catch (IOException e) {
			throw new YouRoom4JException(e);
		}
	}

	public Entry updateEntry(String groupParam, long id, String content) throws YouRoom4JException {
		List<KeyValueString> paramList = new ArrayList<KeyValueString>();
		paramList.add(new KeyValueString("format", "xml"));
		paramList.add(new KeyValueString("entry[content]", content));

		String url = "https://www.youroom.in/r/" + groupParam + "/entries/" + id;
		HttpRequestEntity requestEntity = new HttpRequestEntity();
		requestEntity.setUrl(url);
		requestEntity.setMethod(HttpRequestEntity.PUT);
		requestEntity.setParams(paramList);
		oAuthClient.addOAuthTokenCredentialToRequestEntity(requestEntity, url, paramList);

		HttpRequestClient client = new HttpRequestClientImpl(5000, 10000, 0, Charset.forName("UTF-8"));
		try {
			String responseContent = client.execute(requestEntity, HttpStatus.SC_CREATED);
			Log.d("", responseContent);
			return parseEntries(responseContent).get(0);
		} catch (IOException e) {
			throw new YouRoom4JException(e);
		}
	}

	public Entry destroyEntry(String groupParam, long id) throws YouRoom4JException {
		List<KeyValueString> paramList = new ArrayList<KeyValueString>();
		paramList.add(new KeyValueString("format", "xml"));

		String url = "https://www.youroom.in/r/" + groupParam + "/entries/" + id;
		HttpRequestEntity requestEntity = new HttpRequestEntity();
		requestEntity.setMethod(HttpRequestEntity.DELETE);
		requestEntity.setUrl(url + "?format=xml");
		oAuthClient.addOAuthTokenCredentialToRequestEntity(requestEntity, url, paramList);

		HttpRequestClient client = new HttpRequestClientImpl(5000, 10000, 0, Charset.forName("UTF-8"));
		try {
			String responseContent = client.execute(requestEntity);
			Log.d("", responseContent);
			return parseEntries(responseContent).get(0);
		} catch (IOException e) {
			throw new YouRoom4JException(e);
		}
	}

	public Entry showEntry(String groupParam, long id) throws YouRoom4JException {
		List<KeyValueString> paramList = new ArrayList<KeyValueString>();
		paramList.add(new KeyValueString("format", "xml"));

		String url = "https://www.youroom.in/r/" + groupParam + "/entries/" + id;
		HttpRequestEntity requestEntity = new HttpRequestEntity();
		requestEntity.setMethod(HttpRequestEntity.GET);
		requestEntity.setUrl(url + "?format=xml");
		oAuthClient.addOAuthTokenCredentialToRequestEntity(requestEntity, url, paramList);

		HttpRequestClient client = new HttpRequestClientImpl(5000, 10000, 0, Charset.forName("UTF-8"));
		try {
			String responseContent = client.execute(requestEntity);
			System.out.println(responseContent);
			return parseEntries(responseContent).get(0);
		} catch (IOException e) {
			throw new YouRoom4JException(e);
		}
	}

}
