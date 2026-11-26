# Retrospectiva — Sprint 4 — AgroValle Connect

**Fecha:** [25 de noviembre de 2026]
**Participantes:** Julian Mina (Scrum Master), Josué Mindineros (Backend/DevOps), Lesly Quintero (Product Owner), Yenni Obando (QA/Documentador)
**Formato:** Qué funcionó bien / Qué se puede mejorar / Acciones para el próximo sprint


## 1. Contexto

El equipo trabajó en el Sprint 4 con tres historias comprometidas: HU-08 Seguimiento del envío (3 pts), HU-09 Confirmación de recepción de la mercancía (3 pts) y HU-15 Reporte de inconformidad o producto dañado (5 pts), 11 Story Points en total, que cierran el tramo del pedido desde el despacho hasta la recepción.

Las dificultades del sprint no vinieron de la lógica de negocio sino del flujo de trabajo: dependencias entre pruebas que comparten base de datos, reglas de estilo de Checkstyle, el manejo de ramas en Git.

## 2. Qué funcionó bien

- Las historias estaban divididas en tareas pequeñas, con estimación, responsable y Definition of Done claro, lo que permitió avanzar en un orden lógico en cada una: modelo, repositorio, reglas de negocio, endpoints, evento y pruebas.
- Se aplicaron acciones de la retrospectiva del Sprint 3: en HU-08 las pruebas quedaron en el mismo commit que el código, no hicieron falta excepciones nuevas (se reutilizaron las de los sprints anteriores) y se usó `git add` con rutas específicas.
- Se reutilizaron piezas ya construidas: `obtenerPedidoDelComprador` (404 si el pedido no existe o es de otro comprador), el bloqueo de pedidos, las excepciones y el patrón de eventos. Esto mantuvo el código consistente en las tres historias.
- El patrón Observer quedó como punto de extensión sin modificar los servicios: `InconformidadRegistradaEvent` en HU-15 y `RecepcionConfirmadaEvent` con su listener (que se ejecuta después del commit) en HU-09, que HU-11 reemplazará por la liberación del pago.
- La recepción es segura ante confirmaciones simultáneas gracias al bloqueo pesimista (`findByIdParaActualizar`), y la regla de negocio conecta las historias: no se puede confirmar la recepción si hay una inconformidad en revisión.
- Comparar los registros del CI permitió encontrar la causa real de las fallas de HU-15: `ProductoRepositoryTest` dependía del orden de ejecución. Cuando `PagoConcurrenciaTest` corría primero dejaba dos Aguacates en la misma H2 y el filtro por Palmira y Frutas devolvía tres. El problema se resolvió de forma independiente en el PR #218, con una base H2 propia para la prueba de concurrencia.
- Por primera vez se validó una historia contra PostgreSQL real, en la rama de pruebas de Neon: la inconformidad se registró y se consultó, y los datos del pago se conservaron (`BUILD SUCCESS`).
- Se detectó a tiempo que el SQL versionado y T15.1 discrepaban en los estados de la inconformidad (`ABIERTA` frente a `EN_REVISION`, `RESUELTA` y `RECHAZADA`) y se alinearon antes de integrar.
- En HU-08 se detectó que la fecha estimada de llegada nunca se llenaba en el sistema, y se corrigió dentro de la misma historia (T8.2) en lugar de dejar el seguimiento siempre sin fecha. Lesly gestionó el tablero y revisó los cambios, lo que dio una revisión independiente antes del Pull Request.

## 3. Qué se puede mejorar

- **Convención de ramas:** en HU-15 se abrieron ramas por bloque de tareas, lo que dispersó la revisión y obligó a retomar T15.7 en la rama original.
- **Pruebas que dependen del orden:** las pruebas que comparten la misma H2 y su contexto pueden pasar o fallar según el orden en que corran. 
- **Dato no previsto en el planning:** la fecha estimada de llegada no la llenaba ninguna parte y HU-08 tuvo que modificar código de HU-06. 
- **Checkstyle por la misma regla:** `EmptyLineSeparator` (línea en blanco entre campos) falló en HU-15 y en HU-09, y es la misma clase de error que ya apareció en el Sprint 1.
- **Conexión de pruebas con Neon:** el primer intento falló por metadatos JDBC; funcionó con la URL JDBC directa con SSL.


## 4. Acciones para el próximo sprint

| Acción | Responsable | Criterio de éxito |
|---|---|---|
| Trabajar con una sola rama por historia y agregar commits por bloques coherentes | Todo el equipo | Todos los cambios de cada historia se suben desde esa rama |
| Aislar los datos de las pruebas de integración y de concurrencia (base H2 propia) y verificar la suite con un orden distinto cuando compartan contexto o base | Quien escriba pruebas de integración | Las pruebas de catálogo y de pagos pasan en ambos órdenes |
| Completar `schema.sql` y actualizar la base de Staging en el mismo sprint que crea las tablas | Quien crea la entidad | Tablas presentes en Staging antes de desplegar el sprint |
| Mantener el tablero alineado con los commits: dejar las pruebas dentro de la tarjeta de código que prueban, o indicar en la tarjeta de pruebas en qué commit quedaron | Lesly (Product Owner) | Cada tarjeta indica en qué commit quedó |
| Avisar al equipo de HU-11 del listener temporal de recepción (solo deja traza en el log) para que lo reemplacen por la liberación del pago | Yenni | Aviso hecho antes del merge de HU-09 |

## 5. Conclusión

El equipo concluyó que el Sprint 4 avanzó el flujo del pedido hasta la recepción con tres historias que se apoyan entre sí (seguimiento, inconformidad y confirmación de recepción) y con el patrón Observer listo para la liberación del pago de HU-11. La mayor parte del tiempo perdido no provino de la lógica de negocio sino del flujo de trabajo: pruebas que dependían del orden, reglas de estilo. Para el Sprint 5, el equipo se propone anticipar esos puntos desde el Sprint Planning y en el flujo de cada tarea.
