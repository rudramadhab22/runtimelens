package org.runtimelens.core.model;

public class JdbcEvent extends RuntimeEvent {
    private final String sql;
    private final long durationMs;
    private final boolean success;

    public JdbcEvent(String requestId, String sql, long durationMs, boolean success) {
        super(requestId);
        this.sql = sql;
        this.durationMs = durationMs;
        this.success = success;
    }

    @Override
    public String getType() { return "JDBC_QUERY"; }

    public String getSql() { return sql; }
    public long getDurationMs() { return durationMs; }
    public boolean isSuccess() { return success; }
}
