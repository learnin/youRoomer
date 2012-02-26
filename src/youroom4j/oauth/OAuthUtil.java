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
