package com.salma.salma_accesorios.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.util.List;
import com.salma.salma_accesorios.model.AppUser;
import com.salma.salma_accesorios.model.City;
import com.salma.salma_accesorios.model.CustomerOrder;
import com.salma.salma_accesorios.model.OrderItem;
import com.salma.salma_accesorios.model.OrderStatus;
import com.salma.salma_accesorios.model.Product;
import org.junit.jupiter.api.Test;

class JasperReportServiceTest {

    @Test
    void tableReportGeneratesStyledPdf() {
        JasperReportService service = new JasperReportService();
        List<JasperReportService.ReportColumn> columns = List.of(
                new JasperReportService.ReportColumn("producto", "Producto", 210, JasperReportService.Align.LEFT),
                new JasperReportService.ReportColumn("cantidad", "Cantidad", 80, JasperReportService.Align.CENTER),
                new JasperReportService.ReportColumn("total", "Total", 110, JasperReportService.Align.RIGHT)
        );
        var rows = List.of(
                service.row("producto", "Collar Lotus Rosado", "cantidad", "3", "total", "$ 135.000"),
                service.row("producto", "Pulsera Amar", "cantidad", "5", "total", "$ 125.000")
        );

        byte[] pdf = service.tableReport("Reporte de ventas", "Ventas por producto", columns, rows);
        String content = new String(pdf, StandardCharsets.ISO_8859_1);

        assertThat(content).startsWith("%PDF-");
        assertThat(content).contains("SALMA ACCESORIOS");
        assertThat(content).contains("Collar Lotus Rosado");
        assertThat(content).contains("/Kids [5 0 R");
    }

    @Test
    void invoicePdfKeepsTotalsInsidePage() {
        JasperReportService service = new JasperReportService();
        CustomerOrder order = new CustomerOrder();
        order.setId(14L);
        order.setStatus(OrderStatus.PAGADO);
        order.setShippingAddress("Av 50 # 8-90");
        order.setShippingCost(new BigDecimal("12000"));
        order.setTotal(new BigDecimal("67000"));
        City city = new City();
        city.setName("Barranquilla");
        AppUser user = new AppUser();
        user.setFirstName("Maria");
        user.setFirstLastName("Calderon");
        user.setEmail("maria@example.com");
        user.setPhone("3108905535");
        user.setCity(city);
        order.setUser(user);
        Product product = new Product();
        product.setName("Set pulseras coloridas");
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal("55000"));
        order.getItems().add(item);

        byte[] pdf = service.invoice(order, new BigDecimal("55000"));
        String content = new String(pdf, StandardCharsets.ISO_8859_1);

        assertThat(content).startsWith("%PDF-");
        assertThat(content).contains("Set pulseras coloridas");
        assertThat(content).contains("Subtotal");
        assertThat(content).contains("$ 67.000");
    }

    @Test
    void invoicePdfPlacesTotalsBelowSeveralItems() {
        JasperReportService service = new JasperReportService();
        CustomerOrder order = sampleOrder();
        order.getItems().add(item(order, "Collar Xio", "80000", 3));
        order.getItems().add(item(order, "Pulsera Rosa Serena", "25000", 5));
        order.getItems().add(item(order, "pulsera amar", "25000", 1));
        order.setShippingCost(BigDecimal.ZERO);
        order.setTotal(new BigDecimal("365000"));

        byte[] pdf = service.invoice(order, new BigDecimal("365000"));
        String content = new String(pdf, StandardCharsets.ISO_8859_1);

        assertThat(content).contains("334 406 219 86 re f");
        assertThat(content).contains("Collar Xio");
        assertThat(content).contains("Pulsera Rosa Serena");
        assertThat(content).contains("Total pagado");
    }

    private CustomerOrder sampleOrder() {
        CustomerOrder order = new CustomerOrder();
        order.setId(17L);
        order.setStatus(OrderStatus.PAGADO);
        order.setShippingAddress("av 50 # 8-90");
        City city = new City();
        city.setName("Cali");
        AppUser user = new AppUser();
        user.setFirstName("Salma");
        user.setFirstLastName("Mojica");
        user.setEmail("salma@example.com");
        user.setPhone("3125689455");
        user.setCity(city);
        order.setUser(user);
        return order;
    }

    private OrderItem item(CustomerOrder order, String productName, String unitPrice, int quantity) {
        Product product = new Product();
        product.setName(productName);
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitPrice(new BigDecimal(unitPrice));
        return item;
    }
}
