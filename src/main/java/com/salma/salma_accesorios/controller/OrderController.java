package com.salma.salma_accesorios.controller;

import com.salma.salma_accesorios.model.AppUser;
import com.salma.salma_accesorios.repository.CustomerOrderRepository;
import com.salma.salma_accesorios.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class OrderController {

    private final CustomerOrderRepository orderRepository;
    private final UserService userService;

    public OrderController(CustomerOrderRepository orderRepository, UserService userService) {
        this.orderRepository = orderRepository;
        this.userService = userService;
    }

    @GetMapping("/pedidos")
    public String myOrders(Authentication authentication, Model model) {
        AppUser user = userService.currentUser(authentication);
        model.addAttribute("orders", orderRepository.findByUserOrderByCreatedAtDesc(user));
        return "pedidos";
    }

    @GetMapping("/pedidos/{id}")
    public String orderDetail(@PathVariable Long id, Authentication authentication, Model model) {
        AppUser user = userService.currentUser(authentication);
        model.addAttribute("order", orderRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado")));
        return "pedido-detalle";
    }
}
