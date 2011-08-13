package youroom4j;

public class KeyValueString {

	private String key;
	private String value;

	public KeyValueString(String key, String value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * keyを取得します。
	 * 
	 * @return key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * keyを設定します。
	 * 
	 * @param key key
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * valueを取得します。
	 * 
	 * @return value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * valueを設定します。
	 * 
	 * @param value value
	 */
	public void setValue(String value) {
		this.value = value;
	}

}
