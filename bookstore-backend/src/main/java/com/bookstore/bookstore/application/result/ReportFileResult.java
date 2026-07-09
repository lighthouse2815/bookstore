package com.bookstore.bookstore.application.result;

public record ReportFileResult(
        String filename,
        byte[] content
) {
    public ReportFileResult {
        filename = filename == null || filename.isBlank() ? "report.csv" : filename;
        content = content == null ? new byte[0] : content.clone();
    }
}
