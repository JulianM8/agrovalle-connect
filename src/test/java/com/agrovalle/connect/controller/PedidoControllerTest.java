package com.agrovalle.connect.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agrovalle.connect.config.SecurityConfig;
import com.agrovalle.connect.dto.CancelacionRequest;
import com.agrovalle.connect.dto.CancelacionResponse;
import com.agrovalle.connect.exception.GlobalExceptionHandler;
import com.agrovalle.connect.exception.PedidoInvalidoException;
import com.agrovalle.connect.exception.RecursoNoEncontradoException;
import com.agrovalle.connect.model.enums.EstadoPedido;
import com.agrovalle.connect.service.JwtService;
import com.agrovalle.connect.service.PagoService;
import com.agrovalle.connect.service.PedidoService;
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

@WebMvcTest(PedidoController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
class PedidoControllerTest {

  private static final String URL = "/api/v1/pedidos/100/cancelacion";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private PedidoService pedidoService;

  @MockitoBean
  private PagoService pagoService;

  @MockitoBean
  private JwtService jwtService;

  @BeforeEach
  void autenticar() {
    when(jwtService.extraerUsuarioId(eq("token-comprador"))).thenReturn(5L);
    when(jwtService.extraerRoles(eq("token-comprador"))).thenReturn(List.of("COMPRADOR"));
    when(jwtService.extraerUsuarioId(eq("token-agricultor"))).thenReturn(7L);
    when(jwtService.extraerRoles(eq("token-agricultor"))).thenReturn(List.of("AGRICULTOR"));
  }

  private String cuerpo(String motivo) throws Exception {
    return objectMapper.writeValueAsString(new CancelacionRequest(motivo));
  }

  @Test
  void debeCancelarPedidoYResponder200() throws Exception {
    when(pedidoService.cancelar(100L, "Ya no lo necesito", 5L))
        .thenReturn(new CancelacionResponse(100L, EstadoPedido.CANCELADO,
            "Ya no lo necesito", true, "Pedido cancelado, el pago fue reembolsado"));

    mockMvc.perform(post(URL)
        .header("Authorization", "Bearer token-comprador")
        .contentType(MediaType.APPLICATION_JSON)
        .content(cuerpo("Ya no lo necesito")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.estado").value("CANCELADO"))
        .andExpect(jsonPath("$.reembolsado").value(true));
  }

  @Test
  void debeResponder400SiElEstadoNoEsCancelable() throws Exception {
    when(pedidoService.cancelar(100L, "Motivo", 5L)).thenThrow(
        new PedidoInvalidoException("El pedido no se puede cancelar en estado EN_TRANSPORTE"));

    mockMvc.perform(post(URL)
        .header("Authorization", "Bearer token-comprador")
        .contentType(MediaType.APPLICATION_JSON)
        .content(cuerpo("Motivo")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.mensaje").value(
            "El pedido no se puede cancelar en estado EN_TRANSPORTE"));
  }

  @Test
  void debeResponder404SiElPedidoNoExiste() throws Exception {
    when(pedidoService.cancelar(100L, "Motivo", 5L))
        .thenThrow(new RecursoNoEncontradoException("No existe el pedido con id 100"));

    mockMvc.perform(post(URL)
        .header("Authorization", "Bearer token-comprador")
        .contentType(MediaType.APPLICATION_JSON)
        .content(cuerpo("Motivo")))
        .andExpect(status().isNotFound());
  }

  @Test
  void debeResponder400SiFaltaElMotivo() throws Exception {
    mockMvc.perform(post(URL)
        .header("Authorization", "Bearer token-comprador")
        .contentType(MediaType.APPLICATION_JSON)
        .content(cuerpo("")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.motivo").exists());
  }

  @Test
  void debeRechazarSiElRolNoEsComprador() throws Exception {
    mockMvc.perform(post(URL)
        .header("Authorization", "Bearer token-agricultor")
        .contentType(MediaType.APPLICATION_JSON)
        .content(cuerpo("Motivo")))
        .andExpect(status().isForbidden());
  }

  @Test
  void debeRechazarPeticionSinToken() throws Exception {
    mockMvc.perform(post(URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(cuerpo("Motivo")))
        .andExpect(status().isForbidden());
  }
}