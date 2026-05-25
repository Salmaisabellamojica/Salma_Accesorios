package com.salma.salma_accesorios.dto;

import java.math.BigDecimal;

public record PaymentLine(
        String name,
        String category,
        String imageUrl,
        String altText,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
