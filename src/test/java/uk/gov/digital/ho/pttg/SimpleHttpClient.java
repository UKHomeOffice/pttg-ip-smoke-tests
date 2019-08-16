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

    private final CloseableHttpClient httpClient;

    SimpleHttpClient(Map<String, String> basicAuthConfig) {
        CredentialsProvider credentialsProvider = getCredentialsProvider(basicAuthConfig);
        httpClient = HttpClientBuilder.create()
                                      .setDefaultCredentialsProvider(credentialsProvider)
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

        HttpClientContext context = getClientContext(url);

        try (CloseableHttpResponse response = httpClient.execute(request, context)) {
            return new HttpResponse(response.getStatusLine().getStatusCode(), EntityUtils.toString(response.getEntity()));
        }
    }

    private HttpClientContext getClientContext(String url) {
        HttpClientContext context = HttpClientContext.create();
        addBasicAuth(context, url);
        return context;
    }

    private void addBasicAuth(HttpClientContext context, String url) {
        AuthCache authCache = new BasicAuthCache();
        context.setAuthCache(authCache);

        authCache.put(extractHost(url), new BasicScheme());
    }

    private CredentialsProvider getCredentialsProvider(Map<String, String> basicAuthConfig) {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        basicAuthConfig.forEach((url, credentials) -> {
            HttpHost host = extractHost(url);
            credentialsProvider.setCredentials(new AuthScope(host), splitCredentials(credentials));
        });
        return credentialsProvider;
    }

    private UsernamePasswordCredentials splitCredentials(String credentials) {
        String username = credentials.split(":")[0];
        String password = credentials.split(":")[1];
        return new UsernamePasswordCredentials(username, password);
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
