package com.agrovalle.connect.listener;

import com.agrovalle.connect.event.RecepcionConfirmadaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Observador de {@link RecepcionConfirmadaEvent}. Por ahora solo deja traza;
 * HU-11 lo reemplazará por la liberación del pago retenido.
 */
@Component
public class RecepcionConfirmadaListener {

  private static final Logger LOG =
      LoggerFactory.getLogger(RecepcionConfirmadaListener.class);

  /**
   * Se ejecuta cuando la transacción de la confirmación ya se guardó.
   */
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void alConfirmarRecepcion(RecepcionConfirmadaEvent evento) {
    LOG.info("Recepción confirmada: pedido={}, comprador={}, agricultor={}, fecha={}",
        evento.pedidoId(),
        evento.compradorId(),
        evento.agricultorId(),
        evento.fechaRecepcion());
  }
}