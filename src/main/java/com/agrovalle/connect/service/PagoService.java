package com.agrovalle.connect.service;

import com.agrovalle.connect.dto.PagoRequest;
import com.agrovalle.connect.dto.PagoResponse;
import com.agrovalle.connect.exception.PedidoInvalidoException;
import com.agrovalle.connect.exception.RecursoNoEncontradoException;
import com.agrovalle.connect.model.Pago;
import com.agrovalle.connect.model.Pedido;
import com.agrovalle.connect.model.enums.EstadoPago;
import com.agrovalle.connect.model.enums.EstadoPedido;
import com.agrovalle.connect.repository.PagoRepository;
import com.agrovalle.connect.repository.PedidoRepository;
import com.agrovalle.connect.service.pago.MedioPagoStrategy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contexto del patrón Strategy: delega el procesamiento en la estrategia del
 * medio de pago pedido, registra el Pago y, si queda COMPLETADO, confirma el pedido.
 */
@Service
public class PagoService {

  private final PedidoService pedidoService;

  private final PedidoRepository pedidoRepository;

  private final PagoRepository pagoRepository;

  private final Map<String, MedioPagoStrategy> estrategias;

  public PagoService(PedidoService pedidoService, PedidoRepository pedidoRepository,
      PagoRepository pagoRepository, Map<String, MedioPagoStrategy> estrategias) {
    this.pedidoService = pedidoService;
    this.pedidoRepository = pedidoRepository;
    this.pagoRepository = pagoRepository;
    this.estrategias = estrategias;
  }

  /**
   * Inicia un pago para el pedido. Si un intento anterior fue rechazado, el
   * pedido sigue PENDIENTE y se puede reintentar.
   */
  @Transactional
  public PagoResponse pagar(Long pedidoId, PagoRequest request, Long usuarioId) {
    Pedido pedido = pedidoService.obtenerPedidoDelComprador(pedidoId, usuarioId);
    if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
      throw new PedidoInvalidoException(
          "El pedido no admite pagos en estado " + pedido.getEstado());
    }

    String medio = request.medioPago().trim().toUpperCase();
    MedioPagoStrategy estrategia = estrategias.get(medio);
    if (estrategia == null) {
      throw new PedidoInvalidoException(
          "Medio de pago no soportado: " + request.medioPago());
    }

    BigDecimal monto = pedidoService.calcularTotal(pedidoId);
    EstadoPago resultado = estrategia.procesar(pedido, monto);

    Pago pago = new Pago(pedido, medio, "PAY-" + UUID.randomUUID(), monto);
    pago.setEstado(resultado);
    if (resultado == EstadoPago.COMPLETADO) {
      pago.setFechaPago(LocalDateTime.now());
      pedido.setEstado(EstadoPedido.CONFIRMADO);
      pedidoRepository.save(pedido);
    }
    return aRespuesta(pagoRepository.save(pago));
  }

  /**
   * Consulta el estado del pago más reciente del pedido.
   */
  @Transactional(readOnly = true)
  public PagoResponse consultar(Long pedidoId, Long usuarioId) {
    pedidoService.obtenerPedidoDelComprador(pedidoId, usuarioId);
    Pago pago = pagoRepository.findFirstByPedidoIdOrderByIdDesc(pedidoId)
        .orElseThrow(() -> new RecursoNoEncontradoException(
            "El pedido " + pedidoId + " no tiene pagos registrados"));
    return aRespuesta(pago);
  }

  private PagoResponse aRespuesta(Pago pago) {
    String mensaje = switch (pago.getEstado()) {
      case COMPLETADO -> "Pago completado, pedido confirmado";
      case RECHAZADO -> "Pago rechazado, puede reintentar";
      case PENDIENTE -> "Pago pendiente, se cobrará contra entrega";
      case REEMBOLSADO -> "Pago reembolsado";
    };
    return new PagoResponse(
        pago.getId(),
        pago.getPedido().getId(),
        pago.getMedioPago(),
        pago.getReferenciaTransaccion(),
        pago.getMonto(),
        pago.getEstado(),
        pago.getFechaPago(),
        mensaje);
  }
}