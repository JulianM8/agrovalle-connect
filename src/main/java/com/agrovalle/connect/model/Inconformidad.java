package com.agrovalle.connect.model;

import com.agrovalle.connect.model.enums.EstadoInconformidad;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Reporte de inconformidad de un comprador sobre un pedido.
 */
@Entity
@Table(name = "inconformidades")
public class Inconformidad {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "pedido_id", nullable = false)
  private Pedido pedido;

  @Column(nullable = false, length = 1000)
  private String descripcion;

  @Column(name = "evidencia_url", length = 500)
  private String evidenciaUrl;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EstadoInconformidad estado;

  @Column(length = 1000)
  private String resolucion;

  @Column(name = "fecha_reporte", nullable = false, updatable = false)
  private LocalDateTime fechaReporte;

  public Long getId() {
    return id;
  }

  public Pedido getPedido() {
    return pedido;
  }

  public void setPedido(Pedido pedido) {
    this.pedido = pedido;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getEvidenciaUrl() {
    return evidenciaUrl;
  }

  public void setEvidenciaUrl(String evidenciaUrl) {
    this.evidenciaUrl = evidenciaUrl;
  }

  public EstadoInconformidad getEstado() {
    return estado;
  }

  public void setEstado(EstadoInconformidad estado) {
    this.estado = estado;
  }

  public String getResolucion() {
    return resolucion;
  }

  public void setResolucion(String resolucion) {
    this.resolucion = resolucion;
  }

  public LocalDateTime getFechaReporte() {
    return fechaReporte;
  }

  public void setFechaReporte(LocalDateTime fechaReporte) {
    this.fechaReporte = fechaReporte;
  }
}
