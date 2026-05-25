package com.salma.salma_accesorios.controller;

import com.salma.salma_accesorios.model.AppUser;
import com.salma.salma_accesorios.model.ReturnRequest;
import com.salma.salma_accesorios.repository.CustomerOrderRepository;
import com.salma.salma_accesorios.repository.ReturnRequestRepository;
import com.salma.salma_accesorios.service.UserService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReturnController {

    private final CustomerOrderRepository orderRepository;
    private final ReturnRequestRepository returnRepository;
    private final UserService userService;

    public ReturnController(
            CustomerOrderRepository orderRepository,
            ReturnRequestRepository returnRepository,
            UserService userService) {
        this.orderRepository = orderRepository;
        this.returnRepository = returnRepository;
        this.userService = userService;
    }

    @GetMapping("/devoluciones")
    public String returns(Authentication authentication, Model model, @RequestParam(required = false) Long orderId) {
        AppUser user = userService.currentUser(authentication);
        model.addAttribute("orders", orderRepository.findByUserOrderByCreatedAtDesc(user));
        model.addAttribute("returns", returnRepository.findByUserOrderByCreatedAtDesc(user));
        model.addAttribute("selectedOrderId", orderId);
        return "devoluciones";
    }

    @PostMapping("/devoluciones")
    public String requestReturn(
            Authentication authentication,
            @RequestParam Long orderId,
            @RequestParam @NotBlank @Size(max = 500) String reason,
            RedirectAttributes redirectAttributes) {
        AppUser user = userService.currentUser(authentication);
        var order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
        if (returnRepository.findByOrderAndUser(order, user).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Ya existe una solicitud para ese pedido.");
            return "redirect:/devoluciones";
        }
        ReturnRequest request = new ReturnRequest();
        request.setOrder(order);
        request.setUser(user);
        request.setReason(reason.trim());
        returnRepository.save(request);
        redirectAttributes.addFlashAttribute("success", "Solicitud de devolucion enviada.");
        return "redirect:/devoluciones";
    }
}
