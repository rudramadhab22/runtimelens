package org.runtimelens.core.model;

public class ExceptionEvent extends RuntimeEvent {
    private final String exceptionClass;
    private final String message;
    private final String stackTrace;

    public ExceptionEvent(String requestId, Throwable throwable) {
        super(requestId);
        this.exceptionClass = throwable.getClass().getName();
        this.message = throwable.getMessage();
        this.stackTrace = getStackTrace(throwable);
    }

    @Override
    public String getType() { return "EXCEPTION"; }

    public String getExceptionClass() { return exceptionClass; }
    public String getMessage() { return message; }
    public String getStackTrace() { return stackTrace; }

    private String getStackTrace(Throwable throwable) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}
