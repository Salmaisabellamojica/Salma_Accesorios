package com.salma.salma_accesorios.controller;

import com.salma.salma_accesorios.model.AppUser;
import com.salma.salma_accesorios.model.Cart;
import com.salma.salma_accesorios.model.Role;
import com.salma.salma_accesorios.model.SiteContent;
import com.salma.salma_accesorios.repository.AppUserRepository;
import com.salma.salma_accesorios.repository.CartRepository;
import com.salma.salma_accesorios.repository.CategoryRepository;
import com.salma.salma_accesorios.service.SiteContentService;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    private final ObjectProvider<AppUserRepository> userRepository;
    private final ObjectProvider<CategoryRepository> categoryRepository;
    private final ObjectProvider<CartRepository> cartRepository;
    private final ObjectProvider<SiteContentService> siteContentService;

    public GlobalModelAdvice(
            ObjectProvider<AppUserRepository> userRepository,
            ObjectProvider<CategoryRepository> categoryRepository,
            ObjectProvider<CartRepository> cartRepository,
            ObjectProvider<SiteContentService> siteContentService) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.cartRepository = cartRepository;
        this.siteContentService = siteContentService;
    }

    @ModelAttribute("currentUser")
    public AppUser currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        AppUserRepository repository = userRepository.getIfAvailable();
        if (repository == null) {
            return null;
        }
        Optional<AppUser> user = repository.findByEmail(authentication.getName());
        return user.orElse(null);
    }

    @ModelAttribute("footerCategories")
    public List<?> footerCategories() {
        CategoryRepository repository = categoryRepository.getIfAvailable();
        return repository == null ? List.of() : repository.findAll();
    }

    @ModelAttribute("siteContent")
    public SiteContent siteContent() {
        SiteContentService service = siteContentService.getIfAvailable();
        return service == null ? new SiteContent() : service.current();
    }

    @ModelAttribute("currentCart")
    public Cart currentCart(Authentication authentication) {
        AppUser currentUser = currentUser(authentication);
        if (currentUser == null || currentUser.getRole() != Role.CLIENTE) {
            return null;
        }
        CartRepository repository = cartRepository.getIfAvailable();
        if (repository == null) {
            return null;
        }
        return repository.findByUser(currentUser).orElse(null);
    }

    @ModelAttribute("currentCartTotal")
    public BigDecimal currentCartTotal(Authentication authentication) {
        Cart currentCart = currentCart(authentication);
        if (currentCart == null) {
            return BigDecimal.ZERO;
        }
        return currentCart.getItems().stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @ModelAttribute("currentRequestUri")
    public String currentRequestUri(HttpServletRequest request) {
        String query = request.getQueryString();
        return request.getRequestURI() + (query == null || query.isBlank() ? "" : "?" + query);
    }

    @ModelAttribute("currentRequestPath")
    public String currentRequestPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("languageSwitchUri")
    public String languageSwitchUri(HttpServletRequest request) {
        StringBuilder builder = new StringBuilder(request.getRequestURI());
        request.getParameterMap().forEach((name, values) -> {
            if ("lang".equals(name)) {
                return;
            }
            for (String value : values) {
                builder.append(builder.indexOf("?") < 0 ? "?" : "&")
                        .append(URLEncoder.encode(name, StandardCharsets.UTF_8))
                        .append("=")
                        .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
            }
        });
        return builder.toString();
    }
}
