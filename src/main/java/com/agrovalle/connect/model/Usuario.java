package com.agrovalle.connect.model;

import com.agrovalle.connect.model.enums.Rol;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa las credenciales de una persona que accede a la
 * plataforma. Un mismo Usuario puede tener varios roles a la vez (por
 * ejemplo, ser agricultor y comprador simultáneamente), almacenados en la
 * tabla asociativa roles_usuario. Los perfiles (agricultor, comprador,
 * transportista) se enlazan a ella por usuario_id.
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 20)
  private String identificacion;

  @Column(nullable = false, unique = true)
  private String correo;

  @Column(nullable = false)
  private String contrasena;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
      name = "roles_usuario",
      joinColumns = @JoinColumn(name = "usuario_id"),
      uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "rol"}))
  @Enumerated(EnumType.STRING)
  @Column(name = "rol", nullable = false, length = 20)
  private Set<Rol> roles = new HashSet<>();

  @Column(nullable = false)
  private boolean activo = true;

  public Usuario() {
  }

  public Usuario(String identificacion, String correo, String contrasena, Rol rolInicial) {
    this.identificacion = identificacion;
    this.correo = correo;
    this.contrasena = contrasena;
    this.roles = new HashSet<>();
    this.roles.add(rolInicial);
    this.activo = true;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getIdentificacion() {
    return identificacion;
  }

  public void setIdentificacion(String identificacion) {
    this.identificacion = identificacion;
  }

  public String getCorreo() {
    return correo;
  }

  public void setCorreo(String correo) {
    this.correo = correo;
  }

  public String getContrasena() {
    return contrasena;
  }

  public void setContrasena(String contrasena) {
    this.contrasena = contrasena;
  }

  public Set<Rol> getRoles() {
    return roles;
  }

  public void setRoles(Set<Rol> roles) {
    this.roles = roles;
  }

  /**
   * Indica si el usuario ya ejerce el rol dado.
   */
  public boolean tieneRol(Rol rol) {
    return roles.contains(rol);
  }
  
  public void agregarRol(Rol rol) {
    if (roles == null) {
      roles = new HashSet<>();
    }
    roles.add(rol);
  }

  public boolean isActivo() {
    return activo;
  }

  public void setActivo(boolean activo) {
    this.activo = activo;
  }
}
