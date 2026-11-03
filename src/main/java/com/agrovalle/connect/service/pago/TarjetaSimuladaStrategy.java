package com.agrovalle.connect.service.pago;

import com.agrovalle.connect.model.Pedido;
import com.agrovalle.connect.model.enums.EstadoPago;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 * Simula un pago con tarjeta: rechaza montos superiores al cupo simulado.
 */
@Component("TARJETA")
public class TarjetaSimuladaStrategy implements MedioPagoStrategy {

  private static final BigDecimal CUPO = new BigDecimal("10000000");

  @Override
  public EstadoPago procesar(Pedido pedido, BigDecimal monto) {
    return monto.compareTo(CUPO) > 0 ? EstadoPago.RECHAZADO : EstadoPago.COMPLETADO;
  }
}