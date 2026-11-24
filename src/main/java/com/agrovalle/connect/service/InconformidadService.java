package com.agrovalle.connect.service;

import com.agrovalle.connect.dto.InconformidadRequest;
import com.agrovalle.connect.dto.InconformidadResponse;
import com.agrovalle.connect.event.InconformidadRegistradaEvent;
import com.agrovalle.connect.exception.ConflictoException;
import com.agrovalle.connect.exception.PedidoInvalidoException;
import com.agrovalle.connect.exception.RecursoNoEncontradoException;
import com.agrovalle.connect.model.Inconformidad;
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
 * Registra inconformidades sobre pedidos del comprador autenticado.
 */
@Service
public class InconformidadService {

  private final InconformidadRepository inconformidadRepository;

  private final PedidoRepository pedidoRepository;

  private final ApplicationEventPublisher eventPublisher;

  public InconformidadService(InconformidadRepository inconformidadRepository,
      PedidoRepository pedidoRepository, ApplicationEventPublisher eventPublisher) {
    this.inconformidadRepository = inconformidadRepository;
    this.pedidoRepository = pedidoRepository;
    this.eventPublisher = eventPublisher;
  }

  /**
   * Registra el reporte en revisión para un pedido en transporte.
   */
  @Transactional
  public InconformidadResponse reportar(Long pedidoId, InconformidadRequest request,
      Long usuarioId) {
    Pedido pedido = pedidoRepository.findByIdParaActualizar(pedidoId)
        .orElseThrow(() -> new RecursoNoEncontradoException("No existe el pedido con id " + pedidoId));
    validarPropietario(pedido, usuarioId);
    if (pedido.getEstado() != EstadoPedido.EN_TRANSPORTE) {
      throw new PedidoInvalidoException("El pedido debe estar en transporte para reportar una inconformidad");
    }
    if (inconformidadRepository.existsByPedidoIdAndEstado(pedidoId, EstadoInconformidad.EN_REVISION)) {
      throw new ConflictoException("El pedido ya tiene una inconformidad en revisión");
    }

    Inconformidad inconformidad = new Inconformidad();
    inconformidad.setPedido(pedido);
    inconformidad.setDescripcion(request.descripcion());
    inconformidad.setEvidenciaUrl(request.evidenciaUrl());
    inconformidad.setEstado(EstadoInconformidad.EN_REVISION);
    inconformidad.setFechaReporte(LocalDateTime.now());
    Inconformidad guardada = inconformidadRepository.save(inconformidad);
    eventPublisher.publishEvent(new InconformidadRegistradaEvent(
        guardada.getId(), pedidoId, pedido.getAgricultor().getId()));
    return aRespuesta(guardada);
  }

  /**
   * Devuelve el reporte más reciente de un pedido del comprador autenticado.
   */
  @Transactional(readOnly = true)
  public InconformidadResponse consultar(Long pedidoId, Long usuarioId) {
    Pedido pedido = pedidoRepository.findById(pedidoId)
        .orElseThrow(() -> new RecursoNoEncontradoException("No existe el pedido con id " + pedidoId));
    validarPropietario(pedido, usuarioId);
    return inconformidadRepository.findByPedidoIdOrderByFechaReporteDesc(pedidoId).stream()
        .findFirst()
        .map(this::aRespuesta)
        .orElseThrow(() -> new RecursoNoEncontradoException(
            "No existe una inconformidad para el pedido con id " + pedidoId));
  }

  private void validarPropietario(Pedido pedido, Long usuarioId) {
    if (!pedido.getComprador().getUsuario().getId().equals(usuarioId)) {
      throw new RecursoNoEncontradoException("No existe el pedido con id " + pedido.getId());
    }
  }

  private InconformidadResponse aRespuesta(Inconformidad inconformidad) {
    return new InconformidadResponse(inconformidad.getId(), inconformidad.getPedido().getId(),
        inconformidad.getDescripcion(), inconformidad.getEvidenciaUrl(), inconformidad.getEstado(),
        inconformidad.getResolucion(), inconformidad.getFechaReporte());
  }
}
