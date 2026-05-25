package com.salma.salma_accesorios.repository;

import com.salma.salma_accesorios.model.AppUser;
import com.salma.salma_accesorios.model.Product;
import com.salma.salma_accesorios.model.Review;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @EntityGraph(attributePaths = {"user", "images"})
    List<Review> findByProductAndApprovedTrueOrderByCreatedAtDesc(Product product);

    @Query("""
            select distinct r from Review r
            join fetch r.product p
            left join fetch p.images
            where r.user = :user
            order by r.createdAt desc
            """)
    List<Review> findByUserWithProductOrderByCreatedAtDesc(@Param("user") AppUser user);

    boolean existsByUserAndProduct(AppUser user, Product product);

    @Query("select coalesce(avg(r.rating), 0) from Review r where r.product = :product and r.approved = true")
    BigDecimal averageRating(@Param("product") Product product);
}
