package org.runtimelens.spring.collector;

import org.runtimelens.core.engine.DiagnosticEngine;
import org.runtimelens.core.engine.RequestContext;
import org.runtimelens.core.model.JdbcEvent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Statement;

public class JdbcCollector implements BeanPostProcessor {

    private final ObjectProvider<DiagnosticEngine> engineProvider;

    public JdbcCollector(ObjectProvider<DiagnosticEngine> engineProvider) {
        this.engineProvider = engineProvider;
    }

    private DiagnosticEngine getEngine() {
        return engineProvider.getIfAvailable();
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DataSource && !(Proxy.isProxyClass(bean.getClass()))) {
            return Proxy.newProxyInstance(
                bean.getClass().getClassLoader(),
                new Class[]{DataSource.class},
                new DataSourceInvocationHandler((DataSource) bean, engineProvider)
            );
        }
        return bean;
    }

    private static class DataSourceInvocationHandler implements InvocationHandler {
        private final DataSource delegate;
        private final ObjectProvider<DiagnosticEngine> engineProvider;

        public DataSourceInvocationHandler(DataSource delegate, ObjectProvider<DiagnosticEngine> engineProvider) {
            this.delegate = delegate;
            this.engineProvider = engineProvider;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object result = method.invoke(delegate, args);
            if ("getConnection".equals(method.getName()) && result instanceof Connection) {
                return Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class[]{Connection.class},
                    new ConnectionInvocationHandler((Connection) result, engineProvider)
                );
            }
            return result;
        }
    }

    private static class ConnectionInvocationHandler implements InvocationHandler {
        private final Connection delegate;
        private final ObjectProvider<DiagnosticEngine> engineProvider;

        public ConnectionInvocationHandler(Connection delegate, ObjectProvider<DiagnosticEngine> engineProvider) {
            this.delegate = delegate;
            this.engineProvider = engineProvider;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object result = method.invoke(delegate, args);
            if (("createStatement".equals(method.getName()) || "prepareStatement".equals(method.getName())) 
                && result instanceof Statement) {
                String sql = (args != null && args.length > 0 && args[0] instanceof String) ? (String) args[0] : "UNKNOWN";
                return Proxy.newProxyInstance(
                    Statement.class.getClassLoader(),
                    new Class[]{Statement.class, java.sql.PreparedStatement.class},
                    new StatementInvocationHandler((Statement) result, engineProvider, sql)
                );
            }
            return result;
        }
    }

    private static class StatementInvocationHandler implements InvocationHandler {
        private final Statement delegate;
        private final ObjectProvider<DiagnosticEngine> engineProvider;
        private final String sql;

        public StatementInvocationHandler(Statement delegate, ObjectProvider<DiagnosticEngine> engineProvider, String sql) {
            this.delegate = delegate;
            this.engineProvider = engineProvider;
            this.sql = sql;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String currentSql = sql;
            if ("execute".equals(method.getName()) || "executeQuery".equals(method.getName()) || "executeUpdate".equals(method.getName())) {
                if (args != null && args.length > 0 && args[0] instanceof String) {
                    currentSql = (String) args[0];
                }
                long start = System.currentTimeMillis();
                boolean success = false;
                try {
                    Object res = method.invoke(delegate, args);
                    success = true;
                    return res;
                } finally {
                    long duration = System.currentTimeMillis() - start;
                    String rid = RequestContext.getRequestId();
                    DiagnosticEngine engine = engineProvider.getIfAvailable();
                    if (rid != null && engine != null) {
                        engine.addEvent(new JdbcEvent(rid, currentSql, duration, success));
                    }
                }
            }
            return method.invoke(delegate, args);
        }
    }
}
