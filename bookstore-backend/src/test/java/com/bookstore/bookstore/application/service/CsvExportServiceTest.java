package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class CsvExportServiceTest {

    private final CsvExportService csvExportService = new CsvExportService();

    @Test
    void export_addsUtf8BomAndEscapesCommaQuotesAndNewLine() {
        byte[] content = csvExportService.export(
                List.of("Tiêu đề", "Ghi chú"),
                List.of(List.of("Mã,001", "Anh nói \"xin chào\"\nDòng 2"))
        );

        String csv = new String(content, StandardCharsets.UTF_8);

        assertTrue(csv.startsWith("\uFEFF"));
        assertEquals(
                "\uFEFFTiêu đề,Ghi chú\r\n\"Mã,001\",\"Anh nói \"\"xin chào\"\"\nDòng 2\"\r\n",
                csv
        );
    }
}
