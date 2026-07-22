package org.runtimelens.spring.collector;

import org.runtimelens.core.engine.DiagnosticEngine;
import org.runtimelens.core.model.JvmEvent;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JvmCollector {

    private final DiagnosticEngine engine;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "runtimelens-jvm-collector");
        t.setDaemon(true);
        return t;
    });

    public JvmCollector(DiagnosticEngine engine) {
        this.engine = engine;
        this.scheduler.scheduleAtFixedRate(this::collect, 0, 1, TimeUnit.MINUTES);
    }

    private void collect() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();
        
        long used = memoryMXBean.getHeapMemoryUsage().getUsed();
        long max = memoryMXBean.getHeapMemoryUsage().getMax();
        double load = osMXBean.getSystemLoadAverage();
        
        engine.addEvent(new JvmEvent(used, max, load));
    }
    
    public void stop() {
        scheduler.shutdown();
    }
}
