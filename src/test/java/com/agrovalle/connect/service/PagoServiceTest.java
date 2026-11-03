package com.agrovalle.connect.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.agrovalle.connect.service.pago.ContraentregaStrategy;
import com.agrovalle.connect.service.pago.PseSimuladoStrategy;
import com.agrovalle.connect.service.pago.TarjetaSimuladaStrategy;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

  @Mock
  private PedidoService pedidoService;

  @Mock
  private PedidoRepository pedidoRepository;

  @Mock
  private PagoRepository pagoRepository;

  private PagoService pagoService;

  private Pedido pedido;

  @BeforeEach
  void preparar() {
    pagoService = new PagoService(pedidoService, pedidoRepository, pagoRepository,
        Map.of("TARJETA", new TarjetaSimuladaStrategy(),
            "PSE", new PseSimuladoStrategy(),
            "CONTRAENTREGA", new ContraentregaStrategy()));
    pedido = new Pedido(null, null, "Calle 5 # 10-20");
    pedido.setId(100L);
  }

  @Test
  void debeCompletarPagoYConfirmarPedido() {
    when(pedidoService.obtenerPedidoDelComprador(100L, 5L)).thenReturn(pedido);
    when(pedidoService.calcularTotal(100L)).thenReturn(new BigDecimal("20000.00"));
    when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

    PagoResponse respuesta = pagoService.pagar(100L, new PagoRequest("tarjeta"), 5L);

    assertEquals(EstadoPago.COMPLETADO, respuesta.estado());
    assertEquals(EstadoPedido.CONFIRMADO, pedido.getEstado());
    verify(pedidoRepository).save(pedido);
  }

  @Test
  void debeRechazarPagoYDejarPedidoPendiente() {
    when(pedidoService.obtenerPedidoDelComprador(100L, 5L)).thenReturn(pedido);
    when(pedidoService.calcularTotal(100L)).thenReturn(new BigDecimal("12000000.00"));
    when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

    PagoResponse respuesta = pagoService.pagar(100L, new PagoRequest("TARJETA"), 5L);

    assertEquals(EstadoPago.RECHAZADO, respuesta.estado());
    assertEquals(EstadoPedido.PENDIENTE, pedido.getEstado());
    verify(pedidoRepository, never()).save(any(Pedido.class));
  }

  @Test
  void debePermitirReintentoTrasUnPagoRechazado() {
    when(pedidoService.obtenerPedidoDelComprador(100L, 5L)).thenReturn(pedido);
    when(pedidoService.calcularTotal(100L)).thenReturn(new BigDecimal("12000000.00"));
    when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

    PagoResponse primero = pagoService.pagar(100L, new PagoRequest("TARJETA"), 5L);
    PagoResponse segundo = pagoService.pagar(100L, new PagoRequest("PSE"), 5L);

    assertEquals(EstadoPago.RECHAZADO, primero.estado());
    assertEquals(EstadoPago.COMPLETADO, segundo.estado());
    assertEquals(EstadoPedido.CONFIRMADO, pedido.getEstado());
    verify(pagoRepository, times(2)).save(any(Pago.class));
  }

  @Test
  void debeDejarPagoContraentregaPendiente() {
    when(pedidoService.obtenerPedidoDelComprador(100L, 5L)).thenReturn(pedido);
    when(pedidoService.calcularTotal(100L)).thenReturn(new BigDecimal("20000.00"));
    when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

    PagoResponse respuesta = pagoService.pagar(100L, new PagoRequest("CONTRAENTREGA"), 5L);

    assertEquals(EstadoPago.PENDIENTE, respuesta.estado());
    assertEquals(EstadoPedido.PENDIENTE, pedido.getEstado());
  }

  @Test
  void debeRechazarMedioDePagoNoSoportado() {
    when(pedidoService.obtenerPedidoDelComprador(100L, 5L)).thenReturn(pedido);

    assertThrows(PedidoInvalidoException.class,
        () -> pagoService.pagar(100L, new PagoRequest("BITCOIN"), 5L));
    verify(pagoRepository, never()).save(any(Pago.class));
  }

  @Test
  void debeRechazarPagoDePedidoYaConfirmado() {
    pedido.setEstado(EstadoPedido.CONFIRMADO);
    when(pedidoService.obtenerPedidoDelComprador(100L, 5L)).thenReturn(pedido);

    assertThrows(PedidoInvalidoException.class,
        () -> pagoService.pagar(100L, new PagoRequest("TARJETA"), 5L));
  }

  @Test
  void debeFallarAlConsultarPedidoSinPagos() {
    when(pagoRepository.findFirstByPedidoIdOrderByIdDesc(100L)).thenReturn(Optional.empty());

    assertThrows(RecursoNoEncontradoException.class, () -> pagoService.consultar(100L, 5L));
  }
}