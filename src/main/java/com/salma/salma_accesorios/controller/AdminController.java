package com.salma.salma_accesorios.controller;

import com.salma.salma_accesorios.dto.ProductRequest;
import com.salma.salma_accesorios.model.OrderStatus;
import com.salma.salma_accesorios.repository.AppUserRepository;
import com.salma.salma_accesorios.repository.CategoryRepository;
import com.salma.salma_accesorios.repository.CustomerOrderRepository;
import com.salma.salma_accesorios.repository.ProductRepository;
import com.salma.salma_accesorios.repository.ReviewRepository;
import com.salma.salma_accesorios.service.AdminStatsService;
import com.salma.salma_accesorios.service.ProductImageStorageService;
import com.salma.salma_accesorios.service.ProductService;
import com.salma.salma_accesorios.service.ReviewService;
import jakarta.validation.Valid;
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
    private final ReviewService reviewService;
    private final ProductImageStorageService productImageStorageService;

    public AdminController(
            AdminStatsService statsService,
            ProductService productService,
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            AppUserRepository userRepository,
            CustomerOrderRepository orderRepository,
            ReviewRepository reviewRepository,
            ReviewService reviewService,
            ProductImageStorageService productImageStorageService) {
        this.statsService = statsService;
        this.productService = productService;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.reviewService = reviewService;
        this.productImageStorageService = productImageStorageService;
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
    public String products(Model model) {
        model.addAttribute("products", productRepository.findByActiveTrueOrderByFeaturedDescCreatedAtDesc());
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
    public String users(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/usuarios";
    }

    @GetMapping("/pedidos")
    public String orders(Model model) {
        model.addAttribute("orders", orderRepository.findAll());
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/pedidos";
    }

    @PostMapping("/pedidos/{id}/estado")
    public String updateOrder(@PathVariable Long id, @RequestParam OrderStatus status) {
        orderRepository.findById(id).ifPresent(order -> {
            order.setStatus(status);
            orderRepository.save(order);
        });
        return "redirect:/admin/pedidos";
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
