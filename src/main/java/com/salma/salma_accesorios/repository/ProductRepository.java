package com.salma.salma_accesorios.repository;

import com.salma.salma_accesorios.model.Product;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"category", "images"})
    List<Product> findByActiveTrueOrderByFeaturedDescCreatedAtDesc();

    @EntityGraph(attributePaths = {"category", "images"})
    List<Product> findByActiveTrueAndCategorySlugOrderByFeaturedDescCreatedAtDesc(String slug);

    @EntityGraph(attributePaths = {"category", "images"})
    List<Product> findByActiveTrueAndNameContainingIgnoreCaseOrActiveTrueAndDescriptionContainingIgnoreCaseOrderByFeaturedDescCreatedAtDesc(String name, String description);

    @EntityGraph(attributePaths = {"category", "images"})
    List<Product> findByActiveTrueAndCategorySlugAndNameContainingIgnoreCaseOrActiveTrueAndCategorySlugAndDescriptionContainingIgnoreCaseOrderByFeaturedDescCreatedAtDesc(
            String nameSlug,
            String name,
            String descriptionSlug,
            String description
    );

    List<Product> findTop4ByActiveTrueAndFeaturedTrueOrderByCreatedAtDesc();

    List<Product> findTop8ByActiveTrueOrderBySoldCountDesc();
}
