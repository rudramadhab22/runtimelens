package org.runtimelens.core.model;

public class JvmEvent extends RuntimeEvent {
    private final long heapUsedBytes;
    private final long heapMaxBytes;
    private final double cpuLoad;

    public JvmEvent(long heapUsedBytes, long heapMaxBytes, double cpuLoad) {
        super("SYSTEM"); // JVM events are not necessarily tied to a request
        this.heapUsedBytes = heapUsedBytes;
        this.heapMaxBytes = heapMaxBytes;
        this.cpuLoad = cpuLoad;
    }

    @Override
    public String getType() { return "JVM_HEALTH"; }

    public long getHeapUsedBytes() { return heapUsedBytes; }
    public long getHeapMaxBytes() { return heapMaxBytes; }
    public double getCpuLoad() { return cpuLoad; }
}
