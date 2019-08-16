package uk.gov.digital.ho.pttg;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import uk.gov.digital.ho.pttg.api.HttpResponse;

import java.io.IOException;

class SimpleHttpClient {

    HttpResponse post(String url, String body) {
        try {
            return doPost(url, body);
        } catch (IOException e) {
            throw new AssertionError(String.format("Post to url %s failed", url), e);
        }
    }

    private HttpResponse doPost(String url, String body) throws IOException {
        try (CloseableHttpClient httpClient = getHttpClient()) {
            HttpPost request = new HttpPost(url);
            request.addHeader("Content-Type", "application/json");
            request.setEntity(new StringEntity(body));
            CloseableHttpResponse response = httpClient.execute(request);
            return new HttpResponse(response.getStatusLine().getStatusCode(), EntityUtils.toString(response.getEntity()));
        }
    }

    private CloseableHttpClient getHttpClient() {
        return HttpClientBuilder.create().build();
    }
}
