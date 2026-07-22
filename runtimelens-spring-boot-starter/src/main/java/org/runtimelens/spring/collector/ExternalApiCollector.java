package org.runtimelens.spring.collector;

import org.runtimelens.core.engine.DiagnosticEngine;
import org.runtimelens.core.engine.RequestContext;
import org.runtimelens.core.model.ExternalCallEvent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExternalApiCollector implements BeanPostProcessor {

    private final ObjectProvider<DiagnosticEngine> engineProvider;

    public ExternalApiCollector(ObjectProvider<DiagnosticEngine> engineProvider) {
        this.engineProvider = engineProvider;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof RestTemplate) {
            RestTemplate restTemplate = (RestTemplate) bean;
            List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(restTemplate.getInterceptors());
            interceptors.add(new RuntimeLensInterceptor(engineProvider));
            restTemplate.setInterceptors(interceptors);
        }
        return bean;
    }

    private static class RuntimeLensInterceptor implements ClientHttpRequestInterceptor {
        private final ObjectProvider<DiagnosticEngine> engineProvider;

        public RuntimeLensInterceptor(ObjectProvider<DiagnosticEngine> engineProvider) {
            this.engineProvider = engineProvider;
        }

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            long start = System.currentTimeMillis();
            try {
                ClientHttpResponse response = execution.execute(request, body);
                report(request, response, System.currentTimeMillis() - start);
                return response;
            } catch (IOException e) {
                // For now we don't report failed calls here, but we could
                throw e;
            }
        }

        private void report(HttpRequest request, ClientHttpResponse response, long duration) throws IOException {
            String rid = RequestContext.getRequestId();
            DiagnosticEngine engine = engineProvider.getIfAvailable();
            if (rid != null && engine != null) {
                engine.addEvent(new ExternalCallEvent(
                    rid,
                    request.getURI().toString(),
                    request.getMethod().name(),
                    response.getStatusCode().value(),
                    duration
                ));
            }
        }
    }
}
