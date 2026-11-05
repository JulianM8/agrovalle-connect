# Planificación del Sprint 2 — AgroValle Connect

Proyecto: **AgroValle Connect**
Sprint: **Sprint 2**

## 1. Sprint Goal

Habilitar el flujo de pedido y pago en línea entre comprador y
agricultor —base de todo lo que sigue en logística y seguimiento—,
junto con la posibilidad de cancelar un pedido antes de su despacho y
el contacto directo comprador–agricultor, validando la persistencia en
PostgreSQL y la arquitectura REST ya establecida en el Sprint 1, e
incorporando el patrón de diseño comportamental **Strategy** en el
procesamiento de pagos.

## 2. Capacidad y alcance comprometido

> **Nota de alcance:** el equipo comprometió **11 Story Points** (HU-10:
> 5, HU-13: 3, HU-05: 3). HU-10 se prioriza primero por ser la historia
> fundacional (crea el `Pedido` y el `Pago`, de los que dependen el resto
> de historias del backlog). HU-13 (cancelación) opera sobre ese mismo
> `Pedido` y su mismo controlador, por lo que se construye en el mismo
> sprint para no retrabajar. HU-05 es independiente del flujo de pedido,
> pero comparte con HU-10 el único prerrequisito que le falta a la base
> de datos: la entidad `Comprador`.


| Historia | Prioridad (MoSCoW) | Estimación |
|---|---|---|
| HU-10: Pagos en Línea | Must have | 5 pts |
| HU-13: Cancelación del Pedido | Must have | 3 pts |
| HU-05: Contacto Directo | Must have | 3 pts |
| **Total comprometido** | | **11 pts** |

## 3. Descomposición técnica asociada a ISO/IEC 25010

### Prerrequisitos transversales

Estado actual del código (Sprint 1): existen `Usuario` (con `Set<Rol> roles`
en `roles_usuario`), `Agricultor` y `Producto`. Faltan la entidad
`Comprador` y la autorización por rol en los endpoints, ambas necesarias para
HU-10 y HU-05; no son historias aparte del backlog.

| ID Tarea | Descripción técnica | Componente / Tecnología | Atributo ISO 25010 |
|---|---|---|---|
| T0.1 | Crear la entidad JPA `Comprador` (municipio, dirección de entrega, latitud, longitud) con `@OneToOne` a `Usuario` vía `usuario_id` (`FK,UK`), igual que `Agricultor`. Nombre, apellido y teléfono ya están en `Usuario` | Spring Data JPA, PostgreSQL | Adecuación funcional |
| T0.2 | Implementar `CompradorRepository` (`findByUsuarioId`) | Spring Data JPA | Mantenibilidad |
| T0.3 | Registro de comprador con `POST /api/v1/auth/register/comprador`, siguiendo el patrón de `AgricultorService.registrar`: si la identificación ya existe como `Usuario` sin rol `COMPRADOR`, se reutiliza la cuenta y se le agrega el rol; si no existe, se crea el `Usuario` con rol `COMPRADOR`. Rechaza identificación/correo duplicados (409) | Spring (@Service), @RestController | Adecuación funcional |
| T0.4 | Habilitar autorización por rol (`@EnableMethodSecurity` y `@PreAuthorize("hasRole('COMPRADOR')")` / `hasRole('AGRICULTOR')`), aprovechando las authorities `ROLE_<rol>` que ya carga `JwtAuthenticationFilter`. Hoy `SecurityConfig` solo exige autenticación | Spring Security | Seguridad |
| T0.5 | Pruebas unitarias del registro de comprador (cuenta nueva, reutilización de cuenta existente con otro rol, duplicados) | JUnit 5, Mockito | Mantenibilidad |

### HU-10: Pagos en Línea

| ID Tarea | Descripción técnica | Componente / Tecnología | Atributo ISO 25010 |
|---|---|---|---|
| T10.1 | Crear la entidad JPA `Pedido` (estado, dirección de entrega, fecha de creación, fecha estimada de llegada, fecha de entrega, motivo de cancelación) con `@ManyToOne` hacia `Comprador` y hacia `Agricultor`, respetando el `CHECK` de estado (`PENDIENTE\|CONFIRMADO\|EN_TRANSPORTE\|ENTREGADO\|CANCELADO`) del DER. El total **no** se almacena: se calcula a partir de los detalles | Spring Data JPA, PostgreSQL | Adecuación funcional |
| T10.2 | Crear la entidad JPA `DetallePedido` (`pedido_id` + `producto_id` con `UK` compuesta, cantidad, precio unitario `NUMERIC(38,2)`) | Spring Data JPA, PostgreSQL | Adecuación funcional |
| T10.3 | Crear la entidad JPA `Pago` (`pedido_id` `FK` **sin** `UK`, para permitir reintentos tras un pago rechazado; medio de pago, referencia de transacción `UK`, monto `NUMERIC(38,2)`, estado, fecha de pago, fecha de liberación) con el `CHECK` de estado (`PENDIENTE\|COMPLETADO\|RECHAZADO\|REEMBOLSADO`) | Spring Data JPA, PostgreSQL | Adecuación funcional |
| T10.4 | Implementar `PedidoRepository`, `DetallePedidoRepository` y `PagoRepository` | Spring Data JPA | Mantenibilidad |
| T10.5 | Definir los DTOs de request/response (carrito de productos con cantidades, dirección de entrega, medio de pago) con validaciones (`@NotEmpty`, `@Positive`) | DTOs (record), Bean Validation | Usabilidad |
| T10.6 | Implementar `PedidoService`: valida existencia y cantidad disponible de cada producto, valida que todos los productos del carrito sean del mismo agricultor (ver §7), calcula el total como suma de `cantidad × precio_unitario`, crea el `Pedido` con sus `DetallePedido` en estado `PENDIENTE` | Spring (@Service) | Adecuación funcional |
| T10.7 | Implementar `PagoService` como contexto del patrón **Strategy** (ver T10.11): delega el procesamiento en la estrategia del medio de pago solicitado, registra el `Pago` y, si queda `COMPLETADO`, cambia el `Pedido` a `CONFIRMADO`. Medios simulados en este sprint (ver §7) | Spring (@Service) | Seguridad / Mantenibilidad |
| T10.8 | Exponer `POST /api/v1/pedidos`, restringido a `ROLE_COMPRADOR`, con respuestas 201, 400 y 404 | @RestController, Spring Security | Compatibilidad / Seguridad |
| T10.9 | Exponer `POST /api/v1/pedidos/{id}/pago` y `GET /api/v1/pedidos/{id}/pago` para iniciar y consultar el estado del pago | @RestController, Spring MVC | Compatibilidad |
| T10.10 | Escribir pruebas unitarias de creación de pedido y pago (201, pedido con producto inexistente 404, pago rechazado, reintento de pago, medio de pago no soportado) (traducción del escenario BDD) | JUnit 5, MockMvc | Mantenibilidad |
| T10.11 | Implementar el patrón **Strategy**: interfaz `MedioPagoStrategy` con una implementación `@Component` por medio de pago (`TarjetaSimuladaStrategy`, `PseSimuladoStrategy`, `ContraentregaStrategy`), seleccionadas por `PagoService` mediante `Map<String, MedioPagoStrategy>`. Documentar el problema de negocio y el diagrama de clases en `docs/diseno/patrones.md` | Spring (@Component), Mermaid | Mantenibilidad (modificabilidad) |

### HU-13: Cancelación del Pedido por el Comprador

| ID Tarea | Descripción técnica | Componente / Tecnología | Atributo ISO 25010 |
|---|---|---|---|
| T13.1 | Implementar en `PedidoService` el método `cancelar(pedidoId, motivo)`, que valida que el pedido esté en estado `PENDIENTE` o `CONFIRMADO` (aún no despachado) antes de cambiarlo a `CANCELADO` | Spring (@Service) | Adecuación funcional |
| T13.2 | Guardar el `motivo_cancelacion` en el `Pedido` y dejar visible el cambio de estado al agricultor en su listado de pedidos | Spring (@Service) | Usabilidad |
| T13.3 | Implementar en `PagoService` la devolución: si el pedido tiene un `Pago` `COMPLETADO`, cambiarlo a `REEMBOLSADO`; si solo hay pagos `PENDIENTE` o `RECHAZADO`, no hay nada que devolver | Spring (@Service) | Adecuación funcional |
| T13.4 | Exponer `POST /api/v1/pedidos/{id}/cancelacion` (`ROLE_COMPRADOR`) con el motivo en el cuerpo, respuestas 200, 400 (estado no cancelable) y 404 | @RestController, Spring Security | Compatibilidad / Seguridad |
| T13.5 | Escribir pruebas unitarias de cancelación (200 en `PENDIENTE`/`CONFIRMADO`, 400 si ya está `EN_TRANSPORTE` o `ENTREGADO`, reembolso correcto cuando había pago completado) (traducción del escenario BDD) | JUnit 5, MockMvc | Mantenibilidad |

### HU-05: Contacto Directo

| ID Tarea | Descripción técnica | Componente / Tecnología | Atributo ISO 25010 |
|---|---|---|---|
| T5.1 | Crear la entidad JPA `Contacto` (`comprador_id` FK, `producto_id` FK, mensaje `VARCHAR(1000)`, fecha de envío, respuesta `VARCHAR(1000)`, fecha de respuesta) según el DER | Spring Data JPA, PostgreSQL | Adecuación funcional |
| T5.2 | Implementar `ContactoRepository` | Spring Data JPA | Mantenibilidad |
| T5.3 | Definir los DTOs de solicitud (mensaje) y de respuesta, con validación `@NotBlank` sobre el mensaje | DTOs (record), Bean Validation | Usabilidad |
| T5.4 | Implementar `ContactoService`: registra la solicitud de contacto asociada al producto y permite que el agricultor dueño del producto la responda | Spring (@Service) | Adecuación funcional |
| T5.5 | Exponer `POST /api/v1/productos/{id}/contacto` (`ROLE_COMPRADOR`) para enviar el mensaje, con respuestas 201 y 404 | @RestController, Spring Security | Compatibilidad / Seguridad |
| T5.6 | Exponer `POST /api/v1/contactos/{id}/respuesta` (`ROLE_AGRICULTOR`) para que el agricultor responda, con respuestas 200 y 404 | @RestController, Spring Security | Compatibilidad / Seguridad |
| T5.7 | Realizar la consulta de sus solicitudes con `GET /api/v1/contactos` (pendiente = `respuesta` en `NULL`), sin correo ni push. Publicar un `ContactoRegistradoEvent` con `ApplicationEventPublisher` al guardar el contacto | Spring MVC, ApplicationEventPublisher  | Usabilidad |
| T5.8 | Escribir pruebas unitarias del contacto directo (201 al enviar, 200 al responder, 404 producto inexistente, listado de solicitudes del agricultor) (traducción del escenario BDD) | JUnit 5, MockMvc | Mantenibilidad |
| T5.9 | Exponer `GET /api/v1/contactos` (`ROLE_AGRICULTOR`) con las solicitudes de contacto de los productos del agricultor autenticado y el filtro opcional `?pendientes=true`, respuesta 200 | @RestController, Spring Security | Usabilidad / Compatibilidad |

## 4. Traducción obligatoria de escenarios BDD a pruebas JUnit 5

| Historia | Escenario BDD (resumen) | Clase de prueba | Método de prueba | Verifica |
|---|---|---|---|---|
| HU-10 | Given comprador autenticado con productos seleccionados, When finaliza el pedido y paga, Then el sistema confirma el pago y genera la orden | `PedidoControllerTest` | `debeCrearPedidoYConfirmarPagoAprobado()` | HTTP 201, pedido en estado `CONFIRMADO` |
| HU-10 | (regla de negocio) producto inexistente o sin cantidad disponible | `PedidoControllerTest` | `debeRechazarPedidoConProductoInvalido()` | HTTP 404/400 |
| HU-10 | (regla de negocio) carrito con productos de distintos agricultores | `PedidoServiceTest` | `debeRechazarCarritoConProductosDeVariosAgricultores()` | HTTP 400 |
| HU-10 | (caso alternativo) pago rechazado | `PagoServiceTest` | `debeMantenerPedidoPendienteSiPagoEsRechazado()` | Pago `RECHAZADO`, pedido no cambia a `CONFIRMADO` |
| HU-10 | (caso alternativo) reintento tras pago rechazado | `PagoServiceTest` | `debePermitirReintentoDePagoTrasRechazo()` | Segundo `Pago` `COMPLETADO`, pedido `CONFIRMADO` |
| HU-10 | (patrón Strategy) se usa la estrategia según el medio de pago | `PagoServiceTest` | `debeSeleccionarEstrategiaSegunMedioDePago()` | Se invoca solo la estrategia del medio solicitado |
| HU-10 | (patrón Strategy) medio de pago no soportado | `PagoServiceTest` | `debeRechazarMedioDePagoNoSoportado()` | HTTP 400 |
| HU-13 | Given un pedido confirmado aún no despachado, When el comprador lo cancela indicando el motivo, Then el sistema lo marca `CANCELADO`, informa al agricultor e inicia la devolución del pago | `PedidoControllerTest` | `debeCancelarPedidoYReembolsarPago()` | HTTP 200, pedido `CANCELADO`, pago `REEMBOLSADO` |
| HU-13 | (regla de negocio) pedido ya despachado | `PedidoServiceTest` | `debeRechazarCancelacionDePedidoEnTransporteOEntregado()` | HTTP 400 |
| HU-05 | Given comprador interesado en un producto, When envía un mensaje de contacto, Then el sistema registra la solicitud y notifica al agricultor | `ContactoControllerTest` | `debeRegistrarContactoYNotificarAgricultor()` | HTTP 201 |
| HU-05 | Given una solicitud de contacto registrada, When el agricultor responde, Then el sistema guarda la respuesta | `ContactoControllerTest` | `debeGuardarRespuestaDelAgricultor()` | HTTP 200 con `respuesta` y `fecha_respuesta` |
| HU-05 | Given solicitudes de contacto sobre productos de un agricultor, When el agricultor consulta sus solicitudes, Then ve las suyas y puede filtrar las pendientes | `ContactoControllerTest` | `debeListarSolicitudesPendientesDelAgricultor()` | HTTP 200, solo contactos de sus productos y con `respuesta` nula al filtrar |

Estas pruebas son las que ejecuta el pipeline de GitHub Actions (`ci.yml`) en cada Pull Request, y las que alimentan el reporte de cobertura de JaCoCo (mínimo 60% para que una tarea pueda pasar a la columna Testing).

## 5. Definition of Done del sprint

- Build verde (`./mvnw verify`)
- Checkstyle sin errores
- Pruebas unitarias JUnit 5 pasando, con cobertura de líneas ≥ 60% (JaCoCo)
- Pull Request revisado y fusionado a `develop`
- Patrón Strategy documentado en `docs/diseno/patrones.md` (problema de negocio y diagrama de clases)

## 6. Tablero y trazabilidad

- **Tablero:** GitHub Projects — "AgroValle Connect", vista "Sprint 2"
- **Columnas:** Backlog, To Do, In Progress (WIP 3), Testing, Done
- **Regla de Testing:** una tarea solo pasa a Testing si tiene un Pull Request abierto hacia `develop` y el pipeline de GitHub Actions reporta *Success* (incluida la cobertura > 60%)
- **Sprint Backlog detallado:** ver `BACKLOG.md` en la raíz del repositorio
