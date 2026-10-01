package com.agrovalle.connect.controller;

import com.agrovalle.connect.dto.AgricultorRegistroRequest;
import com.agrovalle.connect.dto.AgricultorRegistroResponse;
import com.agrovalle.connect.service.AgricultorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone el registro de agricultores.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AgricultorController {

    private final AgricultorService agricultorService;

    public AgricultorController(AgricultorService agricultorService) {
        this.agricultorService = agricultorService;
    }

    @PostMapping("/register")
    public ResponseEntity<AgricultorRegistroResponse> registrar(
            @Valid @RequestBody AgricultorRegistroRequest request) {
        AgricultorRegistroResponse respuesta = agricultorService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }
}
