package com.agrovalle.connect.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Representa la solicitud de inicio de sesión de un agricultor.
 */
public record LoginRequest(
        @NotBlank(message = "La identificación es obligatoria") String identificacion,
        @NotBlank(message = "La contraseña es obligatoria") String contrasena) {
}
