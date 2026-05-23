package com.salma.salma_accesorios.controller;

import com.salma.salma_accesorios.dto.JwtResponse;
import com.salma.salma_accesorios.dto.LoginRequest;
import com.salma.salma_accesorios.dto.RegisterRequest;
import com.salma.salma_accesorios.model.AppUser;
import com.salma.salma_accesorios.security.CustomUserDetailsService;
import com.salma.salma_accesorios.security.JwtService;
import com.salma.salma_accesorios.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    public ApiAuthController(
            AuthService authService,
            AuthenticationManager authenticationManager,
            CustomUserDetailsService userDetailsService,
            JwtService jwtService) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @PostMapping("/registro")
    public JwtResponse register(@Valid @RequestBody RegisterRequest request) {
        AppUser user = authService.register(request);
        UserDetails details = userDetailsService.loadUserByUsername(user.getEmail());
        return new JwtResponse(jwtService.generateToken(details), user.getRole().name());
    }

    @PostMapping("/login")
    public JwtResponse login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        UserDetails details = userDetailsService.loadUserByUsername(request.getEmail());
        String role = details.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return new JwtResponse(jwtService.generateToken(details), role);
    }
}
