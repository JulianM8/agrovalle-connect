package com.agrovalle.connect.service.pago;

import com.agrovalle.connect.model.Pedido;
import com.agrovalle.connect.model.enums.EstadoPago;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 * Pago contra entrega: queda PENDIENTE hasta que se cobre al recibir el pedido.
 */
@Component("CONTRAENTREGA")
public class ContraentregaStrategy implements MedioPagoStrategy {

  @Override
  public EstadoPago procesar(Pedido pedido, BigDecimal monto) {
    return EstadoPago.PENDIENTE;
  }
}