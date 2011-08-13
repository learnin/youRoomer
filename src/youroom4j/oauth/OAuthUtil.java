package youroom4j.oauth;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

public class OAuthUtil {

	public static String getTimeStamp() {
		return Long.toString(System.currentTimeMillis() / 1000);
	}

	public static String getSignature(String signatureBaseString, String keyString) {
		String signature = null;
		String algorithm = "HmacSHA1";
		try {
			Mac mac = Mac.getInstance(algorithm);
			Key key = new SecretKeySpec(keyString.getBytes(), algorithm);

			mac.init(key);
			byte[] digest = mac.doFinal(signatureBaseString.getBytes());
			signature = Base64.encodeToString(digest, Base64.NO_WRAP);
		} catch (NoSuchAlgorithmException e) {
			// FIXME
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// FIXME
			e.printStackTrace();
		}
		return signature;
	}

	public static String getNonce() {
		return UUID.randomUUID().toString();
	}

}
