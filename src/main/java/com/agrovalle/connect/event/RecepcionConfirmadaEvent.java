package com.agrovalle.connect.event;

import java.time.LocalDateTime;

/**
 * Identifica el pedido cuya recepción confirmó el comprador, y a las partes involucradas.
 * Es el punto de extensión para la liberación del pago (HU-11).
 */
public record RecepcionConfirmadaEvent(
    Long pedidoId,
    Long compradorId,
    Long agricultorId,
    LocalDateTime fechaRecepcion) {
}