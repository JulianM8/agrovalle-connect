package com.agrovalle.connect.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agrovalle.connect.dto.ContactoRequest;
import com.agrovalle.connect.exception.RecursoNoEncontradoException;
import com.agrovalle.connect.model.Comprador;
import com.agrovalle.connect.repository.CompradorRepository;
import com.agrovalle.connect.repository.ContactoRepository;
import com.agrovalle.connect.repository.ProductoRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ContactoServiceTest {

  @Mock
  private ContactoRepository contactoRepository;

  @Mock
  private CompradorRepository compradorRepository;

  @Mock
  private ProductoRepository productoRepository;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private ContactoService contactoService;

  @Test
  void noDebeGuardarNiNotificarSiElProductoNoExiste() {
    when(compradorRepository.findByUsuarioId(5L)).thenReturn(Optional.of(new Comprador()));
    when(productoRepository.findById(99L)).thenReturn(Optional.empty());

    RecursoNoEncontradoException error = assertThrows(RecursoNoEncontradoException.class,
        () -> contactoService.enviar(99L, new ContactoRequest("¿Está disponible?"), 5L));

    assertEquals("No existe el producto con id 99", error.getMessage());
    verify(contactoRepository, never()).save(any());
    verify(eventPublisher, never()).publishEvent(any());
  }
}
