package com.agrovalle.connect.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.agrovalle.connect.dto.ParadaRutaResponse;
import com.agrovalle.connect.dto.RutaDiaResponse;
import com.agrovalle.connect.exception.CriterioOrdenInvalidoException;
import com.agrovalle.connect.exception.RecursoNoEncontradoException;
import com.agrovalle.connect.model.AsignacionTransporte;
import com.agrovalle.connect.model.Comprador;
import com.agrovalle.connect.model.Pedido;
import com.agrovalle.connect.model.Ruta;
import com.agrovalle.connect.model.Usuario;
import com.agrovalle.connect.model.Vehiculo;
import com.agrovalle.connect.model.enums.Rol;
import com.agrovalle.connect.repository.AsignacionTransporteRepository;
import com.agrovalle.connect.repository.RutaRepository;
import com.agrovalle.connect.repository.VehiculoRepository;
import com.agrovalle.connect.service.ruta.PorMunicipioStrategy;
import com.agrovalle.connect.service.ruta.VecinoMasCercanoStrategy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RutaServiceTest {

  private static final LocalDate FECHA = LocalDate.of(2026, 11, 20);

  @Mock
  private VehiculoRepository vehiculoRepository;

  @Mock
  private RutaRepository rutaRepository;

  @Mock
  private AsignacionTransporteRepository asignacionRepository;

  private RutaService rutaService;

  private Vehiculo vehiculo;

  private Ruta ruta;

  @BeforeEach
  void preparar() {
    rutaService = new RutaService(vehiculoRepository, rutaRepository, asignacionRepository,
        Map.of("MUNICIPIO", new PorMunicipioStrategy(),
            "VECINO_MAS_CERCANO", new VecinoMasCercanoStrategy()));
    vehiculo = new Vehiculo("CAMIONETA", "ABC123", new BigDecimal("800.00"), null);
    vehiculo.setId(1L);
    ruta = new Ruta(FECHA, vehiculo);
    ReflectionTestUtils.setField(ruta, "id", 2L);
  }

  private AsignacionTransporte asignacion(long id, String municipio, String lat, String lon) {
    Usuario usuario = new Usuario("90000" + id, "Comprador", String.valueOf(id), "300",
        "c" + id + "@correo.com", "clave", Rol.COMPRADOR);
    BigDecimal latitud = lat == null ? null : new BigDecimal(lat);
    BigDecimal longitud = lon == null ? null : new BigDecimal(lon);
    Comprador comprador = new Comprador(municipio, "Calle " + id, latitud, longitud, usuario);
    Pedido pedido = new Pedido(comprador, null, "Calle " + id);
    pedido.setId(id);
    return new AsignacionTransporte(pedido, ruta, (int) id);
  }

  private void prepararRutaConParadas() {
    when(vehiculoRepository.findByTransportistaUsuarioId(5L)).thenReturn(Optional.of(vehiculo));
    when(rutaRepository.findByVehiculoIdAndFecha(1L, FECHA)).thenReturn(Optional.of(ruta));
    when(asignacionRepository.findByRutaIdOrderByOrdenEntrega(2L)).thenReturn(List.of(
        asignacion(1, "Cali", "3.451647", "-76.531985"),
        asignacion(2, "Palmira", "3.539400", "-76.303600"),
        asignacion(3, "Jamundi", null, null),
        asignacion(4, "Yumbo", "3.585300", "-76.495300")));
  }

  private List<Long> pedidos(RutaDiaResponse respuesta) {
    return respuesta.paradas().stream().map(ParadaRutaResponse::pedidoId).toList();
  }

  @Test
  void debeUsarVecinoMasCercanoPorDefecto() {
    prepararRutaConParadas();

    RutaDiaResponse respuesta = rutaService.consultarRutaDelDia(5L, FECHA, null);

    assertEquals(List.of(1L, 4L, 2L, 3L), pedidos(respuesta));
    assertEquals(1, respuesta.paradas().get(0).orden());
    assertEquals("Comprador 1", respuesta.paradas().get(0).nombreComprador());
    assertEquals("CAMIONETA ABC123", respuesta.vehiculo());
  }

  @Test
  void debeOrdenarPorMunicipioSiSeSolicita() {
    prepararRutaConParadas();

    RutaDiaResponse respuesta = rutaService.consultarRutaDelDia(5L, FECHA, "municipio");

    assertEquals(List.of(1L, 3L, 2L, 4L), pedidos(respuesta));
  }

  @Test
  void debeRechazarCriterioInvalido() {
    assertThrows(CriterioOrdenInvalidoException.class,
        () -> rutaService.consultarRutaDelDia(5L, FECHA, "ALEATORIO"));
  }

  @Test
  void debeFallarSiElTransportistaNoTieneVehiculo() {
    when(vehiculoRepository.findByTransportistaUsuarioId(5L)).thenReturn(Optional.empty());

    assertThrows(RecursoNoEncontradoException.class,
        () -> rutaService.consultarRutaDelDia(5L, FECHA, null));
  }

  @Test
  void debeFallarSiNoHayRutaParaLaFecha() {
    when(vehiculoRepository.findByTransportistaUsuarioId(5L)).thenReturn(Optional.of(vehiculo));
    when(rutaRepository.findByVehiculoIdAndFecha(1L, FECHA)).thenReturn(Optional.empty());

    assertThrows(RecursoNoEncontradoException.class,
        () -> rutaService.consultarRutaDelDia(5L, FECHA, null));
  }
}