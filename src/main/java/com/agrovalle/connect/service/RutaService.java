package com.agrovalle.connect.service;

import com.agrovalle.connect.dto.ParadaRutaResponse;
import com.agrovalle.connect.dto.RutaDiaResponse;
import com.agrovalle.connect.exception.CriterioOrdenInvalidoException;
import com.agrovalle.connect.exception.RecursoNoEncontradoException;
import com.agrovalle.connect.model.AsignacionTransporte;
import com.agrovalle.connect.model.Pedido;
import com.agrovalle.connect.model.Ruta;
import com.agrovalle.connect.model.Usuario;
import com.agrovalle.connect.model.Vehiculo;
import com.agrovalle.connect.repository.AsignacionTransporteRepository;
import com.agrovalle.connect.repository.RutaRepository;
import com.agrovalle.connect.repository.VehiculoRepository;
import com.agrovalle.connect.service.ruta.OrdenEntregaStrategy;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contexto del patrón Strategy del ordenamiento de entregas. Consulta la ruta
 * del día de un transportista y sugiere el orden de las paradas. Es de solo
 * lectura: no modifica orden_entrega.
 */
@Service
public class RutaService {

  private static final String CRITERIO_POR_DEFECTO = "VECINO_MAS_CERCANO";

  private final VehiculoRepository vehiculoRepository;

  private final RutaRepository rutaRepository;

  private final AsignacionTransporteRepository asignacionRepository;

  private final Map<String, OrdenEntregaStrategy> estrategias;

  public RutaService(VehiculoRepository vehiculoRepository, RutaRepository rutaRepository,
      AsignacionTransporteRepository asignacionRepository,
      Map<String, OrdenEntregaStrategy> estrategias) {
    this.vehiculoRepository = vehiculoRepository;
    this.rutaRepository = rutaRepository;
    this.asignacionRepository = asignacionRepository;
    this.estrategias = estrategias;
  }

  /**
   * Devuelve la ruta del día del transportista autenticado con las paradas en
   * el orden sugerido por la estrategia elegida (por defecto, vecino más cercano).
   * Criterio inválido responde 400; sin vehículo o sin ruta para la fecha, 404.
   */
  @Transactional(readOnly = true)
  public RutaDiaResponse consultarRutaDelDia(Long usuarioId, LocalDate fecha, String criterio) {
    OrdenEntregaStrategy estrategia = elegirEstrategia(criterio);

    Vehiculo vehiculo = vehiculoRepository.findByTransportistaUsuarioId(usuarioId)
        .orElseThrow(() -> new RecursoNoEncontradoException(
            "El transportista no tiene un vehículo registrado"));
    Ruta ruta = rutaRepository.findByVehiculoIdAndFecha(vehiculo.getId(), fecha)
        .orElseThrow(() -> new RecursoNoEncontradoException(
            "No hay una ruta programada para la fecha " + fecha));

    List<Pedido> pedidos = asignacionRepository.findByRutaIdOrderByOrdenEntrega(ruta.getId())
        .stream()
        .map(AsignacionTransporte::getPedido)
        .toList();
    List<Pedido> ordenados = estrategia.ordenar(pedidos);

    List<ParadaRutaResponse> paradas = IntStream.range(0, ordenados.size())
        .mapToObj(i -> aParada(i + 1, ordenados.get(i)))
        .toList();
    String descripcionVehiculo = vehiculo.getTipoTransporte() + " " + vehiculo.getPlaca();
    return new RutaDiaResponse(fecha, descripcionVehiculo, paradas);
  }

  private OrdenEntregaStrategy elegirEstrategia(String criterio) {
    String clave = (criterio == null || criterio.isBlank())
        ? CRITERIO_POR_DEFECTO
        : criterio.trim().toUpperCase();
    OrdenEntregaStrategy estrategia = estrategias.get(clave);
    if (estrategia == null) {
      throw new CriterioOrdenInvalidoException(criterio);
    }
    return estrategia;
  }

  private ParadaRutaResponse aParada(int orden, Pedido pedido) {
    Usuario usuario = pedido.getComprador().getUsuario();
    return new ParadaRutaResponse(
        orden,
        pedido.getId(),
        usuario.getNombre() + " " + usuario.getApellido(),
        pedido.getComprador().getMunicipio(),
        pedido.getDireccionEntrega(),
        pedido.getEstado());
  }
}