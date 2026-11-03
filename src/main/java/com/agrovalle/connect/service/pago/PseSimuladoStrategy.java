package com.agrovalle.connect.service.pago;

import com.agrovalle.connect.model.Pedido;
import com.agrovalle.connect.model.enums.EstadoPago;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 * Simula un pago por PSE: rechaza montos superiores al límite simulado.
 */
@Component("PSE")
public class PseSimuladoStrategy implements MedioPagoStrategy {

  private static final BigDecimal LIMITE = new BigDecimal("50000000");

  @Override
  public EstadoPago procesar(Pedido pedido, BigDecimal monto) {
    return monto.compareTo(LIMITE) > 0 ? EstadoPago.RECHAZADO : EstadoPago.COMPLETADO;
  }
}