package com.agrovalle.connect.controller;
 
import com.agrovalle.connect.dto.ProductoConsultaResponse;
import com.agrovalle.connect.dto.ProductoFiltroResponse;
import com.agrovalle.connect.dto.ProductoPublicacionRequest;
import com.agrovalle.connect.dto.ProductoPublicacionResponse;
import com.agrovalle.connect.model.Producto;
import com.agrovalle.connect.service.ProductoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
 
/**
* Expone la publicación y consulta filtrada de productos.
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

  /**
   * Consulta productos disponibles por categoría y municipio.
   *
   * @param categoria categoría seleccionada
   * @param municipio municipio de origen seleccionado
   * @return productos encontrados con estado HTTP 200
   */
  @GetMapping
  public ResponseEntity<ProductoFiltroResponse> filtrar(
      @RequestParam("categoria") String categoria,
      @RequestParam("municipio") String municipio) {
    List<ProductoConsultaResponse> productos = productoService
        .buscarDisponiblesPorMunicipioYCategoria(municipio, categoria)
        .stream()
        .map(this::convertirAConsulta)
        .toList();
    String mensaje = productos.isEmpty()
        ? "No hay proveedores del producto en ese municipio."
        : null;
    return ResponseEntity.ok(new ProductoFiltroResponse(productos, mensaje));
  }

  private ProductoConsultaResponse convertirAConsulta(Producto producto) {
    return new ProductoConsultaResponse(
        producto.getId(),
        producto.getNombre(),
        producto.getCategoria(),
        producto.getCantidad(),
        producto.getPrecio(),
        producto.getFechaCosecha(),
        producto.getAgricultor().getId());
  }
}
