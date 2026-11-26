package com.agrovalle.connect.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.agrovalle.connect.config.SecurityConfig;
import com.agrovalle.connect.dto.InconformidadRequest;
import com.agrovalle.connect.dto.InconformidadResponse;
import com.agrovalle.connect.exception.ConflictoException;
import com.agrovalle.connect.exception.GlobalExceptionHandler;
import com.agrovalle.connect.exception.PedidoInvalidoException;
import com.agrovalle.connect.exception.RecursoNoEncontradoException;
import com.agrovalle.connect.model.enums.EstadoInconformidad;
import com.agrovalle.connect.service.InconformidadService;
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
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(InconformidadController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class InconformidadControllerTest {

  private static final String RUTA = "/api/v1/pedidos/10/inconformidad";

  private static final InconformidadRequest REPORTE =
      new InconformidadRequest("Producto dañado", "https://example.com/evidencia.jpg");

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private InconformidadService inconformidadService;

  @MockitoBean
  private JwtService jwtService;

  @BeforeEach
  void autenticar() {
    when(jwtService.extraerUsuarioId("comprador")).thenReturn(5L);
    when(jwtService.extraerRoles("comprador")).thenReturn(List.of("COMPRADOR"));
    when(jwtService.extraerUsuarioId("agricultor")).thenReturn(7L);
    when(jwtService.extraerRoles("agricultor")).thenReturn(List.of("AGRICULTOR"));
  }

  @Test
  void debeResponder201ConEvidencia() throws Exception {
    when(inconformidadService.reportar(10L, REPORTE, 5L)).thenReturn(respuesta());

    enviar(REPORTE, "comprador")
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.evidenciaUrl").value(REPORTE.evidenciaUrl()))
        .andExpect(jsonPath("$.estado").value("EN_REVISION"));
  }

  @Test
  void debeResponder400CuandoLaEvidenciaEsInvalida() throws Exception {
    enviar(new InconformidadRequest(REPORTE.descripcion(), "ftp://example.com/foto.jpg"), "comprador")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.evidenciaUrl").exists());
    verifyNoInteractions(inconformidadService);
  }

  @Test
  void debeResponder400CuandoElPedidoNoEstaEnTransporte() throws Exception {
    when(inconformidadService.reportar(10L, REPORTE, 5L))
        .thenThrow(new PedidoInvalidoException("El pedido debe estar en transporte"));

    enviar(REPORTE, "comprador").andExpect(status().isBadRequest());
  }

  @Test
  void debeResponder404ParaUnPedidoAjeno() throws Exception {
    when(inconformidadService.reportar(10L, REPORTE, 5L))
        .thenThrow(new RecursoNoEncontradoException("No existe el pedido con id 10"));

    enviar(REPORTE, "comprador").andExpect(status().isNotFound());
  }

  @Test
  void debeResponder409SiYaHayUnReporteEnRevision() throws Exception {
    when(inconformidadService.reportar(10L, REPORTE, 5L))
        .thenThrow(new ConflictoException("El pedido ya tiene una inconformidad en revisión"));

    enviar(REPORTE, "comprador").andExpect(status().isConflict());
  }

  @Test
  void debeResponder403SinRolComprador() throws Exception {
    enviar(REPORTE, "agricultor").andExpect(status().isForbidden());
    mockMvc.perform(get(RUTA).header("Authorization", "Bearer agricultor"))
        .andExpect(status().isForbidden());
    verifyNoInteractions(inconformidadService);
  }

  @Test
  void debeConsultarElEstadoDeLaRevision() throws Exception {
    when(inconformidadService.consultar(10L, 5L)).thenReturn(respuesta());

    mockMvc.perform(get(RUTA).header("Authorization", "Bearer comprador"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pedidoId").value(10))
        .andExpect(jsonPath("$.estado").value("EN_REVISION"));
  }

  private ResultActions enviar(InconformidadRequest reporte, String token) throws Exception {
    return mockMvc.perform(post(RUTA)
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(reporte)));
  }

  private InconformidadResponse respuesta() {
    return new InconformidadResponse(11L, 10L, REPORTE.descripcion(), REPORTE.evidenciaUrl(),
        EstadoInconformidad.EN_REVISION, null, LocalDateTime.of(2026, 11, 23, 12, 0));
  }
}
