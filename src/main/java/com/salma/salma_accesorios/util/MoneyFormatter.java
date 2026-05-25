package com.salma.salma_accesorios.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component("money")
public class MoneyFormatter {

    private static final Locale COLOMBIA = Locale.forLanguageTag("es-CO");

    public String cop(BigDecimal value) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(COLOMBIA);
        formatter.setMaximumFractionDigits(0);
        formatter.setMinimumFractionDigits(0);
        return formatter.format(value == null ? BigDecimal.ZERO : value);
    }

    public String cop(BigDecimal value, Integer quantity) {
        BigDecimal amount = value == null ? BigDecimal.ZERO : value;
        int safeQuantity = quantity == null ? 0 : quantity;
        return cop(amount.multiply(BigDecimal.valueOf(safeQuantity)));
    }
}
