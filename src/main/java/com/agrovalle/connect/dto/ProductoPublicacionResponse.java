package com.agrovalle.connect.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos que el sistema devuelve tras publicar un producto.
 */
public record ProductoPublicacionResponse(
        Long id,
        String nombre,
        String categoria,
        Integer cantidad,
        BigDecimal precio,
        LocalDate fechaCosecha,
        Long agricultorId,
        String mensaje) {
}