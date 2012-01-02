package youroom4j.oauth;

import java.io.Serializable;

public class OAuthTokenCredential implements Serializable {

	private static final long serialVersionUID = 1L;
	private String token;
	private String tokenSecret;

	/**
	 * tokenを取得します。
	 *
	 * @return token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * tokenを設定します。
	 *
	 * @param token token
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * tokenSecretを取得します。
	 *
	 * @return tokenSecret
	 */
	public String getTokenSecret() {
		return tokenSecret;
	}

	/**
	 * tokenSecretを設定します。
	 *
	 * @param tokenSecret tokenSecret
	 */
	public void setTokenSecret(String tokenSecret) {
		this.tokenSecret = tokenSecret;
	}

}
