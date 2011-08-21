package youroom4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


import org.xmlpull.v1.XmlPullParser;

import com.github.learnin.youroomer.Entry;

import youroom4j.http.HttpRequestClient;
import youroom4j.http.HttpRequestEntity;
import youroom4j.http.httpclient.HttpRequestClientImpl;
import youroom4j.oauth.OAuthClient;
import youroom4j.oauth.OAuthTokenCredential;
import android.util.Xml;

//TODO 流れるようなインターフェースにしてはどうか？
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
	public String oAuthRequestTokenRequest(String oauthCallbackUrl) throws IOException {
		return oAuthClient.temporaryCredential(oauthCallbackUrl);
	}

	public void oAuthAccessTokenRequest(String oauthVerifier) throws IOException {
		oAuthClient.tokenCredential(oauthVerifier);
	}

	public List<Entry> getTimeLine() throws IOException {
		List<KeyValueString> paramList = new ArrayList<KeyValueString>();
		paramList.add(new KeyValueString("format", "xml"));

		HttpRequestEntity requestEntity = new HttpRequestEntity();
		requestEntity.setUrl("https://www.youroom.in/?format=xml");
		requestEntity.setMethod(HttpRequestEntity.GET);
		oAuthClient.addOAuthTokenCredentialToRequestEntity(requestEntity, "https://www.youroom.in/", paramList);

		HttpRequestClient client = new HttpRequestClientImpl(5000, 10000, 0, Charset.forName("UTF-8"));
		String line = client.execute(requestEntity);
		System.out.println(line);
		// FIXME
		// AndroidならXmlPullParser、JDKならStAXでパースしてオブジェクトに詰めた結果を返すので、処理を外出しして切り替えが容易な形にしておく
		List<Entry> results = new ArrayList<Entry>();
		XmlPullParser parser = Xml.newPullParser();
		ByteArrayInputStream byteArrayInputStream = null;
		try {
			byteArrayInputStream = new ByteArrayInputStream(line.getBytes("UTF-8"));
			parser.setInput(byteArrayInputStream, "UTF-8");
			int eventType = parser.getEventType();
			Entry entry = null;
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'Z");
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String tag = null;
				switch (eventType) {
				case XmlPullParser.START_TAG:
					tag = parser.getName();
					if ("entry".equals(tag)) {
						entry = new Entry();
					} else if (entry != null) {
						if ("content".equals(tag)) {
							entry.setContent(parser.nextText());
						} else if ("created-at".equals(tag)) {
							entry.setCreatedAt(df.parse(parser.nextText() + "+0000"));
						}
					}
					break;
				case XmlPullParser.END_TAG:
					tag = parser.getName();
					if ("entry".equals(tag)) {
						results.add(entry);
					}
					break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (byteArrayInputStream != null) {
				byteArrayInputStream.close();
			}
		}
		return results;
	}

	public void getMyGroup() throws IOException {
		List<KeyValueString> paramList = new ArrayList<KeyValueString>();
		paramList.add(new KeyValueString("format", "xml"));

		HttpRequestEntity requestEntity = new HttpRequestEntity();
		requestEntity.setUrl("https://www.youroom.in/groups/my?format=xml");
		requestEntity.setMethod(HttpRequestEntity.GET);
		oAuthClient
			.addOAuthTokenCredentialToRequestEntity(requestEntity, "https://www.youroom.in/groups/my", paramList);

		HttpRequestClient client = new HttpRequestClientImpl(5000, 10000, 0, Charset.forName("UTF-8"));
		String line = client.execute(requestEntity);
		System.out.println(line);
	}

}
