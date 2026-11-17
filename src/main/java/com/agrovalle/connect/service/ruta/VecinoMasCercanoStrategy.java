package com.agrovalle.connect.service.ruta;

import com.agrovalle.connect.model.Comprador;
import com.agrovalle.connect.model.Pedido;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Ordena las entregas con el algoritmo del vecino más cercano usando la
 * distancia de Haversine. Parte de la primera parada con coordenadas y en cada
 * paso va a la más cercana; las paradas sin coordenadas quedan al final.
 */
@Component("VECINO_MAS_CERCANO")
public class VecinoMasCercanoStrategy implements OrdenEntregaStrategy {

  private static final double RADIO_TIERRA_KM = 6371.0;

  @Override
  public List<Pedido> ordenar(List<Pedido> pedidos) {
    List<Pedido> pendientes = new ArrayList<>();
    List<Pedido> sinCoordenadas = new ArrayList<>();
    for (Pedido pedido : pedidos) {
      if (tieneCoordenadas(pedido.getComprador())) {
        pendientes.add(pedido);
      } else {
        sinCoordenadas.add(pedido);
      }
    }

    List<Pedido> resultado = new ArrayList<>();
    if (!pendientes.isEmpty()) {
      Pedido actual = pendientes.remove(0);
      resultado.add(actual);
      while (!pendientes.isEmpty()) {
        Pedido siguiente = masCercano(actual, pendientes);
        pendientes.remove(siguiente);
        resultado.add(siguiente);
        actual = siguiente;
      }
    }
    resultado.addAll(sinCoordenadas);
    return resultado;
  }

  private boolean tieneCoordenadas(Comprador comprador) {
    return comprador.getLatitud() != null && comprador.getLongitud() != null;
  }

  private Pedido masCercano(Pedido origen, List<Pedido> candidatos) {
    Pedido mejor = candidatos.get(0);
    double menorDistancia = distanciaKm(origen.getComprador(), mejor.getComprador());
    for (Pedido candidato : candidatos) {
      double distancia = distanciaKm(origen.getComprador(), candidato.getComprador());
      if (distancia < menorDistancia) {
        menorDistancia = distancia;
        mejor = candidato;
      }
    }
    return mejor;
  }

  private double distanciaKm(Comprador a, Comprador b) {
    double lat1 = Math.toRadians(a.getLatitud().doubleValue());
    double lat2 = Math.toRadians(b.getLatitud().doubleValue());
    double lon1 = Math.toRadians(a.getLongitud().doubleValue());
    double lon2 = Math.toRadians(b.getLongitud().doubleValue());
    double deltaLat = lat2 - lat1;
    double deltaLon = lon2 - lon1;
    double h = Math.pow(Math.sin(deltaLat / 2), 2)
        + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(deltaLon / 2), 2);
    return 2 * RADIO_TIERRA_KM * Math.asin(Math.sqrt(h));
  }
}