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
package youroom4j.oauth;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import youroom4j.KeyValueString;
import youroom4j.http.HttpRequestClient;
import youroom4j.http.HttpRequestEntity;
import youroom4j.http.httpclient.HttpRequestClientImpl;

//TODO 流れるようなインターフェースにしてはどうか？
public class OAuthClient {

	private static final String SIGNATURE_METHOD = "HMAC-SHA1";
	private static final String OAUTH_VERSION = "1.0";

	private String consumerKey;
	private String consumerSecret;

	// テンポラリクレデンシャルリクエストエンドポイントの URI
	private String temporaryCredentialRequestUri;

	// リソースオーナー許可エンドポイントの URI
	private String resourceOwnerAuthorizationUri;

	// トークンクレデンシャルリクエストエンドポイントの URI
	private String tokenRequestUri;

	private OAuthTokenCredential tokenCredential = new OAuthTokenCredential();

	public OAuthClient(String consumerKey, String consumerSecret, String temporaryCredentialRequestUri,
			String resourceOwnerAuthorizationUri, String TokenRequestUri) {
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.temporaryCredentialRequestUri = temporaryCredentialRequestUri;
		this.resourceOwnerAuthorizationUri = resourceOwnerAuthorizationUri;
		this.tokenRequestUri = TokenRequestUri;
	}

	public OAuthTokenCredential getOAuthTokenCredential() {
		return tokenCredential;
	}

	public void setOAuthTokenCredential(OAuthTokenCredential tokenCredential) {
		this.tokenCredential = tokenCredential;
	}

	private List<KeyValueString> createOAuthParameter() {
		List<KeyValueString> paramList = new ArrayList<KeyValueString>();
		paramList.add(new KeyValueString("oauth_consumer_key", consumerKey));
		paramList.add(new KeyValueString("oauth_nonce", OAuthUtil.getNonce()));
		paramList.add(new KeyValueString("oauth_signature_method", SIGNATURE_METHOD));
		paramList.add(new KeyValueString("oauth_timestamp", OAuthUtil.getTimeStamp()));
		paramList.add(new KeyValueString("oauth_version", OAUTH_VERSION));
		return paramList;

	}

	private List<KeyValueString> createOAuthParameterForTemporaryCredential(String callbackUri) {
		List<KeyValueString> paramList = createOAuthParameter();
		if (callbackUri != null && callbackUri.length() > 0) {
			paramList.add(new KeyValueString("oauth_callback", callbackUri));
		} else {
			paramList.add(new KeyValueString("oauth_callback", "oob"));
		}
		return paramList;
	}

	private List<KeyValueString> createOAuthParameterForTokenCredential(String oauthVerifier) {
		List<KeyValueString> paramList = createOAuthParameter();
		paramList.add(new KeyValueString("oauth_token", tokenCredential.getToken()));
		paramList.add(new KeyValueString("oauth_verifier", oauthVerifier));
		return paramList;
	}

	private List<KeyValueString> createOAuthParameterForAuthenticatedRequest() {
		List<KeyValueString> paramList = createOAuthParameter();
		paramList.add(new KeyValueString("oauth_token", tokenCredential.getToken()));
		return paramList;
	}

	private String createSignatureBaseString(String httpMehtod, String baseStringURI, List<KeyValueString> paramList) {
		StringBuilder result = new StringBuilder();
		result
			.append(OAuthEncoder.encode(httpMehtod))
			.append("&")
			.append(OAuthEncoder.encode(baseStringURI))
			.append("&")
			.append(OAuthEncoder.encode(normalize(paramList)));
		System.out.println(result.toString());
		return result.toString();
	}

	private String normalize(List<KeyValueString> paramList) {

		// http://tools.ietf.org/html/rfc5849#section-3.4.1.3.2
		// RFCによると、OAuth 1.0でのノーマライズはエンコードしてからソートするはずだが、それでは署名が合わないようなので先にソートする。
		Collections.sort(paramList, new Comparator<KeyValueString>() {
			public int compare(KeyValueString o1, KeyValueString o2) {
				int result = compare(o1.getKey(), o2.getKey());
				if (result != 0) {
					return result;
				}
				result = compare(o1.getValue(), o2.getValue());
				if (result != 0) {
					return result;
				}
				return 0;
			}

			private int compare(String s1, String s2) {
				if (s1 == null && s2 == null) {
					return 0;
				}
				if (s1 == null) {
					return -1;
				}
				if (s2 == null) {
					return 1;
				}
				return s1.compareTo(s2);
			}
		});

		StringBuilder result = new StringBuilder();
		int i = 0;
		for (KeyValueString keyValueString : paramList) {
			if (i > 0) {
				result.append("&");
			}
			result
				.append(OAuthEncoder.encode(keyValueString.getKey()))
				.append("=")
				.append(OAuthEncoder.encode(keyValueString.getValue()));
			i++;
		}
		return result.toString();
	}

	private String createSignature(String httpMehtod, String baseStringURI, List<KeyValueString> paramList) {
		String keyString = consumerSecret + "&";
		if (tokenCredential.getTokenSecret() != null) {
			keyString = consumerSecret + "&" + tokenCredential.getTokenSecret();
		}
		return OAuthUtil.getSignature(createSignatureBaseString(httpMehtod, baseStringURI, paramList), keyString);
	}

	private String createOAuthHeaderString(List<KeyValueString> paramList, String signature) {
		paramList.add(new KeyValueString("oauth_signature", signature));
		StringBuilder headerString = new StringBuilder();
		headerString.append("OAuth ");
		int i = 0;
		for (KeyValueString keyValueString : paramList) {
			if (i > 0) {
				headerString.append(", ");
			}
			headerString
				.append(OAuthEncoder.encode(keyValueString.getKey()))
				.append("=\"")
				.append(OAuthEncoder.encode(keyValueString.getValue()))
				.append("\"");
			i++;
		}
		return headerString.toString();
	}

	public String temporaryCredential(String callbackUri) throws IOException {
		List<KeyValueString> paramList = createOAuthParameterForTemporaryCredential(callbackUri);
		String signature = createSignature("POST", temporaryCredentialRequestUri, paramList);

		HttpRequestEntity requestEntity = new HttpRequestEntity();
		requestEntity.setUrl(temporaryCredentialRequestUri);
		requestEntity.setMethod(HttpRequestEntity.POST);
		List<KeyValueString> headers = new ArrayList<KeyValueString>();
		headers.add(new KeyValueString("Authorization", createOAuthHeaderString(paramList, signature)));
		requestEntity.setHeaders(headers);

		System.out.println(requestEntity.getHeaders().get(0).getValue());

		HttpRequestClient client = new HttpRequestClientImpl(5000, 10000, 0, Charset.forName("UTF-8"));
		String line = client.execute(requestEntity);
		System.out.println(line);
		String[] parameters = line.split("&");
		for (String parameter : parameters) {
			String[] keyAndValue = parameter.split("=");
			String key = keyAndValue[0];
			String value = keyAndValue[1];
			if ("oauth_token".equals(key)) {
				tokenCredential.setToken(value);
			} else if ("oauth_token_secret".equals(key)) {
				tokenCredential.setTokenSecret(value);
			}
		}
		return resourceOwnerAuthorizationUri + "?oauth_token=" + tokenCredential.getToken();
	}

	public void tokenCredential(String oauthVerifier) throws IOException {

		List<KeyValueString> paramList = createOAuthParameterForTokenCredential(oauthVerifier);
		String signature = createSignature("POST", tokenRequestUri, paramList);

		HttpRequestEntity requestEntity = new HttpRequestEntity();
		requestEntity.setUrl(tokenRequestUri);
		requestEntity.setMethod(HttpRequestEntity.POST);
		List<KeyValueString> headers = new ArrayList<KeyValueString>();
		headers.add(new KeyValueString("Authorization", createOAuthHeaderString(paramList, signature)));
		requestEntity.setHeaders(headers);

		System.out.println(requestEntity.getHeaders().get(0).getValue());

		HttpRequestClient client = new HttpRequestClientImpl(5000, 10000, 0, Charset.forName("UTF-8"));
		String line = client.execute(requestEntity);
		System.out.println(line);
		String[] parameters = line.split("&");
		for (String parameter : parameters) {
			String[] keyAndValue = parameter.split("=");
			String key = keyAndValue[0];
			String value = keyAndValue[1];
			if ("oauth_token".equals(key)) {
				tokenCredential.setToken(value);
			} else if ("oauth_token_secret".equals(key)) {
				tokenCredential.setTokenSecret(value);
			}
		}
	}

	public void addOAuthTokenCredentialToRequestEntity(HttpRequestEntity requestEntity, String baseStringUri,
			List<KeyValueString> paramList) {
		List<KeyValueString> oauthParamList = createOAuthParameterForAuthenticatedRequest();
		List<KeyValueString> parameterList = new ArrayList<KeyValueString>(oauthParamList);
		parameterList.addAll(paramList);

		String signature;
		if (requestEntity.getMethod() == HttpRequestEntity.GET) {
			signature = createSignature("GET", baseStringUri, parameterList);
		} else if (requestEntity.getMethod() == HttpRequestEntity.POST) {
			signature = createSignature("POST", baseStringUri, parameterList);
		} else if (requestEntity.getMethod() == HttpRequestEntity.PUT) {
			signature = createSignature("PUT", baseStringUri, parameterList);
		} else if (requestEntity.getMethod() == HttpRequestEntity.DELETE) {
			signature = createSignature("DELETE", baseStringUri, parameterList);
		} else {
			throw new UnsupportedOperationException();
		}

		if (requestEntity.getHeaders() == null) {
			requestEntity.setHeaders(new ArrayList<KeyValueString>());
		}
		requestEntity.getHeaders().add(
			new KeyValueString("Authorization", createOAuthHeaderString(oauthParamList, signature)));
		System.out.println(requestEntity.getHeaders().get(0).getValue());
	}

}
