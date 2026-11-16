package com.agrovalle.connect.service;

import com.agrovalle.connect.dto.VehiculosPorTipoResponse;
import com.agrovalle.connect.exception.PedidoInvalidoException;
import com.agrovalle.connect.dto.VehiculoRequest;
import com.agrovalle.connect.dto.VehiculoResponse;
import com.agrovalle.connect.exception.ConflictoException;
import com.agrovalle.connect.exception.RecursoNoEncontradoException;
import com.agrovalle.connect.model.Transportista;
import com.agrovalle.connect.model.Vehiculo;
import com.agrovalle.connect.repository.TransportistaRepository;
import com.agrovalle.connect.repository.VehiculoRepository;
import java.util.Locale;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registra y consulta los vehículos de los transportistas.
 */
@Service
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;

    private final TransportistaRepository transportistaRepository;

    public VehiculoService(VehiculoRepository vehiculoRepository,
            TransportistaRepository transportistaRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.transportistaRepository = transportistaRepository;
    }

    /**
     * Registra el vehículo del transportista autenticado. Cada transportista
     * tiene un solo vehículo y la placa no puede repetirse.
     */
    @Transactional
    public VehiculoResponse registrar(VehiculoRequest request, Long usuarioId) {
        Transportista transportista = transportistaRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El usuario autenticado no tiene perfil de transportista"));
        if (vehiculoRepository.findByTransportistaId(transportista.getId()).isPresent()) {
            throw new ConflictoException("El transportista ya tiene un vehículo registrado");
        }
        String placa = normalizar(request.placa());
        if (vehiculoRepository.existsByPlaca(placa)) {
            throw new ConflictoException("Ya existe un vehículo con la placa " + placa);
        }
        Vehiculo vehiculo = vehiculoRepository.save(new Vehiculo(
                normalizar(request.tipoTransporte()), placa, request.capacidadKg(), transportista));
        return aRespuesta(vehiculo);
    }

    /**
     * Lista los vehículos de un tipo de transporte. Si no hay ninguno responde
     * una lista vacía con un mensaje claro.
     */
    @Transactional(readOnly = true)
    public VehiculosPorTipoResponse listarPorTipo(String tipoTransporte) {
        if (tipoTransporte == null || tipoTransporte.isBlank()) {
            throw new PedidoInvalidoException("El tipo de transporte es obligatorio");
        }
        String tipo = normalizar(tipoTransporte);
        List<VehiculoResponse> vehiculos = vehiculoRepository.findByTipoTransporte(tipo)
                .stream()
                .map(this::aRespuesta)
                .toList();
        String mensaje = vehiculos.isEmpty()
                ? "No hay vehículos disponibles del tipo " + tipo
                : vehiculos.size() + " vehículo(s) del tipo " + tipo;
        return new VehiculosPorTipoResponse(mensaje, vehiculos);
    }

    private String normalizar(String texto) {
        return texto.trim().toUpperCase(Locale.ROOT);
    }

    private VehiculoResponse aRespuesta(Vehiculo vehiculo) {
        Transportista transportista = vehiculo.getTransportista();
        return new VehiculoResponse(
                vehiculo.getId(),
                vehiculo.getTipoTransporte(),
                vehiculo.getPlaca(),
                vehiculo.getCapacidadKg(),
                transportista.getUsuario().getNombre() + " "
                        + transportista.getUsuario().getApellido(),
                transportista.getMunicipio());
    }
}
