package com.salma.salma_accesorios.controller;

import com.salma.salma_accesorios.dto.ProductRequest;
import com.salma.salma_accesorios.dto.SiteContentRequest;
import com.salma.salma_accesorios.model.OrderStatus;
import com.salma.salma_accesorios.model.ReturnStatus;
import com.salma.salma_accesorios.repository.AppUserRepository;
import com.salma.salma_accesorios.repository.CategoryRepository;
import com.salma.salma_accesorios.repository.CustomerOrderRepository;
import com.salma.salma_accesorios.repository.ProductRepository;
import com.salma.salma_accesorios.repository.ReviewRepository;
import com.salma.salma_accesorios.repository.ReturnRequestRepository;
import com.salma.salma_accesorios.service.AdminStatsService;
import com.salma.salma_accesorios.service.ProductImageStorageService;
import com.salma.salma_accesorios.service.ProductService;
import com.salma.salma_accesorios.service.ReviewService;
import com.salma.salma_accesorios.service.SiteContentService;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminStatsService statsService;
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final AppUserRepository userRepository;
    private final CustomerOrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final ReturnRequestRepository returnRepository;
    private final ReviewService reviewService;
    private final ProductImageStorageService productImageStorageService;
    private final SiteContentService siteContentService;

    public AdminController(
            AdminStatsService statsService,
            ProductService productService,
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            AppUserRepository userRepository,
            CustomerOrderRepository orderRepository,
            ReviewRepository reviewRepository,
            ReturnRequestRepository returnRepository,
            ReviewService reviewService,
            ProductImageStorageService productImageStorageService,
            SiteContentService siteContentService) {
        this.statsService = statsService;
        this.productService = productService;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.returnRepository = returnRepository;
        this.reviewService = reviewService;
        this.productImageStorageService = productImageStorageService;
        this.siteContentService = siteContentService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("usersCount", statsService.usersCount());
        model.addAttribute("productsCount", statsService.productsCount());
        model.addAttribute("pendingOrders", statsService.pendingOrdersCount());
        model.addAttribute("totalSales", statsService.totalSales());
        return "admin/dashboard";
    }

    @GetMapping("/productos")
    public String products(
            Model model,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId) {
        String query = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        var products = productRepository.findByActiveTrueOrderByFeaturedDescCreatedAtDesc().stream()
                .filter(product -> categoryId == null || product.getCategory().getId().equals(categoryId))
                .filter(product -> query.isBlank()
                        || product.getName().toLowerCase(Locale.ROOT).contains(query)
                        || product.getDescription().toLowerCase(Locale.ROOT).contains(query))
                .toList();
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("q", q);
        model.addAttribute("selectedCategoryId", categoryId);
        return "admin/productos";
    }

    @GetMapping("/productos/nuevo")
    public String newProduct(Model model) {
        model.addAttribute("productRequest", new ProductRequest());
        model.addAttribute("productId", null);
        model.addAttribute("formAction", "/admin/productos");
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/producto-form";
    }

    @PostMapping("/productos")
    public String saveProduct(
            @Valid @ModelAttribute ProductRequest productRequest,
            BindingResult bindingResult,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareProductForm(model, null, "/admin/productos");
            return "admin/producto-form";
        }
        if (!appendUploadedImage(productRequest, imageFile, bindingResult)) {
            prepareProductForm(model, null, "/admin/productos");
            return "admin/producto-form";
        }
        productService.save(productRequest);
        redirectAttributes.addFlashAttribute("success", "Producto creado");
        return "redirect:/admin/productos";
    }

    @GetMapping("/productos/{id}/editar")
    public String editProduct(@PathVariable Long id, Model model) {
        var product = productService.findById(id);
        ProductRequest request = new ProductRequest();
        request.setName(product.getName());
        request.setDescription(product.getDescription());
        request.setPrice(product.getPrice());
        request.setStock(product.getStock());
        request.setCategoryId(product.getCategory().getId());
        request.setActive(product.isActive());
        request.setFeatured(product.isFeatured());
        request.setNewCollection(product.isNewCollection());
        request.setImageUrls(product.getImages().stream().map(image -> image.getImageUrl()).toList());
        model.addAttribute("productRequest", request);
        model.addAttribute("productId", id);
        model.addAttribute("formAction", "/admin/productos/" + id);
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/producto-form";
    }

    @PostMapping("/productos/{id}")
    public String updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute ProductRequest productRequest,
            BindingResult bindingResult,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareProductForm(model, id, "/admin/productos/" + id);
            return "admin/producto-form";
        }
        if (!appendUploadedImage(productRequest, imageFile, bindingResult)) {
            prepareProductForm(model, id, "/admin/productos/" + id);
            return "admin/producto-form";
        }
        productService.update(id, productRequest);
        redirectAttributes.addFlashAttribute("success", "Producto actualizado");
        return "redirect:/admin/productos";
    }

    @PostMapping("/productos/{id}/eliminar")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Producto eliminado del catálogo activo");
        return "redirect:/admin/productos";
    }

    @GetMapping("/usuarios")
    public String users(Model model, @RequestParam(required = false) String q) {
        String query = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        var users = userRepository.findAll().stream()
                .filter(user -> query.isBlank()
                        || user.getFirstName().toLowerCase(Locale.ROOT).contains(query)
                        || user.getFirstLastName().toLowerCase(Locale.ROOT).contains(query)
                        || user.getEmail().toLowerCase(Locale.ROOT).contains(query)
                        || user.getUsername().toLowerCase(Locale.ROOT).contains(query)
                        || user.getRole().name().toLowerCase(Locale.ROOT).contains(query)
                        || user.getCity().getName().toLowerCase(Locale.ROOT).contains(query))
                .toList();
        model.addAttribute("users", users);
        model.addAttribute("q", q);
        return "admin/usuarios";
    }

    @GetMapping("/pedidos")
    public String orders(Model model, @RequestParam(required = false) OrderStatus status) {
        var orders = status == null
                ? orderRepository.findAllByOrderByCreatedAtDesc()
                : orderRepository.findByStatusOrderByCreatedAtDesc(status);
        Map<Long, List<OrderStatus>> allowedStatusesByOrder = new java.util.HashMap<>();
        orders.forEach(order -> allowedStatusesByOrder.put(order.getId(), allowedNextStatuses(order.getStatus())));
        model.addAttribute("orders", orders);
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("allowedStatusesByOrder", allowedStatusesByOrder);
        return "admin/pedidos";
    }

    @PostMapping("/pedidos/{id}/estado")
    public String updateOrder(@PathVariable Long id,
                              @RequestParam OrderStatus status,
                              @RequestParam(required = false) OrderStatus filterStatus,
                              RedirectAttributes redirectAttributes) {
        orderRepository.findById(id).ifPresent(order -> {
            if (!allowedNextStatuses(order.getStatus()).contains(status)) {
                redirectAttributes.addFlashAttribute("error", "No se puede devolver un pedido a un estado anterior.");
                return;
            }
            order.setStatus(status);
            orderRepository.save(order);
            redirectAttributes.addFlashAttribute("success", "Estado del pedido actualizado");
        });
        return filterStatus == null ? "redirect:/admin/pedidos" : "redirect:/admin/pedidos?status=" + filterStatus.name();
    }

    private List<OrderStatus> allowedNextStatuses(OrderStatus current) {
        return switch (current) {
            case PENDIENTE -> Arrays.asList(OrderStatus.PENDIENTE, OrderStatus.PAGADO, OrderStatus.CANCELADO);
            case PAGADO -> Arrays.asList(OrderStatus.PAGADO, OrderStatus.ENVIADO, OrderStatus.ENTREGADO);
            case ENVIADO -> Arrays.asList(OrderStatus.ENVIADO, OrderStatus.ENTREGADO);
            case ENTREGADO -> List.of(OrderStatus.ENTREGADO);
            case CANCELADO -> List.of(OrderStatus.CANCELADO);
        };
    }

    @GetMapping("/devoluciones")
    public String returns(Model model) {
        var returns = returnRepository.findAll();
        Map<Long, List<ReturnStatus>> allowedStatusesByReturn = new java.util.HashMap<>();
        returns.forEach(request -> allowedStatusesByReturn.put(request.getId(), allowedNextReturnStatuses(request.getStatus())));
        model.addAttribute("returns", returns);
        model.addAttribute("statuses", ReturnStatus.values());
        model.addAttribute("allowedStatusesByReturn", allowedStatusesByReturn);
        return "admin/devoluciones";
    }

    @PostMapping("/devoluciones/{id}/estado")
    public String updateReturn(@PathVariable Long id, @RequestParam ReturnStatus status, RedirectAttributes redirectAttributes) {
        returnRepository.findById(id).ifPresent(request -> {
            if (!allowedNextReturnStatuses(request.getStatus()).contains(status)) {
                redirectAttributes.addFlashAttribute("error", "No se puede devolver una devolucion a un estado anterior.");
                return;
            }
            request.setStatus(status);
            returnRepository.save(request);
            redirectAttributes.addFlashAttribute("success", "Estado de devolucion actualizado");
        });
        return "redirect:/admin/devoluciones";
    }

    private List<ReturnStatus> allowedNextReturnStatuses(ReturnStatus current) {
        return switch (current) {
            case SOLICITADA -> Arrays.asList(ReturnStatus.SOLICITADA, ReturnStatus.EN_REVISION);
            case EN_REVISION -> Arrays.asList(ReturnStatus.EN_REVISION, ReturnStatus.APROBADA, ReturnStatus.RECHAZADA);
            case APROBADA -> Arrays.asList(ReturnStatus.APROBADA, ReturnStatus.CERRADA);
            case RECHAZADA -> Arrays.asList(ReturnStatus.RECHAZADA, ReturnStatus.CERRADA);
            case CERRADA -> List.of(ReturnStatus.CERRADA);
        };
    }

    @GetMapping("/contenido")
    public String content(Model model) {
        model.addAttribute("siteContentRequest", siteContentService.toRequest(siteContentService.current()));
        return "admin/contenido";
    }

    @PostMapping("/contenido")
    public String updateContent(
            @Valid @ModelAttribute SiteContentRequest siteContentRequest,
            BindingResult bindingResult,
            @RequestParam(value = "heroImageFile", required = false) MultipartFile heroImageFile,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/contenido";
        }
        try {
            String uploadedImageUrl = productImageStorageService.store(heroImageFile);
            if (uploadedImageUrl != null) {
                siteContentRequest.setHeroImageUrl(uploadedImageUrl);
            }
        } catch (IllegalArgumentException | IllegalStateException exception) {
            bindingResult.reject("heroImageFile", exception.getMessage());
            return "admin/contenido";
        }
        siteContentService.update(siteContentRequest);
        redirectAttributes.addFlashAttribute("success", "Contenido actualizado");
        return "redirect:/admin/contenido";
    }

    @GetMapping("/resenas")
    public String reviews(Model model) {
        model.addAttribute("reviews", reviewRepository.findAll());
        return "admin/resenas";
    }

    @PostMapping("/resenas/{id}/eliminar")
    public String deleteReview(@PathVariable Long id) {
        reviewService.deleteAsAdmin(id);
        return "redirect:/admin/resenas";
    }

    private boolean appendUploadedImage(ProductRequest productRequest, MultipartFile imageFile, BindingResult bindingResult) {
        try {
            String uploadedImageUrl = productImageStorageService.store(imageFile);
            if (uploadedImageUrl != null) {
                productRequest.getImageUrls().add(0, uploadedImageUrl);
            }
            return true;
        } catch (IllegalArgumentException | IllegalStateException exception) {
            bindingResult.reject("imageFile", exception.getMessage());
            return false;
        }
    }

    private void prepareProductForm(Model model, Long productId, String formAction) {
        model.addAttribute("productId", productId);
        model.addAttribute("formAction", formAction);
        model.addAttribute("categories", categoryRepository.findAll());
    }
}
