package org.runtimelens.core.model;

public class ExternalCallEvent extends RuntimeEvent {
    private final String targetUrl;
    private final String method;
    private final int status;
    private final long durationMs;

    public ExternalCallEvent(String requestId, String targetUrl, String method, int status, long durationMs) {
        super(requestId);
        this.targetUrl = targetUrl;
        this.method = method;
        this.status = status;
        this.durationMs = durationMs;
    }

    @Override
    public String getType() { return "EXTERNAL_CALL"; }

    public String getTargetUrl() { return targetUrl; }
    public String getMethod() { return method; }
    public int getStatus() { return status; }
    public long getDurationMs() { return durationMs; }
}
