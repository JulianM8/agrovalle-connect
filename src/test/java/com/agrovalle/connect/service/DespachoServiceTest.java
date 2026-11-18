package com.agrovalle.connect.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agrovalle.connect.dto.AsignacionResponse;
import com.agrovalle.connect.dto.TransporteResponse;
import com.agrovalle.connect.event.AsignacionRegistradaEvent;
import com.agrovalle.connect.exception.ConflictoException;
import com.agrovalle.connect.exception.PedidoInvalidoException;
import com.agrovalle.connect.exception.RecursoNoEncontradoException;
import com.agrovalle.connect.model.Agricultor;
import com.agrovalle.connect.model.AsignacionTransporte;
import com.agrovalle.connect.model.Comprador;
import com.agrovalle.connect.model.Pedido;
import com.agrovalle.connect.model.Ruta;
import com.agrovalle.connect.model.Transportista;
import com.agrovalle.connect.model.Usuario;
import com.agrovalle.connect.model.Vehiculo;
import com.agrovalle.connect.model.enums.EstadoPedido;
import com.agrovalle.connect.model.enums.Rol;
import com.agrovalle.connect.repository.AsignacionTransporteRepository;
import com.agrovalle.connect.repository.PedidoRepository;
import com.agrovalle.connect.repository.RutaRepository;
import com.agrovalle.connect.repository.VehiculoRepository;
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
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class DespachoServiceTest {

  @Mock
  private PedidoRepository pedidoRepository;

  @Mock
  private VehiculoRepository vehiculoRepository;

  @Mock
  private RutaRepository rutaRepository;

  @Mock
  private AsignacionTransporteRepository asignacionRepository;

  @Mock
  private PedidoService pedidoService;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private DespachoService despachoService;

  private Pedido pedido;

  private Vehiculo vehiculo;

  private Ruta ruta;

  @BeforeEach
  void preparar() {
    Usuario usuarioAgricultor = new Usuario("1111000001", "Ana", "Perez", "3001111111",
        "ana@correo.com", "clave", Rol.AGRICULTOR);
    usuarioAgricultor.setId(7L);
    Agricultor agricultor = new Agricultor("Palmira", usuarioAgricultor);
    agricultor.setId(10L);

    Usuario usuarioComprador = new Usuario("1111000002", "Sofia", "Torres", "3002222222",
        "sofia@correo.com", "clave", Rol.COMPRADOR);
    usuarioComprador.setId(5L);
    Comprador comprador = new Comprador("Cali", "Calle 5 # 10-20", null, null, usuarioComprador);
    comprador.setId(1L);

    pedido = new Pedido(comprador, agricultor, "Calle 5 # 10-20");
    pedido.setId(100L);
    pedido.setEstado(EstadoPedido.CONFIRMADO);

    Usuario usuarioTransportista = new Usuario("1111000003", "Luis", "Gomez", "3003333333",
        "luis@correo.com", "clave", Rol.TRANSPORTISTA);
    usuarioTransportista.setId(9L);
    Transportista transportista = new Transportista("Cali", usuarioTransportista);
    transportista.setId(4L);
    vehiculo = new Vehiculo("CAMIONETA", "ABC123", new BigDecimal("1500"), transportista);
    vehiculo.setId(3L);
    ruta = new Ruta(LocalDate.now(), vehiculo);
    ruta.setId(20L);
  }

  @Test
  void debeAsignarVehiculoYPasarElPedidoAEnTransporte() {
    AsignacionTransporte existente = new AsignacionTransporte(pedido, ruta, 1);
    when(pedidoRepository.findById(100L)).thenReturn(Optional.of(pedido));
    when(asignacionRepository.findByPedidoId(100L)).thenReturn(Optional.empty());
    when(vehiculoRepository.findById(3L)).thenReturn(Optional.of(vehiculo));
    when(rutaRepository.findByVehiculoIdAndFecha(eq(3L), any(LocalDate.class)))
        .thenReturn(Optional.of(ruta));
    when(asignacionRepository.findByRutaIdOrderByOrdenEntrega(20L))
        .thenReturn(List.of(existente));
    when(asignacionRepository.save(any(AsignacionTransporte.class)))
        .thenAnswer(invocacion -> invocacion.getArgument(0));

    AsignacionResponse respuesta = despachoService.asignarVehiculo(100L, 3L, 7L);

    assertEquals(2, respuesta.ordenEntrega());
    assertEquals(EstadoPedido.EN_TRANSPORTE, pedido.getEstado());
    assertEquals(3L, respuesta.vehiculoId());
    verify(pedidoRepository).save(pedido);
    verify(eventPublisher).publishEvent(any(AsignacionRegistradaEvent.class));
  }

  @Test
  void debeCrearLaRutaSiElVehiculoNoTieneUnaParaLaFecha() {
    when(pedidoRepository.findById(100L)).thenReturn(Optional.of(pedido));
    when(asignacionRepository.findByPedidoId(100L)).thenReturn(Optional.empty());
    when(vehiculoRepository.findById(3L)).thenReturn(Optional.of(vehiculo));
    when(rutaRepository.findByVehiculoIdAndFecha(eq(3L), any(LocalDate.class)))
        .thenReturn(Optional.empty());
    when(rutaRepository.save(any(Ruta.class))).thenReturn(ruta);
    when(asignacionRepository.findByRutaIdOrderByOrdenEntrega(20L)).thenReturn(List.of());
    when(asignacionRepository.save(any(AsignacionTransporte.class)))
        .thenAnswer(invocacion -> invocacion.getArgument(0));

    AsignacionResponse respuesta = despachoService.asignarVehiculo(100L, 3L, 7L);

    assertEquals(1, respuesta.ordenEntrega());
    verify(rutaRepository).save(any(Ruta.class));
  }

  @Test
  void debeRechazarAsignacionDePedidoNoConfirmado() {
    pedido.setEstado(EstadoPedido.PENDIENTE);
    when(pedidoRepository.findById(100L)).thenReturn(Optional.of(pedido));
    when(asignacionRepository.findByPedidoId(100L)).thenReturn(Optional.empty());

    assertThrows(PedidoInvalidoException.class,
        () -> despachoService.asignarVehiculo(100L, 3L, 7L));
    verify(asignacionRepository, never()).save(any(AsignacionTransporte.class));
  }

  @Test
  void debeRechazarAsignacionDePedidoDeOtroAgricultor() {
    when(pedidoRepository.findById(100L)).thenReturn(Optional.of(pedido));

    assertThrows(RecursoNoEncontradoException.class,
        () -> despachoService.asignarVehiculo(100L, 3L, 99L));
    verify(asignacionRepository, never()).save(any(AsignacionTransporte.class));
  }

  @Test
  void debeRechazarPedidoYaAsignado() {
    pedido.setEstado(EstadoPedido.EN_TRANSPORTE);
    when(pedidoRepository.findById(100L)).thenReturn(Optional.of(pedido));
    when(asignacionRepository.findByPedidoId(100L))
        .thenReturn(Optional.of(new AsignacionTransporte(pedido, ruta, 1)));

    assertThrows(ConflictoException.class,
        () -> despachoService.asignarVehiculo(100L, 3L, 7L));
    verify(asignacionRepository, never()).save(any(AsignacionTransporte.class));
  }

  @Test
  void debeRechazarVehiculoInexistente() {
    when(pedidoRepository.findById(100L)).thenReturn(Optional.of(pedido));
    when(asignacionRepository.findByPedidoId(100L)).thenReturn(Optional.empty());
    when(vehiculoRepository.findById(3L)).thenReturn(Optional.empty());

    assertThrows(RecursoNoEncontradoException.class,
        () -> despachoService.asignarVehiculo(100L, 3L, 7L));
  }

  @Test
  void debeDevolverElTransporteAsignadoAlComprador() {
    when(pedidoService.obtenerPedidoDelComprador(100L, 5L)).thenReturn(pedido);
    when(asignacionRepository.findByPedidoId(100L))
        .thenReturn(Optional.of(new AsignacionTransporte(pedido, ruta, 1)));

    TransporteResponse respuesta = despachoService.consultarTransporte(100L, 5L);

    assertEquals("CAMIONETA", respuesta.tipoTransporte());
    assertEquals("ABC123", respuesta.placa());
  }

  @Test
  void debeResponder404SiElPedidoNoTieneTransporte() {
    when(pedidoService.obtenerPedidoDelComprador(100L, 5L)).thenReturn(pedido);
    when(asignacionRepository.findByPedidoId(100L)).thenReturn(Optional.empty());

    assertThrows(RecursoNoEncontradoException.class,
        () -> despachoService.consultarTransporte(100L, 5L));
  }
}
