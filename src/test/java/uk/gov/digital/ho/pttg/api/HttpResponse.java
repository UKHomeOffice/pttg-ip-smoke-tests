package uk.gov.digital.ho.pttg.api;

public class HttpResponse {
    private final int statusCode;
    private final String body;

    public HttpResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public String body() {
        return body;
    }

    public int statusCode() {
        return statusCode;
    }
}
