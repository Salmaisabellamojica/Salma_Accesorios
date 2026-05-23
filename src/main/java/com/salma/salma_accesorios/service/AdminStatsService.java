package com.salma.salma_accesorios.service;

import com.salma.salma_accesorios.model.OrderStatus;
import com.salma.salma_accesorios.repository.AppUserRepository;
import com.salma.salma_accesorios.repository.CustomerOrderRepository;
import com.salma.salma_accesorios.repository.ProductRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class AdminStatsService {

    private final AppUserRepository userRepository;
    private final ProductRepository productRepository;
    private final CustomerOrderRepository orderRepository;

    public AdminStatsService(AppUserRepository userRepository, ProductRepository productRepository, CustomerOrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    public long usersCount() {
        return userRepository.count();
    }

    public long productsCount() {
        return productRepository.count();
    }

    public long pendingOrdersCount() {
        return orderRepository.countByStatus(OrderStatus.PENDIENTE);
    }

    public BigDecimal totalSales() {
        return orderRepository.totalSales();
    }
}
