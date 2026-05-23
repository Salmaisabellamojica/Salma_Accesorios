package com.salma.salma_accesorios.controller;

import com.salma.salma_accesorios.model.AppUser;
import com.salma.salma_accesorios.repository.AppUserRepository;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    private final ObjectProvider<AppUserRepository> userRepository;

    public GlobalModelAdvice(ObjectProvider<AppUserRepository> userRepository) {
        this.userRepository = userRepository;
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
}
