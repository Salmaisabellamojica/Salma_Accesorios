package com.salma.salma_accesorios.repository;

import com.salma.salma_accesorios.model.AppUser;
import com.salma.salma_accesorios.model.CustomerOrder;
import com.salma.salma_accesorios.model.OrderStatus;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    @EntityGraph(attributePaths = {"items", "items.product"})
    List<CustomerOrder> findByUserOrderByCreatedAtDesc(AppUser user);

    long countByStatus(OrderStatus status);

    @Query("select coalesce(sum(o.total), 0) from CustomerOrder o where o.status <> 'CANCELADO'")
    BigDecimal totalSales();
}
