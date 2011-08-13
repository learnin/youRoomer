package youroom4j.http;

import java.io.IOException;

public interface HttpRequestClient {

	public <T> T execute(HttpRequestEntity requestEntity,
			HttpResponseHandler<T> responseHandler) throws IOException;

	public String execute(HttpRequestEntity requestEntity) throws IOException;
}
