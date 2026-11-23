package com.agrovalle.connect.service;

import com.agrovalle.connect.dto.AsignacionResponse;
import com.agrovalle.connect.dto.TransporteResponse;
import com.agrovalle.connect.event.AsignacionRegistradaEvent;
import com.agrovalle.connect.exception.ConflictoException;
import com.agrovalle.connect.exception.PedidoInvalidoException;
import com.agrovalle.connect.exception.RecursoNoEncontradoException;
import com.agrovalle.connect.model.AsignacionTransporte;
import com.agrovalle.connect.model.Pedido;
import com.agrovalle.connect.model.Ruta;
import com.agrovalle.connect.model.Vehiculo;
import com.agrovalle.connect.model.enums.EstadoPedido;
import com.agrovalle.connect.repository.AsignacionTransporteRepository;
import com.agrovalle.connect.repository.PedidoRepository;
import com.agrovalle.connect.repository.RutaRepository;
import com.agrovalle.connect.repository.VehiculoRepository;
import java.time.LocalDate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica de despacho: asigna un vehículo a un pedido confirmado, lo agrega a
 * la ruta del vehículo para la fecha, pasa el pedido a EN_TRANSPORTE y avisa
 * al comprador mediante un evento.
 */
@Service
public class DespachoService {

    private final PedidoRepository pedidoRepository;

    private final VehiculoRepository vehiculoRepository;

    private final RutaRepository rutaRepository;

    private final AsignacionTransporteRepository asignacionRepository;

    private final PedidoService pedidoService;

    private final ApplicationEventPublisher eventPublisher;

    public DespachoService(PedidoRepository pedidoRepository,
            VehiculoRepository vehiculoRepository, RutaRepository rutaRepository,
            AsignacionTransporteRepository asignacionRepository, PedidoService pedidoService,
            ApplicationEventPublisher eventPublisher) {
        this.pedidoRepository = pedidoRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.rutaRepository = rutaRepository;
        this.asignacionRepository = asignacionRepository;
        this.pedidoService = pedidoService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Asigna el vehículo al pedido del agricultor autenticado. Si el pedido no
     * existe o es de otro agricultor responde 404; si ya tiene asignación, 409;
     * si no está CONFIRMADO, 400.
     */
    @Transactional
    public AsignacionResponse asignarVehiculo(Long pedidoId, Long vehiculoId, Long usuarioId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .filter(p -> p.getAgricultor().getUsuario().getId().equals(usuarioId))
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el pedido con id " + pedidoId));
        // Se valida antes del estado: un pedido ya asignado queda EN_TRANSPORTE.
        if (asignacionRepository.findByPedidoId(pedidoId).isPresent()) {
            throw new ConflictoException("El pedido " + pedidoId + " ya tiene transporte asignado");
        }
        if (pedido.getEstado() != EstadoPedido.CONFIRMADO) {
            throw new PedidoInvalidoException(
                    "Solo se puede asignar transporte a pedidos CONFIRMADO, estado actual: "
                            + pedido.getEstado());
        }
        Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el vehículo con id " + vehiculoId));

        LocalDate fecha = pedido.getFechaEstimadaLlegada() != null
                ? pedido.getFechaEstimadaLlegada()
                : LocalDate.now();

        if (pedido.getFechaEstimadaLlegada() == null) {
            pedido.setFechaEstimadaLlegada(fecha);
        }
        Ruta ruta = rutaRepository.findByVehiculoIdAndFecha(vehiculoId, fecha)
                .orElseGet(() -> rutaRepository.save(new Ruta(fecha, vehiculo)));
        int orden = asignacionRepository.findByRutaIdOrderByOrdenEntrega(ruta.getId()).size() + 1;

        AsignacionTransporte asignacion = asignacionRepository.save(
                new AsignacionTransporte(pedido, ruta, orden));
        pedido.setEstado(EstadoPedido.EN_TRANSPORTE);
        pedidoRepository.save(pedido);

        eventPublisher.publishEvent(new AsignacionRegistradaEvent(
                asignacion.getId(), pedidoId, pedido.getComprador().getId()));
        return aRespuesta(asignacion);
    }

    /**
     * Devuelve al comprador autenticado el transporte asignado a su pedido.
     * Responde 404 si el pedido no es suyo o todavía no tiene transporte.
     */
    @Transactional(readOnly = true)
    public TransporteResponse consultarTransporte(Long pedidoId, Long usuarioId) {
        pedidoService.obtenerPedidoDelComprador(pedidoId, usuarioId);
        AsignacionTransporte asignacion = asignacionRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El pedido " + pedidoId + " aún no tiene transporte asignado"));
        Vehiculo vehiculo = asignacion.getRuta().getVehiculo();
        return new TransporteResponse(pedidoId, vehiculo.getTipoTransporte(),
                vehiculo.getPlaca(), asignacion.getRuta().getFecha());
    }

    private AsignacionResponse aRespuesta(AsignacionTransporte asignacion) {
        Ruta ruta = asignacion.getRuta();
        return new AsignacionResponse(
                asignacion.getId(),
                asignacion.getPedido().getId(),
                asignacion.getPedido().getEstado(),
                ruta.getVehiculo().getId(),
                ruta.getId(),
                ruta.getFecha(),
                asignacion.getOrdenEntrega(),
                asignacion.getFechaAsignacion());
    }
}