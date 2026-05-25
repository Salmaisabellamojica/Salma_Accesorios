package com.salma.salma_accesorios.repository;

import com.salma.salma_accesorios.model.AppUser;
import com.salma.salma_accesorios.model.CustomerOrder;
import com.salma.salma_accesorios.model.ReturnRequest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {

    @EntityGraph(attributePaths = {"order", "order.items", "order.items.product"})
    List<ReturnRequest> findByUserOrderByCreatedAtDesc(AppUser user);

    Optional<ReturnRequest> findByOrderAndUser(CustomerOrder order, AppUser user);
}
