package com.agrovalle.connect.model;

import com.agrovalle.connect.model.enums.EstadoPedido;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa el pedido de un comprador a un agricultor. El total
 * no se almacena: se calcula a partir de sus DetallePedido.
 */
@Entity
@Table(name = "pedidos")
public class Pedido {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EstadoPedido estado;

  @Column(name = "direccion_entrega", nullable = false)
  private String direccionEntrega;

  @Column(name = "fecha_creacion", nullable = false, updatable = false)
  private LocalDateTime fechaCreacion;

  @Column(name = "fecha_estimada_llegada")
  private LocalDate fechaEstimadaLlegada;

  @Column(name = "fecha_entrega")
  private LocalDateTime fechaEntrega;

  @Column(name = "motivo_cancelacion", length = 500)
  private String motivoCancelacion;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "comprador_id", nullable = false)
  private Comprador comprador;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "agricultor_id", nullable = false)
  private Agricultor agricultor;

  public Pedido() {
  }

  /**
   * Crea un pedido nuevo en estado PENDIENTE con la fecha de creación actual.
   */
  public Pedido(Comprador comprador, Agricultor agricultor, String direccionEntrega) {
    this.comprador = comprador;
    this.agricultor = agricultor;
    this.direccionEntrega = direccionEntrega;
    this.estado = EstadoPedido.PENDIENTE;
    this.fechaCreacion = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public EstadoPedido getEstado() {
    return estado;
  }

  public void setEstado(EstadoPedido estado) {
    this.estado = estado;
  }

  public String getDireccionEntrega() {
    return direccionEntrega;
  }

  public void setDireccionEntrega(String direccionEntrega) {
    this.direccionEntrega = direccionEntrega;
  }

  public LocalDateTime getFechaCreacion() {
    return fechaCreacion;
  }

  public void setFechaCreacion(LocalDateTime fechaCreacion) {
    this.fechaCreacion = fechaCreacion;
  }

  public LocalDate getFechaEstimadaLlegada() {
    return fechaEstimadaLlegada;
  }

  public void setFechaEstimadaLlegada(LocalDate fechaEstimadaLlegada) {
    this.fechaEstimadaLlegada = fechaEstimadaLlegada;
  }

  public LocalDateTime getFechaEntrega() {
    return fechaEntrega;
  }

  public void setFechaEntrega(LocalDateTime fechaEntrega) {
    this.fechaEntrega = fechaEntrega;
  }

  public String getMotivoCancelacion() {
    return motivoCancelacion;
  }

  public void setMotivoCancelacion(String motivoCancelacion) {
    this.motivoCancelacion = motivoCancelacion;
  }

  public Comprador getComprador() {
    return comprador;
  }

  public void setComprador(Comprador comprador) {
    this.comprador = comprador;
  }

  public Agricultor getAgricultor() {
    return agricultor;
  }

  public void setAgricultor(Agricultor agricultor) {
    this.agricultor = agricultor;
  }
}