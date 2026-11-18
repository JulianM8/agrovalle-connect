package com.agrovalle.connect.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agrovalle.connect.config.SecurityConfig;
import com.agrovalle.connect.dto.TransportistaRegistroRequest;
import com.agrovalle.connect.dto.TransportistaRegistroResponse;
import com.agrovalle.connect.exception.CorreoDuplicadoException;
import com.agrovalle.connect.exception.CredencialesInvalidasException;
import com.agrovalle.connect.exception.GlobalExceptionHandler;
import com.agrovalle.connect.exception.IdentificacionDuplicadaException;
import com.agrovalle.connect.service.JwtService;
import com.agrovalle.connect.service.TransportistaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(TransportistaController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
class TransportistaControllerTest {

  private static final String URL = "/api/v1/auth/register/transportista";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private TransportistaService transportistaService;

  @MockitoBean
  private JwtService jwtService;

  private TransportistaRegistroRequest crearRequest(String nombre, String identificacion) {
    return new TransportistaRegistroRequest(
        nombre, "Rios", identificacion, "3001234567", "mateo@correo.com", "contrasena123",
        "Palmira");
  }

  @Test
  void debeRegistrarTransportistaConDatosValidos() throws Exception {
    when(transportistaService.registrar(any(TransportistaRegistroRequest.class)))
        .thenReturn(new TransportistaRegistroResponse(
            1L, "Mateo", "Rios", "1111078902", "Palmira", "Registro realizado correctamente"));

    mockMvc.perform(post(URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(crearRequest("Mateo", "1111078902"))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.mensaje").value("Registro realizado correctamente"));
  }

  @Test
  void debeRechazarDatosIncompletos() throws Exception {
    mockMvc.perform(post(URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(crearRequest("", "1111078902"))))
        .andExpect(status().isBadRequest());
  }

  @Test
  void debeRechazarIdentificacionConMenosDe10Digitos() throws Exception {
    mockMvc.perform(post(URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(crearRequest("Mateo", "123456"))))
        .andExpect(status().isBadRequest());
  }

  @Test
  void debeRechazarIdentificacionDuplicada() throws Exception {
    when(transportistaService.registrar(any(TransportistaRegistroRequest.class)))
        .thenThrow(new IdentificacionDuplicadaException("1111078902"));

    mockMvc.perform(post(URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(crearRequest("Mateo", "1111078902"))))
        .andExpect(status().isConflict());
  }

  @Test
  void debeRechazarCorreoDuplicado() throws Exception {
    when(transportistaService.registrar(any(TransportistaRegistroRequest.class)))
        .thenThrow(new CorreoDuplicadoException("mateo@correo.com"));

    mockMvc.perform(post(URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(crearRequest("Mateo", "1111078902"))))
        .andExpect(status().isConflict());
  }

  @Test
  void debeRechazarContrasenaIncorrectaAlAgregarRol() throws Exception {
    when(transportistaService.registrar(any(TransportistaRegistroRequest.class)))
        .thenThrow(new CredencialesInvalidasException());

    mockMvc.perform(post(URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(crearRequest("Mateo", "1111078902"))))
        .andExpect(status().isUnauthorized());
  }
}
