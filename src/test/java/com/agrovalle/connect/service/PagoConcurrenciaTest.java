package com.agrovalle.connect.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.agrovalle.connect.dto.PagoRequest;
import com.agrovalle.connect.dto.PagoResponse;
import com.agrovalle.connect.model.Agricultor;
import com.agrovalle.connect.model.Comprador;
import com.agrovalle.connect.model.DetallePedido;
import com.agrovalle.connect.model.Pedido;
import com.agrovalle.connect.model.Producto;
import com.agrovalle.connect.model.Usuario;
import com.agrovalle.connect.model.enums.EstadoPago;
import com.agrovalle.connect.model.enums.EstadoPedido;
import com.agrovalle.connect.model.enums.Rol;
import com.agrovalle.connect.model.enums.UnidadMedida;
import com.agrovalle.connect.repository.AgricultorRepository;
import com.agrovalle.connect.repository.CompradorRepository;
import com.agrovalle.connect.repository.DetallePedidoRepository;
import com.agrovalle.connect.repository.PagoRepository;
import com.agrovalle.connect.repository.PedidoRepository;
import com.agrovalle.connect.repository.ProductoRepository;
import com.agrovalle.connect.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntFunction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Prueba de carga liviana del flujo de pago, lanza varias
 * peticiones de pago a la vez contra PagoService y verifica que no se pierdan
 * ni se dupliquen pagos.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:pago-concurrencia;DB_CLOSE_DELAY=-1")
@ActiveProfiles("test")
class PagoConcurrenciaTest {

  private static final AtomicLong CONSECUTIVO = new AtomicLong(9_000_000_000L);

  /** Cantidad de pagos simultáneos: "liviano" = decenas, no miles. */
  private static final int PETICIONES = 20;

  /** Tiempo máximo razonable para que todas terminen. */
  private static final long LIMITE_MS = 10_000;

  @Autowired
  private PagoService pagoService;

  @Autowired
  private UsuarioRepository usuarioRepository;

  @Autowired
  private AgricultorRepository agricultorRepository;

  @Autowired
  private CompradorRepository compradorRepository;

  @Autowired
  private ProductoRepository productoRepository;

  @Autowired
  private PedidoRepository pedidoRepository;

  @Autowired
  private DetallePedidoRepository detallePedidoRepository;

  @Autowired
  private PagoRepository pagoRepository;

  /**
   * Caso 1: 20 pedidos DISTINTOS se pagan al mismo tiempo. Todos deben quedar
   * COMPLETADO, con referencia única y el pedido CONFIRMADO.
   */
  @Test
  void debeSoportarPagosSimultaneosDePedidosDistintos() throws Exception {
    Comprador comprador = crearComprador();
    Long usuarioId = comprador.getUsuario().getId();
    Agricultor agricultor = crearAgricultor();
    Producto producto = crearProducto(agricultor);
    List<Pedido> pedidos = new ArrayList<>();
    for (int i = 0; i < PETICIONES; i++) {
      pedidos.add(crearPedidoConDetalle(comprador, agricultor, producto));
    }

    long inicio = System.nanoTime();
    List<Object> resultados = ejecutarEnParalelo(PETICIONES, i -> pagoService.pagar(
        pedidos.get(i).getId(), new PagoRequest("TARJETA"), usuarioId));
    long ms = (System.nanoTime() - inicio) / 1_000_000;

    Set<String> referencias = new HashSet<>();
    for (Object resultado : resultados) {
      assertTrue(resultado instanceof PagoResponse, "Falló una petición: " + resultado);
      PagoResponse pago = (PagoResponse) resultado;
      assertEquals(EstadoPago.COMPLETADO, pago.estado());
      referencias.add(pago.referenciaTransaccion());
    }
    assertEquals(PETICIONES, referencias.size(), "Hay referencias de transacción repetidas");

    for (Pedido pedido : pedidos) {
      assertEquals(EstadoPedido.CONFIRMADO,
          pedidoRepository.findById(pedido.getId()).orElseThrow().getEstado());
      assertEquals(1,
          pagoRepository.findByPedidoIdAndEstado(pedido.getId(), EstadoPago.COMPLETADO).size());
    }
    assertTrue(ms < LIMITE_MS, "Tardó demasiado: " + ms + " ms");
    System.out.println("[carga] " + PETICIONES + " pagos en pedidos distintos: " + ms + " ms");
  }

  /**
   * Caso 2: 10 peticiones intentan pagar el MISMO pedido a la vez (por ejemplo,
   * un doble clic en «Pagar»). Solo UN pago debe quedar COMPLETADO; el resto
   * debe ser rechazado porque el pedido ya no está PENDIENTE.
   */
  @Test
  void soloUnPagoDebeCompletarseParaElMismoPedido() throws Exception {
    int intentos = 10;
    Comprador comprador = crearComprador();
    Long usuarioId = comprador.getUsuario().getId();
    Agricultor agricultor = crearAgricultor();
    Producto producto = crearProducto(agricultor);
    Pedido pedido = crearPedidoConDetalle(comprador, agricultor, producto);

    List<Object> resultados = ejecutarEnParalelo(intentos, i -> pagoService.pagar(
        pedido.getId(), new PagoRequest("TARJETA"), usuarioId));

    long exitosos = resultados.stream().filter(r -> r instanceof PagoResponse).count();
    int completados =
        pagoRepository.findByPedidoIdAndEstado(pedido.getId(), EstadoPago.COMPLETADO).size();

    assertEquals(1, completados, "Doble cobro: hay " + completados + " pagos COMPLETADO");
    assertEquals(1, exitosos, "Más de una petición recibió 201 Creado");
    assertEquals(EstadoPedido.CONFIRMADO,
        pedidoRepository.findById(pedido.getId()).orElseThrow().getEstado());
  }

  private List<Object> ejecutarEnParalelo(int hilos, IntFunction<Object> tarea)
      throws Exception {
    ExecutorService pool = Executors.newFixedThreadPool(hilos);
    CountDownLatch listos = new CountDownLatch(hilos);
    CountDownLatch salida = new CountDownLatch(1);
    List<Future<Object>> futuros = new ArrayList<>();
    try {
      for (int i = 0; i < hilos; i++) {
        final int indice = i;
        futuros.add(pool.submit(() -> {
          listos.countDown();
          salida.await();
          try {
            return tarea.apply(indice);
          } catch (Exception e) {
            return e;
          }
        }));
      }
      listos.await();
      salida.countDown();

      List<Object> resultados = new ArrayList<>();
      for (Future<Object> futuro : futuros) {
        resultados.add(futuro.get(30, TimeUnit.SECONDS));
      }
      return resultados;
    } finally {
      pool.shutdownNow();
    }
  }

  private Comprador crearComprador() {
    String id = String.valueOf(CONSECUTIVO.incrementAndGet());
    Usuario usuario = usuarioRepository.save(new Usuario(id, "Comprador", "Carga",
        "3001234567", id + "@correo.com", "hash", Rol.COMPRADOR));
    return compradorRepository.save(new Comprador("Cali", "Calle 1", null, null, usuario));
  }

  private Agricultor crearAgricultor() {
    String id = String.valueOf(CONSECUTIVO.incrementAndGet());
    Usuario usuario = usuarioRepository.save(new Usuario(id, "Agricultor", "Carga",
        "3001234567", id + "@correo.com", "hash", Rol.AGRICULTOR));
    return agricultorRepository.save(new Agricultor("Palmira", usuario));
  }

  private Producto crearProducto(Agricultor agricultor) {
    return productoRepository.save(new Producto("Aguacate", "Frutas", 1000,
        UnidadMedida.KILOGRAMO, new BigDecimal("3500"), LocalDate.now().plusDays(5),
        agricultor));
  }

  private Pedido crearPedidoConDetalle(Comprador comprador, Agricultor agricultor,
      Producto producto) {
    Pedido pedido = pedidoRepository.save(
        new Pedido(comprador, agricultor, "Calle 5 # 10-20"));
    detallePedidoRepository.save(
        new DetallePedido(pedido, producto, 2, new BigDecimal("3500")));
    return pedido;
  }
}
