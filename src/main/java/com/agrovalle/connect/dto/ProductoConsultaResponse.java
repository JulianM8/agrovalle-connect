package com.agrovalle.connect.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos de un producto que se muestra en una consulta del catálogo.
 */
public record ProductoConsultaResponse(
    Long id,
    String nombre,
    String categoria,
    Integer cantidad,
    BigDecimal precio,
    LocalDate fechaCosecha,
    Long agricultorId) {
}
