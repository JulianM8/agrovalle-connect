package com.agrovalle.connect.service;

import com.agrovalle.connect.dto.RecepcionResponse;
import com.agrovalle.connect.event.RecepcionConfirmadaEvent;
import com.agrovalle.connect.exception.ConflictoException;
import com.agrovalle.connect.exception.PedidoInvalidoException;
import com.agrovalle.connect.exception.RecursoNoEncontradoException;
import com.agrovalle.connect.model.Pedido;
import com.agrovalle.connect.model.enums.EstadoInconformidad;
import com.agrovalle.connect.model.enums.EstadoPedido;
import com.agrovalle.connect.repository.InconformidadRepository;
import com.agrovalle.connect.repository.PedidoRepository;
import java.time.LocalDateTime;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Permite al comprador confirmar que recibió la mercancía de un pedido.
 */
@Service
public class RecepcionService {

  private final PedidoRepository pedidoRepository;
  
  private final InconformidadRepository inconformidadRepository;
  
  private final ApplicationEventPublisher eventPublisher;

  /**
   * Crea el servicio con sus dependencias.
   */
  public RecepcionService(
      PedidoRepository pedidoRepository,
      InconformidadRepository inconformidadRepository,
      ApplicationEventPublisher eventPublisher) {
    this.pedidoRepository = pedidoRepository;
    this.inconformidadRepository = inconformidadRepository;
    this.eventPublisher = eventPublisher;
  }

  /**
   * Marca el pedido como ENTREGADO y publica {@link RecepcionConfirmadaEvent}.
   *
   * @param pedidoId pedido cuya recepción se confirma
   * @param usuarioId comprador autenticado
   */
  @Transactional
  public RecepcionResponse confirmarRecepcion(Long pedidoId, Long usuarioId) {
    // 404 tanto si no existe como si es de otro comprador (no revela su existencia)
    Pedido pedido = pedidoRepository.findByIdParaActualizar(pedidoId)
        .filter(p -> p.getComprador().getUsuario().getId().equals(usuarioId))
        .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado."));

    if (pedido.getEstado() == EstadoPedido.ENTREGADO) {
      throw new ConflictoException("El pedido ya fue confirmado como entregado.");
    }
    if (pedido.getEstado() != EstadoPedido.EN_TRANSPORTE) {
      throw new PedidoInvalidoException(
          "Solo se puede confirmar la recepción de un pedido en transporte.");
    }
    if (inconformidadRepository.existsByPedidoIdAndEstado(
        pedidoId, EstadoInconformidad.EN_REVISION)) {
      throw new ConflictoException("El pedido tiene una inconformidad en revisión.");
    }

    LocalDateTime ahora = LocalDateTime.now();
    pedido.setEstado(EstadoPedido.ENTREGADO);
    pedido.setFechaEntrega(ahora);
    pedidoRepository.save(pedido);

    eventPublisher.publishEvent(new RecepcionConfirmadaEvent(
        pedido.getId(),
        pedido.getComprador().getId(),
        pedido.getAgricultor().getId(),
        ahora));

    return new RecepcionResponse(pedido.getId(), pedido.getEstado(), ahora);
  }
}