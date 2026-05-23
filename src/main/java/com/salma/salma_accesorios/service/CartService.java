package com.salma.salma_accesorios.service;

import com.salma.salma_accesorios.model.AppUser;
import com.salma.salma_accesorios.model.Cart;
import com.salma.salma_accesorios.model.CartItem;
import com.salma.salma_accesorios.model.CustomerOrder;
import com.salma.salma_accesorios.model.OrderItem;
import com.salma.salma_accesorios.model.OrderStatus;
import com.salma.salma_accesorios.model.Product;
import com.salma.salma_accesorios.repository.CartItemRepository;
import com.salma.salma_accesorios.repository.CartRepository;
import com.salma.salma_accesorios.repository.CustomerOrderRepository;
import com.salma.salma_accesorios.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CustomerOrderRepository orderRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            CustomerOrderRepository orderRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Cart getOrCreate(AppUser user) {
        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(user);
            return cartRepository.save(cart);
        });
    }

    @Transactional
    public void add(AppUser user, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        Cart cart = getOrCreate(user);
        Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        if (!product.isAvailable()) {
            throw new IllegalArgumentException("Producto agotado");
        }
        CartItem item = cartItemRepository.findByCartAndProduct(cart, product).orElseGet(() -> {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(0);
            newItem.setSelected(true);
            return newItem;
        });
        item.setQuantity(Math.min(product.getStock(), item.getQuantity() + quantity));
        cartItemRepository.save(item);
    }

    @Transactional
    public void updateSelection(AppUser user, List<Long> selectedItemIds) {
        Cart cart = getOrCreate(user);
        List<Long> selected = selectedItemIds == null ? List.of() : selectedItemIds;
        for (CartItem item : cart.getItems()) {
            item.setSelected(selected.contains(item.getId()));
        }
    }

    @Transactional
    public void remove(AppUser user, Long itemId) {
        Cart cart = getOrCreate(user);
        CartItem item = cart.getItems().stream()
                .filter(cartItem -> cartItem.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item no encontrado"));
        cart.getItems().remove(item);
    }

    @Transactional
    public CustomerOrder checkoutSelected(AppUser user) {
        Cart cart = getOrCreate(user);
        List<CartItem> selected = new ArrayList<>(cart.getItems().stream().filter(CartItem::isSelected).toList());
        if (selected.isEmpty()) {
            throw new IllegalArgumentException("Selecciona al menos un producto para comprar");
        }

        CustomerOrder order = new CustomerOrder();
        order.setUser(user);
        order.setShippingAddress(user.getAddress());
        order.setStatus(OrderStatus.PENDIENTE);

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : selected) {
            Product product = cartItem.getProduct();
            if (!product.isAvailable() || product.getStock() < cartItem.getQuantity()) {
                throw new IllegalArgumentException("Stock insuficiente para " + product.getName());
            }
            product.setStock(product.getStock() - cartItem.getQuantity());
            product.setSoldCount(product.getSoldCount() + cartItem.getQuantity());

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(cartItem.getQuantity());
            item.setUnitPrice(product.getPrice());
            order.getItems().add(item);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }
        order.setTotal(total);
        CustomerOrder saved = orderRepository.save(order);
        cart.getItems().removeAll(selected);
        return saved;
    }
}
