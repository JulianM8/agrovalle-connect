package com.agrovalle.connect.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "La identificación es obligatoria") String identificacion,
        @NotBlank(message = "La contraseña es obligatoria") String contrasena) {
}
