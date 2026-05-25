package com.salma.salma_accesorios.service;

import com.salma.salma_accesorios.model.CustomerOrder;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class JasperReportService {

    public enum Align {
        LEFT, CENTER, RIGHT
    }

    public record ReportColumn(String key, String title, int width, Align align) {
    }

    public byte[] tableReport(String reportName, String subtitle, List<ReportColumn> columns, List<? extends Map<String, ?>> rows) {
        Map<String, Object> params = baseParams(reportName, subtitle);
        String jrxml = reportTemplate(
                reportName.replaceAll("\\W+", "_"),
                columns,
                titleBandXml(false),
                "",
                false);
        return export(jrxml, params, rows, columns);
    }

    public byte[] invoice(CustomerOrder order, BigDecimal subtotal) {
        List<ReportColumn> columns = List.of(
                new ReportColumn("product", "Producto", 220, Align.LEFT),
                new ReportColumn("qty", "Cant.", 55, Align.CENTER),
                new ReportColumn("unit", "Unitario", 90, Align.RIGHT),
                new ReportColumn("total", "Total", 95, Align.RIGHT)
        );
        List<Map<String, ?>> rows = order.getItems().stream()
                .map(item -> {
                    BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("product", clean(item.getProduct().getName()));
                    row.put("qty", String.valueOf(item.getQuantity()));
                    row.put("unit", money(item.getUnitPrice()));
                    row.put("total", money(lineTotal));
                    return row;
                })
                .collect(ArrayList::new, (list, row) -> list.add((Map<String, ?>) row), ArrayList::addAll);

        Map<String, Object> params = baseParams("SALMA ACCESORIOS", "Factura pedido #" + order.getId());
        params.put("CUSTOMER_NAME", clean(order.getUser().getFirstName() + " " + order.getUser().getFirstLastName()));
        params.put("CUSTOMER_EMAIL", clean(order.getUser().getEmail()));
        params.put("CUSTOMER_PHONE", clean(order.getUser().getPhone()));
        params.put("SHIPPING_ADDRESS", clean(order.getShippingAddress() + " - " + order.getUser().getCity().getName()));
        params.put("ORDER_STATUS", clean(order.getStatus().name()));
        params.put("SUBTOTAL", money(subtotal));
        params.put("SHIPPING", order.getShippingCost().signum() == 0 ? "Gratis" : money(order.getShippingCost()));
        params.put("TOTAL", money(order.getTotal()));

        String summary = """
                <summary>
                    <band height="150">
                        <line><reportElement x="292" y="18" width="219" height="1" forecolor="#E6DDD3"/></line>
                        <staticText><reportElement x="312" y="34" width="90" height="16" forecolor="#6F6762"/><textElement verticalAlignment="Middle"/><text><![CDATA[Subtotal]]></text></staticText>
                        <textField><reportElement x="414" y="34" width="88" height="16" forecolor="#111111"/><textElement textAlignment="Right" verticalAlignment="Middle"/><textFieldExpression><![CDATA[$P{SUBTOTAL}]]></textFieldExpression></textField>
                        <staticText><reportElement x="312" y="56" width="90" height="16" forecolor="#6F6762"/><textElement verticalAlignment="Middle"/><text><![CDATA[Envio]]></text></staticText>
                        <textField><reportElement x="414" y="56" width="88" height="16" forecolor="#111111"/><textElement textAlignment="Right" verticalAlignment="Middle"/><textFieldExpression><![CDATA[$P{SHIPPING}]]></textFieldExpression></textField>
                        <rectangle><reportElement x="292" y="84" width="219" height="32" mode="Opaque" backcolor="#FBF8F3" forecolor="#E6DDD3"/></rectangle>
                        <staticText><reportElement x="312" y="94" width="100" height="16" forecolor="#111111"/><textElement verticalAlignment="Middle"/><text><![CDATA[Total pagado]]></text></staticText>
                        <textField><reportElement x="414" y="94" width="88" height="16" forecolor="#39746F"/><textElement textAlignment="Right" verticalAlignment="Middle"/><textFieldExpression><![CDATA[$P{TOTAL}]]></textFieldExpression></textField>
                        <staticText><reportElement x="18" y="126" width="420" height="14" forecolor="#6F6762"/><textElement verticalAlignment="Middle"/><text><![CDATA[Gracias por tu compra. Conserva esta factura como soporte de pago.]]></text></staticText>
                    </band>
                </summary>
                """;

        String jrxml = reportTemplate(
                "factura_salma_" + order.getId(),
                columns,
                titleBandXml(true),
                summary,
                true);
            return export(jrxml, params, rows, columns);
    }

    public Map<String, String> row(Object... values) {
        Map<String, String> row = new LinkedHashMap<>();
        for (int index = 0; index < values.length; index += 2) {
            row.put(String.valueOf(values[index]), clean(String.valueOf(values[index + 1])));
        }
        return row;
    }

    public String money(BigDecimal value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator('.');
        DecimalFormat format = new DecimalFormat("#,##0", symbols);
        return "$ " + format.format(value == null ? BigDecimal.ZERO : value);
    }

    private byte[] export(String jrxml, Map<String, Object> params, List<? extends Map<String, ?>> rows, List<ReportColumn> columns) {
        try {
            Class<?> compileManager = Class.forName("net.sf.jasperreports.engine.JasperCompileManager");
            Class<?> fillManager = Class.forName("net.sf.jasperreports.engine.JasperFillManager");
            Class<?> exportManager = Class.forName("net.sf.jasperreports.engine.JasperExportManager");
            Class<?> dataSourceType = Class.forName("net.sf.jasperreports.engine.data.JRMapCollectionDataSource");
            Class<?> reportType = Class.forName("net.sf.jasperreports.engine.JasperReport");
            Class<?> jrDataSourceType = Class.forName("net.sf.jasperreports.engine.JRDataSource");
            Class<?> printType = Class.forName("net.sf.jasperreports.engine.JasperPrint");

            Method compileReport = compileManager.getMethod("compileReport", java.io.InputStream.class);
            Object report = compileReport.invoke(null, new ByteArrayInputStream(jrxml.getBytes(StandardCharsets.UTF_8)));
            Constructor<?> dataSourceConstructor = dataSourceType.getConstructor(java.util.Collection.class);
            Object dataSource = dataSourceConstructor.newInstance(new ArrayList<>(rows));
            Method fillReport = fillManager.getMethod("fillReport", reportType, Map.class, jrDataSourceType);
            Object print = fillReport.invoke(null, report, params, dataSource);
            Method exportReportToPdf = exportManager.getMethod("exportReportToPdf", printType);
            return (byte[]) exportReportToPdf.invoke(null, print);
        } catch (ReflectiveOperationException exception) {
            return simplePdf(params, rows, columns);
        }
    }

    private byte[] simplePdf(Map<String, Object> params, List<? extends Map<String, ?>> rows, List<ReportColumn> columns) {
        boolean invoice = params.containsKey("CUSTOMER_NAME");
        int firstPageRows = invoice ? 13 : 22;
        int nextPageRows = 24;
        List<List<? extends Map<String, ?>>> pages = new ArrayList<>();
        int index = 0;
        while (index < rows.size()) {
            int pageSize = pages.isEmpty() ? firstPageRows : nextPageRows;
            int end = Math.min(index + pageSize, rows.size());
            pages.add(rows.subList(index, end));
            index = end;
        }
        if (pages.isEmpty()) {
            pages.add(List.of());
        }

        List<String> pageContents = new ArrayList<>();
        for (int page = 0; page < pages.size(); page++) {
            pageContents.add(pageContent(params, pages.get(page), columns, page + 1, pages.size(), invoice, page == pages.size() - 1));
        }
        return pdfDocument(pageContents);
    }

    private String pageContent(Map<String, Object> params, List<? extends Map<String, ?>> rows, List<ReportColumn> columns,
                               int pageNumber, int pageCount, boolean invoice, boolean lastPage) {
        StringBuilder content = new StringBuilder();
        content.append(rect(0, 0, 595, 842, "1 1 1"));
        content.append(rect(42, 770, 511, 46, "0.623 0.847 0.824"));
        content.append(text(String.valueOf(params.getOrDefault("REPORT_TITLE", "SALMA ACCESORIOS")), 58, 794, 18, true, "0.067 0.067 0.067", Align.LEFT, 320));
        content.append(text(String.valueOf(params.getOrDefault("REPORT_SUBTITLE", "")), 58, 778, 10, false, "0.435 0.404 0.384", Align.LEFT, 330));
        content.append(text(String.valueOf(params.getOrDefault("GENERATED_AT", "")), 540, 778, 9, false, "0.435 0.404 0.384", Align.RIGHT, 170));
        content.append(line(42, 754, 553, 754, "0.902 0.867 0.827"));

        int tableTop = 710;
        if (invoice) {
            content.append(infoBox(58, 676, 220, "Cliente",
                    List.of(String.valueOf(params.get("CUSTOMER_NAME")), String.valueOf(params.get("CUSTOMER_EMAIL")),
                            String.valueOf(params.get("CUSTOMER_PHONE")))));
            content.append(infoBox(314, 676, 220, "Envio",
                    List.of(String.valueOf(params.get("SHIPPING_ADDRESS")), "Estado: " + params.get("ORDER_STATUS"))));
            tableTop = 620;
        }

        content.append(tableHeader(columns, tableTop));
        int y = tableTop - 26;
        int rowIndex = 0;
        for (Map<String, ?> row : rows) {
            content.append(tableRow(columns, row, y, rowIndex % 2 == 0));
            y -= 26;
            rowIndex++;
        }

        if (invoice && lastPage) {
            content.append(totalsBox(params, Math.max(110, y - 110)));
        }

        content.append(line(42, 54, 553, 54, "0.902 0.867 0.827"));
        content.append(text("SALMA ACCESORIOS", 42, 36, 8, true, "0.067 0.067 0.067", Align.LEFT, 160));
        content.append(text("WhatsApp +57 3212292317 | original designs", 297, 36, 8, false, "0.435 0.404 0.384", Align.CENTER, 250));
        content.append(text("Pagina " + pageNumber + " de " + pageCount, 553, 36, 8, false, "0.435 0.404 0.384", Align.RIGHT, 100));
        return content.toString();
    }

    private byte[] pdfDocument(List<String> pageContents) {
        List<String> objects = new ArrayList<>();
        objects.add("<< /Type /Catalog /Pages 2 0 R >>");
        StringBuilder kids = new StringBuilder();
        for (int page = 0; page < pageContents.size(); page++) {
            kids.append(5 + page * 2).append(" 0 R ");
        }
        objects.add("<< /Type /Pages /Kids [" + kids + "] /Count " + pageContents.size() + " >>");
        objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>");
        objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>");
        for (int page = 0; page < pageContents.size(); page++) {
            int contentObject = 6 + page * 2;
            byte[] stream = pageContents.get(page).getBytes(StandardCharsets.UTF_8);
            objects.add("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents " + contentObject + " 0 R >>");
            objects.add("<< /Length " + stream.length + " >>\nstream\n" + pageContents.get(page) + "endstream");
        }
        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        for (int index = 0; index < objects.size(); index++) {
            offsets.add(pdf.toString().getBytes(StandardCharsets.UTF_8).length);
            pdf.append(index + 1).append(" 0 obj\n").append(objects.get(index)).append("\nendobj\n");
        }
        int xref = pdf.toString().getBytes(StandardCharsets.UTF_8).length;
        pdf.append("xref\n0 ").append(objects.size() + 1).append("\n");
        pdf.append("0000000000 65535 f \n");
        for (Integer offset : offsets) {
            pdf.append(String.format("%010d 00000 n \n", offset));
        }
        pdf.append("trailer\n<< /Size ").append(objects.size() + 1).append(" /Root 1 0 R >>\n");
        pdf.append("startxref\n").append(xref).append("\n%%EOF");
        return pdf.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String infoBox(int x, int y, int width, String title, List<String> values) {
        StringBuilder content = new StringBuilder();
        content.append(rect(x, y, width, 64, "0.984 0.973 0.953"));
        content.append(strokeRect(x, y, width, 64, "0.902 0.867 0.827"));
        content.append(text(title, x + 12, y + 44, 10, true, "0.223 0.455 0.435", Align.LEFT, width - 24));
        int lineY = y + 28;
        for (String value : values) {
            content.append(text(value, x + 12, lineY, 8, false, "0.067 0.067 0.067", Align.LEFT, width - 24));
            lineY -= 13;
        }
        return content.toString();
    }

    private String tableHeader(List<ReportColumn> columns, int y) {
        StringBuilder content = new StringBuilder();
        content.append(rect(42, y, 511, 24, "0.067 0.067 0.067"));
        int x = 52;
        for (ReportColumn column : columns) {
            int width = scaledWidth(column);
            content.append(text(column.title(), x, y + 8, 8, true, "1 1 1", column.align(), width - 8));
            x += width;
        }
        return content.toString();
    }

    private String tableRow(List<ReportColumn> columns, Map<String, ?> row, int y, boolean soft) {
        StringBuilder content = new StringBuilder();
        content.append(rect(42, y, 511, 26, soft ? "0.984 0.973 0.953" : "1 1 1"));
        content.append(line(42, y, 553, y, "0.902 0.867 0.827"));
        int x = 52;
        for (ReportColumn column : columns) {
            int width = scaledWidth(column);
            Object value = row.containsKey(column.key()) ? row.get(column.key()) : "";
            content.append(text(String.valueOf(value), x, y + 9, 8, false, "0.067 0.067 0.067", column.align(), width - 8));
            x += width;
        }
        return content.toString();
    }

    private String totalsBox(Map<String, Object> params, int y) {
        StringBuilder content = new StringBuilder();
        content.append(rect(334, y, 219, 86, "0.984 0.973 0.953"));
        content.append(strokeRect(334, y, 219, 86, "0.902 0.867 0.827"));
        content.append(text("Subtotal", 350, y + 60, 10, false, "0.435 0.404 0.384", Align.LEFT, 80));
        content.append(text(String.valueOf(params.get("SUBTOTAL")), 435, y + 60, 10, false, "0.067 0.067 0.067", Align.RIGHT, 100));
        content.append(text("Envio", 350, y + 39, 10, false, "0.435 0.404 0.384", Align.LEFT, 80));
        content.append(text(String.valueOf(params.get("SHIPPING")), 435, y + 39, 10, false, "0.067 0.067 0.067", Align.RIGHT, 100));
        content.append(line(350, y + 28, 535, y + 28, "0.902 0.867 0.827"));
        content.append(text("Total pagado", 350, y + 12, 12, true, "0.067 0.067 0.067", Align.LEFT, 100));
        content.append(text(String.valueOf(params.get("TOTAL")), 435, y + 12, 12, true, "0.223 0.455 0.435", Align.RIGHT, 100));
        return content.toString();
    }

    private int scaledWidth(ReportColumn column) {
        return Math.max(45, Math.round(column.width() * 491f / 511f));
    }

    private String rect(int x, int y, int width, int height, String rgb) {
        return "q " + rgb + " rg " + x + " " + y + " " + width + " " + height + " re f Q\n";
    }

    private String strokeRect(int x, int y, int width, int height, String rgb) {
        return "q " + rgb + " RG 0.8 w " + x + " " + y + " " + width + " " + height + " re S Q\n";
    }

    private String line(int x1, int y1, int x2, int y2, String rgb) {
        return "q " + rgb + " RG 0.8 w " + x1 + " " + y1 + " m " + x2 + " " + y2 + " l S Q\n";
    }

    private String text(String value, int x, int y, int size, boolean bold, String rgb, Align align, int width) {
        String cleanValue = truncate(clean(value), width, size);
        int textX = switch (align) {
            case CENTER -> x + Math.max(0, (width - approximateTextWidth(cleanValue, size)) / 2);
            case RIGHT -> x + Math.max(0, width - approximateTextWidth(cleanValue, size));
            default -> x;
        };
        return "BT\n" + rgb + " rg\n/" + (bold ? "F2" : "F1") + " " + size + " Tf\n" + textX + " " + y + " Td\n(" + pdfText(cleanValue) + ") Tj\nET\n";
    }

    private String truncate(String value, int width, int size) {
        String normalized = clean(value);
        int maxCharacters = Math.max(6, width / Math.max(4, size / 2));
        if (normalized.length() <= maxCharacters) {
            return normalized;
        }
        return normalized.substring(0, Math.max(0, maxCharacters - 3)) + "...";
    }

    private int approximateTextWidth(String value, int size) {
        return clean(value).length() * Math.max(4, size / 2);
    }

    private String reportTemplate(String name, List<ReportColumn> columns, String title, String summary, boolean invoice) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <jasperReport xmlns="http://jasperreports.sourceforge.net/jasperreports"
                    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:schemaLocation="http://jasperreports.sourceforge.net/jasperreports https://jasperreports.sourceforge.net/xsd/jasperreport.xsd"
                    name="%s" pageWidth="595" pageHeight="842" columnWidth="511" leftMargin="42" rightMargin="42" topMargin="28" bottomMargin="28">
                    <property name="net.sf.jasperreports.default.font.name" value="SansSerif"/>
                    %s
                    %s
                    %s
                    <title>%s</title>
                    %s
                    %s
                    %s
                </jasperReport>
                """.formatted(
                xml(name),
                parameters(invoice),
                fields(columns),
                styleXml(),
                title,
                tableHeaderXml(columns),
                detailXml(columns),
                summary.isBlank() ? pageFooterXml() : summary + pageFooterXml());
    }

    private String parameters(boolean invoice) {
        List<String> names = new ArrayList<>(List.of("REPORT_TITLE", "REPORT_SUBTITLE", "GENERATED_AT"));
        if (invoice) {
            names.addAll(List.of("CUSTOMER_NAME", "CUSTOMER_EMAIL", "CUSTOMER_PHONE", "SHIPPING_ADDRESS",
                    "ORDER_STATUS", "SUBTOTAL", "SHIPPING", "TOTAL"));
        }
        StringBuilder builder = new StringBuilder();
        for (String name : names) {
            builder.append("<parameter name=\"").append(name).append("\" class=\"java.lang.String\"/>\n");
        }
        return builder.toString();
    }

    private String fields(List<ReportColumn> columns) {
        StringBuilder builder = new StringBuilder();
        for (ReportColumn column : columns) {
            builder.append("<field name=\"").append(xml(column.key())).append("\" class=\"java.lang.String\"/>\n");
        }
        return builder.toString();
    }

    private String styleXml() {
        return """
                <style name="Base" isDefault="true" fontName="SansSerif" fontSize="10" pdfFontName="Helvetica" pdfEncoding="Cp1252"/>
                """;
    }

    private String titleBandXml(boolean invoice) {
        String base = """
                <band height="%d">
                    <rectangle><reportElement x="0" y="0" width="511" height="42" mode="Opaque" backcolor="#9FD8D2" forecolor="#9FD8D2"/></rectangle>
                    <textField><reportElement x="18" y="8" width="300" height="28" forecolor="#111111"/><textElement verticalAlignment="Middle"><font size="18" isBold="true"/></textElement><textFieldExpression><![CDATA[$P{REPORT_TITLE}]]></textFieldExpression></textField>
                    <textField><reportElement x="20" y="42" width="330" height="18" forecolor="#6F6762"/><textElement verticalAlignment="Middle"/><textFieldExpression><![CDATA[$P{REPORT_SUBTITLE}]]></textFieldExpression></textField>
                    <textField><reportElement x="350" y="42" width="145" height="18" forecolor="#6F6762"/><textElement textAlignment="Right" verticalAlignment="Middle"/><textFieldExpression><![CDATA[$P{GENERATED_AT}]]></textFieldExpression></textField>
                    %s
                    <line><reportElement x="0" y="%d" width="511" height="1" forecolor="#E6DDD3"/></line>
                </band>
                """;
        if (!invoice) {
            return base.formatted(118, "", 88);
        }
        String invoiceInfo = """
                    <staticText><reportElement x="18" y="104" width="90" height="16" forecolor="#39746F"/><textElement verticalAlignment="Middle"><font isBold="true"/></textElement><text><![CDATA[Cliente]]></text></staticText>
                    <textField><reportElement x="18" y="122" width="220" height="16" forecolor="#111111"/><textElement verticalAlignment="Middle"/><textFieldExpression><![CDATA[$P{CUSTOMER_NAME}]]></textFieldExpression></textField>
                    <textField><reportElement x="18" y="138" width="220" height="14" forecolor="#6F6762"/><textElement verticalAlignment="Middle"/><textFieldExpression><![CDATA[$P{CUSTOMER_EMAIL}]]></textFieldExpression></textField>
                    <textField><reportElement x="18" y="153" width="220" height="14" forecolor="#6F6762"/><textElement verticalAlignment="Middle"/><textFieldExpression><![CDATA[$P{CUSTOMER_PHONE}]]></textFieldExpression></textField>
                    <staticText><reportElement x="284" y="104" width="90" height="16" forecolor="#39746F"/><textElement verticalAlignment="Middle"><font isBold="true"/></textElement><text><![CDATA[Envio]]></text></staticText>
                    <textField textAdjust="StretchHeight"><reportElement x="284" y="122" width="210" height="32" forecolor="#111111"/><textElement verticalAlignment="Middle"/><textFieldExpression><![CDATA[$P{SHIPPING_ADDRESS}]]></textFieldExpression></textField>
                    <rectangle><reportElement x="386" y="62" width="92" height="22" mode="Opaque" backcolor="#FBF8F3" forecolor="#39746F"/></rectangle>
                    <textField><reportElement x="386" y="67" width="92" height="14" forecolor="#39746F"/><textElement textAlignment="Center" verticalAlignment="Middle"><font isBold="true"/></textElement><textFieldExpression><![CDATA[$P{ORDER_STATUS}]]></textFieldExpression></textField>
                """;
        return base.formatted(190, invoiceInfo, 170);
    }

    private String tableHeaderXml(List<ReportColumn> columns) {
        StringBuilder items = new StringBuilder();
        int x = 10;
        for (ReportColumn column : columns) {
            items.append("""
                        <staticText><reportElement x="%d" y="6" width="%d" height="14" forecolor="#FFFFFF"/><textElement textAlignment="%s" verticalAlignment="Middle"><font size="9" isBold="true"/></textElement><text><![CDATA[%s]]></text></staticText>
                    """.formatted(x, column.width(), align(column.align()), clean(column.title())));
            x += column.width();
        }
        return """
                <pageHeader>
                    <band height="28">
                        <rectangle><reportElement x="0" y="0" width="511" height="24" mode="Opaque" backcolor="#111111" forecolor="#111111"/></rectangle>
                        %s
                    </band>
                </pageHeader>
                """.formatted(items);
    }

    private String detailXml(List<ReportColumn> columns) {
        StringBuilder items = new StringBuilder();
        int x = 10;
        for (ReportColumn column : columns) {
            items.append("""
                        <textField><reportElement x="%d" y="6" width="%d" height="14" forecolor="#111111"/><textElement textAlignment="%s" verticalAlignment="Middle"><font size="9"/></textElement><textFieldExpression><![CDATA[$F{%s}]]></textFieldExpression></textField>
                    """.formatted(x, column.width(), align(column.align()), column.key()));
            x += column.width();
        }
        return """
                <detail>
                    <band height="26">
                        <line><reportElement x="0" y="24" width="511" height="1" forecolor="#E6DDD3"/></line>
                        %s
                    </band>
                </detail>
                """.formatted(items);
    }

    private String pageFooterXml() {
        return """
                <pageFooter>
                    <band height="32">
                        <line><reportElement x="0" y="2" width="511" height="1" forecolor="#E6DDD3"/></line>
                        <staticText><reportElement x="0" y="12" width="130" height="12" forecolor="#111111"/><textElement verticalAlignment="Middle"><font size="8" isBold="true"/></textElement><text><![CDATA[SALMA ACCESORIOS]]></text></staticText>
                        <staticText><reportElement x="158" y="12" width="230" height="12" forecolor="#6F6762"/><textElement textAlignment="Center" verticalAlignment="Middle"><font size="8"/></textElement><text><![CDATA[WhatsApp +57 3212292317 | original designs]]></text></staticText>
                        <textField><reportElement x="430" y="12" width="80" height="12" forecolor="#6F6762"/><textElement textAlignment="Right" verticalAlignment="Middle"><font size="8"/></textElement><textFieldExpression><![CDATA["Pagina " + $V{PAGE_NUMBER}]]></textFieldExpression></textField>
                    </band>
                </pageFooter>
                """;
    }

    private Map<String, Object> baseParams(String title, String subtitle) {
        Map<String, Object> params = new HashMap<>();
        params.put("REPORT_TITLE", clean(title));
        params.put("REPORT_SUBTITLE", clean(subtitle));
        params.put("GENERATED_AT", "Generado " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        return params;
    }

    private String align(Align align) {
        return switch (align) {
            case CENTER -> "Center";
            case RIGHT -> "Right";
            default -> "Left";
        };
    }

    private String clean(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^\\x20-\\x7E]", "");
    }

    private String xml(String value) {
        return clean(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String pdfText(String value) {
        return clean(value)
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }
}
