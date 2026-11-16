package com.agrovalle.connect.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agrovalle.connect.config.SecurityConfig;
import com.agrovalle.connect.dto.AsignacionRequest;
import com.agrovalle.connect.dto.AsignacionResponse;
import com.agrovalle.connect.dto.TransporteResponse;
import com.agrovalle.connect.exception.ConflictoException;
import com.agrovalle.connect.exception.GlobalExceptionHandler;
import com.agrovalle.connect.exception.PedidoInvalidoException;
import com.agrovalle.connect.exception.RecursoNoEncontradoException;
import com.agrovalle.connect.model.enums.EstadoPedido;
import com.agrovalle.connect.service.DespachoService;
import com.agrovalle.connect.service.JwtService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(DespachoController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
class DespachoControllerTest {

  private static final String URL_ASIGNACION = "/api/v1/pedidos/100/asignacion";

  private static final String URL_TRANSPORTE = "/api/v1/pedidos/100/transporte";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private DespachoService despachoService;

  @MockitoBean
  private JwtService jwtService;

  @BeforeEach
  void autenticar() {
    when(jwtService.extraerUsuarioId(eq("token-agricultor"))).thenReturn(7L);
    when(jwtService.extraerRoles(eq("token-agricultor"))).thenReturn(List.of("AGRICULTOR"));
    when(jwtService.extraerUsuarioId(eq("token-comprador"))).thenReturn(5L);
    when(jwtService.extraerRoles(eq("token-comprador"))).thenReturn(List.of("COMPRADOR"));
  }

  private String cuerpo(Long vehiculoId) throws Exception {
    return objectMapper.writeValueAsString(new AsignacionRequest(vehiculoId));
  }

  @Test
  void debeAsignarVehiculoAPedidoConfirmado() throws Exception {
    when(despachoService.asignarVehiculo(100L, 3L, 7L)).thenReturn(new AsignacionResponse(
        1L, 100L, EstadoPedido.EN_TRANSPORTE, 3L, 20L, LocalDate.of(2026, 11, 20), 1,
        LocalDateTime.now()));

    mockMvc.perform(post(URL_ASIGNACION)
        .header("Authorization", "Bearer token-agricultor")
        .contentType(MediaType.APPLICATION_JSON)
        .content(cuerpo(3L)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.estadoPedido").value("EN_TRANSPORTE"))
        .andExpect(jsonPath("$.ordenEntrega").value(1));
  }

  @Test
  void debeResponder400SiFaltaElVehiculo() throws Exception {
    mockMvc.perform(post(URL_ASIGNACION)
        .header("Authorization", "Bearer token-agricultor")
        .contentType(MediaType.APPLICATION_JSON)
        .content(cuerpo(null)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.vehiculoId").exists());
  }

  @Test
  void debeResponder400SiElPedidoNoEstaConfirmado() throws Exception {
    when(despachoService.asignarVehiculo(100L, 3L, 7L)).thenThrow(new PedidoInvalidoException(
        "Solo se puede asignar transporte a pedidos CONFIRMADO, estado actual: PENDIENTE"));

    mockMvc.perform(post(URL_ASIGNACION)
        .header("Authorization", "Bearer token-agricultor")
        .contentType(MediaType.APPLICATION_JSON)
        .content(cuerpo(3L)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void debeResponder404SiElPedidoOElVehiculoNoExisten() throws Exception {
    when(despachoService.asignarVehiculo(100L, 3L, 7L))
        .thenThrow(new RecursoNoEncontradoException("No existe el pedido con id 100"));

    mockMvc.perform(post(URL_ASIGNACION)
        .header("Authorization", "Bearer token-agricultor")
        .contentType(MediaType.APPLICATION_JSON)
        .content(cuerpo(3L)))
        .andExpect(status().isNotFound());
  }

  @Test
  void debeResponder409SiElPedidoYaEstaAsignado() throws Exception {
    when(despachoService.asignarVehiculo(100L, 3L, 7L))
        .thenThrow(new ConflictoException("El pedido 100 ya tiene transporte asignado"));

    mockMvc.perform(post(URL_ASIGNACION)
        .header("Authorization", "Bearer token-agricultor")
        .contentType(MediaType.APPLICATION_JSON)
        .content(cuerpo(3L)))
        .andExpect(status().isConflict());
  }

  @Test
  void debeRechazarAsignacionSinRolAgricultor() throws Exception {
    mockMvc.perform(post(URL_ASIGNACION)
        .header("Authorization", "Bearer token-comprador")
        .contentType(MediaType.APPLICATION_JSON)
        .content(cuerpo(3L)))
        .andExpect(status().isForbidden());
  }

  @Test
  void debeMostrarAlCompradorElTransporteAsignado() throws Exception {
    when(despachoService.consultarTransporte(100L, 5L)).thenReturn(
        new TransporteResponse(100L, "CAMIONETA", "ABC123", LocalDate.of(2026, 11, 20)));

    mockMvc.perform(get(URL_TRANSPORTE).header("Authorization", "Bearer token-comprador"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tipoTransporte").value("CAMIONETA"))
        .andExpect(jsonPath("$.placa").value("ABC123"));
  }

  @Test
  void debeResponder404SiElPedidoAunNoTieneTransporte() throws Exception {
    when(despachoService.consultarTransporte(100L, 5L)).thenThrow(
        new RecursoNoEncontradoException("El pedido 100 aún no tiene transporte asignado"));

    mockMvc.perform(get(URL_TRANSPORTE).header("Authorization", "Bearer token-comprador"))
        .andExpect(status().isNotFound());
  }

  @Test
  void debeRechazarConsultaDeTransporteSinRolComprador() throws Exception {
    mockMvc.perform(get(URL_TRANSPORTE).header("Authorization", "Bearer token-agricultor"))
        .andExpect(status().isForbidden());
  }
}
