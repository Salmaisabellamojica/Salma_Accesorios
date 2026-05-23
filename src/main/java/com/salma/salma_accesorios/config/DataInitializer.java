package com.salma.salma_accesorios.config;

import com.salma.salma_accesorios.model.AppUser;
import com.salma.salma_accesorios.model.City;
import com.salma.salma_accesorios.model.Role;
import com.salma.salma_accesorios.repository.AppUserRepository;
import com.salma.salma_accesorios.repository.CityRepository;
import com.salma.salma_accesorios.service.ProductService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductService productService;
    private final AppUserRepository userRepository;
    private final CityRepository cityRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            ProductService productService,
            AppUserRepository userRepository,
            CityRepository cityRepository,
            PasswordEncoder passwordEncoder) {
        this.productService = productService;
        this.userRepository = userRepository;
        this.cityRepository = cityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        productService.createCategoryIfMissing("Collares");
        productService.createCategoryIfMissing("Pulseras");
        productService.createCategoryIfMissing("Anillos");
        productService.createCategoryIfMissing("Llaveros");

        if (!userRepository.existsByEmail("admin@salma.com")) {
            City city = findOrCreateCity("Bogota");
            AppUser admin = new AppUser();
            admin.setFirstName("Admin");
            admin.setFirstLastName("Salma");
            admin.setSecondLastName("Accesorios");
            admin.setEmail("admin@salma.com");
            admin.setUsername("admin");
            admin.setPhone("3000000000");
            admin.setPassword(passwordEncoder.encode("Admin123*"));
            admin.setCity(city);
            admin.setAddress("Calle 1 # 1-1");
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
        }

        createClientIfMissing(
                "cliente1@salma.com",
                "cliente1",
                "Valentina",
                "Mariana",
                "Rojas",
                "Martinez",
                "3111111111",
                "Medellin",
                "Carrera 43A # 10-20",
                "Cliente123*");

        createClientIfMissing(
                "cliente2@salma.com",
                "cliente2",
                "Camila",
                "Sofia",
                "Gomez",
                "Torres",
                "3222222222",
                "Cali",
                "Avenida 6N # 23-45",
                "Compra456*");

        createClientIfMissing(
                "laura.mendez@example.com",
                "lauramendez",
                "Laura",
                "Isabel",
                "Mendez",
                "Castro",
                "3134567890",
                "Bogota",
                "Calle 72 # 11-45",
                "Laura2026*");

        createClientIfMissing(
                "natalia.giraldo@example.com",
                "natalia.g",
                "Natalia",
                "Andrea",
                "Giraldo",
                "Vargas",
                "3159876543",
                "Medellin",
                "Carrera 48 # 18-35",
                "Natalia789*");

        createClientIfMissing(
                "sofia.ramirez@example.com",
                "sofiaramirez",
                "Sofia",
                "Valentina",
                "Ramirez",
                "Moreno",
                "3206543210",
                "Barranquilla",
                "Calle 84 # 51B-20",
                "Sofia321*");
    }

    private void createClientIfMissing(
            String email,
            String username,
            String firstName,
            String secondName,
            String firstLastName,
            String secondLastName,
            String phone,
            String cityName,
            String address,
            String rawPassword) {
        if (userRepository.existsByEmail(email)
                || userRepository.existsByUsername(username)
                || userRepository.existsByPhone(phone)) {
            return;
        }

        AppUser client = new AppUser();
        client.setFirstName(firstName);
        client.setSecondName(secondName);
        client.setFirstLastName(firstLastName);
        client.setSecondLastName(secondLastName);
        client.setEmail(email);
        client.setUsername(username);
        client.setPhone(phone);
        client.setPassword(passwordEncoder.encode(rawPassword));
        client.setCity(findOrCreateCity(cityName));
        client.setAddress(address);
        client.setRole(Role.CLIENTE);
        userRepository.save(client);
    }

    private City findOrCreateCity(String name) {
        return cityRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            City newCity = new City();
            newCity.setName(name);
            return cityRepository.save(newCity);
        });
    }
}
