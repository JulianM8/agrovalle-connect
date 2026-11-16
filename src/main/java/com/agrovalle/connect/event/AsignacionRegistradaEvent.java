package com.agrovalle.connect.event;

/**
 * Se publica al asignar transporte a un pedido; un futuro listener puede
 * avisar al comprador por correo o push sin modificar DespachoService.
 */
public record AsignacionRegistradaEvent(Long asignacionId, Long pedidoId, Long compradorId) {
}
