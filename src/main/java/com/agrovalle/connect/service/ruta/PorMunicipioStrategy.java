package com.agrovalle.connect.service.ruta;

import com.agrovalle.connect.model.Pedido;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Agrupa las entregas por municipio del comprador, en orden alfabético.
 */
@Component("MUNICIPIO")
public class PorMunicipioStrategy implements OrdenEntregaStrategy {

  @Override
  public List<Pedido> ordenar(List<Pedido> pedidos) {
    return pedidos.stream()
        .sorted(Comparator
            .comparing((Pedido p) -> p.getComprador().getMunicipio())
            .thenComparing(Pedido::getId))
        .toList();
  }
}