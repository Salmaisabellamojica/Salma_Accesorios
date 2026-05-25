package com.salma.salma_accesorios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PaymentRequest {

    @NotBlank(message = "Selecciona un metodo de pago.")
    private String paymentMethod = "card";

    @NotBlank(message = "Confirma tu nombre completo.")
    @Size(min = 4, max = 120, message = "El nombre debe tener entre 4 y 120 caracteres.")
    private String customerName;

    @NotBlank(message = "Confirma tu correo.")
    @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "Escribe un correo valido.")
    private String customerEmail;

    @NotBlank(message = "Confirma tu telefono.")
    @Pattern(regexp = "^[0-9]{10}$", message = "El telefono debe tener 10 digitos.")
    private String customerPhone;

    @NotBlank(message = "Confirma tu direccion.")
    @Size(min = 6, max = 160, message = "La direccion debe tener entre 6 y 160 caracteres.")
    private String shippingAddress;

    @NotBlank(message = "Confirma tu ciudad.")
    @Size(min = 2, max = 80, message = "La ciudad debe tener entre 2 y 80 caracteres.")
    private String city;

    @Size(max = 300)
    private String notes;

    private String cardName;

    private String cardNumber;

    private String expiry;

    private String cvv;

    @Pattern(regexp = "^$|^[0-9]{10}$", message = "El celular debe tener 10 digitos.")
    private String walletPhone;

    private String bank;

    public String normalizedCardNumber() {
        return cardNumber == null ? "" : cardNumber.replaceAll("\\s+", "");
    }

    public String maskedCardNumber() {
        String normalized = normalizedCardNumber();
        if (normalized.length() < 4) {
            return "****";
        }
        return "**** **** **** " + normalized.substring(normalized.length() - 4);
    }
}
