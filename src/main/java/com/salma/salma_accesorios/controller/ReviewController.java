package com.salma.salma_accesorios.controller;

import com.salma.salma_accesorios.dto.ReviewRequest;
import com.salma.salma_accesorios.model.AppUser;
import com.salma.salma_accesorios.service.ReviewImageStorageService;
import com.salma.salma_accesorios.service.ReviewService;
import com.salma.salma_accesorios.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;
    private final ReviewImageStorageService reviewImageStorageService;

    public ReviewController(
            ReviewService reviewService,
            UserService userService,
            ReviewImageStorageService reviewImageStorageService) {
        this.reviewService = reviewService;
        this.userService = userService;
        this.reviewImageStorageService = reviewImageStorageService;
    }

    @GetMapping("/resenas")
    public String customerReviews(Authentication authentication, Model model) {
        AppUser user = userService.currentUser(authentication);
        model.addAttribute("pendingReviewItems", reviewService.pendingReviewsFor(user));
        model.addAttribute("myReviews", reviewService.reviewsBy(user));
        return "resenas";
    }

    @PostMapping("/producto/{productId}/resenas")
    public String create(
            Authentication authentication,
            @PathVariable Long productId,
            @Valid @ModelAttribute ReviewRequest request,
            BindingResult bindingResult,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Revisa la calificacion y el comentario");
            return "redirect:/producto/" + productId;
        }
        try {
            request.setImageUrl(reviewImageStorageService.store(imageFile));
            reviewService.create(userService.currentUser(authentication), productId, request);
            redirectAttributes.addFlashAttribute("success", "Gracias por tu resena");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/producto/" + productId;
    }
}
