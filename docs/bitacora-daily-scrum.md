# Bitácora de Daily Scrum — Sprint 1 — AgroValle Connect

Formato por integrante: **¿Qué hice ayer?** / **¿Qué haré hoy?** / **¿Qué impedimentos tengo?**
Duración máxima: 15 minutos. 

**Nota sobre la dinámica del equipo:** las tareas se completaron en orden por dependencia (primero HU-01, luego HU-02, luego HU-04), no en paralelo entre las 3 historias. La excepción fue HU-02: sus primeras tareas (T2.1–T2.4: entidad, repositorio, DTOs y servicio de `Producto`) se desarrollaron en paralelo con HU-01, porque no dependían del endpoint de registro para existir. El resto de HU-02 (T2.5–T2.7: controlador, decisión de identificación y pruebas) se completó una vez HU-01 estuvo terminada. Mientras una historia estaba en desarrollo, quienes no tenían tareas activas revisaban los Pull Requests abiertos y adelantaban diseño o lectura de su propia historia, sin escribir código todavía.

---

## Daily — Domingo 27 de septiembre de 2026
*(Arranque de HU-01; inicio en paralelo de las primeras tareas de HU-02)*

**Julian Mina (Scrum Master)**
- Ayer: Definió la estructura de paquetes del proyecto (`model`, `repository`, `service`, `controller`, `dto`, `exception`, `config`) e instaló PostgreSQL localmente.
- Hoy: Crear la entidad `Agricultor`, su repositorio y los DTOs de registro (T1.1–T1.3).
- Impedimentos: Falla de autenticación al conectar la aplicación con PostgreSQL, causada por una variable de entorno `DB_PASSWORD` desincronizada con la contraseña real de la base de datos. Resuelto en el mismo día definiendo la variable en la sesión activa y alineando la contraseña en pgAdmin.

**Lesly Quintero (Product Owner)**
- Ayer: Revisó el Product Backlog para entender el alcance de HU-02.
- Hoy: Crear la entidad `Producto` (nombre, categoría, cantidad, precio, fecha de cosecha) con su relación hacia `Agricultor`, y `ProductoRepository` (T2.1, T2.2).
- Impedimentos: fallos con el trabajo en paralelo con las tareas de HU-01 al subir los cambios al repositorio, se resolvio ese mismo dia al entender el problema de sincronización.

**Josué Mindineros (Backend / DevOps)**
- Ayer: Revisar la HU-04 (filtro por categoría y municipio) y leer el Product Backlog para entender el alcance de la historia.
- Hoy: Analizar la HU-04 (filtro por categoría y municipio) y revisar el Pull Request de Julian en cuanto esté disponible. No inicia código todavía porque el filtro depende de que exista la entidad `Producto` de HU-02.
- Impedimentos: Ninguno reportado.

**Yenni Obando (QA / Documentador)**
- Ayer: No participó en el desarrollo por no contar con un computador disponible.
- Hoy: Enfocarse en ver errores de la documentacion y dar sugerencias de cambios del backlog y spring goal con la descripcion de las tareas y sus atributos ISO 25010.
- Impedimentos: Falta de equipo de cómputo propio para el sprint.

---

## Daily — Lunes 28 de septiembre de 2026
*(HU-01 avanza hacia su cierre; HU-02 continúa en paralelo con T2.3 y T2.4)*

**Julian Mina (Scrum Master)**
- Ayer: Completó T1.1–T1.4 (entidad, repositorio, DTOs y servicio de `Agricultor`) y las pruebas unitarias de `AgricultorService`.
- Hoy: Configurar `src/test/resources/application.properties` con H2 para separar la base de pruebas de la real, e implementar el controlador de registro (T1.5) con su configuración de seguridad.
- Impedimentos: El pipeline de JaCoCo falló localmente (cobertura de 6 %) por falta de pruebas en test, se resolvio al implementar las pruebas faltantes.

**Lesly Quintero (Product Owner)**
- Ayer: Terminó T2.1 y T2.2 (entidad `Producto` y `ProductoRepository`).
- Hoy: Definir los DTOs de publicación (T2.3) con las validaciones de cantidad y precio mayores a 0, e implementar `ProductoService` (T2.4) con la validación de fecha de cosecha y de existencia del agricultor, en paralelo con el cierre de HU-01.
- Impedimentos: Ninguno reportado.

**Josué Mindineros (Backend / DevOps)**
- Ayer: Leyó la HU-04 y revisó el avance de HU-01.
- Hoy: Revisar el Pull Request de Julian (T1.1–T1.4) cuando se abra. Continúa sin escribir código, a la espera de que exista la entidad `Producto`.
- Impedimentos: Ninguno reportado.

**Yenni Obando  (QA / Documentador)**
- Ayer: Sin avance por falta de equipo de cómputo.
- Hoy: Revisar pull requests de HU-01 y HU-02, y verificar que la documentación del Product Backlog esté alineada con el Sprint Goal.
- Impedimentos: Continúa sin computador disponible.

*Nota: en este daily se decidió no adoptar JWT para el Sprint 1 y mantener `agricultorId` en el cuerpo de la petición como mecanismo de identificación (T2.6), para no ampliar el alcance ya comprometido de 13 puntos. Se documentó como candidato para un sprint de autenticación futuro.*

---

## Daily — Martes 29 de septiembre de 2026
*(Cierre de HU-01; HU-02 entra en su fase final: controlador y pruebas)*

**Julian Mina (Scrum Master)**
- Ayer: Implementó el endpoint `POST /api/v1/auth/register` (T1.5) y sus pruebas MockMvc (T1.6, casos 201/400/409). Abrió el Pull Request de HU-01 hacia `develop`.
- Hoy: Revisar el Pull Request de HU-01 una vez el pipeline confirme, y apoyar a Lesly con la parte final de HU-02.
- Impedimentos: Ninguno reportado.

**Lesly Quintero (Product Owner)**
- Ayer: Con HU-01 ya cerrada, implementó el endpoint `POST /api/v1/productos` (T2.5), extendió `GlobalExceptionHandler` para los casos 404 y 400, y escribió las pruebas MockMvc de `ProductoController` (T2.7).
- Hoy: Abrir el Pull Request de HU-02 hacia `develop` y verificar que el pipeline pase en verde.
- Impedimentos: Violaciones de Checkstyle por líneas mayores a 120 caracteres en `ProductoPublicacionRequest` y una línea en blanco faltante en `ProductoService`. Resueltas reformateando las anotaciones de validación en líneas separadas.

**Yenni Obando (QA / Documentador)**
- Ayer: No participó en el desarrollo por no contar con un computador disponible.
- Hoy: Revisar pull requests de los ultimos cambios de HU-01 y HU-02.
- Impedimentos: Continua sin computador disponible.

---

## Daily — (Mismo martes 29 de septiembre de 2026, continuación)
*(Con HU-01 y HU-02 cerradas, HU-04 inicia su desarrollo el mismo dia)*

**Josué Mindineros (Backend / DevOps)**

- Ayer: Revisé y definí el alcance de la HU-04, relacionada con el filtrado de productos por categoría y municipio. Analicé la estructura necesaria para implementar el filtro y establecí la consulta que debía utilizarse en `ProductoRepository`, junto con el flujo de procesamiento en `ProductoService`.

- Hoy: Completé la implementación de la HU-04. Se creó la consulta derivada `findByAgricultorMunicipioAndCategoria` en `ProductoRepository` y se desarrolló la lógica de filtrado en `ProductoService`. También se implementó el endpoint `GET /api/v1/productos?categoria=&municipio=`, incluyendo la validación de los parámetros y una respuesta informativa cuando no existen productos que coincidan con los filtros.

Además, cargué datos de prueba mediante `data.sql` y desarrollé pruebas unitarias con JUnit 5 y MockMvc para validar los escenarios con resultados y sin coincidencias. Finalmente, revisé el build del proyecto y verifiqué el cumplimiento de Checkstyle y las pruebas para dejar la HU-04 preparada para su integración con la rama de desarrollo.

- Impedimentos: Durante la implementación fue necesario ajustar el manejo del escenario en el que no existen resultados para los filtros aplicados y validar correctamente los parámetros del endpoint. También se realizaron ajustes menores de estilo y en las pruebas para cumplir con la Definition of Done (DoD), asegurando que el build fuera exitoso, Checkstyle no presentara errores y las pruebas unitarias pasaran correctamente.

---

## Daily — Miércoles 30 de septiembre de 2026

cierre de de documentacion, verificacion de evidencias y revision de las historias de usuario con sus respectivas tareas para el sprint 1.


