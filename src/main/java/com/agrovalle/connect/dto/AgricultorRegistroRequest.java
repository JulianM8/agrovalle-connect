package com.agrovalle.connect.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Datos que el usuario envía al registrarse como agricultor.
 */
public record AgricultorRegistroRequest(
    @NotBlank(message = "El nombre es obligatorio") String nombre,
    @NotBlank(message = "El apellido es obligatorio") String apellido,
    @NotBlank(message = "La identificación es obligatoria") 
    @Pattern(regexp = "^[0-9]{10}$", message = "La identificación debe tener exactamente 10 dígitos")
    String identificacion,
    @NotBlank(message = "El municipio es obligatorio") String municipio,
    @NotBlank(message = "El teléfono es obligatorio") String telefono,
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido") String correo) {
}