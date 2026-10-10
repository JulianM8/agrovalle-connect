package com.agrovalle.connect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos que el agricultor envía al publicar un producto.
 */
public record ProductoPublicacionRequest(
        @NotBlank(message = "El nombre del producto es obligatorio") String nombre,
        @NotBlank(message = "La categoría es obligatoria") String categoria,
        @NotNull(message = "La cantidad es obligatoria") 
        @Positive(message = "La cantidad debe ser mayor a 0") Integer cantidad,
        @NotNull(message = "El precio es obligatorio") 
        @Positive(message = "El precio debe ser mayor a 0") BigDecimal precio,
        @NotNull(message = "La fecha de cosecha es obligatoria") LocalDate fechaCosecha) {
}