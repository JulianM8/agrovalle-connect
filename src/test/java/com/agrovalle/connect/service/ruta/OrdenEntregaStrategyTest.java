package com.agrovalle.connect.service.ruta;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.agrovalle.connect.model.Comprador;
import com.agrovalle.connect.model.Pedido;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrdenEntregaStrategyTest {

  private final PorMunicipioStrategy porMunicipio = new PorMunicipioStrategy();

  private final VecinoMasCercanoStrategy vecino = new VecinoMasCercanoStrategy();

  private Pedido pedido(long id, String municipio, String latitud, String longitud) {
    BigDecimal lat = latitud == null ? null : new BigDecimal(latitud);
    BigDecimal lon = longitud == null ? null : new BigDecimal(longitud);
    Comprador comprador = new Comprador(municipio, "Calle " + id, lat, lon, null);
    Pedido pedido = new Pedido(comprador, null, "Calle " + id);
    pedido.setId(id);
    return pedido;
  }

  private List<Long> ids(List<Pedido> pedidos) {
    return pedidos.stream().map(Pedido::getId).toList();
  }

  @Test
  void debeAgruparPorMunicipioEnOrdenAlfabetico() {
    List<Pedido> entrada = List.of(
        pedido(1, "Palmira", "3.5394", "-76.3036"),
        pedido(2, "Cali", "3.4516", "-76.5319"),
        pedido(3, "Jamundi", null, null));

    assertEquals(List.of(2L, 3L, 1L), ids(porMunicipio.ordenar(entrada)));
  }

  @Test
  void debeOrdenarPorVecinoMasCercanoYDejarSinCoordenadasAlFinal() {
    List<Pedido> entrada = List.of(
        pedido(1, "Cali", "3.451647", "-76.531985"),
        pedido(2, "Palmira", "3.539400", "-76.303600"),
        pedido(3, "Jamundi", null, null),
        pedido(4, "Yumbo", "3.585300", "-76.495300"));

    assertEquals(List.of(1L, 4L, 2L, 3L), ids(vecino.ordenar(entrada)));
  }

  @Test
  void debeManejarListaVacia() {
    assertEquals(List.of(), vecino.ordenar(List.of()));
  }

  @Test
  void debeManejarSoloCompradoresSinCoordenadas() {
    List<Pedido> entrada = List.of(pedido(1, "Cali", null, null), pedido(2, "Yumbo", null, null));

    assertEquals(List.of(1L, 2L), ids(vecino.ordenar(entrada)));
  }
}