package com.agrovalle.connect.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Ruta del día de un transportista: fecha, vehículo y paradas ordenadas.
 */
public record RutaDiaResponse(
    LocalDate fecha,
    String vehiculo,
    List<ParadaRutaResponse> paradas) {
}