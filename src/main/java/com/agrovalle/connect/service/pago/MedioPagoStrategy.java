package com.agrovalle.connect.service.pago;

import com.agrovalle.connect.model.Pedido;
import com.agrovalle.connect.model.enums.EstadoPago;
import java.math.BigDecimal;

/**
 * Estrategia de procesamiento de un medio de pago. Cada medio implementa su
 * propia regla y PagoService elige cuál usar sin conocer los detalles.
 */
public interface MedioPagoStrategy {

  /**
   * Procesa el cobro del pedido y devuelve el estado resultante del pago.
   */
  EstadoPago procesar(Pedido pedido, BigDecimal monto);
}