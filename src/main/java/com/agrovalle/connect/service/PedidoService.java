package com.agrovalle.connect.service;

import com.agrovalle.connect.dto.DetallePedidoResponse;
import com.agrovalle.connect.dto.DetallePedidoRequest;
import com.agrovalle.connect.dto.PedidoRequest;
import com.agrovalle.connect.dto.PedidoResponse;
import com.agrovalle.connect.exception.RecursoNoEncontradoException;
import com.agrovalle.connect.exception.PedidoInvalidoException;
import com.agrovalle.connect.model.Agricultor;
import com.agrovalle.connect.model.Comprador;
import com.agrovalle.connect.model.DetallePedido;
import com.agrovalle.connect.model.Pedido;
import com.agrovalle.connect.model.Producto;
import com.agrovalle.connect.repository.CompradorRepository;
import com.agrovalle.connect.repository.DetallePedidoRepository;
import com.agrovalle.connect.repository.PedidoRepository;
import com.agrovalle.connect.repository.ProductoRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica de negocio de los pedidos: valida el carrito, calcula el total y
 * crea el pedido con sus detalles en estado PENDIENTE.
 */
@Service
public class PedidoService {

  private final PedidoRepository pedidoRepository;

  private final DetallePedidoRepository detallePedidoRepository;

  private final ProductoRepository productoRepository;

  private final CompradorRepository compradorRepository;

  public PedidoService(PedidoRepository pedidoRepository,
      DetallePedidoRepository detallePedidoRepository, ProductoRepository productoRepository,
      CompradorRepository compradorRepository) {
    this.pedidoRepository = pedidoRepository;
    this.detallePedidoRepository = detallePedidoRepository;
    this.productoRepository = productoRepository;
    this.compradorRepository = compradorRepository;
  }

  /**
   * Crea un pedido para el comprador autenticado. Valida que cada producto
   * exista, que haya cantidad disponible y que todos sean del mismo agricultor.
   */
  @Transactional
  public PedidoResponse crear(PedidoRequest request, Long usuarioId) {
    Comprador comprador = compradorRepository.findByUsuarioId(usuarioId)
        .orElseThrow(() -> new RecursoNoEncontradoException(
            "El usuario autenticado no tiene perfil de comprador"));

    // Si un producto viene repetido en el carrito, se suman sus cantidades.
    Map<Long, Integer> cantidades = new LinkedHashMap<>();
    for (DetallePedidoRequest item : request.items()) {
      cantidades.merge(item.productoId(), item.cantidad(), Integer::sum);
    }

    Map<Producto, Integer> lineas = new LinkedHashMap<>();
    for (Map.Entry<Long, Integer> entrada : cantidades.entrySet()) {
      Producto producto = productoRepository.findById(entrada.getKey())
          .orElseThrow(() -> new RecursoNoEncontradoException(
              "No existe el producto con id " + entrada.getKey()));
      if (entrada.getValue() > producto.getCantidad()) {
        throw new PedidoInvalidoException("Cantidad insuficiente de "
            + producto.getNombre() + ": disponible " + producto.getCantidad());
      }
      lineas.put(producto, entrada.getValue());
    }

    Set<Long> agricultores = lineas.keySet().stream()
        .map(producto -> producto.getAgricultor().getId())
        .collect(Collectors.toSet());
    if (agricultores.size() > 1) {
      throw new PedidoInvalidoException(
          "Todos los productos del pedido deben ser del mismo agricultor");
    }
    Agricultor agricultor = lineas.keySet().iterator().next().getAgricultor();

    Pedido pedido = pedidoRepository.save(
        new Pedido(comprador, agricultor, request.direccionEntrega()));

    List<DetallePedido> detalles = new ArrayList<>();
    for (Map.Entry<Producto, Integer> linea : lineas.entrySet()) {
      BigDecimal precio = linea.getKey().getPrecio().setScale(2, RoundingMode.HALF_UP);
      detalles.add(new DetallePedido(pedido, linea.getKey(), linea.getValue(), precio));
    }
    detallePedidoRepository.saveAll(detalles);

    return construirRespuesta(pedido, detalles);
  }

  /**
   * Calcula el total de un pedido como la suma de cantidad x precio unitario.
   */
  @Transactional(readOnly = true)
  public BigDecimal calcularTotal(Long pedidoId) {
    return total(detallePedidoRepository.findByPedidoId(pedidoId));
  }

  /**
   * Obtiene un pedido solo si pertenece al comprador autenticado. Si no existe
   * o es de otro comprador responde igual (404) para no revelar su existencia.
   */
  @Transactional(readOnly = true)
  public Pedido obtenerPedidoDelComprador(Long pedidoId, Long usuarioId) {
    Pedido pedido = pedidoRepository.findById(pedidoId)
        .orElseThrow(() -> new RecursoNoEncontradoException(
            "No existe el pedido con id " + pedidoId));
    if (!pedido.getComprador().getUsuario().getId().equals(usuarioId)) {
      throw new RecursoNoEncontradoException("No existe el pedido con id " + pedidoId);
    }
    return pedido;
  }

  private BigDecimal total(List<DetallePedido> detalles) {
    return detalles.stream()
        .map(d -> d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private PedidoResponse construirRespuesta(Pedido pedido, List<DetallePedido> detalles) {
    List<DetallePedidoResponse> lineas = detalles.stream()
        .map(d -> new DetallePedidoResponse(
            d.getProducto().getId(),
            d.getProducto().getNombre(),
            d.getCantidad(),
            d.getPrecioUnitario(),
            d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad()))))
        .toList();
    return new PedidoResponse(
        pedido.getId(),
        pedido.getEstado(),
        pedido.getDireccionEntrega(),
        pedido.getFechaCreacion(),
        pedido.getAgricultor().getId(),
        lineas,
        total(detalles));
  }
}