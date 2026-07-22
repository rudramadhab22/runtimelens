package org.runtimelens.spring.collector;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.runtimelens.core.engine.DiagnosticEngine;
import org.runtimelens.core.engine.RequestContext;
import org.runtimelens.core.model.HttpRequestEvent;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class HttpCollectorFilter extends OncePerRequestFilter {

    private final DiagnosticEngine diagnosticEngine;

    public HttpCollectorFilter(DiagnosticEngine diagnosticEngine) {
        this.diagnosticEngine = diagnosticEngine;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestId = UUID.randomUUID().toString();
        RequestContext.setRequestId(requestId);
        long startTime = System.currentTimeMillis();
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            HttpRequestEvent event = new HttpRequestEvent(
                requestId,
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration,
                request.getRemoteAddr()
            );
            diagnosticEngine.addEvent(event);
            RequestContext.clear();
        }
    }
}
