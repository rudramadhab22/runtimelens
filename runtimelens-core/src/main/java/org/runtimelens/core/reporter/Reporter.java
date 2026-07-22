package org.runtimelens.core.reporter;

import org.runtimelens.core.engine.DiagnosticEngine.AnalysisResult;

public interface Reporter {
    void report(AnalysisResult result);
}
