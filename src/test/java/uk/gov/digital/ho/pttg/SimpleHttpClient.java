package uk.gov.digital.ho.pttg;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import uk.gov.digital.ho.pttg.api.HttpResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

class SimpleHttpClient {

    private final Map<String, String> basicAuthConfig;
    private final CloseableHttpClient httpClient;

    SimpleHttpClient(Map<String, String> basicAuthConfig) {
        this.basicAuthConfig = basicAuthConfig;
        httpClient = HttpClientBuilder.create()
                                      .setDefaultCredentialsProvider(getCredentialsProvider())
                                      .build();
    }

    HttpResponse post(String url, String body) {
        try {
            return doPost(url, body);
        } catch (IOException e) {
            throw new AssertionError(String.format("Post to url %s failed", url), e);
        }
    }

    private HttpResponse doPost(String url, String body) throws IOException {
        HttpPost request = new HttpPost(url);
        request.addHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(body));

        HttpClientContext context = getClientContext();

        try (CloseableHttpResponse response = httpClient.execute(request, context)) {
            return new HttpResponse(response.getStatusLine().getStatusCode(), EntityUtils.toString(response.getEntity()));
        }
    }

    private HttpClientContext getClientContext() {
        HttpClientContext context = HttpClientContext.create();
        addBasicAuth(context);
        return context;
    }

    private void addBasicAuth(HttpClientContext context) {
        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme();

        for (Map.Entry<String, String> credentialsEntry : basicAuthConfig.entrySet()) {
            HttpHost host = extractHost(credentialsEntry.getKey());
            authCache.put(host, basicAuth);
        }
        context.setAuthCache(authCache);
    }

    private CredentialsProvider getCredentialsProvider() {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        for (Map.Entry<String, String> credentialsEntry : basicAuthConfig.entrySet()) {
            HttpHost host = extractHost(credentialsEntry.getKey());
            String credentials = credentialsEntry.getValue();
            String username = credentials.split(":")[0];
            String password = credentials.split(":")[1];

            credentialsProvider.setCredentials(new AuthScope(host),
                                               new UsernamePasswordCredentials(username, password));
        }
        return credentialsProvider;
    }

    private HttpHost extractHost(String url) {
        try {
            URI uri = new URI(url);
            return new HttpHost(uri.getHost(), uri.getPort());
        } catch (URISyntaxException e) {
            throw new AssertionError(String.format("Could not get host from URL %s", url));
        }
    }
}
