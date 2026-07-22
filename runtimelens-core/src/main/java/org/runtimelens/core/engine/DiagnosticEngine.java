package org.runtimelens.core.engine;

import org.runtimelens.core.model.*;
import org.runtimelens.core.reporter.Reporter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DiagnosticEngine {
    private final List<Reporter> reporters;
    private final Map<String, List<RuntimeEvent>> contextMap = new ConcurrentHashMap<>();
    
    private static final long SLOW_REQUEST_THRESHOLD_MS = 1000;
    private static final int N_PLUS_ONE_THRESHOLD = 5;

    public DiagnosticEngine(List<Reporter> reporters) {
        this.reporters = reporters;
    }

    public void addEvent(RuntimeEvent event) {
        if (event.getRequestId() == null) return;
        contextMap.computeIfAbsent(event.getRequestId(), k -> new ArrayList<>()).add(event);
        
        if (event instanceof HttpRequestEvent) {
            analyzeRequest(event.getRequestId());
        } else if (event instanceof JvmEvent) {
            analyzeJvm((JvmEvent) event);
        }
    }

    private void analyzeJvm(JvmEvent event) {
        if (event.getHeapUsedBytes() > event.getHeapMaxBytes() * 0.9) {
            AnalysisResult result = new AnalysisResult("SYSTEM");
            result.setSummary("JVM Health Alert");
            result.addFinding("LOW_MEMORY", String.format("Heap usage is above 90%% (%dMB / %dMB)", 
                event.getHeapUsedBytes() / 1024 / 1024, event.getHeapMaxBytes() / 1024 / 1024));
            report(result);
        }
    }

    private void analyzeRequest(String requestId) {
        List<RuntimeEvent> events = contextMap.remove(requestId);
        if (events == null) return;

        AnalysisResult result = new AnalysisResult(requestId);
        HttpRequestEvent mainRequest = null;
        int jdbcCount = 0;
        List<ExceptionEvent> exceptions = new ArrayList<>();
        List<ExternalCallEvent> externalCalls = new ArrayList<>();

        for (RuntimeEvent event : events) {
            if (event instanceof HttpRequestEvent) {
                mainRequest = (HttpRequestEvent) event;
            } else if (event instanceof JdbcEvent) {
                jdbcCount++;
            } else if (event instanceof ExceptionEvent) {
                exceptions.add((ExceptionEvent) event);
            } else if (event instanceof ExternalCallEvent) {
                externalCalls.add((ExternalCallEvent) event);
            }
        }

        if (mainRequest != null) {
            result.setSummary(String.format("%s %s -> %d (%dms)", 
                mainRequest.getMethod(), mainRequest.getUri(), mainRequest.getStatus(), mainRequest.getDurationMs()));
            
            if (mainRequest.getDurationMs() > SLOW_REQUEST_THRESHOLD_MS) {
                result.addFinding("SLOW_REQUEST", "Request took longer than " + SLOW_REQUEST_THRESHOLD_MS + "ms");
            }
        }

        if (jdbcCount > N_PLUS_ONE_THRESHOLD) {
            result.addFinding("POTENTIAL_N_PLUS_ONE", "Request triggered " + jdbcCount + " database queries");
        }

        for (ExceptionEvent ex : exceptions) {
            result.addFinding("EXCEPTION", "Unhandled exception: " + ex.getExceptionClass() + " - " + ex.getMessage());
        }

        for (ExternalCallEvent call : externalCalls) {
            if (call.getDurationMs() > 500) {
                result.addFinding("SLOW_EXTERNAL_API", String.format("External call to %s took %dms", call.getTargetUrl(), call.getDurationMs()));
            }
        }

        if (!result.getFindings().isEmpty()) {
            report(result);
        }
    }

    private void report(AnalysisResult result) {
        for (Reporter reporter : reporters) {
            reporter.report(result);
        }
    }

    public static class AnalysisResult {
        private final String requestId;
        private String summary;
        private final List<Finding> findings = new ArrayList<>();

        public AnalysisResult(String requestId) { this.requestId = requestId; }
        public void setSummary(String summary) { this.summary = summary; }
        public String getSummary() { return summary; }
        public void addFinding(String type, String message) { findings.add(new Finding(type, message)); }
        public List<Finding> getFindings() { return findings; }
        public String getRequestId() { return requestId; }
    }

    public static class Finding {
        private final String type;
        private final String message;

        public Finding(String type, String message) {
            this.type = type;
            this.message = message;
        }

        public String getType() { return type; }
        public String getMessage() { return message; }
    }
}
