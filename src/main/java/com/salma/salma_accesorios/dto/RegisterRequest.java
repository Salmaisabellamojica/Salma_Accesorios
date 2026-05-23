package com.salma.salma_accesorios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    private static final String NAME_REGEX = "^[A-Za-zÁÉÍÓÚáéíóúÑñ]{2,60}$";

    @NotBlank
    @Pattern(regexp = NAME_REGEX, message = "El primer nombre solo debe contener letras y minimo 2 caracteres")
    private String firstName;

    @Pattern(regexp = "^$|" + NAME_REGEX, message = "El segundo nombre solo debe contener letras")
    private String secondName;

    @NotBlank
    @Pattern(regexp = NAME_REGEX, message = "El primer apellido solo debe contener letras")
    private String firstLastName;

    @NotBlank
    @Pattern(regexp = NAME_REGEX, message = "El segundo apellido solo debe contener letras")
    private String secondLastName;

    @NotBlank
    @Email
    @Size(max = 120)
    private String email;

    @NotBlank
    @Pattern(regexp = "^3\\d{9}$", message = "El celular debe ser colombiano, iniciar en 3 y tener 10 digitos")
    private String phone;

    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9._-]{4,40}$", message = "El usuario debe tener 4 a 40 caracteres: letras, numeros, punto, guion o guion bajo")
    private String username;

    @NotBlank
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ ]{2,80}$", message = "La ciudad solo debe contener letras")
    private String city;

    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9ÁÉÍÓÚáéíóúÑñ#.,\\- ]{8,160}$", message = "La direccion debe tener entre 8 y 160 caracteres validos")
    private String address;

    @NotBlank
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.#_-])[A-Za-z\\d@$!%*?&.#_-]{8,64}$",
            message = "La contrasena debe tener mayuscula, minuscula, numero, simbolo y minimo 8 caracteres"
    )
    private String password;

    @NotBlank
    private String confirmPassword;
}
