package com.salma.salma_accesorios.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductImageStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final Path uploadPath;

    public ProductImageStorageService(@Value("${app.upload.products-dir:uploads/products}") String productsDir) {
        this.uploadPath = Path.of(productsDir).toAbsolutePath().normalize();
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Solo se permiten imagenes JPG, PNG o WEBP.");
        }

        try {
            Files.createDirectories(uploadPath);
            String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "producto" : file.getOriginalFilename());
            String fileName = UUID.randomUUID() + getExtension(originalName);
            Path destination = uploadPath.resolve(fileName).normalize();

            if (!destination.startsWith(uploadPath)) {
                throw new IllegalArgumentException("Nombre de archivo no valido.");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }
            return "/uploads/products/" + fileName;
        } catch (IOException exception) {
            throw new IllegalStateException("No fue posible guardar la imagen del producto.", exception);
        }
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0) {
            return ".jpg";
        }
        return fileName.substring(dotIndex).toLowerCase();
    }
}
