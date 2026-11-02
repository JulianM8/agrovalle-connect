package com.agrovalle.connect.dto;

import com.agrovalle.connect.model.enums.EstadoPago;
import java.math.BigDecimal;

public record PagoResponse(
    Long id,
    Long pedidoId,
    String referenciaTransaccion,
    BigDecimal monto,
    EstadoPago estado,
    String medioPago) {
}