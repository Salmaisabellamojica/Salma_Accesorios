package com.salma.salma_accesorios.service;

import com.salma.salma_accesorios.model.AppUser;
import com.salma.salma_accesorios.model.Favorite;
import com.salma.salma_accesorios.model.Product;
import com.salma.salma_accesorios.repository.FavoriteRepository;
import com.salma.salma_accesorios.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;

    public FavoriteService(FavoriteRepository favoriteRepository, ProductRepository productRepository) {
        this.favoriteRepository = favoriteRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public void toggle(AppUser user, Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        if (favoriteRepository.existsByUserAndProduct(user, product)) {
            favoriteRepository.deleteByUserAndProduct(user, product);
            return;
        }
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setProduct(product);
        favoriteRepository.save(favorite);
    }

    @Transactional
    public void remove(AppUser user, Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        favoriteRepository.deleteByUserAndProduct(user, product);
    }

    public boolean isFavorite(AppUser user, Product product) {
        return user != null && favoriteRepository.existsByUserAndProduct(user, product);
    }

    public List<Favorite> list(AppUser user) {
        return favoriteRepository.findByUser(user);
    }
}
