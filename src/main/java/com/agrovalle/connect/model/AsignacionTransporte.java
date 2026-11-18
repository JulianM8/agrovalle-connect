package com.agrovalle.connect.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Asignación de un pedido a una ruta de transporte, con su orden de entrega.
 * Un pedido solo puede tener una asignación.
 */
@Entity
@Table(name = "asignaciones_transporte")
public class AsignacionTransporte {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "pedido_id", nullable = false, unique = true)
  private Pedido pedido;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "ruta_id", nullable = false)
  private Ruta ruta;

  @Column(name = "orden_entrega", nullable = false)
  private Integer ordenEntrega;

  @Column(name = "fecha_asignacion", nullable = false, updatable = false)
  private LocalDateTime fechaAsignacion;

  public AsignacionTransporte() {
  }

  /**
   * Crea la asignación con la fecha y hora actuales.
   */
  public AsignacionTransporte(Pedido pedido, Ruta ruta, Integer ordenEntrega) {
    this.pedido = pedido;
    this.ruta = ruta;
    this.ordenEntrega = ordenEntrega;
    this.fechaAsignacion = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Pedido getPedido() {
    return pedido;
  }

  public void setPedido(Pedido pedido) {
    this.pedido = pedido;
  }

  public Ruta getRuta() {
    return ruta;
  }

  public void setRuta(Ruta ruta) {
    this.ruta = ruta;
  }

  public Integer getOrdenEntrega() {
    return ordenEntrega;
  }

  public void setOrdenEntrega(Integer ordenEntrega) {
    this.ordenEntrega = ordenEntrega;
  }

  public LocalDateTime getFechaAsignacion() {
    return fechaAsignacion;
  }

  public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
    this.fechaAsignacion = fechaAsignacion;
  }
}
