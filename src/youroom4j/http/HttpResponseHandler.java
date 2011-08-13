package youroom4j.http;

import java.io.IOException;

public interface HttpResponseHandler<T> {

	public T handleResponse(HttpResponseEntity responseEntity) throws IOException;
}
