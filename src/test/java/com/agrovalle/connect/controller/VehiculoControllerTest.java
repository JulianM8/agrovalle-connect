package com.agrovalle.connect.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agrovalle.connect.config.SecurityConfig;
import com.agrovalle.connect.dto.VehiculoRequest;
import com.agrovalle.connect.dto.VehiculoResponse;
import com.agrovalle.connect.dto.VehiculosPorTipoResponse;
import com.agrovalle.connect.exception.ConflictoException;
import com.agrovalle.connect.exception.GlobalExceptionHandler;
import com.agrovalle.connect.exception.PedidoInvalidoException;
import com.agrovalle.connect.exception.RecursoNoEncontradoException;
import com.agrovalle.connect.service.JwtService;
import com.agrovalle.connect.service.VehiculoService;
import java.math.BigDecimal;
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

@WebMvcTest(VehiculoController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
class VehiculoControllerTest {

  private static final String URL = "/api/v1/vehiculos";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private VehiculoService vehiculoService;

  @MockitoBean
  private JwtService jwtService;

  @BeforeEach
  void autenticar() {
    when(jwtService.extraerUsuarioId(eq("token-transportista"))).thenReturn(9L);
    when(jwtService.extraerRoles(eq("token-transportista")))
        .thenReturn(List.of("TRANSPORTISTA"));
    when(jwtService.extraerUsuarioId(eq("token-agricultor"))).thenReturn(7L);
    when(jwtService.extraerRoles(eq("token-agricultor"))).thenReturn(List.of("AGRICULTOR"));
  }

  private String cuerpo(String tipo, String placa, String capacidad) throws Exception {
    return objectMapper.writeValueAsString(
        new VehiculoRequest(tipo, placa, capacidad == null ? null : new BigDecimal(capacidad)));
  }

  private VehiculoResponse vehiculo() {
    return new VehiculoResponse(3L, "CAMIONETA", "ABC123", new BigDecimal("1500.00"),
        "Luis Gomez", "Cali");
  }

  @Test
  void debeRegistrarVehiculoYResponder201() throws Exception {
    when(vehiculoService.registrar(any(VehiculoRequest.class), eq(9L))).thenReturn(vehiculo());

    mockMvc.perform(post(URL)
        .header("Authorization", "Bearer token-transportista")
        .contentType(MediaType.APPLICATION_JSON)
        .content(cuerpo("camioneta", "abc123", "1500")))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.placa").value("ABC123"))
        .andExpect(jsonPath("$.tipoTransporte").value("CAMIONETA"));
  }

  @Test
  void debeRechazarPlacaDuplicada() throws Exception {
    when(vehiculoService.registrar(any(VehiculoRequest.class), eq(9L)))
        .thenThrow(new ConflictoException("Ya existe un vehículo con la placa ABC123"));

    mockMvc.perform(post(URL)
        .header("Authorization", "Bearer token-transportista")
        .contentType(MediaType.APPLICATION_JSON)
        .content(cuerpo("CAMIONETA", "ABC123", "1500")))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.mensaje").value("Ya existe un vehículo con la placa ABC123"));
  }

  @Test
  void debeResponder400SiLaCapacidadNoEsPositiva() throws Exception {
    mockMvc.perform(post(URL)
        .header("Authorization", "Bearer token-transportista")
        .contentType(MediaType.APPLICATION_JSON)
        .content(cuerpo("CAMIONETA", "ABC123", "0")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.capacidadKg").exists());
  }

  @Test
  void debeResponder404SiElTransportistaNoTienePerfil() throws Exception {
    when(vehiculoService.registrar(any(VehiculoRequest.class), eq(9L)))
        .thenThrow(new RecursoNoEncontradoException(
            "El usuario autenticado no tiene perfil de transportista"));

    mockMvc.perform(post(URL)
        .header("Authorization", "Bearer token-transportista")
        .contentType(MediaType.APPLICATION_JSON)
        .content(cuerpo("CAMIONETA", "ABC123", "1500")))
        .andExpect(status().isNotFound());
  }

  @Test
  void debeRechazarRegistroSinRolTransportista() throws Exception {
    mockMvc.perform(post(URL)
        .header("Authorization", "Bearer token-agricultor")
        .contentType(MediaType.APPLICATION_JSON)
        .content(cuerpo("CAMIONETA", "ABC123", "1500")))
        .andExpect(status().isForbidden());
  }

  @Test
  void debeListarVehiculosPorTipoDeTransporte() throws Exception {
    when(vehiculoService.listarPorTipo("CAMIONETA")).thenReturn(
        new VehiculosPorTipoResponse("1 vehículo(s) del tipo CAMIONETA", List.of(vehiculo())));

    mockMvc.perform(get(URL)
        .param("tipoTransporte", "CAMIONETA")
        .header("Authorization", "Bearer token-agricultor"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.vehiculos[0].placa").value("ABC123"))
        .andExpect(jsonPath("$.vehiculos.length()").value(1));
  }

  @Test
  void debeResponder200ConMensajeSiNoHayVehiculosDelTipo() throws Exception {
    when(vehiculoService.listarPorTipo("MOTOCARRO")).thenReturn(
        new VehiculosPorTipoResponse("No hay vehículos disponibles del tipo MOTOCARRO",
            List.of()));

    mockMvc.perform(get(URL)
        .param("tipoTransporte", "MOTOCARRO")
        .header("Authorization", "Bearer token-agricultor"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.mensaje").value(
            "No hay vehículos disponibles del tipo MOTOCARRO"))
        .andExpect(jsonPath("$.vehiculos").isEmpty());
  }

  @Test
  void debeResponder400SiFaltaElTipoDeTransporte() throws Exception {
    when(vehiculoService.listarPorTipo(null))
        .thenThrow(new PedidoInvalidoException("El tipo de transporte es obligatorio"));

    mockMvc.perform(get(URL).header("Authorization", "Bearer token-agricultor"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void debeRechazarListadoSinRolAgricultor() throws Exception {
    mockMvc.perform(get(URL)
        .param("tipoTransporte", "CAMIONETA")
        .header("Authorization", "Bearer token-transportista"))
        .andExpect(status().isForbidden());
  }
}
