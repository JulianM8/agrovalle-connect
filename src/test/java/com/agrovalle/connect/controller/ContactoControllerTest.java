package com.agrovalle.connect.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agrovalle.connect.config.SecurityConfig;
import com.agrovalle.connect.dto.ContactoRequest;
import com.agrovalle.connect.dto.ContactoResponse;
import com.agrovalle.connect.dto.ContactoRespuestaRequest;
import com.agrovalle.connect.exception.GlobalExceptionHandler;
import com.agrovalle.connect.exception.RecursoNoEncontradoException;
import com.agrovalle.connect.service.ContactoService;
import com.agrovalle.connect.service.JwtService;
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

@WebMvcTest({ProductoContactoController.class, ContactoController.class})
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ContactoControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private ContactoService contactoService;

  @MockitoBean
  private JwtService jwtService;

  @BeforeEach
  void autenticar() {
    when(jwtService.extraerUsuarioId("token-comprador")).thenReturn(5L);
    when(jwtService.extraerRoles("token-comprador")).thenReturn(List.of("COMPRADOR"));
    when(jwtService.extraerUsuarioId("token-agricultor")).thenReturn(7L);
    when(jwtService.extraerRoles("token-agricultor")).thenReturn(List.of("AGRICULTOR"));
  }

  @Test
  void debeResponder201AlEnviarContacto() throws Exception {
    ContactoResponse respuesta = new ContactoResponse(11L, 3L, 10L,
        "¿Está disponible?", LocalDateTime.now(), null, null);
    when(contactoService.enviar(eq(10L), any(ContactoRequest.class), eq(5L)))
        .thenReturn(respuesta);

    mockMvc.perform(post("/api/v1/productos/10/contacto")
        .header("Authorization", "Bearer token-comprador")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(new ContactoRequest("¿Está disponible?"))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(11))
        .andExpect(jsonPath("$.mensaje").value("¿Está disponible?"));
  }

  @Test
  void debeResponder200AlResponderContacto() throws Exception {
    ContactoResponse respuesta = new ContactoResponse(11L, 3L, 10L,
        "¿Está disponible?", LocalDateTime.now(), "Sí, disponible", LocalDateTime.now());
    when(contactoService.responder(11L, "Sí, disponible", 7L)).thenReturn(respuesta);

    mockMvc.perform(post("/api/v1/contactos/11/respuesta")
        .header("Authorization", "Bearer token-agricultor")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(
            new ContactoRespuestaRequest("Sí, disponible"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(11))
        .andExpect(jsonPath("$.respuesta").value("Sí, disponible"));
  }

  @Test
  void debeResponder404SiElProductoNoExiste() throws Exception {
    when(contactoService.enviar(eq(99L), any(ContactoRequest.class), eq(5L)))
        .thenThrow(new RecursoNoEncontradoException("No existe el producto con id 99"));

    mockMvc.perform(post("/api/v1/productos/99/contacto")
        .header("Authorization", "Bearer token-comprador")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(new ContactoRequest("¿Está disponible?"))))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.mensaje").value("No existe el producto con id 99"));
  }
}
