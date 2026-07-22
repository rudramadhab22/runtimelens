package org.runtimelens.core.model;

import java.time.Instant;
import java.util.UUID;

public abstract class RuntimeEvent {
    private final String id;
    private final Instant timestamp;
    private final String requestId;

    protected RuntimeEvent(String requestId) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.requestId = requestId;
    }

    public String getId() { return id; }
    public Instant getTimestamp() { return timestamp; }
    public String getRequestId() { return requestId; }
    
    public abstract String getType();
}
