package com.agrovalle.connect.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;

/**
 * Ruta de un vehículo para una fecha: agrupa los pedidos que llevará ese día.
 */
@Entity
@Table(name = "rutas", uniqueConstraints = @UniqueConstraint(
    columnNames = { "vehiculo_id", "fecha" }))
public class Ruta {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private LocalDate fecha;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "vehiculo_id", nullable = false)
  private Vehiculo vehiculo;

  public Ruta() {
  }

  public Ruta(LocalDate fecha, Vehiculo vehiculo) {
    this.fecha = fecha;
    this.vehiculo = vehiculo;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public LocalDate getFecha() {
    return fecha;
  }

  public void setFecha(LocalDate fecha) {
    this.fecha = fecha;
  }

  public Vehiculo getVehiculo() {
    return vehiculo;
  }

  public void setVehiculo(Vehiculo vehiculo) {
    this.vehiculo = vehiculo;
  }
}
