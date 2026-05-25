package com.salma.salma_accesorios.controller;

import com.salma.salma_accesorios.dto.PaymentLine;
import com.salma.salma_accesorios.dto.PaymentRequest;
import com.salma.salma_accesorios.model.AppUser;
import com.salma.salma_accesorios.model.Cart;
import com.salma.salma_accesorios.model.CustomerOrder;
import com.salma.salma_accesorios.model.OrderStatus;
import com.salma.salma_accesorios.repository.CustomerOrderRepository;
import com.salma.salma_accesorios.service.CartService;
import com.salma.salma_accesorios.service.JasperReportService;
import com.salma.salma_accesorios.service.PaymentValidationService;
import com.salma.salma_accesorios.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PaymentController {

    private static final List<String> BANKS = List.of(
            "Bancolombia", "Banco de Bogota", "Davivienda", "BBVA Colombia",
            "Banco Popular", "Scotiabank Colpatria", "Banco Caja Social", "Nequi"
    );

    private final CustomerOrderRepository orderRepository;
    private final UserService userService;
    private final CartService cartService;
    private final PaymentValidationService paymentValidationService;

    public PaymentController(
            CustomerOrderRepository orderRepository,
            UserService userService,
            CartService cartService,
            PaymentValidationService paymentValidationService) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.cartService = cartService;
        this.paymentValidationService = paymentValidationService;
    }

    @GetMapping("/pago/carrito")
    public String cartPayment(Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        AppUser user = userService.currentUser(authentication);
        Cart cart = cartService.getOrCreate(user);
        try {
            cartService.validateSelected(user);
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/carrito";
        }
        prepareCartPaymentModel(model, cart, user, prefilledRequest(user));
        return "pago";
    }

    @PostMapping("/pago/carrito/revisar")
    public String reviewCartPayment(
            Authentication authentication,
            @Valid @ModelAttribute PaymentRequest paymentRequest,
            BindingResult bindingResult,
            Model model) {
        AppUser user = userService.currentUser(authentication);
        Cart cart = cartService.getOrCreate(user);
        cartService.validateSelected(user);
        paymentValidationService.validate(paymentRequest, bindingResult);
        if (bindingResult.hasErrors()) {
            prepareCartPaymentModel(model, cart, user, paymentRequest);
            return "pago";
        }
        prepareCartPaymentModel(model, cart, user, paymentRequest);
        model.addAttribute("maskedCard", paymentRequest.maskedCardNumber());
        return "pago-confirmacion";
    }

    @PostMapping("/pago/carrito/confirmar")
    public String confirmCartPayment(
            Authentication authentication,
            @Valid @ModelAttribute PaymentRequest paymentRequest,
            BindingResult bindingResult,
            Model model) {
        AppUser user = userService.currentUser(authentication);
        Cart cart = cartService.getOrCreate(user);
        cartService.validateSelected(user);
        paymentValidationService.validate(paymentRequest, bindingResult);
        if (bindingResult.hasErrors()) {
            prepareCartPaymentModel(model, cart, user, paymentRequest);
            return "pago";
        }
        CustomerOrder order = cartService.checkoutSelected(user);
        order.setStatus(OrderStatus.PAGADO);
        orderRepository.save(order);
        return "redirect:/pago/" + order.getId() + "/factura";
    }

    @GetMapping("/pago/{id}")
    public String payment(@PathVariable Long id, Authentication authentication, Model model) {
        AppUser user = userService.currentUser(authentication);
        CustomerOrder order = findOrder(id, user);
        if (order.getStatus() == OrderStatus.PAGADO) {
            return "redirect:/pago/" + id + "/factura";
        }
        prepareOrderPaymentModel(model, order, user, prefilledRequest(user));
        return "pago";
    }

    @PostMapping("/pago/{id}/revisar")
    public String reviewPayment(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @ModelAttribute PaymentRequest paymentRequest,
            BindingResult bindingResult,
            Model model) {
        AppUser user = userService.currentUser(authentication);
        CustomerOrder order = findOrder(id, user);
        paymentValidationService.validate(paymentRequest, bindingResult);
        if (bindingResult.hasErrors()) {
            prepareOrderPaymentModel(model, order, user, paymentRequest);
            return "pago";
        }
        prepareOrderPaymentModel(model, order, user, paymentRequest);
        model.addAttribute("maskedCard", paymentRequest.maskedCardNumber());
        return "pago-confirmacion";
    }

    @PostMapping("/pago/{id}/confirmar")
    public String confirmPayment(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @ModelAttribute PaymentRequest paymentRequest,
            BindingResult bindingResult,
            Model model) {
        AppUser user = userService.currentUser(authentication);
        CustomerOrder order = findOrder(id, user);
        paymentValidationService.validate(paymentRequest, bindingResult);
        if (bindingResult.hasErrors()) {
            prepareOrderPaymentModel(model, order, user, paymentRequest);
            return "pago";
        }
        order.setStatus(OrderStatus.PAGADO);
        orderRepository.save(order);
        return "redirect:/pago/" + id + "/factura";
    }

    @GetMapping("/pago/{id}/factura")
    public String invoice(@PathVariable Long id, Authentication authentication, Model model) {
        CustomerOrder order = findOrder(id, userService.currentUser(authentication));
        model.addAttribute("order", order);
        model.addAttribute("subtotal", orderSubtotal(order));
        model.addAttribute("issuedAt", LocalDateTime.now());
        return "factura";
    }

    @GetMapping("/pago/{id}/factura.pdf")
    public void invoicePdf(@PathVariable Long id, Authentication authentication, HttpServletResponse response) throws IOException {
        CustomerOrder order = findOrder(id, userService.currentUser(authentication));
        JasperReportService jasperReportService = new JasperReportService();
        byte[] pdf = jasperReportService.invoice(order, orderSubtotal(order));
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=factura-salma-" + order.getId() + ".pdf");
        response.setContentLength(pdf.length);
        response.getOutputStream().write(pdf);
    }

    private void prepareCartPaymentModel(Model model, Cart cart, AppUser user, PaymentRequest paymentRequest) {
        BigDecimal subtotal = cartService.selectedSubtotal(cart);
        BigDecimal shipping = cartService.shippingCost(subtotal);
        model.addAttribute("paymentRequest", paymentRequest);
        model.addAttribute("paymentAction", "/pago/carrito");
        model.addAttribute("backUrl", "/carrito");
        model.addAttribute("lines", cart.getItems().stream()
                .filter(item -> item.isSelected())
                .map(item -> new PaymentLine(
                        item.getProduct().getName(),
                        item.getProduct().getCategory().getName(),
                        item.getProduct().getImages().isEmpty() ? null : item.getProduct().getImages().get(0).getImageUrl(),
                        item.getProduct().getImages().isEmpty() ? item.getProduct().getName() : item.getProduct().getImages().get(0).getAltText(),
                        item.getQuantity(),
                        item.getProduct().getPrice(),
                        item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))))
                .toList());
        addTotals(model, subtotal, shipping);
        model.addAttribute("userPhone", user.getPhone());
    }

    private void prepareOrderPaymentModel(Model model, CustomerOrder order, AppUser user, PaymentRequest paymentRequest) {
        BigDecimal subtotal = orderSubtotal(order);
        model.addAttribute("order", order);
        model.addAttribute("paymentRequest", paymentRequest);
        model.addAttribute("paymentAction", "/pago/" + order.getId());
        model.addAttribute("backUrl", "/pedidos/" + order.getId());
        model.addAttribute("lines", order.getItems().stream()
                .map(item -> new PaymentLine(
                        item.getProduct().getName(),
                        item.getProduct().getCategory().getName(),
                        item.getProduct().getImages().isEmpty() ? null : item.getProduct().getImages().get(0).getImageUrl(),
                        item.getProduct().getImages().isEmpty() ? item.getProduct().getName() : item.getProduct().getImages().get(0).getAltText(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))))
                .toList());
        addTotals(model, subtotal, order.getShippingCost());
        model.addAttribute("userPhone", user.getPhone());
    }

    private void addTotals(Model model, BigDecimal subtotal, BigDecimal shipping) {
        model.addAttribute("banks", BANKS);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shippingCost", shipping);
        model.addAttribute("freeShippingThreshold", CartService.FREE_SHIPPING_THRESHOLD);
        model.addAttribute("freeShippingRemaining", cartService.freeShippingRemaining(subtotal));
        model.addAttribute("total", subtotal.add(shipping));
    }

    private PaymentRequest prefilledRequest(AppUser user) {
        PaymentRequest request = new PaymentRequest();
        request.setCustomerName((user.getFirstName() + " " + user.getFirstLastName()).trim());
        request.setCustomerEmail(user.getEmail());
        request.setCustomerPhone(user.getPhone());
        request.setWalletPhone(user.getPhone());
        request.setShippingAddress(user.getAddress());
        request.setCity(user.getCity().getName());
        request.setCardName((user.getFirstName() + " " + user.getFirstLastName()).toUpperCase());
        return request;
    }

    private CustomerOrder findOrder(Long id, AppUser user) {
        return orderRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
    }

    private BigDecimal orderSubtotal(CustomerOrder order) {
        return order.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
