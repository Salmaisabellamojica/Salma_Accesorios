package com.salma.salma_accesorios.repository;

import com.salma.salma_accesorios.model.AppUser;
import com.salma.salma_accesorios.model.OrderItem;
import com.salma.salma_accesorios.model.OrderStatus;
import com.salma.salma_accesorios.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query("""
            select count(i) > 0 from OrderItem i
            where i.order.user = :user
              and i.product = :product
              and i.order.status in :statuses
            """)
    boolean existsPurchasedProduct(@Param("user") AppUser user, @Param("product") Product product, @Param("statuses") Iterable<OrderStatus> statuses);
}
