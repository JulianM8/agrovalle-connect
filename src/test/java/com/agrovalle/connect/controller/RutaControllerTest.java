package com.agrovalle.connect.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agrovalle.connect.config.SecurityConfig;
import com.agrovalle.connect.dto.ParadaRutaResponse;
import com.agrovalle.connect.dto.RutaDiaResponse;
import com.agrovalle.connect.exception.CriterioOrdenInvalidoException;
import com.agrovalle.connect.exception.GlobalExceptionHandler;
import com.agrovalle.connect.exception.RecursoNoEncontradoException;
import com.agrovalle.connect.model.enums.EstadoPedido;
import com.agrovalle.connect.service.JwtService;
import com.agrovalle.connect.service.RutaService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RutaController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
class RutaControllerTest {

  private static final String URL = "/api/v1/rutas";

  private static final LocalDate FECHA = LocalDate.of(2026, 11, 20);

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private RutaService rutaService;

  @MockitoBean
  private JwtService jwtService;

  @BeforeEach
  void autenticar() {
    when(jwtService.extraerUsuarioId(eq("token-transportista"))).thenReturn(9L);
    when(jwtService.extraerRoles(eq("token-transportista"))).thenReturn(List.of("TRANSPORTISTA"));
    when(jwtService.extraerUsuarioId(eq("token-comprador"))).thenReturn(5L);
    when(jwtService.extraerRoles(eq("token-comprador"))).thenReturn(List.of("COMPRADOR"));
  }

  private RutaDiaResponse rutaConUnaParada() {
    return new RutaDiaResponse(FECHA, "CAMIONETA ABC123", List.of(
        new ParadaRutaResponse(1, 100L, "Sofia Torres", "Cali", "Calle 5 # 10-20",
            EstadoPedido.EN_TRANSPORTE)));
  }

  @Test
  void debeDevolverLaRutaConParadasOrdenadas() throws Exception {
    when(rutaService.consultarRutaDelDia(9L, FECHA, "MUNICIPIO")).thenReturn(rutaConUnaParada());

    mockMvc.perform(get(URL).param("fecha", "2026-11-20").param("criterio", "MUNICIPIO")
        .header("Authorization", "Bearer token-transportista"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.vehiculo").value("CAMIONETA ABC123"))
        .andExpect(jsonPath("$.paradas[0].orden").value(1))
        .andExpect(jsonPath("$.paradas[0].municipio").value("Cali"));
  }

  @Test
  void debeUsarLaFechaDeHoyYElCriterioPorDefectoSiNoSeEnvian() throws Exception {
    when(rutaService.consultarRutaDelDia(eq(9L), any(LocalDate.class), isNull()))
        .thenReturn(rutaConUnaParada());

    mockMvc.perform(get(URL).header("Authorization", "Bearer token-transportista"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paradas.length()").value(1));
  }

  @Test
  void debeResponder400ConCriterioInvalido() throws Exception {
    when(rutaService.consultarRutaDelDia(eq(9L), any(LocalDate.class), eq("ALEATORIO")))
        .thenThrow(new CriterioOrdenInvalidoException("ALEATORIO"));

    mockMvc.perform(get(URL).param("criterio", "ALEATORIO")
        .header("Authorization", "Bearer token-transportista"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.mensaje").exists());
  }

  @Test
  void debeResponder404SinVehiculoORutaParaLaFecha() throws Exception {
    when(rutaService.consultarRutaDelDia(eq(9L), any(LocalDate.class), isNull()))
        .thenThrow(new RecursoNoEncontradoException("No hay una ruta programada"));

    mockMvc.perform(get(URL).header("Authorization", "Bearer token-transportista"))
        .andExpect(status().isNotFound());
  }

  @Test
  void debeRechazarSinRolTransportista() throws Exception {
    mockMvc.perform(get(URL).header("Authorization", "Bearer token-comprador"))
        .andExpect(status().isForbidden());
  }
}