package youroom4j;

public class YouRoom4JException extends Exception {

	public YouRoom4JException() {
	}

	public YouRoom4JException(String message) {
		super(message);
	}

	public YouRoom4JException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public YouRoom4JException(Throwable throwable) {
		super(throwable);
	}

}
