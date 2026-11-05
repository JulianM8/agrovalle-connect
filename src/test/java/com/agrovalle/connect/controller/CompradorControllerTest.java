package com.agrovalle.connect.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agrovalle.connect.config.SecurityConfig;
import com.agrovalle.connect.dto.CompradorRegistroRequest;
import com.agrovalle.connect.dto.CompradorRegistroResponse;
import com.agrovalle.connect.exception.CorreoDuplicadoException;
import com.agrovalle.connect.exception.CredencialesInvalidasException;
import com.agrovalle.connect.exception.GlobalExceptionHandler;
import com.agrovalle.connect.exception.IdentificacionDuplicadaException;
import com.agrovalle.connect.service.CompradorService;
import com.agrovalle.connect.service.JwtService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(CompradorController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
class CompradorControllerTest {

  private static final String URL = "/api/v1/auth/register/comprador";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private CompradorService compradorService;

  @MockitoBean
  private JwtService jwtService;

  private CompradorRegistroRequest crearRequest(String nombre, BigDecimal latitud) {
    return new CompradorRegistroRequest(
        nombre, "Torres", "1111078901", "3001234567", "sofia@correo.com", "contrasena123",
        "Cali", "Calle 5 # 10-20", latitud, new BigDecimal("-76.531985"));
  }

  @Test
  void debeRegistrarCompradorConDatosValidos() throws Exception {
    when(compradorService.registrar(any(CompradorRegistroRequest.class)))
        .thenReturn(new CompradorRegistroResponse(
            1L, "Sofia", "Torres", "1111078901", "Cali", "Calle 5 # 10-20",
            "Registro realizado correctamente"));

    mockMvc.perform(post(URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(
            crearRequest("Sofia", new BigDecimal("3.451647")))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.mensaje").value("Registro realizado correctamente"));
  }

  @Test
  void debeRechazarDatosIncompletos() throws Exception {
    mockMvc.perform(post(URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(
            crearRequest("", new BigDecimal("3.451647")))))
        .andExpect(status().isBadRequest());
  }

  @Test
  void debeRechazarLatitudFueraDeRango() throws Exception {
    mockMvc.perform(post(URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(
            crearRequest("Sofia", new BigDecimal("95.0")))))
        .andExpect(status().isBadRequest());
  }

  @Test
  void debeRechazarIdentificacionDuplicada() throws Exception {
    when(compradorService.registrar(any(CompradorRegistroRequest.class)))
        .thenThrow(new IdentificacionDuplicadaException("1111078901"));

    mockMvc.perform(post(URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(
            crearRequest("Sofia", new BigDecimal("3.451647")))))
        .andExpect(status().isConflict());
  }

  @Test
  void debeRechazarCorreoDuplicado() throws Exception {
    when(compradorService.registrar(any(CompradorRegistroRequest.class)))
        .thenThrow(new CorreoDuplicadoException("sofia@correo.com"));

    mockMvc.perform(post(URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(
            crearRequest("Sofia", new BigDecimal("3.451647")))))
        .andExpect(status().isConflict());
  }

  @Test
  void debeRechazarContrasenaIncorrectaAlAgregarRol() throws Exception {
    when(compradorService.registrar(any(CompradorRegistroRequest.class)))
        .thenThrow(new CredencialesInvalidasException());

    mockMvc.perform(post(URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(
            crearRequest("Sofia", new BigDecimal("3.451647")))))
        .andExpect(status().isUnauthorized());
  }
}
