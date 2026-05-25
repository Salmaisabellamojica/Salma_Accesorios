package com.salma.salma_accesorios.controller;

import com.salma.salma_accesorios.model.AppUser;
import com.salma.salma_accesorios.service.CartService;
import com.salma.salma_accesorios.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.ResponseEntity;
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
        var cart = cartService.getOrCreate(user);
        var subtotal = cartService.selectedSubtotal(cart);
        model.addAttribute("cart", cart);
        model.addAttribute("selectedSubtotal", subtotal);
        model.addAttribute("shippingCost", cartService.shippingCost(subtotal));
        model.addAttribute("standardShippingCost", CartService.SHIPPING_COST);
        model.addAttribute("freeShippingRemaining", cartService.freeShippingRemaining(subtotal));
        model.addAttribute("freeShippingThreshold", CartService.FREE_SHIPPING_THRESHOLD);
        model.addAttribute("selectedTotal", cartService.selectedTotal(cart));
        return "carrito";
    }

    @PostMapping("/carrito/agregar")
    public String add(
            Authentication authentication,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestParam(defaultValue = "false") boolean openCart,
            @RequestParam(required = false) String redirectTo,
            RedirectAttributes redirectAttributes) {
        try {
            cartService.add(userService.currentUser(authentication), productId, quantity);
            redirectAttributes.addFlashAttribute("success", "Producto agregado al carrito");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        String target = redirectTo == null || redirectTo.isBlank() ? "/producto/" + productId : redirectTo;
        return "redirect:" + target + (openCart ? (target.contains("?") ? "&" : "?") + "cart=open" : "");
    }

    @PostMapping("/carrito/seleccion")
    public String selection(Authentication authentication, @RequestParam(required = false) List<Long> selectedItems) {
        cartService.updateSelection(userService.currentUser(authentication), selectedItems);
        return "redirect:/carrito";
    }

    @PostMapping("/carrito/item/seleccion")
    public Object itemSelection(
            Authentication authentication,
            @RequestParam Long itemId,
            @RequestParam(defaultValue = "false") boolean selected,
            HttpServletRequest request) {
        cartService.updateItemSelection(userService.currentUser(authentication), itemId, selected);
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return ResponseEntity.noContent().build();
        }
        return "redirect:/carrito";
    }

    @PostMapping("/carrito/eliminar")
    public String remove(
            Authentication authentication,
            @RequestParam Long itemId,
            @RequestParam(required = false) String redirectTo) {
        cartService.remove(userService.currentUser(authentication), itemId);
        String target = redirectTo == null || redirectTo.isBlank() ? "/carrito" : redirectTo;
        return "redirect:" + target + (target.contains("?") ? "&" : "?") + "cart=open";
    }

    @PostMapping("/carrito/disminuir")
    public String decrease(
            Authentication authentication,
            @RequestParam Long itemId,
            @RequestParam(required = false) String redirectTo) {
        cartService.decrease(userService.currentUser(authentication), itemId);
        String target = redirectTo == null || redirectTo.isBlank() ? "/carrito" : redirectTo;
        return "redirect:" + target + (target.contains("?") ? "&" : "?") + "cart=open";
    }

    @PostMapping("/checkout")
    public String checkout(Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            cartService.validateSelected(userService.currentUser(authentication));
            return "redirect:/pago/carrito";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/carrito";
    }
}
