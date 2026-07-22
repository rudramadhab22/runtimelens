package org.runtimelens.core.reporter;

import org.runtimelens.core.engine.DiagnosticEngine.AnalysisResult;
import org.runtimelens.core.engine.DiagnosticEngine.Finding;

public class ConsoleReporter implements Reporter {
    
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_CYAN = "\u001B[36m";

    @Override
    public void report(AnalysisResult result) {
        System.out.println(ANSI_CYAN + "--- RuntimeLens Diagnostic [" + result.getRequestId() + "] ---" + ANSI_RESET);
        System.out.println("Summary: " + result.getSummary());
        for (Finding finding : result.getFindings()) {
            String color = finding.getType().equals("EXCEPTION") ? ANSI_RED : ANSI_YELLOW;
            System.out.println(color + "  [!] " + finding.getType() + ": " + finding.getMessage() + ANSI_RESET);
        }
        System.out.println(ANSI_CYAN + "---------------------------------------------------" + ANSI_RESET);
    }
}
