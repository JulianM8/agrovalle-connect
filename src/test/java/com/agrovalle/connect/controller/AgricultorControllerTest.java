package com.agrovalle.connect.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agrovalle.connect.config.SecurityConfig;
import com.agrovalle.connect.dto.AgricultorRegistroRequest;
import com.agrovalle.connect.dto.AgricultorRegistroResponse;
import com.agrovalle.connect.exception.GlobalExceptionHandler;
import com.agrovalle.connect.exception.IdentificacionDuplicadaException;
import com.agrovalle.connect.service.AgricultorService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AgricultorController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AgricultorControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private AgricultorService agricultorService;

  private AgricultorRegistroRequest crearRequestValido() {
    return new AgricultorRegistroRequest(
        "Juan", "Perez", "111078900", "Palmira", "3001234567", "juan@correo.com");
  }

  @Test
  void debeRegistrarAgricultorConDatosValidos() throws Exception {
    AgricultorRegistroResponse respuesta = new AgricultorRegistroResponse(
        1L, "Juan", "Perez", "111078900", "Palmira", "Registro realizado correctamente");
    when(agricultorService.registrar(any(AgricultorRegistroRequest.class)))
        .thenReturn(respuesta);

    mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(crearRequestValido())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.mensaje").value("Registro realizado correctamente"));
  }

  @Test
  void debeRechazarDatosIncompletos() throws Exception {
    AgricultorRegistroRequest requestInvalido = new AgricultorRegistroRequest(
        "", "Perez", "111078900", "Palmira", "3001234567", "juan@correo.com");

    mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestInvalido)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void debeRechazarIdentificacionDuplicada() throws Exception {
    when(agricultorService.registrar(any(AgricultorRegistroRequest.class)))
        .thenThrow(new IdentificacionDuplicadaException("123456"));

    mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(crearRequestValido())))
        .andExpect(status().isConflict());
  }
}