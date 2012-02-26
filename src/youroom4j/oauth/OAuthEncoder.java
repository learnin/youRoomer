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

import java.io.UnsupportedEncodingException;

public class OAuthEncoder {

	private static final char HEXDIGITS[] = {
		'0',
		'1',
		'2',
		'3',
		'4',
		'5',
		'6',
		'7',
		'8',
		'9',
		'A',
		'B',
		'C',
		'D',
		'E',
		'F' };

	/**
	 * The OAuth 1.0 Protocol で規定されるパーセントエンコーディングを行う。<br>
	 * エンコーディング対象文字は、RFC3986 にて規定されており、<br>
	 * 「アルファベット, 数字, "-", ".", "_", "~"」以外のすべての文字が対象となる。<br>
	 *
	 * @param value エンコード対象文字列
	 * @return パーセントエンコーディングされた文字列
	 * @throws UnsupportedEncodingException
	 */
	public static String encode(String value) {
		if (value == null) {
			return null;
		}
		StringBuilder result = new StringBuilder();
		byte[] bytes;
		try {
			bytes = value.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		for (byte b : bytes) {
			int i = b & 0xFF;
			// ASCIIバイト値(10進数)でエンコード対象文字か判定
			if ((65 <= i && i <= 90)
				|| (97 <= i && i <= 122)
				|| (48 <= i && i <= 57)
				|| i == 45
				|| i == 46
				|| i == 95
				|| i == 126) {
				result.append((char) b);
			} else {
				// 対象文字の場合、2桁の16進数(大文字)へ変換
				result.append("%").append(HEXDIGITS[i >> 4 & 0x0F]).append(HEXDIGITS[i & 0x0F]);
			}
		}
		return result.toString();
	}
}
