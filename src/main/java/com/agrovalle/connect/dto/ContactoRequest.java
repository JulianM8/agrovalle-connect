package com.agrovalle.connect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Mensaje que un comprador envía sobre un producto.
 */
public record ContactoRequest(
    @NotBlank @Size(max = 1000) String mensaje) {
}
