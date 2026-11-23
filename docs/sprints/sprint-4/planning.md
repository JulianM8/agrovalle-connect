# Planificación del Sprint 4 — AgroValle Connect

Proyecto: **AgroValle Connect**
Sprint: **Sprint 4**

## 1. Sprint Goal

Cerrar el ciclo del pedido después del despacho: que el comprador consulte el
estado actualizado de su pedido y su fecha estimada de llegada, pueda reportar
una inconformidad con evidencia fotográfica si el producto no cumple lo
acordado, y confirme la recepción de la mercancía, dejando el pedido en estado
`ENTREGADO` y publicando el evento que habilita la liberación del pago al
agricultor (HU-11, Sprint 5). El sprint se apoya en el `Pedido`, el `Pago` y la
asignación de transporte construidos en los Sprints 2 y 3.

## 2. Capacidad y alcance comprometido

> **Nota de alcance:** el equipo compromete **11 Story Points** (HU-08: 3,
> HU-09: 3, HU-15: 5). Las tres historias forman el mismo tramo del flujo
> (pedido en camino → recepción) y comparten el pedido en estado
> `EN_TRANSPORTE`. HU-15 se implementa antes que HU-09 porque la confirmación
> de recepción debe verificar que no haya una inconformidad abierta; HU-08 es
> independiente y puede avanzar en paralelo.


| Historia | Prioridad (MoSCoW) | Estimación |
|---|---|---|
| HU-08: Seguimiento del Envío | Should have | 3 pts |
| HU-09: Confirmación de Recepción de la Mercancía | Must have | 3 pts |
| HU-15: Reporte de Inconformidad o Producto Dañado | Must have | 5 pts |
| **Total comprometido** | | **11 pts** |

## 3. Descomposición técnica asociada a ISO/IEC 25010

### HU-08: Seguimiento del Envío

| ID Tarea | Descripción técnica | Componente / Tecnología | Atributo ISO 25010 |
|---|---|---|---|
| T8.1 | Definir el DTO `SeguimientoResponse` (pedido, estado, fecha de creación, fecha estimada de llegada, fecha de entrega y un mensaje cuando aún no hay fecha estimada) | DTOs (record) | Usabilidad |
| T8.2 | Ajustar `DespachoService.asignarVehiculo` para guardar la `fecha_estimada_llegada` del pedido igual a la fecha de la ruta cuando el pedido no la tiene (ver §7) y actualizar sus pruebas | Spring (@Service) | Adecuación funcional |
| T8.3 | Implementar `PedidoService.consultarSeguimiento(pedidoId, usuarioId)`: valida que el pedido sea del comprador autenticado y devuelve el estado actual y la fecha estimada de llegada cuando exista | Spring (@Service) | Adecuación funcional |
| T8.4 | Exponer `GET /api/v1/pedidos/{id}/seguimiento` (`ROLE_COMPRADOR`), con respuestas 200 y 404 (pedido inexistente o de otro comprador) | @RestController, Spring Security | Compatibilidad / Seguridad |
| T8.5 | Escribir pruebas unitarias del seguimiento (200 con estado y fecha estimada, 200 con mensaje cuando no hay fecha, 404 de otro comprador, 403 sin rol `COMPRADOR`) (traducción del escenario BDD) | JUnit 5, MockMvc, Mockito | Mantenibilidad |

### HU-15: Reporte de Inconformidad o Producto Dañado

| ID Tarea | Descripción técnica | Componente / Tecnología | Atributo ISO 25010 |
|---|---|---|---|
| T15.1 | Crear el enum `EstadoInconformidad` (`EN_REVISION`, `RESUELTA`, `RECHAZADA`) y la entidad JPA `Inconformidad` (descripción hasta 1000, `evidencia_url` hasta 500, estado, resolución, fecha de reporte, `@ManyToOne` a `Pedido`) según el DER | Spring Data JPA, PostgreSQL | Adecuación funcional |
| T15.2 | Implementar `InconformidadRepository` (`findByPedidoIdOrderByFechaReporteDesc`, `existsByPedidoIdAndEstado`) | Spring Data JPA | Mantenibilidad |
| T15.3 | Definir los DTOs `InconformidadRequest` (descripción obligatoria, `evidenciaUrl` obligatoria con formato `http(s)://`, longitudes máximas) e `InconformidadResponse` | DTOs (record), Bean Validation | Usabilidad / Seguridad |
| T15.4 | Implementar `InconformidadService.reportar(pedidoId, request, usuarioId)`: valida que el pedido sea del comprador autenticado y esté `EN_TRANSPORTE`, que no tenga otra inconformidad `EN_REVISION`, crea la inconformidad en `EN_REVISION` con la fecha de reporte y no modifica el pago (queda sin liberar) | Spring (@Service) | Adecuación funcional |
| T15.5 | Exponer `POST /api/v1/pedidos/{id}/inconformidad` (`ROLE_COMPRADOR`, respuestas 201, 400, 404 y 409) y `GET /api/v1/pedidos/{id}/inconformidad` (`ROLE_COMPRADOR`) para consultar el estado de la revisión. Registrar las excepciones en `GlobalExceptionHandler` si hace falta | @RestController, Spring Security | Compatibilidad / Seguridad |
| T15.6 | Publicar un `InconformidadRegistradaEvent` con `ApplicationEventPublisher` al registrar la inconformidad (patrón Observer), como punto de extensión para avisar al agricultor y para la revisión de HU-11. Sin correo ni push | Spring, ApplicationEventPublisher | Mantenibilidad (modificabilidad) |
| T15.7 | Escribir pruebas unitarias de la inconformidad (201 con evidencia, evidencia inválida 400, pedido que no está en transporte 400, pedido de otro comprador 404, inconformidad abierta duplicada 409, el pago no cambia, evento publicado, 403 sin rol) (traducción del escenario BDD) | JUnit 5, MockMvc, Mockito | Mantenibilidad |

### HU-09: Confirmación de Recepción de la Mercancía

| ID Tarea | Descripción técnica | Componente / Tecnología | Atributo ISO 25010 |
|---|---|---|---|
| T9.1 | Crear el `RecepcionConfirmadaEvent` (pedido, comprador, agricultor, fecha) como punto de extensión para la liberación del pago de HU-11 (patrón Observer) | Spring, ApplicationEventPublisher | Mantenibilidad (modificabilidad) |
| T9.2 | Implementar `RecepcionService.confirmarRecepcion(pedidoId, usuarioId)`: valida que el pedido sea del comprador autenticado, que no esté ya `ENTREGADO` (409), que esté `EN_TRANSPORTE` (400) y que no tenga una inconformidad `EN_REVISION` (409); cambia el pedido a `ENTREGADO`, guarda `fecha_entrega` y publica el `RecepcionConfirmadaEvent` | Spring (@Service) | Adecuación funcional |
| T9.3 | Exponer `POST /api/v1/pedidos/{id}/recepcion` (`ROLE_COMPRADOR`) en un `RecepcionController` separado, con respuestas 200, 400, 404 y 409 | @RestController, Spring Security | Compatibilidad / Seguridad |
| T9.4 | Escribir pruebas unitarias de la recepción (200 y pedido `ENTREGADO`, pedido ya entregado 409, pedido que no está en transporte 400, inconformidad abierta 409, pedido de otro comprador 404, evento publicado, 403 sin rol) (traducción del escenario BDD) | JUnit 5, MockMvc, Mockito | Mantenibilidad |
| T9.5 | Documentar el patrón Observer (`ContactoRegistradoEvent`, `AsignacionRegistradaEvent`, `InconformidadRegistradaEvent` y `RecepcionConfirmadaEvent`) en `docs/diseno/patrones.md`: problema de negocio, participantes y diagrama de clases | Mermaid | Mantenibilidad |

## 4. Traducción obligatoria de escenarios BDD a pruebas JUnit 5

| Historia | Escenario BDD (resumen) | Clase de prueba | Método de prueba | Verifica |
|---|---|---|---|---|
| HU-08 | Given el comprador tiene un pedido en despacho o en camino, When entra al detalle del pedido, Then el sistema muestra el estado actualizado y la fecha estimada de llegada | `PedidoControllerTest` | `debeMostrarEstadoYFechaEstimadaDelPedido()` | HTTP 200 con estado y fecha estimada |
| HU-08 | (caso alternativo) el pedido aún no tiene fecha estimada | `PedidoServiceTest` | `debeIndicarQueNoHayFechaEstimada()` | Mensaje y fecha nula |
| HU-08 | (regla de negocio) pedido de otro comprador | `PedidoServiceTest` | `debeRechazarSeguimientoDePedidoAjeno()` | HTTP 404 |
| HU-08 | (seguridad) solo el comprador consulta el seguimiento | `PedidoControllerTest` | `debeRechazarSeguimientoSinRolComprador()` | HTTP 403 |
| HU-15 | Given el comprador recibe un producto que no cumple lo acordado, When adjunta la evidencia y envía el reporte antes de confirmar la recepción, Then el sistema registra la inconformidad, mantiene el pago sin liberar e inicia la revisión | `InconformidadControllerTest` | `debeRegistrarInconformidadConEvidencia()` | HTTP 201, estado `EN_REVISION` |
| HU-15 | (regla de negocio) el pago no cambia | `InconformidadServiceTest` | `debeMantenerElPagoSinCambios()` | Pago sin modificar |
| HU-15 | (regla de negocio) evidencia inválida | `InconformidadControllerTest` | `debeRechazarEvidenciaInvalida()` | HTTP 400 |
| HU-15 | (regla de negocio) pedido que no está en transporte | `InconformidadServiceTest` | `debeRechazarInconformidadDePedidoNoEnTransporte()` | HTTP 400 |
| HU-15 | (regla de negocio) ya hay una inconformidad en revisión | `InconformidadServiceTest` | `debeRechazarInconformidadAbiertaDuplicada()` | HTTP 409 |
| HU-15 | (regla de negocio) pedido de otro comprador | `InconformidadServiceTest` | `debeRechazarInconformidadDePedidoAjeno()` | HTTP 404 |
| HU-15 | (Observer) se publica el evento al registrar | `InconformidadServiceTest` | `debePublicarInconformidadRegistradaEvent()` | Evento publicado una vez |
| HU-09 | Given el pedido fue entregado al comprador, When revisa la carga y confirma la recepción, Then el sistema marca el pedido como entregado y publica el evento que habilita la liberación del pago | `RecepcionControllerTest` | `debeConfirmarRecepcionDelPedido()` | HTTP 200, pedido `ENTREGADO` |
| HU-09 | (Observer) se publica el evento de recepción | `RecepcionServiceTest` | `debePublicarRecepcionConfirmadaEvent()` | Evento publicado una vez |
| HU-09 | (regla de negocio) pedido que no está en transporte | `RecepcionServiceTest` | `debeRechazarRecepcionDePedidoNoEnTransporte()` | HTTP 400 |
| HU-09 | (regla de negocio) pedido ya entregado | `RecepcionServiceTest` | `debeRechazarRecepcionDePedidoYaEntregado()` | HTTP 409 |
| HU-09 | (regla de negocio) hay una inconformidad en revisión | `RecepcionServiceTest` | `debeRechazarRecepcionConInconformidadAbierta()` | HTTP 409 |
| HU-09 | (regla de negocio) pedido de otro comprador | `RecepcionServiceTest` | `debeRechazarRecepcionDePedidoAjeno()` | HTTP 404 |
| HU-09 | (seguridad) solo el comprador confirma | `RecepcionControllerTest` | `debeRechazarRecepcionSinRolComprador()` | HTTP 403 |

Estas pruebas son las que ejecuta el pipeline de GitHub Actions (`ci.yml`) en cada Pull Request, y las que alimentan el reporte de cobertura de JaCoCo (mínimo 60% para que una tarea pueda pasar a la columna Testing).

## 5. Definition of Done del sprint

- Build verde (`./mvnw verify`)
- Checkstyle sin errores
- Pruebas unitarias JUnit 5 pasando, con cobertura de líneas ≥ 60% (JaCoCo)
- Pruebas escritas en el mismo commit que el código de cada tarea
- Excepciones nuevas listadas antes de codificar y registradas en `GlobalExceptionHandler`, con prueba de 400 y 404 en cada endpoint
- `schema.sql` y la base de Staging actualizados en la misma historia que crea las tablas
- Pull Request revisado y fusionado a `develop`, con el CI en verde
- Patrón Observer documentado en `docs/diseno/patrones.md`

## 6. Tablero y trazabilidad

- **Tablero:** GitHub Projects — "AgroValle Connect", vista "Sprint 4"
- **Columnas:** Backlog, To Do, In Progress (WIP 3), Testing, Done
- **Regla de Testing:** una tarea solo pasa a Testing si tiene un Pull Request abierto hacia `develop` y el pipeline de GitHub Actions reporta *Success* (incluida la cobertura > 60%)
- **Sprint Backlog detallado:** ver `product_backlog_bdd.md` en `docs/requirements/`, con la trazabilidad de cada historia a sus criterios de aceptación Given-When-Then y a las pruebas unitarias JUnit 5 que los implementan.

