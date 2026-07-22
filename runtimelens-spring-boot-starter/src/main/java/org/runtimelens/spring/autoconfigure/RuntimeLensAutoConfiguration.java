package org.runtimelens.spring.autoconfigure;

import org.runtimelens.core.engine.DiagnosticEngine;
import org.runtimelens.core.reporter.ConsoleReporter;
import org.runtimelens.core.reporter.FileReporter;
import org.runtimelens.core.reporter.Reporter;
import org.runtimelens.spring.collector.ExceptionCollector;
import org.runtimelens.spring.collector.ExternalApiCollector;
import org.runtimelens.spring.collector.HttpCollectorFilter;
import org.runtimelens.spring.collector.JdbcCollector;
import org.runtimelens.spring.collector.JvmCollector;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@AutoConfiguration
public class RuntimeLensAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DiagnosticEngine diagnosticEngine(List<Reporter> reporters) {
        return new DiagnosticEngine(reporters);
    }

    @Bean
    @ConditionalOnMissingBean
    public ConsoleReporter consoleReporter() {
        return new ConsoleReporter();
    }

    @Bean
    @ConditionalOnMissingBean
    public FileReporter fileReporter() {
        return new FileReporter();
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public HttpCollectorFilter httpCollectorFilter(DiagnosticEngine engine) {
        return new HttpCollectorFilter(engine);
    }

    @Bean
    public static JdbcCollector jdbcCollector(ObjectProvider<DiagnosticEngine> engineProvider) {
        return new JdbcCollector(engineProvider);
    }

    @Bean
    public ExceptionCollector exceptionCollector(DiagnosticEngine engine) {
        return new ExceptionCollector(engine);
    }

    @Bean
    public static ExternalApiCollector externalApiCollector(ObjectProvider<DiagnosticEngine> engineProvider) {
        return new ExternalApiCollector(engineProvider);
    }

    @Bean(destroyMethod = "stop")
    public JvmCollector jvmCollector(DiagnosticEngine engine) {
        return new JvmCollector(engine);
    }
}
