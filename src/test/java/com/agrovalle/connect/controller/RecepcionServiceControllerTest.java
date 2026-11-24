package com.agrovalle.connect.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agrovalle.connect.dto.RecepcionResponse;
import com.agrovalle.connect.exception.ConflictoException;
import com.agrovalle.connect.model.enums.EstadoPedido;
import com.agrovalle.connect.service.RecepcionService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecepcionControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private RecepcionService recepcionService;

  private static UsernamePasswordAuthenticationToken usuario(Long id, String rol) {
    return new UsernamePasswordAuthenticationToken(
        id, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
  }

  @Test
  void compradorConfirmaRecepcionYRecibe200() throws Exception {
    when(recepcionService.confirmarRecepcion(eq(1L), eq(7L)))
        .thenReturn(new RecepcionResponse(1L, EstadoPedido.ENTREGADO, LocalDateTime.now()));

    mockMvc.perform(post("/api/v1/pedidos/1/recepcion")
            .with(authentication(usuario(7L, "COMPRADOR"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.estado").value("ENTREGADO"));
  }

  @Test
  void conflictoDelServicioSeTraduceA409() throws Exception {
    when(recepcionService.confirmarRecepcion(eq(1L), eq(7L)))
        .thenThrow(new ConflictoException("El pedido ya fue confirmado como entregado."));

    mockMvc.perform(post("/api/v1/pedidos/1/recepcion")
            .with(authentication(usuario(7L, "COMPRADOR"))))
        .andExpect(status().isConflict());
  }

  @Test
  void usuarioSinRolCompradorRecibe403() throws Exception {
    mockMvc.perform(post("/api/v1/pedidos/1/recepcion")
            .with(authentication(usuario(3L, "AGRICULTOR"))))
        .andExpect(status().isForbidden());
  }
}