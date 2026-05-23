package com.salma.salma_accesorios.service;

import com.salma.salma_accesorios.dto.ProductRequest;
import com.salma.salma_accesorios.model.Category;
import com.salma.salma_accesorios.model.Product;
import com.salma.salma_accesorios.model.ProductImage;
import com.salma.salma_accesorios.repository.CategoryRepository;
import com.salma.salma_accesorios.repository.ProductRepository;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Product> search(String category, String q) {
        String normalizedCategory = blankToNull(category);
        String normalizedQuery = blankToNull(q);

        if (normalizedCategory == null && normalizedQuery == null) {
            return productRepository.findByActiveTrueOrderByFeaturedDescCreatedAtDesc();
        }
        if (normalizedCategory != null && normalizedQuery == null) {
            return productRepository.findByActiveTrueAndCategorySlugOrderByFeaturedDescCreatedAtDesc(normalizedCategory);
        }
        if (normalizedCategory == null) {
            return productRepository.findByActiveTrueAndNameContainingIgnoreCaseOrActiveTrueAndDescriptionContainingIgnoreCaseOrderByFeaturedDescCreatedAtDesc(
                    normalizedQuery,
                    normalizedQuery
            );
        }
        return productRepository.findByActiveTrueAndCategorySlugAndNameContainingIgnoreCaseOrActiveTrueAndCategorySlugAndDescriptionContainingIgnoreCaseOrderByFeaturedDescCreatedAtDesc(
                normalizedCategory,
                normalizedQuery,
                normalizedCategory,
                normalizedQuery
        );
    }

    public Product findById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
    }

    @Transactional
    public Product save(ProductRequest request) {
        Product product = new Product();
        apply(product, request);
        return productRepository.save(product);
    }

    @Transactional
    public Product update(Long id, ProductRequest request) {
        Product product = findById(id);
        apply(product, request);
        return product;
    }

    @Transactional
    public void delete(Long id) {
        Product product = findById(id);
        product.setActive(false);
    }

    private void apply(Product product, ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada"));
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription().trim());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(category);
        product.setActive(request.isActive());
        product.setFeatured(request.isFeatured());
        product.getImages().clear();
        for (String imageUrl : request.getImageUrls()) {
            if (imageUrl == null || imageUrl.isBlank()) {
                continue;
            }
            ProductImage image = new ProductImage();
            image.setProduct(product);
            image.setImageUrl(imageUrl.trim());
            image.setAltText("Imagen de " + product.getName());
            image.setMainImage(product.getImages().isEmpty());
            product.getImages().add(image);
        }
    }

    public Category createCategoryIfMissing(String name) {
        String slug = slugify(name);
        return categoryRepository.findBySlug(slug).orElseGet(() -> {
            Category category = new Category();
            category.setName(name);
            category.setSlug(slug);
            return categoryRepository.save(category);
        });
    }

    private String slugify(String value) {
        String normalized = Normalizer.normalize(value.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
