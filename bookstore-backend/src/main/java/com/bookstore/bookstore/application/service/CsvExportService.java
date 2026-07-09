package com.bookstore.bookstore.application.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CsvExportService {

    private static final char BOM = '\uFEFF';

    public byte[] export(List<String> headers, List<? extends List<?>> rows) {
        StringBuilder content = new StringBuilder();
        content.append(BOM);
        appendRow(content, headers);
        for (List<?> row : rows == null ? List.<List<?>>of() : rows) {
            appendRow(content, row);
        }
        return content.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void appendRow(StringBuilder content, List<?> values) {
        List<?> safeValues = values == null ? List.of() : values;
        for (int index = 0; index < safeValues.size(); index++) {
            if (index > 0) {
                content.append(',');
            }
            content.append(escape(safeValues.get(index)));
        }
        content.append("\r\n");
    }

    private String escape(Object value) {
        String text = value == null ? "" : value.toString();
        boolean requiresQuotes = text.contains(",")
                || text.contains("\"")
                || text.contains("\r")
                || text.contains("\n");
        if (!requiresQuotes) {
            return text;
        }
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}
