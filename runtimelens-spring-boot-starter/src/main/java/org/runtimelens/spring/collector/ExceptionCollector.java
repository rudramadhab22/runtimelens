package org.runtimelens.spring.collector;

import org.runtimelens.core.engine.DiagnosticEngine;
import org.runtimelens.core.engine.RequestContext;
import org.runtimelens.core.model.ExceptionEvent;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionCollector {

    private final DiagnosticEngine engine;

    public ExceptionCollector(DiagnosticEngine engine) {
        this.engine = engine;
    }

    @ExceptionHandler(Exception.class)
    public void handleException(Exception ex) throws Exception {
        String rid = RequestContext.getRequestId();
        if (rid != null) {
            engine.addEvent(new ExceptionEvent(rid, ex));
        }
        throw ex; // Re-throw so the application's normal error handling continues
    }
}
