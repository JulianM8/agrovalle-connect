package com.agrovalle.connect.controller;

import com.agrovalle.connect.dto.AsignacionRequest;
import com.agrovalle.connect.dto.AsignacionResponse;
import com.agrovalle.connect.service.DespachoService;
import com.agrovalle.connect.dto.TransporteResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone la asignación de transporte a los pedidos.
 */
@RestController
@RequestMapping("/api/v1/pedidos")
public class DespachoController {

    private final DespachoService despachoService;

    public DespachoController(DespachoService despachoService) {
        this.despachoService = despachoService;
    }

    /**
     * Asigna un vehículo a un pedido confirmado del agricultor autenticado
     * (201, 400, 404, 409).
     */
    @PostMapping("/{id}/asignacion")
    @PreAuthorize("hasRole('AGRICULTOR')")
    public ResponseEntity<AsignacionResponse> asignar(@PathVariable("id") Long id,
            @Valid @RequestBody AsignacionRequest request, Authentication authentication) {
        Long usuarioId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(despachoService.asignarVehiculo(id, request.vehiculoId(), usuarioId));
    }

    /**
     * Muestra al comprador el tipo de transporte y la placa asignados a su pedido.
     */
    @GetMapping("/{id}/transporte")
    @PreAuthorize("hasRole('COMPRADOR')")
    public ResponseEntity<TransporteResponse> consultarTransporte(
            @PathVariable("id") Long id, Authentication authentication) {
        Long usuarioId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(despachoService.consultarTransporte(id, usuarioId));
    }
}
