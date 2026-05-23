package com.salma.salma_accesorios.controller;

import com.salma.salma_accesorios.dto.RegisterRequest;
import com.salma.salma_accesorios.repository.ProductRepository;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ObjectProvider<ProductRepository> productRepository;

    public HomeController(ObjectProvider<ProductRepository> productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        ProductRepository repository = productRepository.getIfAvailable();
        model.addAttribute("featuredProducts", repository == null ? List.of() : repository.findTop4ByActiveTrueAndFeaturedTrueOrderByCreatedAtDesc());
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "registro";
    }
}
