package com.agrovalle.connect.dto;

import java.util.List;

/**
 * Lista de vehículos de un tipo de transporte, con un mensaje descriptivo
 * (por ejemplo, cuando no hay ninguno disponible).
 */
public record VehiculosPorTipoResponse(String mensaje, List<VehiculoResponse> vehiculos) {
}
