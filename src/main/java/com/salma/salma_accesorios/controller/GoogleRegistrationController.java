package com.salma.salma_accesorios.controller;

import com.salma.salma_accesorios.config.GoogleOAuth2SuccessHandler;
import com.salma.salma_accesorios.dto.GoogleRegistrationRequest;
import com.salma.salma_accesorios.dto.PendingGoogleUser;
import com.salma.salma_accesorios.security.WebLoginService;
import com.salma.salma_accesorios.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class GoogleRegistrationController {

    private final AuthService authService;
    private final WebLoginService webLoginService;

    public GoogleRegistrationController(AuthService authService, WebLoginService webLoginService) {
        this.authService = authService;
        this.webLoginService = webLoginService;
    }

    @GetMapping("/registro/google/completar")
    public String completeGoogleRegistration(HttpSession session, Model model) {
        PendingGoogleUser pendingUser = pendingUser(session);
        if (pendingUser == null) {
            return "redirect:/login";
        }
        if (!model.containsAttribute("googleRegistrationRequest")) {
            GoogleRegistrationRequest request = new GoogleRegistrationRequest();
            request.setFirstName(pendingUser.firstName());
            request.setSecondName(pendingUser.secondName());
            request.setFirstLastName(pendingUser.firstLastName());
            request.setUsername(pendingUser.username());
            model.addAttribute("googleRegistrationRequest", request);
        }
        model.addAttribute("googleEmail", pendingUser.email());
        return "registro-google";
    }

    @PostMapping("/registro/google/completar")
    public String saveGoogleRegistration(
            @Valid @ModelAttribute GoogleRegistrationRequest googleRegistrationRequest,
            BindingResult bindingResult,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes,
            Model model) {
        PendingGoogleUser pendingUser = pendingUser(request.getSession(false));
        if (pendingUser == null) {
            return "redirect:/login";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("googleEmail", pendingUser.email());
            return "registro-google";
        }
        try {
            authService.registerGoogleUser(pendingUser.email(), googleRegistrationRequest);
            request.getSession().removeAttribute(GoogleOAuth2SuccessHandler.PENDING_GOOGLE_USER);
            webLoginService.login(request, pendingUser.email());
            return "redirect:/";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("googleEmail", pendingUser.email());
            model.addAttribute("error", exception.getMessage());
            return "registro-google";
        }
    }

    private PendingGoogleUser pendingUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object pendingUser = session.getAttribute(GoogleOAuth2SuccessHandler.PENDING_GOOGLE_USER);
        return pendingUser instanceof PendingGoogleUser googleUser ? googleUser : null;
    }
}
