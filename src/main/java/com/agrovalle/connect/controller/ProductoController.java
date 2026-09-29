package com.agrovalle.connect.controller;
 
import com.agrovalle.connect.dto.ProductoPublicacionRequest;
import com.agrovalle.connect.dto.ProductoPublicacionResponse;
import com.agrovalle.connect.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
 
/**
* Expone la publicación de productos.
*/
@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {
 
  private final ProductoService productoService;
 
  public ProductoController(ProductoService productoService) {
    this.productoService = productoService;
  }
 
  @PostMapping
  public ResponseEntity<ProductoPublicacionResponse> publicar(
      @Valid @RequestBody ProductoPublicacionRequest request) {
    ProductoPublicacionResponse respuesta = productoService.publicar(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
  }
}