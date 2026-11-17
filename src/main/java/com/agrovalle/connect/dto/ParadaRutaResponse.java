package com.agrovalle.connect.dto;

import com.agrovalle.connect.model.enums.EstadoPedido;

/**
 * Parada de la ruta del día con su orden sugerido de entrega.
 */
public record ParadaRutaResponse(
    int orden,
    Long pedidoId,
    String nombreComprador,
    String municipio,
    String direccionEntrega,
    EstadoPedido estadoPedido) {
}