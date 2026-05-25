package com.salma.salma_accesorios.dto;

import java.io.Serializable;

public record PendingGoogleUser(
        String email,
        String firstName,
        String secondName,
        String firstLastName,
        String username
) implements Serializable {
}
