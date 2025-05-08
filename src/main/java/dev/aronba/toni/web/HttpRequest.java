package dev.aronba.toni.web;

import java.util.Map;

public class HttpRequest {
  private String body;
  private Map<String, String> headers;
  private String method;
  private String path;

  private HttpRequest() {}

  public static HttpRequestBuilder builder() {
    return new HttpRequestBuilder();
  }

  public String getBody() {
    return body;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public String getMethod() {
    return method;
  }

  public String getPath() {
    return path;
  }

  public static class HttpRequestBuilder {
    private final HttpRequest httpRequest;

    public HttpRequestBuilder() {
      this.httpRequest = new HttpRequest();
    }

    public HttpRequestBuilder body(String body) {
      httpRequest.body = body;
      return this;
    }

    public HttpRequestBuilder headers(Map<String, String> headers) {
      httpRequest.headers = headers;
      return this;
    }

    public HttpRequestBuilder method(String method) {
      httpRequest.method = method;
      return this;
    }

    public HttpRequestBuilder path(String path) {
      httpRequest.path = path;
      return this;
    }

    public HttpRequest build() {
      return httpRequest;
    }
  }
}
