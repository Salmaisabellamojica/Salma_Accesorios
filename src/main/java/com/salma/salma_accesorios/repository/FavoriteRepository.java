package com.salma.salma_accesorios.repository;

import com.salma.salma_accesorios.model.AppUser;
import com.salma.salma_accesorios.model.Favorite;
import com.salma.salma_accesorios.model.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    boolean existsByUserAndProduct(AppUser user, Product product);

    void deleteByUserAndProduct(AppUser user, Product product);

    List<Favorite> findByUser(AppUser user);
}
