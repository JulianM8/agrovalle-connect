package com.agrovalle.connect.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.agrovalle.connect.dto.InconformidadRequest;
import com.agrovalle.connect.dto.InconformidadResponse;
import com.agrovalle.connect.event.InconformidadRegistradaEvent;
import com.agrovalle.connect.exception.ConflictoException;
import com.agrovalle.connect.exception.PedidoInvalidoException;
import com.agrovalle.connect.exception.RecursoNoEncontradoException;
import com.agrovalle.connect.model.Agricultor;
import com.agrovalle.connect.model.Comprador;
import com.agrovalle.connect.model.Inconformidad;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class InconformidadServiceTest {

  private static final InconformidadRequest REPORTE =
      new InconformidadRequest("Producto dañado", "https://example.com/evidencia.jpg");

  @Mock
  private InconformidadRepository inconformidadRepository;

  @Mock
  private PedidoRepository pedidoRepository;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private InconformidadService inconformidadService;

  private Pedido pedido;

  @BeforeEach
  void prepararPedido() {
    Usuario usuario = new Usuario();
    usuario.setId(5L);
    Comprador comprador = new Comprador("Palmira", "Dirección de prueba", null, null, usuario);
    Agricultor agricultor = new Agricultor();
    agricultor.setId(7L);
    pedido = new Pedido(comprador, agricultor, "Dirección de prueba");
    pedido.setId(10L);
    pedido.setEstado(EstadoPedido.EN_TRANSPORTE);
    when(pedidoRepository.findByIdParaActualizar(10L)).thenReturn(Optional.of(pedido));
  }

  @Test
  void debeRegistrarElReporteYPublicarElEvento() {
    when(inconformidadRepository.save(any(Inconformidad.class))).thenAnswer(invocacion -> {
      Inconformidad reporte = invocacion.getArgument(0);
      Inconformidad guardada = spy(reporte);
      doReturn(11L).when(guardada).getId();
      return guardada;
    });

    InconformidadResponse respuesta = inconformidadService.reportar(10L, REPORTE, 5L);

    assertEquals(EstadoInconformidad.EN_REVISION, respuesta.estado());
    assertEquals(REPORTE.descripcion(), respuesta.descripcion());
    assertEquals(REPORTE.evidenciaUrl(), respuesta.evidenciaUrl());
    assertNotNull(respuesta.fechaReporte());
    assertEquals(EstadoPedido.EN_TRANSPORTE, pedido.getEstado());
    verify(eventPublisher).publishEvent(new InconformidadRegistradaEvent(11L, 10L, 7L));
  }

  @Test
  void debeRechazarUnPedidoDeOtroComprador() {
    assertThrows(RecursoNoEncontradoException.class,
        () -> inconformidadService.reportar(10L, REPORTE, 99L));

    verifyNoInteractions(inconformidadRepository, eventPublisher);
  }

  @Test
  void debeRechazarUnPedidoFueraDeTransporte() {
    pedido.setEstado(EstadoPedido.ENTREGADO);

    assertThrows(PedidoInvalidoException.class,
        () -> inconformidadService.reportar(10L, REPORTE, 5L));

    verifyNoInteractions(inconformidadRepository, eventPublisher);
  }

  @Test
  void debeRechazarUnSegundoReporteEnRevision() {
    when(inconformidadRepository.existsByPedidoIdAndEstado(10L, EstadoInconformidad.EN_REVISION))
        .thenReturn(true);

    assertThrows(ConflictoException.class,
        () -> inconformidadService.reportar(10L, REPORTE, 5L));

    verifyNoInteractions(eventPublisher);
  }
}
