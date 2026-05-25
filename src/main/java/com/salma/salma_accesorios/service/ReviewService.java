package com.salma.salma_accesorios.service;

import com.salma.salma_accesorios.dto.ReviewRequest;
import com.salma.salma_accesorios.model.AppUser;
import com.salma.salma_accesorios.model.OrderItem;
import com.salma.salma_accesorios.model.OrderStatus;
import com.salma.salma_accesorios.model.Product;
import com.salma.salma_accesorios.model.Review;
import com.salma.salma_accesorios.model.ReviewImage;
import com.salma.salma_accesorios.repository.OrderItemRepository;
import com.salma.salma_accesorios.repository.ProductRepository;
import com.salma.salma_accesorios.repository.ReviewRepository;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    public ReviewService(ReviewRepository reviewRepository, OrderItemRepository orderItemRepository, ProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
    }

    public List<Review> reviewsFor(Product product) {
        return reviewRepository.findByProductAndApprovedTrueOrderByCreatedAtDesc(product);
    }

    public boolean hasPurchasedProduct(AppUser user, Product product) {
        return user != null && orderItemRepository.existsPurchasedProduct(
                user,
                product,
                List.of(OrderStatus.PAGADO, OrderStatus.ENVIADO, OrderStatus.ENTREGADO)
        );
    }

    public boolean hasReviewed(AppUser user, Product product) {
        return user != null && reviewRepository.existsByUserAndProduct(user, product);
    }

    public boolean canReview(AppUser user, Product product) {
        return hasPurchasedProduct(user, product) && !hasReviewed(user, product);
    }

    public List<OrderItem> pendingReviewsFor(AppUser user) {
        List<OrderItem> purchasedItems = orderItemRepository.findReviewablePurchasedItems(
                user,
                List.of(OrderStatus.PAGADO, OrderStatus.ENVIADO, OrderStatus.ENTREGADO)
        );
        Set<Long> productIds = new LinkedHashSet<>();
        List<OrderItem> pending = new ArrayList<>();
        for (OrderItem item : purchasedItems) {
            Long productId = item.getProduct().getId();
            if (productIds.add(productId) && !hasReviewed(user, item.getProduct())) {
                pending.add(item);
            }
        }
        return pending;
    }

    public List<Review> reviewsBy(AppUser user) {
        return reviewRepository.findByUserWithProductOrderByCreatedAtDesc(user);
    }

    @Transactional
    public Review create(AppUser user, Long productId, ReviewRequest request) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        if (!hasPurchasedProduct(user, product)) {
            throw new IllegalArgumentException("Solo puedes resenar productos comprados");
        }
        if (hasReviewed(user, product)) {
            throw new IllegalArgumentException("Ya resenaste este producto");
        }

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(request.getRating());
        review.setComment(request.getComment().trim());
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            ReviewImage image = new ReviewImage();
            image.setReview(review);
            image.setImageUrl(request.getImageUrl().trim());
            review.getImages().add(image);
        }
        Review saved = reviewRepository.save(review);
        product.setAverageRating(reviewRepository.averageRating(product).setScale(2, RoundingMode.HALF_UP));
        return saved;
    }

    @Transactional
    public void deleteAsAdmin(Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new IllegalArgumentException("Resena no encontrada"));
        Product product = review.getProduct();
        reviewRepository.delete(review);
        product.setAverageRating(reviewRepository.averageRating(product).setScale(2, RoundingMode.HALF_UP));
    }
}
