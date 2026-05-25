package com.salma.salma_accesorios.config;

import com.salma.salma_accesorios.dto.PendingGoogleUser;
import com.salma.salma_accesorios.repository.AppUserRepository;
import com.salma.salma_accesorios.security.WebLoginService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.Normalizer;
import java.util.Locale;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    public static final String PENDING_GOOGLE_USER = "PENDING_GOOGLE_USER";

    private final AppUserRepository userRepository;
    private final WebLoginService webLoginService;

    public GoogleOAuth2SuccessHandler(AppUserRepository userRepository, WebLoginService webLoginService) {
        this.userRepository = userRepository;
        this.webLoginService = webLoginService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = value(oauthUser, "email").toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            webLoginService.login(request, email);
            response.sendRedirect("/");
            return;
        }

        request.getSession(true).setAttribute(PENDING_GOOGLE_USER, pendingUser(oauthUser, email));
        response.sendRedirect("/registro/google/completar");
    }

    private PendingGoogleUser pendingUser(OAuth2User oauthUser, String email) {
        String givenName = value(oauthUser, "given_name");
        String familyName = value(oauthUser, "family_name");
        String name = value(oauthUser, "name");
        if (givenName.isBlank() && !name.isBlank()) {
            String[] parts = name.trim().split("\\s+");
            givenName = parts[0];
            if (familyName.isBlank() && parts.length > 1) {
                familyName = parts[parts.length - 1];
            }
        }
        return new PendingGoogleUser(
                email,
                sanitizeName(givenName, "Usuario"),
                "",
                sanitizeName(familyName, "Google"),
                usernameFromEmail(email)
        );
    }

    private String value(OAuth2User oauthUser, String attribute) {
        Object value = oauthUser.getAttribute(attribute);
        return value == null ? "" : value.toString().trim();
    }

    private String sanitizeName(String value, String fallback) {
        String letters = value == null ? "" : value.replaceAll("[^A-Za-zÁÉÍÓÚáéíóúÑñ]", "");
        return letters.length() >= 2 ? letters : fallback;
    }

    private String usernameFromEmail(String email) {
        String base = Normalizer.normalize(email.substring(0, email.indexOf("@")).toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9._-]", "");
        if (base.length() < 4) {
            base = "google" + base;
        }
        String candidate = base.substring(0, Math.min(base.length(), 32));
        int counter = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base.substring(0, Math.min(base.length(), 28)) + counter;
            counter++;
        }
        return candidate;
    }
}
