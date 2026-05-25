package com.salma.salma_accesorios.service;

import com.salma.salma_accesorios.dto.PaymentRequest;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

@Service
public class PaymentValidationService {

    private static final DateTimeFormatter EXPIRY_FORMAT = DateTimeFormatter.ofPattern("MM/yy");

    public void validate(PaymentRequest request, BindingResult bindingResult) {
        String method = request.getPaymentMethod() == null ? "" : request.getPaymentMethod();
        if (!List.of("card", "nequi", "pse", "efecty", "daviplata").contains(method)) {
            bindingResult.rejectValue("paymentMethod", "payment.method", "Selecciona un metodo de pago valido.");
            return;
        }
        if ("nequi".equals(method) || "daviplata".equals(method)) {
            if (request.getWalletPhone() == null || !request.getWalletPhone().matches("[0-9]{10}")) {
                bindingResult.rejectValue("walletPhone", "wallet.phone", "El celular de la billetera debe tener 10 digitos.");
            }
            return;
        }
        if ("pse".equals(method)) {
            if (request.getBank() == null || request.getBank().isBlank()) {
                bindingResult.rejectValue("bank", "bank.required", "Selecciona tu banco.");
            }
            return;
        }
        if ("efecty".equals(method)) {
            return;
        }
        validateCardFields(request, bindingResult);
        if (bindingResult.hasFieldErrors("cardNumber")) {
            return;
        }
        String cardNumber = request.normalizedCardNumber();
        if (!cardNumber.matches("\\d{16}")) {
            bindingResult.rejectValue("cardNumber", "card.invalid", "Ingresa los 16 digitos de la tarjeta.");
            return;
        }
        if (!passesLuhn(cardNumber)) {
            bindingResult.rejectValue("cardNumber", "card.invalid", "Revisa el numero de tarjeta. Para pruebas puedes usar 4242 4242 4242 4242.");
        }
        validateExpiry(request, bindingResult);
    }

    private void validateCardFields(PaymentRequest request, BindingResult bindingResult) {
        if (request.getCardName() == null || !request.getCardName().matches("[A-Za-zÁÉÍÓÚáéíóúÑñ ]{4,80}")) {
            bindingResult.rejectValue("cardName", "card.name", "El nombre debe tener solo letras y entre 4 y 80 caracteres.");
        }
        if (request.getCardNumber() == null || !request.getCardNumber().matches("[0-9 ]{16,19}")) {
            bindingResult.rejectValue("cardNumber", "card.number", "Ingresa solo numeros, sin puntos ni guiones.");
        }
        if (request.getExpiry() == null || !request.getExpiry().matches("(0[1-9]|1[0-2])/[0-9]{2}")) {
            bindingResult.rejectValue("expiry", "card.expiry", "Usa el formato MM/AA.");
        }
        if (request.getCvv() == null || !request.getCvv().matches("[0-9]{3,4}")) {
            bindingResult.rejectValue("cvv", "card.cvv", "El CVV debe tener 3 o 4 digitos.");
        }
    }

    private void validateExpiry(PaymentRequest request, BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors("expiry")) {
            return;
        }
        try {
            YearMonth expiry = YearMonth.parse(request.getExpiry(), EXPIRY_FORMAT);
            if (expiry.isBefore(YearMonth.now())) {
                bindingResult.rejectValue("expiry", "expiry.expired", "La tarjeta esta vencida.");
            }
        } catch (DateTimeParseException exception) {
            bindingResult.rejectValue("expiry", "expiry.invalid", "Usa el formato MM/AA.");
        }
    }

    private boolean passesLuhn(String value) {
        int sum = 0;
        boolean alternate = false;
        for (int index = value.length() - 1; index >= 0; index--) {
            int digit = Character.digit(value.charAt(index), 10);
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }
}
