package org.runtimelens.core.reporter;

import org.runtimelens.core.engine.DiagnosticEngine.AnalysisResult;
import org.runtimelens.core.engine.DiagnosticEngine.Finding;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public class FileReporter implements Reporter {
    
    private static final String LOG_FILE = "runtimelens.log";

    @Override
    public void report(AnalysisResult result) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            
            pw.println("Timestamp: " + LocalDateTime.now());
            pw.println("Request ID: " + result.getRequestId());
            pw.println("Summary: " + result.getSummary());
            for (Finding finding : result.getFindings()) {
                pw.println("  [!] " + finding.getType() + ": " + finding.getMessage());
            }
            pw.println("---------------------------------------------------");
        } catch (IOException e) {
            System.err.println("RuntimeLens: Failed to write to log file: " + e.getMessage());
        }
    }
}
