package com.agrovalle.connect.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Datos que el usuario envía al registrarse como agricultor.
 */
public record AgricultorRegistroRequest(
    @NotBlank(message = "El nombre es obligatorio") String nombre,
    @NotBlank(message = "El apellido es obligatorio") String apellido,
    @NotBlank(message = "La identificación es obligatoria") String identificacion,
    @NotBlank(message = "El municipio es obligatorio") String municipio,
    @NotBlank(message = "El teléfono es obligatorio") String telefono,
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido") String correo) {
}