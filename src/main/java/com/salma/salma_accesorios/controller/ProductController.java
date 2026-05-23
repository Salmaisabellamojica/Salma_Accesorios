package com.salma.salma_accesorios.controller;

import com.salma.salma_accesorios.model.Product;
import com.salma.salma_accesorios.repository.CategoryRepository;
import com.salma.salma_accesorios.service.ProductService;
import com.salma.salma_accesorios.service.ReviewService;
import com.salma.salma_accesorios.service.UserService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final ReviewService reviewService;
    private final UserService userService;

    public ProductController(
            ProductService productService,
            CategoryRepository categoryRepository,
            ReviewService reviewService,
            UserService userService) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
        this.reviewService = reviewService;
        this.userService = userService;
    }

    @GetMapping("/catalogo")
    public String catalog(Model model, @RequestParam(required = false) String categoria, @RequestParam(required = false) String q) {
        model.addAttribute("products", productService.search(categoria, q));
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("categoria", categoria);
        model.addAttribute("q", q);
        return "catalogo";
    }

    @GetMapping("/producto/{id}")
    public String detail(@PathVariable Long id, Model model, Authentication authentication) {
        Product product = productService.findById(id);
        boolean authenticated = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
        var currentUser = authenticated ? userService.currentUser(authentication) : null;

        model.addAttribute("product", product);
        model.addAttribute("reviews", reviewService.reviewsFor(product));
        model.addAttribute("authenticated", authenticated);
        model.addAttribute("hasPurchasedProduct", reviewService.hasPurchasedProduct(currentUser, product));
        model.addAttribute("hasReviewedProduct", reviewService.hasReviewed(currentUser, product));
        model.addAttribute("canReview", reviewService.canReview(currentUser, product));
        return "producto";
    }
}
