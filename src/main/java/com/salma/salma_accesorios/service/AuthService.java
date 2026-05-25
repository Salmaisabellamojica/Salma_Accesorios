package com.salma.salma_accesorios.service;

import com.salma.salma_accesorios.dto.RegisterRequest;
import com.salma.salma_accesorios.dto.GoogleRegistrationRequest;
import com.salma.salma_accesorios.model.AppUser;
import com.salma.salma_accesorios.model.City;
import com.salma.salma_accesorios.model.Role;
import com.salma.salma_accesorios.repository.AppUserRepository;
import com.salma.salma_accesorios.repository.CityRepository;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final CityRepository cityRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AppUserRepository userRepository, CityRepository cityRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.cityRepository = cityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Las contrasenas no coinciden");
        }
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        String username = request.getUsername().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("El correo ya esta registrado");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("El username ya esta registrado");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("El celular ya esta registrado");
        }

        City city = cityRepository.findByNameIgnoreCase(request.getCity().trim())
                .orElseGet(() -> {
                    City newCity = new City();
                    newCity.setName(normalizeName(request.getCity()));
                    return cityRepository.save(newCity);
                });

        AppUser user = new AppUser();
        user.setFirstName(normalizeName(request.getFirstName()));
        user.setSecondName(blankToNull(request.getSecondName()));
        user.setFirstLastName(normalizeName(request.getFirstLastName()));
        user.setSecondLastName(normalizeName(request.getSecondLastName()));
        user.setEmail(email);
        user.setUsername(username);
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCity(city);
        user.setAddress(request.getAddress().trim());
        user.setRole(Role.CLIENTE);
        return userRepository.save(user);
    }

    @Transactional
    public AppUser registerGoogleUser(String email, GoogleRegistrationRequest request) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        String username = request.getUsername().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("El correo ya esta registrado");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("El username ya esta registrado");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("El celular ya esta registrado");
        }

        City city = cityRepository.findByNameIgnoreCase(request.getCity().trim())
                .orElseGet(() -> {
                    City newCity = new City();
                    newCity.setName(normalizeName(request.getCity()));
                    return cityRepository.save(newCity);
                });

        AppUser user = new AppUser();
        user.setFirstName(normalizeName(request.getFirstName()));
        user.setSecondName(blankToNull(request.getSecondName()));
        user.setFirstLastName(normalizeName(request.getFirstLastName()));
        user.setSecondLastName(normalizeName(request.getSecondLastName()));
        user.setEmail(normalizedEmail);
        user.setUsername(username);
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode("GOOGLE_OAUTH_USER"));
        user.setCity(city);
        user.setAddress(request.getAddress().trim());
        user.setRole(Role.CLIENTE);
        return userRepository.save(user);
    }

    private String normalizeName(String value) {
        String trimmed = value.trim().replaceAll("\\s+", " ");
        return trimmed.substring(0, 1).toUpperCase(Locale.ROOT) + trimmed.substring(1).toLowerCase(Locale.ROOT);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : normalizeName(value);
    }
}
