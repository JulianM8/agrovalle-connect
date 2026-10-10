
package com.agrovalle.connect.service;

import com.agrovalle.connect.dto.AgricultorRegistroRequest;
import com.agrovalle.connect.dto.AgricultorRegistroResponse;
import com.agrovalle.connect.exception.IdentificacionDuplicadaException;
import com.agrovalle.connect.model.Agricultor;
import com.agrovalle.connect.repository.AgricultorRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Lógica de negocio para el registro de agricultores.
 */
@Service
public class AgricultorService {

  private final AgricultorRepository agricultorRepository;
  
  private final PasswordEncoder passwordEncoder;

  public AgricultorService(AgricultorRepository agricultorRepository, PasswordEncoder passwordEncoder) {
    this.agricultorRepository = agricultorRepository;
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * Registra un nuevo agricultor, validando que su identificación no exista.
   *
   * @param request datos enviados por el usuario
   * @return datos del agricultor registrado
   * @throws IdentificacionDuplicadaException si la identificación ya está
   *                                          registrada
   * 
   */
  public AgricultorRegistroResponse registrar(AgricultorRegistroRequest request) {
    if (agricultorRepository.existsByIdentificacion(request.identificacion())) {
      throw new IdentificacionDuplicadaException(request.identificacion());
    }

    Agricultor agricultor = new Agricultor(
        request.nombre(),
        request.apellido(),
        request.identificacion(),
        request.municipio(),
        request.telefono(),
        request.correo(),
        passwordEncoder.encode(request.contrasena()));

    Agricultor guardado = agricultorRepository.save(agricultor);

    return new AgricultorRegistroResponse(
        guardado.getId(),
        guardado.getNombre(),
        guardado.getApellido(),
        guardado.getIdentificacion(),
        guardado.getMunicipio(),
        "Registro realizado correctamente");
  }
}