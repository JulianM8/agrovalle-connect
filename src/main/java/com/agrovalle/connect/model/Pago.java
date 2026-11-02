package com.agrovalle.connect.model;

import com.agrovalle.connect.model.enums.EstadoPago;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Intento de pago de un pedido. No hay unicidad sobre pedido_id: un pedido
 * puede tener varios pagos para permitir reintentos tras un rechazo.
 */
@Entity
@Table(name = "pagos")
public class Pago {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "pedido_id", nullable = false)
  private Pedido pedido;

  @Column(name = "medio_pago", nullable = false, length = 30)
  private String medioPago;

  @Column(name = "referencia_transaccion", nullable = false, unique = true)
  private String referenciaTransaccion;

  @Column(nullable = false, precision = 38, scale = 2)
  private BigDecimal monto;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EstadoPago estado;

  @Column(name = "fecha_pago")
  private LocalDateTime fechaPago;

  @Column(name = "fecha_liberacion")
  private LocalDateTime fechaLiberacion;

  public Pago() {
  }

  /**
   * Crea un pago nuevo en estado PENDIENTE.
   */
  public Pago(Pedido pedido, String medioPago, String referenciaTransaccion, BigDecimal monto) {
    this.pedido = pedido;
    this.medioPago = medioPago;
    this.referenciaTransaccion = referenciaTransaccion;
    this.monto = monto;
    this.estado = EstadoPago.PENDIENTE;
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

  public String getMedioPago() {
    return medioPago;
  }

  public void setMedioPago(String medioPago) {
    this.medioPago = medioPago;
  }

  public String getReferenciaTransaccion() {
    return referenciaTransaccion;
  }

  public void setReferenciaTransaccion(String referenciaTransaccion) {
    this.referenciaTransaccion = referenciaTransaccion;
  }

  public BigDecimal getMonto() {
    return monto;
  }

  public void setMonto(BigDecimal monto) {
    this.monto = monto;
  }

  public EstadoPago getEstado() {
    return estado;
  }

  public void setEstado(EstadoPago estado) {
    this.estado = estado;
  }

  public LocalDateTime getFechaPago() {
    return fechaPago;
  }

  public void setFechaPago(LocalDateTime fechaPago) {
    this.fechaPago = fechaPago;
  }

  public LocalDateTime getFechaLiberacion() {
    return fechaLiberacion;
  }

  public void setFechaLiberacion(LocalDateTime fechaLiberacion) {
    this.fechaLiberacion = fechaLiberacion;
  }
}