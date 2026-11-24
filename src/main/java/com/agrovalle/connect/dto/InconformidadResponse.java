package com.agrovalle.connect.dto;

import com.agrovalle.connect.model.enums.EstadoInconformidad;
import java.time.LocalDateTime;

/**
 * Datos del reporte y estado de su revisión.
 */
public record InconformidadResponse(
    Long id,
    Long pedidoId,
    String descripcion,
    String evidenciaUrl,
    EstadoInconformidad estado,
    String resolucion,
    LocalDateTime fechaReporte) {
}
