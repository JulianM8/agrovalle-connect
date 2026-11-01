package com.agrovalle.connect.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Datos que el usuario envía al registrarse como comprador. La latitud y la
 * longitud son opcionales.
 */
public record CompradorRegistroRequest(
    @NotBlank(message = "El nombre es obligatorio") String nombre,
    @NotBlank(message = "El apellido es obligatorio") String apellido,
    @NotBlank(message = "La identificación es obligatoria")
    @Pattern(regexp = "^[0-9]{10}$",
        message = "La identificación debe tener exactamente 10 dígitos") String identificacion,
    @NotBlank(message = "El teléfono es obligatorio") String telefono,
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido") String correo,
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres") String contrasena,
    @NotBlank(message = "El municipio es obligatorio") String municipio,
    @NotBlank(message = "La dirección de entrega es obligatoria") String direccionEntrega,
    @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90")
    BigDecimal latitud,
    @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180")
    BigDecimal longitud) {
}
