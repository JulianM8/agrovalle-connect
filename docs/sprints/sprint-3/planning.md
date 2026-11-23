# Planificación del Sprint 3 — AgroValle Connect

Proyecto: **AgroValle Connect**
Sprint: **Sprint 3**

## 1. Sprint Goal

Habilitar el despacho de los pedidos confirmados: que el agricultor pueda
asignar un vehículo de transporte a un pedido (notificando al comprador) y que
el transportista consulte la secuencia sugerida de entregas del día, ordenada
según la ubicación de cada comprador. El sprint se apoya en el `Pedido` y el
`Pago` construidos en el Sprint 2, y aplica el patrón comportamental **Strategy**
para el ordenamiento de las entregas.

Además, automatizar el despliegue continuo (CD) de la aplicación hacia un
ambiente de Staging en la nube: cada integración verificada en la rama `main`
publica la aplicación Spring Boot en una URL pública conectada a una base de
datos PostgreSQL de preproducción, con las credenciales protegidas en GitHub
Secrets y la decisión de arquitectura documentada en un ADR.

## 2. Capacidad y alcance comprometido

> **Nota de alcance:** el equipo compromete **10 Story Points** (HU-06: 5,
> HU-07: 5). Ambas historias forman el mismo flujo de logística de despacho y
> comparten las entidades `Vehiculo`, `Ruta` y `AsignacionTransporte`, por lo
> que se construyen en el mismo sprint para no retrabajar. HU-06 se implementa
> primero porque crea la asignación del pedido a una ruta, que es lo que HU-07
> consulta y ordena.
>
> El sprint incluye además cinco tareas técnicas de infraestructura (T03.1 a
> T03.5) para el despliegue continuo a Staging. Igual que los prerrequisitos,
> son trabajo habilitador del equipo y no suman Story Points.

| Historia | Prioridad (MoSCoW) | Estimación |
|---|---|---|
| HU-06: Asignación de Vehículo de Transporte | Must have | 5 pts |
| HU-07: Programación de Rutas de Despacho | Must have | 5 pts |
| Infraestructura: Despliegue Continuo a Staging (T03.1 a T03.5) | Must have | Tarea técnica, sin puntos |
| **Total comprometido** | | **10 pts** (más las tareas técnicas) |

## 3. Descomposición técnica asociada a ISO/IEC 25010

### Prerrequisitos transversales: perfil Transportista

| ID Tarea | Descripción técnica | Componente / Tecnología | Atributo ISO 25010 |
|---|---|---|---|
| T0.6 | Crear la entidad JPA `Transportista` (municipio) con `@OneToOne` a `Usuario` vía `usuario_id` (`FK,UK`), igual que `Agricultor` y `Comprador`. Nombre, apellido y teléfono ya están en `Usuario` | Spring Data JPA, PostgreSQL | Adecuación funcional |
| T0.7 | Implementar `TransportistaRepository` (`findByUsuarioId`) | Spring Data JPA | Mantenibilidad |
| T0.8 | Registro de transportista con `POST /api/v1/auth/register/transportista`, siguiendo el patrón de `CompradorService.registrar`: si la identificación ya existe como `Usuario`, se valida la contraseña y se le agrega el rol `TRANSPORTISTA`; si no existe, se crea el `Usuario` con ese rol. Rechaza identificación o correo duplicados (409) y contraseña incorrecta al reutilizar cuenta (401) | Spring (@Service), @RestController | Adecuación funcional / Seguridad |
| T0.9 | Pruebas unitarias del registro de transportista (cuenta nueva, reutilización de cuenta con otro rol, contraseña incorrecta, duplicados) | JUnit 5, Mockito, MockMvc | Mantenibilidad |

### Infraestructura: Despliegue Continuo (CD) a Staging

Estado actual: el pipeline `ci.yml` ya verifica cada Pull Request hacia
`develop` y `main` (compilación, Checkstyle, pruebas y JaCoCo), pero la
aplicación solo se ejecuta en local. Este bloque agrega el ambiente de Staging
y el pipeline que lo despliega.

| ID Tarea | Descripción técnica | Componente / Tecnología | Atributo ISO 25010 |
|---|---|---|---|
| T03.1 | Configurar el perfil de Staging: crear `application-staging.yml` en `src/main/resources/` con la conexión a la base de datos, el puerto y la llave JWT tomados de variables de entorno (`${DB_URL_VAR}`, `${DB_USER_VAR}`, `${DB_PASS_VAR}`, `${JWT_SECRET_VAR}`, `${PORT:8080}`), sin valores fijos en el código | Spring Boot (perfiles), YAML | Portabilidad / Seguridad |
| T03.2 | Registrar los secretos de infraestructura en GitHub Secrets (Settings > Secrets and variables > Actions): `STAGING_DB_URL`, `STAGING_DB_USER`, `STAGING_DB_PASSWORD`, `JWT_SECRET_KEY` y `DEPLOY_HOOK_URL`, para evitar la fuga de credenciales en el historial del repositorio | GitHub Secrets | Seguridad |
| T03.3 | Configurar el pipeline de Despliegue Continuo `.github/workflows/cd.yml` con dos jobs: `verify-and-test` (compilación, Checkstyle, pruebas JUnit y cobertura JaCoCo ≥ 60%) y `deploy-to-staging`, que depende del anterior (`needs`) y dispara el Deploy Hook de la PaaS | GitHub Actions, Render (Deploy Hook) | Fiabilidad / Mantenibilidad |
| T03.4 | Documentar la decisión de arquitectura en `docs/architecture-decisions/ADR-001-despliegue-staging-paas.md` con la plantilla Nygard (estado, fecha, autores, contexto, opciones consideradas, decisión, consecuencias), justificando la PaaS y GitHub Actions para el CD en Staging | Markdown (ADR) | Mantenibilidad |
| T03.5 | Verificar el despliegue y ejecutar pruebas de integración en Staging: con el pipeline en verde, obtener la URL pública, consultar `GET /api/v1/estado` (200 OK) y probar `POST /api/v1/auth/register` (201) para comprobar la comunicación con PostgreSQL remoto | Render, Neon (PostgreSQL), Postman | Fiabilidad / Compatibilidad |

Trabajo de soporte que habilita las tareas anteriores:

| Actividad | Descripción |
|---|---|
| Base de datos de Staging | Crear la base `agrovalle_staging` en Neon y ejecutar `docs/diseno/schema.sql`, verificando las tablas con `information_schema.tables` |
| Empaquetado con Docker | `Dockerfile` multietapa (JDK 17 para compilar, JRE 17 para ejecutar) y `.dockerignore` en la raíz del repositorio, requeridos por Render para ejecutar Java |
| Servicio web en Render | Crear el servicio conectado a Neon, con las variables de entorno del perfil `staging` y el Deploy Hook que usa `cd.yml` |
| Endpoint de estado y seguridad | `GET /api/v1/estado` (`EstadoController`, con su `EstadoControllerTest`) y regla en `SecurityConfig` que permite sin token solo ese endpoint y `/actuator/health` |

### HU-06: Asignación de Vehículo de Transporte

| ID Tarea | Descripción técnica | Componente / Tecnología | Atributo ISO 25010 |
|---|---|---|---|
| T6.1 | Crear la entidad JPA `Vehiculo` (tipo de transporte, placa `UK`, capacidad en kg) con `@OneToOne` a `Transportista` vía `transportista_id` (`FK,UK`) según el DER | Spring Data JPA, PostgreSQL | Adecuación funcional |
| T6.2 | Crear las entidades JPA `Ruta` (fecha, `@ManyToOne` a `Vehiculo`) y `AsignacionTransporte` (`pedido_id` `FK,UK`, `ruta_id` `FK`, orden de entrega, fecha de asignación) según el DER | Spring Data JPA, PostgreSQL | Adecuación funcional |
| T6.3 | Implementar `VehiculoRepository` (`findByTipoTransporte`, `existsByPlaca`, `findByTransportistaId`), `RutaRepository` (`findByVehiculoIdAndFecha`) y `AsignacionTransporteRepository` (`findByPedidoId`, `findByRutaIdOrderByOrdenEntrega`) | Spring Data JPA | Mantenibilidad |
| T6.4 | Registro del vehículo del transportista: `POST /api/v1/vehiculos` (`ROLE_TRANSPORTISTA`) con DTOs validados (tipo, placa, capacidad `@Positive`); respuestas 201, 400 y 409 (placa duplicada o transportista que ya tiene vehículo) | @RestController, Bean Validation, Spring Security | Compatibilidad / Seguridad |
| T6.5 | Consulta de vehículos por tipo de transporte: `GET /api/v1/vehiculos?tipoTransporte=` (`ROLE_AGRICULTOR`), respuesta 200 con la lista de vehículos del tipo solicitado y mensaje claro cuando no hay ninguno | @RestController, @RequestParam | Usabilidad |
| T6.6 | Implementar `DespachoService.asignarVehiculo(pedidoId, vehiculoId, usuarioId)`: valida que el pedido sea del agricultor autenticado y esté `CONFIRMADO`, que no tenga asignación previa y que el vehículo exista; busca o crea la `Ruta` del vehículo para la fecha, crea la `AsignacionTransporte` con el siguiente orden de entrega y cambia el `Pedido` a `EN_TRANSPORTE` | Spring (@Service) | Adecuación funcional |
| T6.7 | Exponer `POST /api/v1/pedidos/{id}/asignacion` (`ROLE_AGRICULTOR`) con el `vehiculoId` en el cuerpo, respuestas 201, 400 (pedido no confirmado), 404 (pedido o vehículo inexistente) y 409 (pedido ya asignado) | @RestController, Spring Security | Compatibilidad / Seguridad |
| T6.8 | Notificar al comprador (ver §7): publicar un `AsignacionRegistradaEvent` con `ApplicationEventPublisher` al asignar, y exponer `GET /api/v1/pedidos/{id}/transporte` (`ROLE_COMPRADOR`) con el tipo de transporte y la placa asignados | Spring MVC, ApplicationEventPublisher | Usabilidad |
| T6.9 | Escribir pruebas unitarias de la asignación (201, pedido no confirmado 400, pedido de otro agricultor 404, pedido ya asignado 409, listado de vehículos por tipo, registro de vehículo y placa duplicada) (traducción del escenario BDD) | JUnit 5, MockMvc, Mockito | Mantenibilidad |

### HU-07: Programación de Rutas de Despacho

| ID Tarea | Descripción técnica | Componente / Tecnología | Atributo ISO 25010 |
|---|---|---|---|
| T7.1 | Definir los DTOs de la ruta del día (`RutaDiaResponse`: fecha, vehículo, lista de paradas con orden sugerido, pedido, nombre del comprador, municipio, dirección de entrega y estado del pedido) | DTOs (record) | Usabilidad |
| T7.2 | Implementar el patrón **Strategy** para el ordenamiento: interfaz `OrdenEntregaStrategy` con `PorMunicipioStrategy` (agrupa por municipio) y `VecinoMasCercanoStrategy` (distancia de Haversine entre latitud/longitud de los compradores; los que no tienen coordenadas van al final), seleccionadas por `RutaService` mediante `Map<String, OrdenEntregaStrategy>` | Spring (@Component) | Mantenibilidad (modificabilidad) |
| T7.3 | Implementar `RutaService.consultarRutaDelDia(usuarioId, fecha, criterio)`: busca el vehículo del transportista autenticado y su `Ruta` de la fecha, toma las asignaciones y devuelve la secuencia sugerida según la estrategia elegida (por defecto, vecino más cercano). Es solo de lectura: no modifica `orden_entrega` | Spring (@Service) | Adecuación funcional |
| T7.4 | Exponer `GET /api/v1/rutas?fecha=&criterio=` (`ROLE_TRANSPORTISTA`; la fecha por defecto es hoy), respuestas 200, 400 (criterio inválido) y 404 (el transportista no tiene vehículo o ruta para esa fecha) | @RestController, Spring Security | Compatibilidad / Seguridad |
| T7.5 | Cargar datos de prueba (`data.sql`): un transportista con su vehículo, una ruta y asignaciones de pedidos de compradores en distintos municipios, para poder verificar el ordenamiento | SQL (data.sql), PostgreSQL | Mantenibilidad |
| T7.6 | Documentar el patrón Strategy del ordenamiento (problema de negocio y diagrama de clases) en `docs/diseno/patrones.md` | Mermaid | Mantenibilidad |
| T7.7 | Escribir pruebas unitarias de la ruta del día (200 con paradas ordenadas, ordenamiento por cada estrategia, 404 sin ruta, 400 con criterio inválido, 403 sin rol `TRANSPORTISTA`) (traducción del escenario BDD) | JUnit 5, MockMvc, Mockito | Mantenibilidad |

## 4. Traducción obligatoria de escenarios BDD a pruebas JUnit 5

| Historia | Escenario BDD (resumen) | Clase de prueba | Método de prueba | Verifica |
|---|---|---|---|---|
| HU-06 | Given el agricultor tiene un pedido confirmado, When selecciona el vehículo del tipo de transporte necesario y confirma la asignación, Then el sistema guarda la asignación y notifica al comprador | `DespachoControllerTest` | `debeAsignarVehiculoAPedidoConfirmado()` | HTTP 201, pedido `EN_TRANSPORTE`, asignación guardada |
| HU-06 | (notificación) el comprador ve el transporte asignado | `DespachoControllerTest` | `debeMostrarAlCompradorElTransporteAsignado()` | HTTP 200 con tipo de transporte y placa |
| HU-06 | (regla de negocio) pedido que no está confirmado | `DespachoServiceTest` | `debeRechazarAsignacionDePedidoNoConfirmado()` | HTTP 400 |
| HU-06 | (regla de negocio) pedido de otro agricultor | `DespachoServiceTest` | `debeRechazarAsignacionDePedidoDeOtroAgricultor()` | HTTP 404 |
| HU-06 | (regla de negocio) pedido ya asignado | `DespachoServiceTest` | `debeRechazarPedidoYaAsignado()` | HTTP 409 |
| HU-06 | (selección) listar vehículos por tipo de transporte | `VehiculoControllerTest` | `debeListarVehiculosPorTipoDeTransporte()` | HTTP 200 con los vehículos del tipo |
| HU-06 | (registro) placa duplicada | `VehiculoControllerTest` | `debeRechazarPlacaDuplicada()` | HTTP 409 |
| HU-07 | Given el transportista tiene varios pedidos asignados, When consulta las rutas de entregas del día, Then el sistema muestra la secuencia sugerida ordenada según la ubicación de cada comprador | `RutaControllerTest` | `debeMostrarRutaDelDiaOrdenadaPorUbicacion()` | HTTP 200, paradas en el orden esperado |
| HU-07 | (patrón Strategy) se usa la estrategia según el criterio | `RutaServiceTest` | `debeOrdenarSegunLaEstrategiaElegida()` | Orden distinto por municipio y por vecino más cercano |
| HU-07 | (caso alternativo) compradores sin coordenadas | `RutaServiceTest` | `debeUbicarAlFinalALosCompradoresSinCoordenadas()` | Paradas sin latitud/longitud al final |
| HU-07 | (caso alternativo) sin ruta para la fecha | `RutaControllerTest` | `debeResponder404SiNoHayRutaParaLaFecha()` | HTTP 404 |
| HU-07 | (regla de negocio) criterio de ordenamiento inválido | `RutaControllerTest` | `debeRechazarCriterioInvalido()` | HTTP 400 |
| HU-07 | (seguridad) solo el transportista consulta rutas | `RutaControllerTest` | `debeRechazarUsuarioSinRolTransportista()` | HTTP 403 |
| T03.5 | Given el servicio desplegado con la base de datos conectada, When se consulta el estado de la aplicación, Then responde que está operativa con el perfil activo | `EstadoControllerTest` | `estadoRetornaOkCuandoLaBaseDeDatosResponde()` | HTTP 200 con `estado` OK y `PostgreSQL Connected` |
| T03.5 | (caso alternativo) la base de datos no responde | `EstadoControllerTest` | `estadoRetorna503CuandoLaBaseDeDatosFalla()` | HTTP 503 |

Estas pruebas son las que ejecuta el pipeline de GitHub Actions (`ci.yml`) en cada Pull Request, y las que alimentan el reporte de cobertura de JaCoCo (mínimo 60% para que una tarea pueda pasar a la columna Testing).

## 5. Definition of Done del sprint

- Build verde (`./mvnw verify`)
- Checkstyle sin errores
- Pruebas unitarias JUnit 5 pasando, con cobertura de líneas ≥ 60% (JaCoCo)
- Pull Request revisado y fusionado a `develop`

## 6. Tablero y trazabilidad

- **Tablero:** GitHub Projects — "AgroValle Connect", vista "Sprint 3"
- **Columnas:** Backlog, To Do, In Progress (WIP 3), Testing, Done
- **Regla de Testing:** una tarea solo pasa a Testing si tiene un Pull Request abierto hacia `develop` y el pipeline de GitHub Actions reporta *Success* (incluida la cobertura > 60%)
- **Sprint Backlog detallado:** ver `product_backlog_bdd.md` en `docs/requirements/`, con la trazabilidad de cada historia a sus criterios de aceptación Given-When-Then y a las pruebas unitarias JUnit 5 que los implementan.
- **Despliegue:** el CI corre en cada Pull Request; el CD (`cd.yml`) corre en cada push a `main` y publica en Staging