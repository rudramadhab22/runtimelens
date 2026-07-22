package org.runtimelens.core.model;

public class HttpRequestEvent extends RuntimeEvent {
    private final String method;
    private final String uri;
    private final int status;
    private final long durationMs;
    private final String clientIp;

    public HttpRequestEvent(String requestId, String method, String uri, int status, long durationMs, String clientIp) {
        super(requestId);
        this.method = method;
        this.uri = uri;
        this.status = status;
        this.durationMs = durationMs;
        this.clientIp = clientIp;
    }

    @Override
    public String getType() { return "HTTP_REQUEST"; }

    public String getMethod() { return method; }
    public String getUri() { return uri; }
    public int getStatus() { return status; }
    public long getDurationMs() { return durationMs; }
    public String getClientIp() { return clientIp; }
}
