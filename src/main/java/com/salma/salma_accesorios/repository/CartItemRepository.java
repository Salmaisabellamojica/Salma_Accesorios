package com.salma.salma_accesorios.repository;

import com.salma.salma_accesorios.model.Cart;
import com.salma.salma_accesorios.model.CartItem;
import com.salma.salma_accesorios.model.Product;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}
