package com.agrovalle.connect.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Entidad que representa el vehículo de un transportista. Cada transportista
 * tiene un solo vehículo y la placa es única en toda la plataforma.
 */
@Entity
@Table(name = "vehiculos")
public class Vehiculo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tipo_transporte", nullable = false)
  private String tipoTransporte;

  @Column(nullable = false, unique = true, length = 10)
  private String placa;

  @Column(name = "capacidad_kg", nullable = false, precision = 10, scale = 2)
  private BigDecimal capacidadKg;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "transportista_id", nullable = false, unique = true)
  private Transportista transportista;

  public Vehiculo() {
  }

  public Vehiculo(String tipoTransporte, String placa, BigDecimal capacidadKg,
      Transportista transportista) {
    this.tipoTransporte = tipoTransporte;
    this.placa = placa;
    this.capacidadKg = capacidadKg;
    this.transportista = transportista;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTipoTransporte() {
    return tipoTransporte;
  }

  public void setTipoTransporte(String tipoTransporte) {
    this.tipoTransporte = tipoTransporte;
  }

  public String getPlaca() {
    return placa;
  }

  public void setPlaca(String placa) {
    this.placa = placa;
  }

  public BigDecimal getCapacidadKg() {
    return capacidadKg;
  }

  public void setCapacidadKg(BigDecimal capacidadKg) {
    this.capacidadKg = capacidadKg;
  }

  public Transportista getTransportista() {
    return transportista;
  }

  public void setTransportista(Transportista transportista) {
    this.transportista = transportista;
  }
}
