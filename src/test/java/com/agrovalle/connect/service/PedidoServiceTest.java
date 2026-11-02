package com.agrovalle.connect.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agrovalle.connect.dto.DetallePedidoRequest;
import com.agrovalle.connect.dto.PedidoRequest;
import com.agrovalle.connect.dto.PedidoResponse;
import com.agrovalle.connect.exception.RecursoNoEncontradoException;
import com.agrovalle.connect.exception.PedidoInvalidoException;
import com.agrovalle.connect.model.Agricultor;
import com.agrovalle.connect.model.Comprador;
import com.agrovalle.connect.model.Pedido;
import com.agrovalle.connect.model.Producto;
import com.agrovalle.connect.model.Usuario;
import com.agrovalle.connect.model.enums.EstadoPedido;
import com.agrovalle.connect.model.enums.Rol;
import com.agrovalle.connect.repository.CompradorRepository;
import com.agrovalle.connect.repository.DetallePedidoRepository;
import com.agrovalle.connect.repository.PedidoRepository;
import com.agrovalle.connect.repository.ProductoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

  @Mock
  private PedidoRepository pedidoRepository;

  @Mock
  private DetallePedidoRepository detallePedidoRepository;

  @Mock
  private ProductoRepository productoRepository;

  @Mock
  private CompradorRepository compradorRepository;

  @InjectMocks
  private PedidoService pedidoService;

  private Comprador comprador;

  private Agricultor agricultor;

  @BeforeEach
  void preparar() {
    Usuario usuario = new Usuario("1111078901", "Sofia", "Torres", "3001234567",
        "sofia@correo.com", "clave", Rol.COMPRADOR);
    usuario.setId(5L);
    comprador = new Comprador("Cali", "Calle 5 # 10-20", null, null, usuario);
    comprador.setId(1L);
    agricultor = new Agricultor("Palmira", null);
    agricultor.setId(10L);
  }

  private Producto producto(String nombre, int cantidad, String precio, Agricultor dueno) {
    return new Producto(nombre, "Hortalizas", cantidad, null, new BigDecimal(precio),
        LocalDate.now(), dueno);
  }

  private PedidoRequest carrito(DetallePedidoRequest... items) {
    return new PedidoRequest(List.of(items), "Calle 5 # 10-20");
  }

  @Test
  void debeCrearPedidoPendienteYCalcularTotal() {
    when(compradorRepository.findByUsuarioId(5L)).thenReturn(Optional.of(comprador));
    when(productoRepository.findById(1L))
        .thenReturn(Optional.of(producto("Tomate", 100, "5000", agricultor)));
    when(productoRepository.findById(2L))
        .thenReturn(Optional.of(producto("Cebolla", 50, "2500", agricultor)));
    when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

    PedidoResponse respuesta = pedidoService.crear(
        carrito(new DetallePedidoRequest(1L, 2), new DetallePedidoRequest(2L, 4)), 5L);

    assertEquals(EstadoPedido.PENDIENTE, respuesta.estado());
    assertEquals(2, respuesta.detalles().size());
    assertEquals(0, new BigDecimal("20000.00").compareTo(respuesta.total()));
  }

  @Test
  void debeRechazarProductoInexistente() {
    when(compradorRepository.findByUsuarioId(5L)).thenReturn(Optional.of(comprador));
    when(productoRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(RecursoNoEncontradoException.class,
        () -> pedidoService.crear(carrito(new DetallePedidoRequest(99L, 1)), 5L));
    verify(pedidoRepository, never()).save(any(Pedido.class));
  }

  @Test
  void debeRechazarCantidadMayorALaDisponible() {
    when(compradorRepository.findByUsuarioId(5L)).thenReturn(Optional.of(comprador));
    when(productoRepository.findById(1L))
        .thenReturn(Optional.of(producto("Tomate", 3, "5000", agricultor)));

    assertThrows(PedidoInvalidoException.class,
        () -> pedidoService.crear(carrito(new DetallePedidoRequest(1L, 10)), 5L));
    verify(pedidoRepository, never()).save(any(Pedido.class));
  }

  @Test
  void debeRechazarCarritoConVariosAgricultores() {
    Agricultor otro = new Agricultor("Tuluá", null);
    otro.setId(11L);
    when(compradorRepository.findByUsuarioId(5L)).thenReturn(Optional.of(comprador));
    when(productoRepository.findById(1L))
        .thenReturn(Optional.of(producto("Tomate", 100, "5000", agricultor)));
    when(productoRepository.findById(2L))
        .thenReturn(Optional.of(producto("Cebolla", 50, "2500", otro)));

    assertThrows(PedidoInvalidoException.class, () -> pedidoService.crear(
        carrito(new DetallePedidoRequest(1L, 1), new DetallePedidoRequest(2L, 1)), 5L));
    verify(pedidoRepository, never()).save(any(Pedido.class));
  }

  @Test
  void debeRechazarUsuarioSinPerfilDeComprador() {
    when(compradorRepository.findByUsuarioId(5L)).thenReturn(Optional.empty());

    assertThrows(RecursoNoEncontradoException.class,
        () -> pedidoService.crear(carrito(new DetallePedidoRequest(1L, 1)), 5L));
  }
}