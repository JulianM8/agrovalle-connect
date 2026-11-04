package com.agrovalle.connect.service;

import com.agrovalle.connect.dto.ContactoRequest;
import com.agrovalle.connect.dto.ContactoResponse;
import com.agrovalle.connect.event.ContactoRegistradoEvent;
import com.agrovalle.connect.exception.RecursoNoEncontradoException;
import com.agrovalle.connect.model.Comprador;
import com.agrovalle.connect.model.Contacto;
import com.agrovalle.connect.model.Producto;
import com.agrovalle.connect.repository.CompradorRepository;
import com.agrovalle.connect.repository.ContactoRepository;
import com.agrovalle.connect.repository.ProductoRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registra contactos sobre productos y permite responder al agricultor dueño.
 */
@Service
public class ContactoService {

  private final ContactoRepository contactoRepository;

  private final CompradorRepository compradorRepository;

  private final ProductoRepository productoRepository;

  private final ApplicationEventPublisher eventPublisher;

  public ContactoService(ContactoRepository contactoRepository,
      CompradorRepository compradorRepository, ProductoRepository productoRepository,
      ApplicationEventPublisher eventPublisher) {
    this.contactoRepository = contactoRepository;
    this.compradorRepository = compradorRepository;
    this.productoRepository = productoRepository;
    this.eventPublisher = eventPublisher;
  }

  /**
   * Registra el mensaje del comprador autenticado sobre un producto existente.
   */
  @Transactional
  public ContactoResponse enviar(Long productoId, ContactoRequest request, Long usuarioId) {
    Comprador comprador = compradorRepository.findByUsuarioId(usuarioId)
        .orElseThrow(() -> new RecursoNoEncontradoException(
            "El usuario autenticado no tiene perfil de comprador"));
    Producto producto = productoRepository.findById(productoId)
        .orElseThrow(() -> new RecursoNoEncontradoException(
            "No existe el producto con id " + productoId));

    Contacto contacto = contactoRepository.save(
        new Contacto(comprador, producto, request.mensaje()));
    eventPublisher.publishEvent(new ContactoRegistradoEvent(
        contacto.getId(), producto.getAgricultor().getId()));
    return aRespuesta(contacto);
  }

  /**
   * Guarda la respuesta solo si el agricultor autenticado publicó el producto.
   */
  @Transactional
  public ContactoResponse responder(Long contactoId, String respuesta, Long usuarioId) {
    Contacto contacto = contactoRepository.findById(contactoId)
        .orElseThrow(() -> new RecursoNoEncontradoException(
            "No existe el contacto con id " + contactoId));
    Long duenoId = contacto.getProducto().getAgricultor().getUsuario().getId();
    if (!duenoId.equals(usuarioId)) {
      throw new RecursoNoEncontradoException("No existe el contacto con id " + contactoId);
    }

    contacto.setRespuesta(respuesta);
    contacto.setFechaRespuesta(LocalDateTime.now());
    return aRespuesta(contactoRepository.save(contacto));
  }

  /**
   * Lista las solicitudes de los productos del agricultor autenticado.
   */
  @Transactional(readOnly = true)
  public List<ContactoResponse> listar(Long usuarioId, boolean pendientes) {
    return contactoRepository.listarDelAgricultor(usuarioId, pendientes).stream()
        .map(this::aRespuesta)
        .toList();
  }

  private ContactoResponse aRespuesta(Contacto contacto) {
    return new ContactoResponse(
        contacto.getId(),
        contacto.getComprador().getId(),
        contacto.getProducto().getId(),
        contacto.getMensaje(),
        contacto.getFechaEnvio(),
        contacto.getRespuesta(),
        contacto.getFechaRespuesta());
  }
}
