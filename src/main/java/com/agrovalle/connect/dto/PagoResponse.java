package com.agrovalle.connect.dto;

import com.agrovalle.connect.model.enums.EstadoPago;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Respuesta con el resultado o estado de un pago.
 */
public record PagoResponse(
    Long id,
    Long pedidoId,
    String medioPago,
    String referenciaTransaccion,
    BigDecimal monto,
    EstadoPago estado,
    LocalDateTime fechaPago,
    String mensaje) {
}