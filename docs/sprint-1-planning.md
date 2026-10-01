# Planificación del Sprint 1 — AgroValle Connect

Proyecto: **AgroValle Connect**
Sprint: **Sprint 1** 

## 1. Sprint Goal

Habilitar el registro inicial de agricultores del Valle del Cauca, la publicación de sus cosechas en el inventario y la consulta filtrada dentro del catálogo agrícola, validando la persistencia en la base de datos y la arquitectura REST.

## 2. Capacidad y alcance comprometido

> **Nota de alcance:** el equipo evaluó su capacidad mediante Planning Poker y comprometió **13 Story Points** (HU-01: 3, HU-02: 5, HU-04: 5), no 10. La decisión se tomó porque las tres historias son "Must have" y tienen una dependencia funcional directa entre sí (sin agricultores registrados no hay productos que publicar, y sin productos no hay nada que filtrar).

| Historia | Prioridad (MoSCoW) | Estimación |
|---|---|---|
| HU-01: Registro de Agricultores | Must have | 3 pts |
| HU-02: Publicación de Productos | Must have | 5 pts |
| HU-04: Filtro de Categorías | Must have | 5 pts |
| **Total comprometido** | | **13 pts** |

## 3. Descomposición técnica asociada a ISO/IEC 25010

### HU-01: Registro de Agricultores

| ID Tarea | Descripción técnica | Componente / Tecnología | Atributo ISO 25010 |
|---|---|---|---|
| T1.1 | Crear la entidad JPA Agricultor (nombre, apellido, identificación única, municipio, teléfono, correo) y generar su tabla en PostgreSQL | Spring Data JPA (@Entity), PostgreSQL | Adecuación funcional |
| T1.2 | Implementar AgricultorRepository con el método existsByIdentificacion | Spring Data JPA | Mantenibilidad |
| T1.3 | Definir los DTOs de request y response con validaciones de campos obligatorios | DTOs (record), Bean Validation (@NotBlank, @Email) | Usabilidad |
| T1.4 | Implementar AgricultorService con la regla que rechaza identificaciones duplicadas | Spring (@Service) | Adecuación funcional |
| T1.5 | Exponer POST /api/v1/auth/register con respuestas 201, 400 y 409 | @RestController, Spring MVC | Compatibilidad |
| T1.6 | Escribir pruebas unitarias del registro para los casos 201, 400 y 409 (traducción del escenario BDD) | JUnit 5, MockMvc | Mantenibilidad |

### HU-02: Publicación de Productos

| ID Tarea | Descripción técnica | Componente / Tecnología | Atributo ISO 25010 |
|---|---|---|---|
| T2.1 | Crear la entidad Producto (nombre, categoría, cantidad, precio, fecha de cosecha) con relación @ManyToOne hacia Agricultor | Spring Data JPA, PostgreSQL | Adecuación funcional |
| T2.2 | Implementar ProductoRepository y mapearlo en PostgreSQL | Spring Data JPA | Mantenibilidad |
| T2.3 | Definir DTOs con validaciones (campos obligatorios, cantidad y precio mayores a 0) | DTOs, Bean Validation | Usabilidad |
| T2.4 | Implementar ProductoService que valida que la fecha de cosecha no sea anterior a hoy y que el agricultor exista | Spring (@Service) | Adecuación funcional |
| T2.5 | Exponer POST /api/v1/productos con respuesta 201 Created | @RestController, Spring MVC | Compatibilidad |
| T2.6 | Definir y documentar el mecanismo con que se identifica al agricultor al publicar (agricultorId o JWT) | Spring Security / JWT (según la decisión) | Seguridad |
| T2.7 | Escribir pruebas unitarias de la publicación: 201, fecha pasada (400) y agricultor inexistente (404) (traducción del escenario BDD) | JUnit 5, MockMvc | Mantenibilidad |

### HU-04: Filtro de Categorías

| ID Tarea | Descripción técnica | Componente / Tecnología | Atributo ISO 25010 |
|---|---|---|---|
| T4.1 | Crear la consulta derivada por categoría y municipio del agricultor (findByAgricultorMunicipioAndCategoria) | Spring Data JPA | Adecuación funcional |
| T4.2 | Implementar el método de filtrado en ProductoService usando la consulta del repositorio | Spring (@Service) | Adecuación funcional |
| T4.3 | Exponer GET /api/v1/productos?categoria=&municipio= con respuesta 200 | @RestController, @RequestParam | Compatibilidad |
| T4.4 | Devolver un mensaje claro cuando no hay proveedores del producto en el municipio | Spring MVC | Usabilidad |
| T4.5 | Cargar datos de prueba que permitan verificar el filtro | SQL (data.sql), PostgreSQL | Mantenibilidad |
| T4.6 | Escribir pruebas unitarias del filtro con resultados y sin resultados (traducción del escenario BDD) | JUnit 5, MockMvc | Mantenibilidad |

## 4. Traducción obligatoria de escenarios BDD a pruebas JUnit 5

Cada historia comprometida tiene un escenario Given-When-Then que se traduce en un método de prueba automatizada. Esta tabla es la trazabilidad exigida entre el requisito ágil y la prueba de código:

| Historia | Escenario BDD (resumen) | Clase de prueba | Método de prueba | Verifica |
|---|---|---|---|---|
| HU-01 | Given datos válidos, When se registra, Then se guarda y confirma | `AgricultorControllerTest` | `debeRegistrarAgricultorConDatosValidos()` | HTTP 201 y cuerpo con mensaje de confirmación |
| HU-01 | (regla de negocio) identificación duplicada | `AgricultorControllerTest` | `debeRechazarIdentificacionDuplicada()` | HTTP 409 |
| HU-01 | (regla de negocio) datos inválidos | `AgricultorControllerTest` | `debeRechazarDatosIncompletos()` | HTTP 400 |
| HU-02 | Given agricultor registrado, When publica producto, Then valida fecha y lo muestra en catálogo | `ProductoControllerTest` | `debePublicarProductoConFechaValida()` | HTTP 201 |
| HU-02 | (regla de negocio) fecha de cosecha anterior a hoy | `ProductoControllerTest` | `debeRechazarFechaDeCosechaPasada()` | HTTP 400 |
| HU-02 | (regla de negocio) agricultor inexistente | `ProductoControllerTest` | `debeRechazarAgricultorInexistente()` | HTTP 404 |
| HU-04 | Given productos publicados, When filtra por categoría y municipio, Then muestra solo los que coinciden | `ProductoControllerTest` | `debeFiltrarProductosPorCategoriaYMunicipio()` | HTTP 200 con la lista filtrada |
| HU-04 | (caso alternativo) sin coincidencias | `ProductoControllerTest` | `debeMostrarMensajeCuandoNoHayResultados()` | HTTP 200 con mensaje de "no hay proveedores..." |

Estas pruebas son las que ejecuta el pipeline de GitHub Actions (`ci.yml`) en cada Pull Request, y las que alimentan el reporte de cobertura de JaCoCo (mínimo 60% para que una tarea pueda pasar a la columna Testing).

## 5. Definition of Done del sprint

- Build verde (`./mvnw verify`)
- Checkstyle sin errores
- Pruebas unitarias JUnit 5 pasando, con cobertura de líneas ≥ 60% (JaCoCo)
- Pull Request revisado y fusionado a `develop`

## 6. Tablero y trazabilidad

- **Tablero:** GitHub Projects — "AgroValle Connect", vista "Sprint 1"
- **Columnas:** Backlog, To Do, In Progress (WIP 3), Testing, Done
- **Regla de Testing:** una tarea solo pasa a Testing si tiene un Pull Request abierto hacia `develop` y el pipeline de GitHub Actions reporta *Success* (incluida la cobertura > 60%)
- **Sprint Backlog detallado:** ver `BACKLOG.md` en la raíz del repositorio