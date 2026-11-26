package com.agrovalle.connect.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agrovalle.connect.dto.RecepcionResponse;
import com.agrovalle.connect.event.RecepcionConfirmadaEvent;
import com.agrovalle.connect.exception.ConflictoException;
import com.agrovalle.connect.exception.PedidoInvalidoException;
import com.agrovalle.connect.exception.RecursoNoEncontradoException;
import com.agrovalle.connect.model.Agricultor;
import com.agrovalle.connect.model.Comprador;
import com.agrovalle.connect.model.Pedido;
import com.agrovalle.connect.model.Usuario;
import com.agrovalle.connect.model.enums.EstadoInconformidad;
import com.agrovalle.connect.model.enums.EstadoPedido;
import com.agrovalle.connect.repository.InconformidadRepository;
import com.agrovalle.connect.repository.PedidoRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class RecepcionServiceTest {

  private static final Long PEDIDO_ID = 1L;
  private static final Long COMPRADOR_ID = 7L;
  private static final Long AGRICULTOR_ID = 3L;

  @Mock private PedidoRepository pedidoRepository;
  @Mock private InconformidadRepository inconformidadRepository;
  @Mock private ApplicationEventPublisher eventPublisher;
  @InjectMocks private RecepcionService service;

  private Pedido pedido;

@BeforeEach
void prepararPedido() {
    Comprador comprador = mock(Comprador.class);
    Agricultor agricultor = mock(Agricultor.class);
    Usuario usuario = mock(Usuario.class);

    lenient().when(comprador.getId()).thenReturn(COMPRADOR_ID);
    lenient().when(comprador.getUsuario()).thenReturn(usuario);
    lenient().when(usuario.getId()).thenReturn(COMPRADOR_ID);

    pedido = new Pedido();
    pedido.setId(PEDIDO_ID);
    pedido.setComprador(comprador);
    pedido.setAgricultor(agricultor);
    pedido.setEstado(EstadoPedido.EN_TRANSPORTE);
}

  @Test
  void confirmaRecepcionYDejaElPedidoEntregado() {
    when(pedidoRepository.findByIdParaActualizar(PEDIDO_ID)).thenReturn(Optional.of(pedido));
    when(inconformidadRepository.existsByPedidoIdAndEstado(
        PEDIDO_ID, EstadoInconformidad.EN_REVISION)).thenReturn(false);
    when(pedido.getAgricultor().getId()).thenReturn(AGRICULTOR_ID);

    RecepcionResponse respuesta = service.confirmarRecepcion(PEDIDO_ID, COMPRADOR_ID);

    assertEquals(EstadoPedido.ENTREGADO, respuesta.estado());
    assertNotNull(respuesta.fechaEntrega());
    assertEquals(EstadoPedido.ENTREGADO, pedido.getEstado());
    assertEquals(respuesta.fechaEntrega(), pedido.getFechaEntrega());
    verify(pedidoRepository).save(pedido);
  }

  @Test
  void publicaRecepcionConfirmadaEventConLosDatosDelPedido() {
    when(pedidoRepository.findByIdParaActualizar(PEDIDO_ID)).thenReturn(Optional.of(pedido));
    when(pedido.getAgricultor().getId()).thenReturn(AGRICULTOR_ID);

    service.confirmarRecepcion(PEDIDO_ID, COMPRADOR_ID);

    ArgumentCaptor<RecepcionConfirmadaEvent> captor =
        ArgumentCaptor.forClass(RecepcionConfirmadaEvent.class);
    verify(eventPublisher).publishEvent(captor.capture());
    assertEquals(PEDIDO_ID, captor.getValue().pedidoId());
    assertEquals(COMPRADOR_ID, captor.getValue().compradorId());
    assertEquals(AGRICULTOR_ID, captor.getValue().agricultorId());
    assertNotNull(captor.getValue().fechaRecepcion());
  }

  @Test
  void pedidoYaEntregadoLanzaConflicto() {
    pedido.setEstado(EstadoPedido.ENTREGADO);
    when(pedidoRepository.findByIdParaActualizar(PEDIDO_ID)).thenReturn(Optional.of(pedido));

    assertThrows(ConflictoException.class,
        () -> service.confirmarRecepcion(PEDIDO_ID, COMPRADOR_ID));
    verify(eventPublisher, never()).publishEvent(any(Object.class));
  }

  @Test
  void pedidoQueNoEstaEnTransporteLanzaPedidoInvalido() {
    pedido.setEstado(EstadoPedido.CONFIRMADO);
    when(pedidoRepository.findByIdParaActualizar(PEDIDO_ID)).thenReturn(Optional.of(pedido));

    assertThrows(PedidoInvalidoException.class,
        () -> service.confirmarRecepcion(PEDIDO_ID, COMPRADOR_ID));
    verify(eventPublisher, never()).publishEvent(any(Object.class));
  }

  @Test
  void inconformidadEnRevisionLanzaConflicto() {
    when(pedidoRepository.findByIdParaActualizar(PEDIDO_ID)).thenReturn(Optional.of(pedido));
    when(inconformidadRepository.existsByPedidoIdAndEstado(
        PEDIDO_ID, EstadoInconformidad.EN_REVISION)).thenReturn(true);

    assertThrows(ConflictoException.class,
        () -> service.confirmarRecepcion(PEDIDO_ID, COMPRADOR_ID));
    verify(eventPublisher, never()).publishEvent(any(Object.class));
  }

  @Test
  void pedidoDeOtroCompradorLanzaNoEncontrado() {
    when(pedidoRepository.findByIdParaActualizar(PEDIDO_ID)).thenReturn(Optional.of(pedido));

    assertThrows(RecursoNoEncontradoException.class,
        () -> service.confirmarRecepcion(PEDIDO_ID, 99L));
    verify(eventPublisher, never()).publishEvent(any(Object.class));
  }

  @Test
  void pedidoInexistenteLanzaNoEncontrado() {
    when(pedidoRepository.findByIdParaActualizar(PEDIDO_ID)).thenReturn(Optional.empty());

    assertThrows(RecursoNoEncontradoException.class,
        () -> service.confirmarRecepcion(PEDIDO_ID, COMPRADOR_ID));
  }
}