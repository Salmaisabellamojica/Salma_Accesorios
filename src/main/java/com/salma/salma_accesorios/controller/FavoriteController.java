package com.salma.salma_accesorios.controller;

import com.salma.salma_accesorios.service.FavoriteService;
import com.salma.salma_accesorios.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final UserService userService;

    public FavoriteController(FavoriteService favoriteService, UserService userService) {
        this.favoriteService = favoriteService;
        this.userService = userService;
    }

    @GetMapping("/favoritos")
    public String list(Authentication authentication, Model model) {
        model.addAttribute("favorites", favoriteService.list(userService.currentUser(authentication)));
        return "favoritos";
    }

    @PostMapping("/favoritos/toggle")
    public String toggle(
            Authentication authentication,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "") String redirectTo) {
        favoriteService.toggle(userService.currentUser(authentication), productId);
        return "redirect:" + (redirectTo.isBlank() ? "/producto/" + productId : redirectTo);
    }

    @PostMapping("/favoritos/eliminar")
    public String remove(Authentication authentication, @RequestParam Long productId) {
        favoriteService.remove(userService.currentUser(authentication), productId);
        return "redirect:/favoritos";
    }
}
