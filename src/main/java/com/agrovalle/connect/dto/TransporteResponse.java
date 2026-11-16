package com.agrovalle.connect.dto;

import java.time.LocalDate;

/**
 * Transporte asignado a un pedido, para que el comprador lo consulte.
 */
public record TransporteResponse(
    Long pedidoId,
    String tipoTransporte,
    String placa,
    LocalDate fechaRuta) {
}
