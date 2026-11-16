package com.agrovalle.connect.dto;

import java.math.BigDecimal;

/**
 * Datos de un vehículo junto con el transportista que lo conduce.
 */
public record VehiculoResponse(
    Long id,
    String tipoTransporte,
    String placa,
    BigDecimal capacidadKg,
    String transportista,
    String municipio) {
}
