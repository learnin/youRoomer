package youroom4j.http.httpclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.params.CoreConnectionPNames;

import youroom4j.KeyValueString;
import youroom4j.http.HttpRequestClient;
import youroom4j.http.HttpRequestEntity;
import youroom4j.http.HttpResponseEntity;
import youroom4j.http.HttpResponseHandler;

public class HttpRequestClientImpl implements HttpRequestClient {

	int connectionTimeout;
	int responseTimeout;
	int retryCount = 0;
	Charset charset;
	String proxyHost;
	int proxyPort;

	public HttpRequestClientImpl(int connectionTimeout, int responseTimeout, int retryCount, Charset charset) {
		this.connectionTimeout = connectionTimeout;
		this.responseTimeout = responseTimeout;
		this.retryCount = retryCount;
		this.charset = charset;
	}

	public HttpRequestClientImpl(int connectionTimeout, int responseTimeout, int retryCount, Charset charset,
			String proxyHost, int proxyPort) {
		this.connectionTimeout = connectionTimeout;
		this.responseTimeout = responseTimeout;
		this.retryCount = retryCount;
		this.charset = charset;
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
	}

	public <T> T execute(HttpRequestEntity requestEntity, final HttpResponseHandler<T> responseHandler)
			throws IOException {

		if (requestEntity == null) {
			throw new IllegalArgumentException("requestEntity must not be null.");
		}
		if (responseHandler == null) {
			throw new IllegalArgumentException("responseHandler must not be null.");
		}

		HttpRequestBase httpRequestBase;
		if (requestEntity.getMethod() == HttpRequestEntity.GET) {
			httpRequestBase = new HttpGet(requestEntity.getUrl());
		} else if (requestEntity.getMethod() == HttpRequestEntity.POST) {
			httpRequestBase = new HttpPost(requestEntity.getUrl());
		} else {
			throw new UnsupportedOperationException();
		}

		List<KeyValueString> headers = requestEntity.getHeaders();
		if (headers != null) {
			for (KeyValueString keyValueString : headers) {
				httpRequestBase.addHeader(keyValueString.getKey(), keyValueString.getValue());
			}
		}

		// FIXME param, bodyのセット実装

		DefaultHttpClient httpClient = null;
		try {
			httpClient = createDefaultHttpClient();

			// 接続時のエラーは HttpRequestRetryHandler でリトライされないため手動でリトライする。
			T result = null;
			for (int i = 1, n = retryCount + 1; i <= n; i++) {
				try {
					result = httpClient.execute(httpRequestBase, new ResponseHandler<T>() {
						public T handleResponse(HttpResponse httpresponse) throws ClientProtocolException, IOException {
							System.out.println(httpresponse.getStatusLine());
							HttpResponseEntity responseEntity = new HttpResponseEntity();
							responseEntity.setStatusCode(httpresponse.getStatusLine().getStatusCode());
							responseEntity.setContent(httpresponse.getEntity().getContent());
							return responseHandler.handleResponse(responseEntity);
						}
					});
					break;
				} catch (ConnectTimeoutException e) {
					// 接続タイムアウト
					if (i == n) {
						throw e;
					}
				}
			}
			return result;
		} catch (SocketTimeoutException e) {
			// レスポンスタイムアウト
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			if (httpRequestBase != null) {
				httpRequestBase.abort();
			}
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}
		}
	}

	protected DefaultHttpClient createDefaultHttpClient() {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectionTimeout);
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, responseTimeout);

		if (retryCount > 0) {
			// リクエスト送信後のエラー発生時のリトライについては、サーバー側で処理が行われている可能性を考慮し、行わない。
			httpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(retryCount, false));
		}

		if (proxyHost != null) {
			httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, new HttpHost(proxyHost, proxyPort));
		}
		return httpClient;
	}

	public String execute(HttpRequestEntity requestEntity) throws IOException {
		return execute(requestEntity, new HttpResponseHandler<String>() {
			public String handleResponse(HttpResponseEntity responseEntity) throws IOException {
				if (responseEntity.getStatusCode() == HttpStatus.SC_OK) {
					BufferedReader br = null;
					try {
						br = new BufferedReader(new InputStreamReader(responseEntity.getContent(), charset));
						StringBuilder result = new StringBuilder();
						String line;
						while ((line = br.readLine()) != null) {
							result.append(line);
						}
						if (result.length() == 0) {
							// FIXME
							throw new IOException("レスポンスのボディが空です。");
						}
						return result.toString();
					} finally {
						if (br != null) {
							try {
								br.close();
								// TODO responseEntity.getContent
								// のinputStreamも閉じられるか確認する
							} catch (IOException e) {
							}
						}
					}
				}
				throw new RuntimeException(String.valueOf(responseEntity.getStatusCode()));
			}
		});
	}
}
