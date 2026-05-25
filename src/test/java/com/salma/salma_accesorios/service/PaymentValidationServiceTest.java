package com.salma.salma_accesorios.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.salma.salma_accesorios.dto.PaymentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;

class PaymentValidationServiceTest {

    private final PaymentValidationService service = new PaymentValidationService();

    @Test
    void cardPaymentAcceptsOnlySixteenDigitLuhnCards() {
        PaymentRequest request = validRequest();
        request.setCardNumber("4242 4242 4242 4242");
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(request, "paymentRequest");

        service.validate(request, result);

        assertThat(result.hasFieldErrors("cardNumber")).isFalse();
    }

    @Test
    void cardPaymentRejectsLongCardNumbers() {
        PaymentRequest request = validRequest();
        request.setCardNumber("4242 4242 4242 4242 424");
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(request, "paymentRequest");

        service.validate(request, result);

        assertThat(result.hasFieldErrors("cardNumber")).isTrue();
    }

    private PaymentRequest validRequest() {
        PaymentRequest request = new PaymentRequest();
        request.setPaymentMethod("card");
        request.setCardName("SALMA MOJICA");
        request.setExpiry("12/30");
        request.setCvv("123");
        return request;
    }
}
