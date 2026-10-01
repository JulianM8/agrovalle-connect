package com.agrovalle.connect.service;
 
import com.agrovalle.connect.dto.ProductoPublicacionRequest;
import com.agrovalle.connect.dto.ProductoPublicacionResponse;
import com.agrovalle.connect.exception.AgricultorNoEncontradoException;
import com.agrovalle.connect.exception.FechaCosechaInvalidaException;
import com.agrovalle.connect.model.Agricultor;
import com.agrovalle.connect.model.Producto;
import com.agrovalle.connect.repository.AgricultorRepository;
import com.agrovalle.connect.repository.ProductoRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
 
/**
* Lógica de negocio para la publicación de productos.
*/
@Service
public class ProductoService {
 
  private final ProductoRepository productoRepository;
  
  private final AgricultorRepository agricultorRepository;
 
  public ProductoService(ProductoRepository productoRepository,
      AgricultorRepository agricultorRepository) {
    this.productoRepository = productoRepository;
    this.agricultorRepository = agricultorRepository;
  }

  /**
   * Busca productos disponibles por el municipio del agricultor y su categoría.
   *
   * @param municipio municipio de origen del agricultor
   * @param categoria categoría del producto
   * @return productos coincidentes con unidades disponibles
   */
  public List<Producto> buscarDisponiblesPorMunicipioYCategoria(
      String municipio, String categoria) {
    return productoRepository.findByAgricultorMunicipioAndCategoria(municipio, categoria)
        .stream()
        .filter(producto -> producto.getCantidad() > 0)
        .toList();
  }
 
  /**
   * Publica un producto, validando que el agricultor exista y que la fecha
   * de cosecha no sea anterior a hoy.
   *
   * @param request datos del producto a publicar
   * @return datos del producto publicado
   * @throws AgricultorNoEncontradoException si el agricultor no existe
   * @throws FechaCosechaInvalidaException si la fecha de cosecha ya pasó
   */
  public ProductoPublicacionResponse publicar(ProductoPublicacionRequest request) {
    Agricultor agricultor = agricultorRepository.findById(request.agricultorId())
        .orElseThrow(() -> new AgricultorNoEncontradoException(request.agricultorId()));
 
    if (request.fechaCosecha().isBefore(LocalDate.now())) {
      throw new FechaCosechaInvalidaException(request.fechaCosecha());
    }
 
    Producto producto = new Producto(
        request.nombre(),
        request.categoria(),
        request.cantidad(),
        request.precio(),
        request.fechaCosecha(),
        agricultor);
 
    Producto guardado = productoRepository.save(producto);
 
    return new ProductoPublicacionResponse(
        guardado.getId(),
        guardado.getNombre(),
        guardado.getCategoria(),
        guardado.getCantidad(),
        guardado.getPrecio(),
        guardado.getFechaCosecha(),
        guardado.getAgricultor().getId(),
        "Producto publicado correctamente");
  }
}
