package com.agrovalle.connect.event;

/**
 * Identifica el reporte registrado, su pedido y el agricultor asociado.
 */
public record InconformidadRegistradaEvent(Long inconformidadId, Long pedidoId, Long agricultorId) {
}
