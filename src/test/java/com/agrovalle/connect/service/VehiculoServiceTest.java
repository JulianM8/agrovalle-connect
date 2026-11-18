package com.agrovalle.connect.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agrovalle.connect.dto.VehiculoRequest;
import com.agrovalle.connect.dto.VehiculoResponse;
import com.agrovalle.connect.dto.VehiculosPorTipoResponse;
import com.agrovalle.connect.exception.ConflictoException;
import com.agrovalle.connect.exception.PedidoInvalidoException;
import com.agrovalle.connect.model.Transportista;
import com.agrovalle.connect.model.Usuario;
import com.agrovalle.connect.model.Vehiculo;
import com.agrovalle.connect.model.enums.Rol;
import com.agrovalle.connect.repository.TransportistaRepository;
import com.agrovalle.connect.repository.VehiculoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehiculoServiceTest {

  @Mock
  private VehiculoRepository vehiculoRepository;

  @Mock
  private TransportistaRepository transportistaRepository;

  @InjectMocks
  private VehiculoService vehiculoService;

  private Transportista transportista;

  @BeforeEach
  void preparar() {
    Usuario usuario = new Usuario("1111000003", "Luis", "Gomez", "3003333333",
        "luis@correo.com", "clave", Rol.TRANSPORTISTA);
    usuario.setId(9L);
    transportista = new Transportista("Cali", usuario);
    transportista.setId(4L);
  }

  private VehiculoRequest solicitud() {
    return new VehiculoRequest("camioneta", "abc123", new BigDecimal("1500"));
  }

  @Test
  void debeRegistrarVehiculoNormalizandoTipoYPlaca() {
    when(transportistaRepository.findByUsuarioId(9L)).thenReturn(Optional.of(transportista));
    when(vehiculoRepository.findByTransportistaId(4L)).thenReturn(Optional.empty());
    when(vehiculoRepository.existsByPlaca("ABC123")).thenReturn(false);
    when(vehiculoRepository.save(any(Vehiculo.class)))
        .thenAnswer(invocacion -> invocacion.getArgument(0));

    VehiculoResponse respuesta = vehiculoService.registrar(solicitud(), 9L);

    assertEquals("ABC123", respuesta.placa());
    assertEquals("CAMIONETA", respuesta.tipoTransporte());
    assertEquals("Luis Gomez", respuesta.transportista());
  }

  @Test
  void debeRechazarPlacaDuplicada() {
    when(transportistaRepository.findByUsuarioId(9L)).thenReturn(Optional.of(transportista));
    when(vehiculoRepository.findByTransportistaId(4L)).thenReturn(Optional.empty());
    when(vehiculoRepository.existsByPlaca("ABC123")).thenReturn(true);

    assertThrows(ConflictoException.class, () -> vehiculoService.registrar(solicitud(), 9L));
    verify(vehiculoRepository, never()).save(any(Vehiculo.class));
  }

  @Test
  void debeRechazarTransportistaQueYaTieneVehiculo() {
    when(transportistaRepository.findByUsuarioId(9L)).thenReturn(Optional.of(transportista));
    when(vehiculoRepository.findByTransportistaId(4L))
        .thenReturn(Optional.of(new Vehiculo("CAMION", "XYZ789", BigDecimal.TEN, transportista)));

    assertThrows(ConflictoException.class, () -> vehiculoService.registrar(solicitud(), 9L));
  }

  @Test
  void debeListarVehiculosDelTipoSolicitado() {
    when(vehiculoRepository.findByTipoTransporte("CAMIONETA")).thenReturn(
        List.of(new Vehiculo("CAMIONETA", "ABC123", new BigDecimal("1500"), transportista)));

    VehiculosPorTipoResponse respuesta = vehiculoService.listarPorTipo("camioneta");

    assertEquals(1, respuesta.vehiculos().size());
  }

  @Test
  void debeResponderMensajeClaroSiNoHayVehiculosDelTipo() {
    when(vehiculoRepository.findByTipoTransporte("MOTOCARRO")).thenReturn(List.of());

    VehiculosPorTipoResponse respuesta = vehiculoService.listarPorTipo("MOTOCARRO");

    assertTrue(respuesta.vehiculos().isEmpty());
    assertTrue(respuesta.mensaje().startsWith("No hay vehículos"));
  }

  @Test
  void debeRechazarTipoDeTransporteVacio() {
    assertThrows(PedidoInvalidoException.class, () -> vehiculoService.listarPorTipo(" "));
  }
}
