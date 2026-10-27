# Implementación: módulo de usuarios y roles — AgroValle Connect

**Fecha:** Lunes 19 de octubre de 2024

## Qué se implementó

Se creó la tabla/entidad **`usuarios`**, compartida por los distintos roles
de la plataforma (`AGRICULTOR`, `COMPRADOR`, `TRANSPORTISTA`), según el
modelo entidad-relación del Sprint de Diseño. Antes, `Agricultor` guardaba
sus propias credenciales (`identificacion`, `correo`, `contrasena`)
directamente; ahora esas credenciales viven en `usuarios`, y `Agricultor` se
enlaza a su cuenta por `usuario_id`.

- **`Usuario`** (`model/Usuario.java`): `identificacion` (única), `correo`
  (único), `contrasena` (cifrada con BCrypt), `rol` (enum `RolUsuario`:
  `AGRICULTOR`, `COMPRADOR`, `TRANSPORTISTA`) y `activo` (boolean, para
  dar de baja una cuenta sin borrar su historial).
- **`UsuarioRepository`**: acceso a datos de `Usuario`.
- **`Agricultor`** refactorizado: ya no tiene credenciales propias; tiene
  una relación `@OneToOne` a `Usuario` vía `usuario_id` (`FK`, `UNIQUE`).
- **Registro** (`AgricultorService.registrar`): crea primero el `Usuario`
  con rol `AGRICULTOR` y luego el `Agricultor` enlazado, en una sola
  transacción. El endpoint y el JSON de entrada/salida no cambiaron.
- **Login** (`AuthService.login`): autentica contra `UsuarioRepository` en
  vez de `AgricultorRepository` — ya funciona igual para cualquier rol,
  sin necesidad de tocarlo cuando se agreguen `Comprador`/`Transportista` —
  y valida que la cuenta esté `activo`.
- **JWT**: el token ahora identifica al `usuario.id` (antes era el
  `agricultor.id`) y lleva el `rol` como claim. El filtro de autenticación
  deja la authority `ROLE_<rol>` lista en el `SecurityContext`, para poder
  restringir endpoints por rol en el futuro sin trabajo adicional.
- **`data.sql`**: los datos de demostración ahora crean primero el
  `Usuario` y luego el `Agricultor` enlazado por `usuario_id`.
- Tests actualizados para reflejar el nuevo modelo de roles.

## Resultado

El sistema de autenticación quedó desacoplado del perfil de agricultor:
hoy solo existe el rol `AGRICULTOR` implementado, pero la base de
usuarios/roles ya está lista para que `Comprador` y `Transportista` se
sumen siguiendo el mismo patrón, sin cambios en `AuthService` ni
`JwtService`.


# Implementación: usuarios con múltiples roles — AgroValle Connect

**Fecha:** Viernes 23 de octubre de 2026

## Qué se implementó

Se reformó el módulo de usuarios para que una misma cuenta pueda tener
**más de un rol a la vez** (agricultor, comprador y transportista
simultáneamente), según el nuevo modelo entidad-relación acordado. Antes,
`usuarios.rol` era un solo valor, lo que impedía que una persona ejerciera
más de un rol sin crear una cuenta nueva.

- **`Usuario`** (`model/Usuario.java`): el campo único `rol` se reemplazó
  por `Set<Rol> roles`, mapeado con `@ElementCollection` a la nueva tabla
  asociativa **`roles_usuario`** (`usuario_id` + `rol` como clave
  compuesta, con `UniqueConstraint` para que el mismo rol no se repita).
  Se agregaron los métodos `tieneRol(Rol)` y `agregarRol(Rol)`.
- **`JwtService`**: `generarToken` ahora recibe `Set<Rol>` en vez de un
  solo `Rol`, y el token lleva el claim `"roles"` como lista (antes era
  `"rol"`, un solo string). Se agregó `extraerRoles(token)` en reemplazo
  de `extraerRol(token)`.
- **`JwtAuthenticationFilter`**: otorga una authority `ROLE_<rol>` por
  cada rol que tenga el usuario, no solo una — el `SecurityContext` queda
  listo para proteger endpoints de cualquier combinación de roles.
- **`AuthService.login`**: sin cambios en su lógica, ahora pasa
  `usuario.getRoles()` al generar el token.
- **Registro** (`AgricultorService.registrar`): si la identificación ya
  existe como `Usuario` (porque la persona ya tiene cuenta con otro rol,
  por ejemplo comprador) y **no** tiene el rol `AGRICULTOR`, se reutiliza
  esa misma cuenta y se le agrega el rol, en vez de crear una cuenta
  nueva o rechazar el registro. Si ya tenía el rol, se rechaza como
  duplicado, igual que antes. De paso se conectó la validación de correo
  duplicado (`CorreoDuplicadoException`), que ya existía en el proyecto
  pero no estaba siendo usada por el servicio.
- **`data.sql`**: los datos de demostración ahora insertan el rol de cada
  usuario en `roles_usuario` en vez de en una columna de `usuarios`.
- Tests actualizados (`AuthServiceTest`, `AgricultorServiceTest`,
  `ProductoControllerTest`) y se agregaron casos nuevos para la
  reutilización de cuenta al agregar un rol y para el correo duplicado.

## Resultado

Un mismo usuario puede ser agricultor, comprador y transportista a la vez
sin necesidad de varias cuentas, y el registro de un nuevo rol sobre una
identificación existente reutiliza la cuenta en vez de duplicarla. El
patrón ya queda listo para que `Comprador` y `Transportista` lo sigan tal
cual cuando se implementen, sin más cambios en `AuthService` ni
`JwtService`.