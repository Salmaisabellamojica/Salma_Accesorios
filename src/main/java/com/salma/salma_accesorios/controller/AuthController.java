package com.salma.salma_accesorios.controller;

import com.salma.salma_accesorios.dto.RegisterRequest;
import com.salma.salma_accesorios.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    public String registrar(
            @Valid @ModelAttribute RegisterRequest registerRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (bindingResult.hasErrors()) {
            return "registro";
        }
        try {
            authService.register(registerRequest);
            redirectAttributes.addFlashAttribute("success", "Registro exitoso. Ahora puedes iniciar sesion.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "registro";
        }
    }
}
