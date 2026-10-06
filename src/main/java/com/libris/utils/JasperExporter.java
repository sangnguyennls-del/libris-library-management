package com.libris.utils;

import com.libris.model.Book;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import javax.swing.*;
import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class JasperExporter {

    /**
     * Xuất danh sách sách ra PDF bằng JasperReports rồi mở file
     */
    public static void exportBooks(Component parent, List<Book> books) {
        try {
            File pdf = File.createTempFile("Libris_DanhSachSach_", ".pdf");
            writeBooksPdf(books, pdf);
            Desktop.getDesktop().open(pdf);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent, "Lỗi xuất báo cáo PDF: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void writeBooksPdf(List<Book> books, File out) throws Exception {
        try (InputStream jrxml = JasperExporter.class.getResourceAsStream("/reports/books.jrxml")) {
            JasperReport report = JasperCompileManager.compileReport(jrxml);
            JasperPrint print = JasperFillManager.fillReport(report,
                    new HashMap<>(Map.of(JRParameter.REPORT_LOCALE, Locale.of("vi", "VN"))), new JRBeanCollectionDataSource(books));
            JasperExportManager.exportReportToPdfFile(print, out.getAbsolutePath());
        }
    }
}
