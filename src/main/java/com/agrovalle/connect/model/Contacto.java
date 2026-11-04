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
import java.time.LocalDateTime;

/**
 * Solicitud de un comprador sobre un producto publicado por un agricultor.
 */
@Entity
@Table(name = "contactos")
public class Contacto {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "comprador_id", nullable = false)
  private Comprador comprador;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "producto_id", nullable = false)
  private Producto producto;

  @Column(nullable = false, length = 1000)
  private String mensaje;

  @Column(name = "fecha_envio", nullable = false, updatable = false)
  private LocalDateTime fechaEnvio;

  @Column(length = 1000)
  private String respuesta;

  @Column(name = "fecha_respuesta")
  private LocalDateTime fechaRespuesta;

  public Contacto() {
  }

  /** Crea una solicitud pendiente con la fecha de envío actual. */
  public Contacto(Comprador comprador, Producto producto, String mensaje) {
    this.comprador = comprador;
    this.producto = producto;
    this.mensaje = mensaje;
    this.fechaEnvio = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Comprador getComprador() {
    return comprador;
  }

  public void setComprador(Comprador comprador) {
    this.comprador = comprador;
  }

  public Producto getProducto() {
    return producto;
  }

  public void setProducto(Producto producto) {
    this.producto = producto;
  }

  public String getMensaje() {
    return mensaje;
  }

  public void setMensaje(String mensaje) {
    this.mensaje = mensaje;
  }

  public LocalDateTime getFechaEnvio() {
    return fechaEnvio;
  }

  public void setFechaEnvio(LocalDateTime fechaEnvio) {
    this.fechaEnvio = fechaEnvio;
  }

  public String getRespuesta() {
    return respuesta;
  }

  public void setRespuesta(String respuesta) {
    this.respuesta = respuesta;
  }

  public LocalDateTime getFechaRespuesta() {
    return fechaRespuesta;
  }

  public void setFechaRespuesta(LocalDateTime fechaRespuesta) {
    this.fechaRespuesta = fechaRespuesta;
  }
}
