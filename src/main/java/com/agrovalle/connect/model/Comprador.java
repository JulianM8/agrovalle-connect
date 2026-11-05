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
 * Entidad que representa el perfil de un comprador registrado en la plataforma.
 * Sus credenciales y datos personales viven en la entidad Usuario.
 */
@Entity
@Table(name = "compradores")
public class Comprador {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String municipio;

  @Column(name = "direccion_entrega", nullable = false)
  private String direccionEntrega;

  @Column(precision = 9, scale = 6)
  private BigDecimal latitud;

  @Column(precision = 9, scale = 6)
  private BigDecimal longitud;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "usuario_id", nullable = false, unique = true)
  private Usuario usuario;

  public Comprador() {
  }

  public Comprador(String municipio, String direccionEntrega, BigDecimal latitud,
      BigDecimal longitud, Usuario usuario) {
    this.municipio = municipio;
    this.direccionEntrega = direccionEntrega;
    this.latitud = latitud;
    this.longitud = longitud;
    this.usuario = usuario;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getMunicipio() {
    return municipio;
  }

  public void setMunicipio(String municipio) {
    this.municipio = municipio;
  }

  public String getDireccionEntrega() {
    return direccionEntrega;
  }

  public void setDireccionEntrega(String direccionEntrega) {
    this.direccionEntrega = direccionEntrega;
  }

  public BigDecimal getLatitud() {
    return latitud;
  }

  public void setLatitud(BigDecimal latitud) {
    this.latitud = latitud;
  }

  public BigDecimal getLongitud() {
    return longitud;
  }

  public void setLongitud(BigDecimal longitud) {
    this.longitud = longitud;
  }

  public Usuario getUsuario() {
    return usuario;
  }

  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }
}