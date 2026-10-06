package com.libris.unit;

import com.libris.model.Book;
import com.libris.utils.JasperExporter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class JasperExporterTest {

    @Test
    void writesBooksPdfWithVietnameseFont() throws Exception {
        List<Book> books = List.of(
                new Book(1, "B001", "Dế Mèn Phiêu Lưu Ký", "Tô Hoài", "Thiếu nhi", "Kim Đồng", 2019, "Available", 10000, 50000),
                new Book(2, "B002", "Lão Hạc", "Nam Cao", "Văn học", "Giáo Dục", 2018, "Rented", 12000, 50000));
        File out = File.createTempFile("libris_test_", ".pdf");
        out.deleteOnExit();

        JasperExporter.writeBooksPdf(books, out);

        String pdf = new String(Files.readAllBytes(out.toPath()), StandardCharsets.ISO_8859_1);
        assertTrue(pdf.startsWith("%PDF"), "Không phải file PDF");
        assertTrue(pdf.contains("DejaVu"), "Font DejaVu (hỗ trợ tiếng Việt) chưa được nhúng");
    }
}
