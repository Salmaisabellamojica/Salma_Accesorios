package com.salma.salma_accesorios.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final Path productUploadsPath;
    private final Path reviewUploadsPath;

    public WebConfig(
            @Value("${app.upload.products-dir:uploads/products}") String productsDir,
            @Value("${app.upload.reviews-dir:uploads/reviews}") String reviewsDir) {
        this.productUploadsPath = Path.of(productsDir).toAbsolutePath().normalize();
        this.reviewUploadsPath = Path.of(reviewsDir).toAbsolutePath().normalize();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/products/**")
                .addResourceLocations(productUploadsPath.toUri().toString());
        registry.addResourceHandler("/uploads/reviews/**")
                .addResourceLocations(reviewUploadsPath.toUri().toString());
    }
}
