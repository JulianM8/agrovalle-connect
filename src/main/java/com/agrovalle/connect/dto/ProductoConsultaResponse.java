package com.agrovalle.connect.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.agrovalle.connect.model.enums.UnidadMedida;

/**
 * Datos de un producto que se muestra en una consulta del catálogo.
 */
public record ProductoConsultaResponse(
    Long id,
    String nombre,
    String categoria,
    Integer cantidad,
    UnidadMedida unidadMedida,
    BigDecimal precio,
    LocalDate fechaCosecha,
    Long agricultorId) {
}
