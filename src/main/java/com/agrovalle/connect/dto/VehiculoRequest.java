package com.agrovalle.connect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Datos para registrar el vehículo del transportista autenticado.
 */
public record VehiculoRequest(
    @NotBlank(message = "El tipo de transporte es obligatorio") String tipoTransporte,
    @NotBlank(message = "La placa es obligatoria")
    @Size(max = 20, message = "La placa no puede superar 20 caracteres") String placa,
    @NotNull(message = "La capacidad es obligatoria")
    @Positive(message = "La capacidad debe ser mayor que cero") BigDecimal capacidadKg) {
}
