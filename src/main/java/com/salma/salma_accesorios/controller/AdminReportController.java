package com.salma.salma_accesorios.controller;

import com.salma.salma_accesorios.model.OrderStatus;
import com.salma.salma_accesorios.repository.CustomerOrderRepository;
import com.salma.salma_accesorios.repository.ProductRepository;
import com.salma.salma_accesorios.repository.ReturnRequestRepository;
import com.salma.salma_accesorios.service.JasperReportService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/reportes")
public class AdminReportController {

    private final CustomerOrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ReturnRequestRepository returnRepository;

    public AdminReportController(
            CustomerOrderRepository orderRepository,
            ProductRepository productRepository,
            ReturnRequestRepository returnRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.returnRepository = returnRepository;
    }

    @GetMapping
    public String reports(Model model) {
        model.addAttribute("reports", List.of(
                Map.of("title", "Reporte de ventas", "description", "Pedidos, clientes, estados y totales.", "url", "/admin/reportes/ventas.pdf"),
                Map.of("title", "Reporte de stock", "description", "Inventario disponible, vendidos y alerta de bajo stock.", "url", "/admin/reportes/stock.pdf"),
                Map.of("title", "Reporte de devoluciones", "description", "Solicitudes de devolucion y seguimiento operativo.", "url", "/admin/reportes/devoluciones.pdf")
        ));
        return "admin/reportes";
    }

    @GetMapping("/ventas.pdf")
    public void salesReport(HttpServletResponse response) throws IOException {
        JasperReportService jasperReportService = new JasperReportService();
        var columns = List.of(
                new JasperReportService.ReportColumn("order", "Pedido", 58, JasperReportService.Align.LEFT),
                new JasperReportService.ReportColumn("date", "Fecha", 82, JasperReportService.Align.LEFT),
                new JasperReportService.ReportColumn("customer", "Cliente", 145, JasperReportService.Align.LEFT),
                new JasperReportService.ReportColumn("status", "Estado", 82, JasperReportService.Align.LEFT),
                new JasperReportService.ReportColumn("total", "Total", 104, JasperReportService.Align.RIGHT)
        );
        List<Map<String, ?>> rows = orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(order -> jasperReportService.row(
                        "order", "#" + order.getId(),
                        "date", order.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                        "customer", order.getUser().getEmail(),
                        "status", order.getStatus().name(),
                        "total", jasperReportService.money(order.getTotal())
                ))
                .collect(java.util.ArrayList::new, (list, row) -> list.add((Map<String, ?>) row), java.util.ArrayList::addAll);
        BigDecimal totalPaid = orderRepository.findAll().stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELADO)
                .map(order -> order.getTotal() == null ? BigDecimal.ZERO : order.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        byte[] pdf = jasperReportService.tableReport(
                "Reporte de ventas",
                "Ventas acumuladas: " + jasperReportService.money(totalPaid),
                columns,
                rows);
        writePdf(response, "reporte-ventas-salma.pdf", pdf);
    }

    @GetMapping("/stock.pdf")
    public void stockReport(HttpServletResponse response) throws IOException {
        JasperReportService jasperReportService = new JasperReportService();
        var columns = List.of(
                new JasperReportService.ReportColumn("product", "Producto", 174, JasperReportService.Align.LEFT),
                new JasperReportService.ReportColumn("category", "Categoria", 96, JasperReportService.Align.LEFT),
                new JasperReportService.ReportColumn("stock", "Stock", 62, JasperReportService.Align.CENTER),
                new JasperReportService.ReportColumn("sold", "Vendidos", 70, JasperReportService.Align.CENTER),
                new JasperReportService.ReportColumn("alert", "Alerta", 70, JasperReportService.Align.LEFT)
        );
        List<Map<String, ?>> rows = productRepository.findByActiveTrueOrderByFeaturedDescCreatedAtDesc().stream()
                .map(product -> jasperReportService.row(
                        "product", product.getName(),
                        "category", product.getCategory().getName(),
                        "stock", product.getStock(),
                        "sold", product.getSoldCount(),
                        "alert", product.getStock() <= 3 ? "Bajo" : "OK"
                ))
                .collect(java.util.ArrayList::new, (list, row) -> list.add((Map<String, ?>) row), java.util.ArrayList::addAll);
        byte[] pdf = jasperReportService.tableReport(
                "Reporte de stock",
                "Inventario activo y alerta de bajo stock",
                columns,
                rows);
        writePdf(response, "reporte-stock-salma.pdf", pdf);
    }

    @GetMapping("/devoluciones.pdf")
    public void returnsReport(HttpServletResponse response) throws IOException {
        JasperReportService jasperReportService = new JasperReportService();
        var columns = List.of(
                new JasperReportService.ReportColumn("id", "ID", 46, JasperReportService.Align.LEFT),
                new JasperReportService.ReportColumn("order", "Pedido", 60, JasperReportService.Align.LEFT),
                new JasperReportService.ReportColumn("customer", "Cliente", 140, JasperReportService.Align.LEFT),
                new JasperReportService.ReportColumn("status", "Estado", 92, JasperReportService.Align.LEFT),
                new JasperReportService.ReportColumn("reason", "Motivo", 134, JasperReportService.Align.LEFT)
        );
        List<Map<String, ?>> rows = returnRepository.findAll().stream()
                .map(request -> jasperReportService.row(
                        "id", "#" + request.getId(),
                        "order", "#" + request.getOrder().getId(),
                        "customer", request.getUser().getEmail(),
                        "status", request.getStatus().name(),
                        "reason", request.getReason()
                ))
                .collect(java.util.ArrayList::new, (list, row) -> list.add((Map<String, ?>) row), java.util.ArrayList::addAll);
        byte[] pdf = jasperReportService.tableReport(
                "Reporte de devoluciones",
                "Solicitudes registradas y estado actual",
                columns,
                rows);
        writePdf(response, "reporte-devoluciones-salma.pdf", pdf);
    }

    private void writePdf(HttpServletResponse response, String filename, byte[] pdf) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        response.setContentLength(pdf.length);
        response.getOutputStream().write(pdf);
    }
}
