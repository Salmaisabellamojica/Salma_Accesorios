package com.salma.salma_accesorios.controller;

import com.salma.salma_accesorios.model.AppUser;
import com.salma.salma_accesorios.service.CartService;
import com.salma.salma_accesorios.service.UserService;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping("/carrito")
    public String cart(Authentication authentication, Model model) {
        AppUser user = userService.currentUser(authentication);
        model.addAttribute("cart", cartService.getOrCreate(user));
        return "carrito";
    }

    @PostMapping("/carrito/agregar")
    public String add(Authentication authentication, @RequestParam Long productId, @RequestParam(defaultValue = "1") int quantity, RedirectAttributes redirectAttributes) {
        try {
            cartService.add(userService.currentUser(authentication), productId, quantity);
            redirectAttributes.addFlashAttribute("success", "Producto agregado al carrito");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/producto/" + productId;
    }

    @PostMapping("/carrito/seleccion")
    public String selection(Authentication authentication, @RequestParam(required = false) List<Long> selectedItems) {
        cartService.updateSelection(userService.currentUser(authentication), selectedItems);
        return "redirect:/carrito";
    }

    @PostMapping("/carrito/eliminar")
    public String remove(Authentication authentication, @RequestParam Long itemId) {
        cartService.remove(userService.currentUser(authentication), itemId);
        return "redirect:/carrito";
    }

    @PostMapping("/checkout")
    public String checkout(Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            Long orderId = cartService.checkoutSelected(userService.currentUser(authentication)).getId();
            redirectAttributes.addFlashAttribute("success", "Pedido creado #" + orderId + ". Los productos no seleccionados siguen en tu carrito.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/carrito";
    }
}
