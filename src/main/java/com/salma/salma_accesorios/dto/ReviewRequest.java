package com.salma.salma_accesorios.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewRequest {
    @Min(1)
    @Max(5)
    private Integer rating;

    @NotBlank
    @Size(min = 8, max = 800)
    private String comment;

    @Size(max = 500)
    private String imageUrl;
}
