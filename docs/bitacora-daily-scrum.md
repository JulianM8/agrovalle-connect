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

---

## Daily — Jueves 8 de octubre de 2026 (posterior al cierre formal del Sprint 1)
*(Revisión de la decisión de T2.6 y migración a jwt posterior al cierre del sprint)*



- Ayer: Con el Sprint 1 ya cerrado y entregado, el equipo revisó la decisión tomada el 28
  de septiembre de mantener `agricultorId` en el cuerpo de la petición para identificar al
  agricultor al publicar un producto.

- Hoy: todo el equipo participó en la migración a autenticación JWT: se agregó el campo `contrasena` (cifrado con BCrypt)
  a `Agricultor` y al registro, implementó `JwtService` y `AuthService` con el endpoint
  `POST /api/v1/auth/login` y creó
  `JwtAuthenticationFilter`. Actualizó `ProductoController`/`ProductoService` para tomar el
  agricultor del token en vez del cuerpo, y reescribió las pruebas afectadas
  (`AgricultorServiceTest`, `AuthServiceTest`, `AgricultorControllerTest`,
  `ProductoServiceTest`, `ProductoControllerTest`, `ProductoRepositoryTest`).
- Impedimentos: algunas fallas en los test por una mala implementacion de los cambios o logica faltante en la migracion a JWT, se resolvieron al revisar el flujo de autenticación y la inyección del token en los test.

**Motivo del cambio:** mejorar la seguridad de la aplicación al implementar un sistema de autenticación basado en tokens JWT, ademas de ser un requisito importante para la aplicación.

- En esta misma seccion se planteo la actualizacion de la documentacion entre todo el equipo para reflejar los cambios en la autenticación y el flujo de uso de la API.

# Bitácora de Daily Scrum — Sprint 2 — AgroValle Connect


## Daily — Sabado 31 de octubre de 2026

**Julian Mina (Scrum Master) — prerrequisitos (registro de comprador y roles)**

- **Ayer:** Implementó la entidad `Comprador` con relación uno a uno con `Usuario`, dirección de entrega y coordenadas opcionales, además de `CompradorRepository`. Creó los DTOs de registro con validaciones, `CompradorService` y el endpoint `POST /api/v1/auth/register/comprador`. El servicio reutiliza una cuenta existente cuando la identificación ya pertenece a un usuario, agrega el rol `COMPRADOR` y valida primero la contraseña. También activó la seguridad por método y restringió la publicación de productos al rol `AGRICULTOR` mediante `@PreAuthorize`. Escribió pruebas JUnit 5 y MockMvc para el registro y la autorización.
- **Hoy:** Ejecutar `mvnw verify` completo sin omitir JaCoCo, revisar `git status`, subir la rama `feature/sprint2-prerrequisitos-comprador` con dos commits separados (uno para T0.1–T0.5 y otro para la corrección de seguridad en `AgricultorService`) y abrir el Pull Request hacia `develop`, solicitando revisión.
- **Impedimentos:** No se reportaron impedimentos para implementar el registro de comprador, porque se tomó como base la estructura existente de agricultor.
- **Hallazgo adicional:** Durante el trabajo se detectó que `AgricultorService` no validaba la contraseña al reutilizar una cuenta existente. Se corrigió este problema y se ajustaron sus pruebas para evitar que alguien pudiera agregar un rol a una cuenta ajena con solo conocer su identificación.

## Daily — Lunes 2 de noviembre de 2026

**Yenni Obando (QA / Documentador) — HU-10 Pagos en línea (T10.1–T10.11)**

- **Ayer:** Implementó las entidades `Pedido`, `DetallePedido` y `Pago`, sus enums de estado y los repositorios correspondientes. También creó los DTOs con validaciones y `PedidoService`, incluyendo validaciones de producto y cantidad disponible, la regla de un solo agricultor por carrito y el cálculo del total. Implementó el patrón Strategy para los medios de pago (Tarjeta, PSE y Contraentrega), `PagoService` y los endpoints para crear pedidos y consultar/registrar pagos.
- **Hoy:** Escribir y subir las pruebas JUnit 5 y MockMvc de pedido y pago (T10.10), ejecutar `mvnw verify` completo para alcanzar el 60 % de cobertura de JaCoCo y abrir el Pull Request de T10.8 y T10.9 hacia `develop`, solicitando revisión.
- **Impedimentos:** El CI falló por cobertura de JaCoCo inferior al 60 %, debido a que todavía faltaban las pruebas de pedido y pago.
- **Otros avances y dificultades reportados:** Corrigió errores de compilación causados por diferencias entre las clases locales y las subidas (`PedidoResponse`, `PagoResponse` y `MedioPagoStrategy`). También resolvió un rechazo de `push` integrando los cambios remotos de la rama.

---

## Daily — Martes 3 de noviembre de 2026

**Lesly Quintero (Product Owner) — HU-13 Cancelación del pedido (T13.1–T13.5)**

- **Ayer:** Implementó `PedidoService.cancelar`, permitiendo cancelar pedidos en estado `PENDIENTE` o `CONFIRMADO`, guardar el motivo y cambiar el estado a `CANCELADO`. Validó que el pedido perteneciera al comprador autenticado y agregó el listado de pedidos del agricultor. Implementó el reembolso de pagos completados, expuso `POST /api/v1/pedidos/{id}/cancelacion` para el rol `COMPRADOR` y registró las excepciones de pedido inválido y recurso no encontrado en `GlobalExceptionHandler`. También escribió pruebas JUnit 5 y MockMvc para los escenarios de cancelación, reembolso, validaciones, roles y autenticación.
- **Hoy:** Ejecutar `mvnw verify` completo con JaCoCo y Checkstyle, corregir los errores que aparezcan, hacer `git pull origin develop`, subir los cuatro commits semánticos a la rama `feature/HU-13-cancelacion-pedido` y abrir el Pull Request hacia `develop` para solicitar revisión.
- **Impedimentos:** El código todavía no se había compilado ni probado en su máquina, por lo que estaba pendiente confirmar el resultado de `mvnw verify`. Las tareas de cancelación y reembolso están acopladas y requieren acordar el orden de integración con el equipo. El cambio en el constructor de `PedidoService` obligó a actualizar sus pruebas.
---

**Josué Mindineros (Backend / DevOps) — HU-05 Contacto directo (T5.1–T5.9)**

- **Ayer:** Mapeó la entidad `Contacto` según el DER y creó `ContactoRepository`. Definió los DTOs y sus validaciones, implementó `ContactoService` y los endpoints para registrar solicitudes de contacto y responderlas. Publicó `ContactoRegistradoEvent`; el alcance implementado permite consultar las solicitudes en la aplicación, sin envío de correos ni notificaciones push. Añadió pruebas JUnit 5 y MockMvc, e implementó `GET /api/v1/contactos` con el filtro opcional `?pendientes=true`, restringido al agricultor autenticado.
- **Hoy:** Completó todas las tareas asignadas de HU-05 (T5.1–T5.9). T5.9 quedó subida a GitHub y fue revisada por un compañero, quien confirmó que la funcionalidad opera correctamente.
- **Impedimentos:** Al inicio fue necesario ajustar el alcance y la ubicación de algunos archivos para seguir el patrón del repositorio; el PR final de T5.1 quedó limitado a `Contacto.java`. Las pruebas del listado se completaron junto con T5.9 porque el endpoint GET todavía no existía al trabajar en T5.8. La verificación automatizada se realizó con H2; no se ejecutó una comprobación contra una instancia real de PostgreSQL.
- **Verificación reportada:** La última ejecución local de T5.9 terminó con `BUILD SUCCESS`, 74 pruebas aprobadas, cero errores de Checkstyle y cobertura de JaCoCo aprobada.

---

## Daily — Miércoles 4 de noviembre de 2026

cierre de de documentacion, verificacion de evidencias y revision de las historias de usuario con sus respectivas tareas para el sprint 2.



