# Retrospectiva — Sprint 3 — AgroValle Connect

**Fecha:** [16 de noviembre de 2026]
**Participantes:** Julian Mina (Scrum Master), Josué Mindineros (Backend/DevOps), Lesly Quintero (Product Owner), Yenni Obando (QA/Documentador)
**Formato:** Qué funcionó bien / Qué se puede mejorar / Acciones para el próximo sprint

## 1. Contexto

El equipo trabajó en el Sprint 3 con dos historias de usuario (HU-06 Asignación de vehículo de transporte y HU-07 Programación de rutas de despacho, 10 Story Points), los prerrequisitos del rol Transportista (T0.6–T0.9) y las tareas de Despliegue Continuo a Staging (T03.1–T03.5). A diferencia de los sprints anteriores, parte de las dificultades vino de la infraestructura de despliegue (perfil de Spring, formato de conexión a Neon, Docker en Render) y de la sincronización entre ramas, más que de la lógica de negocio.

## 2. Qué funcionó bien

- Las historias estaban divididas en tareas pequeñas, con estimación, responsable y Definition of Done claro, lo que permitió avanzar en orden lógico (entidades, repositorios, servicios, endpoints, datos de prueba y pruebas).
- El patrón Strategy en HU-07 (`OrdenEntregaStrategy` con `PorMunicipioStrategy` y `VecinoMasCercanoStrategy`) permite agregar criterios de ordenamiento nuevos sin modificar `RutaService`, y quedó documentado en `docs/diseno/patrones.md`.
- La notificación al comprador con `ApplicationEventPublisher` (`AsignacionRegistradaEvent`, patrón Observer) deja listo el punto para enviar correo o push después sin modificar `DespachoService`, igual que en el contacto de HU-05.
- Se reutilizaron las excepciones y patrones de los sprints anteriores (`RecursoNoEncontradoException`, `PedidoInvalidoException`, principal del JWT como `Long`, registro con reutilización de cuenta y validación de contraseña), lo que mantuvo el código consistente.
- Se detectó a tiempo un error de orden en las validaciones de la asignación: un pedido ya asignado queda `EN_TRANSPORTE`, así que la regla de "pedido ya asignado" (409) debe validarse antes que la de "pedido no confirmado" (400).
- Se creó `DespachoController` aparte en lugar de ampliar `PedidoController`, evitando romper las pruebas existentes.
- Desacoplar la configuración con variables de entorno permitió usar el mismo artefacto en distintos ambientes sin guardar credenciales en el código, y la cláusula `needs: verify-and-test` garantiza que solo se despliega si el CI aprueba compilación, Checkstyle, pruebas y cobertura.
- Leer los logs de Render permitió encontrar la causa real del fallo del primer despliegue (perfil `staging` sin activar).
- Se siguió la convención del equipo: rama por historia, commits semánticos con el ID de la tarea y Pull Request hacia `develop`. Las decisiones de despliegue quedaron registradas en el ADR-001.

## 3. Qué se puede mejorar

- **Excepciones no previstas:** no existía una excepción para el 409, por lo que fue necesario crear `ConflictoException` y registrarla en `GlobalExceptionHandler`. Debió preverse al planificar T6.4 y T6.7.
- **Dependencia entre tareas:** T6.5 y T6.8 modifican archivos creados en T6.4 y T6.6 (`VehiculoService`, `VehiculoController`, `DespachoService`), por lo que al hacer `git add` por tarea hubo que elegir los archivos con cuidado para que cada commit compilara por sí solo.
- **Pruebas en un commit aparte:** en HU-06 las pruebas quedaron en un commit final (T6.9) y no junto al código de cada tarea, como pide la Definition of Done y como se acordó en la retrospectiva del Sprint 1.
- **Perfil de despliegue sin activar:** la aplicación se caía en Render con "Connection to localhost:5432 refused" porque faltaba `SPRING_PROFILES_ACTIVE=staging`; debió verificarse el nombre y el guardado de las variables antes de desplegar.
- **Script copiado con error:** el encabezado de `schema.sql` quedó dañado al pegarlo en el SQL Editor; debió revisarse antes de ejecutarlo.
- **Paso redundante en el pipeline:** `./mvnw jacoco:check` por separado repetía la verificación que ya hace `verify`; debió revisarse el `pom.xml` antes de copiar el YAML de la guía.

## 4. Acciones para el próximo sprint

| Acción | Responsable | Criterio de éxito |
|---|---|---|
| Incluir las pruebas en el mismo commit que el código de cada tarea | Todo el equipo | Cada commit de tarea trae su test |
| Al planificar, listar las excepciones nuevas y sus códigos HTTP (por ejemplo 409) antes de empezar los endpoints, y revisar `GlobalExceptionHandler` al crear una excepción | Quien implementa | Handler registrado y prueba de 400 y 404 en cada endpoint |
| Actualizar `schema.sql` y la base de staging en la misma historia que crea las tablas | Quien crea la entidad | Tablas presentes en staging antes de desplegar el sprint |
| Hacer `git pull --rebase` al empezar cada tarea y antes de cada push; avisar en el daily antes de subir a una rama compartida | Todo el equipo | Cero push rechazados |
| Usar `git add` con rutas específicas y revisar `git status` antes de cada commit; ejecutar los comandos en líneas separadas en PowerShell | Todo el equipo | Ningún archivo ajeno o de otra tarea en los commits; sin comandos pegados |
| Correr `mvnw verify` completo (sin saltar JaCoCo) antes de abrir cada PR | Todo el equipo | CI en verde al abrir el PR |
| Revisar las variables de entorno de la PaaS contra `application-staging.yml` antes del primer despliegue | Quien despliega | El log muestra "The following 1 profile is active: staging" |
| Guardar credenciales solo en GitHub Secrets y en las variables de la PaaS, y rotar las que se hayan expuesto | Todos / DevOps | Ninguna clave visible en el repositorio, historial o capturas; contraseña nueva en Neon, GitHub y Render con el servicio en Live |

## 5. Conclusión

El equipo concluyó que el Sprint 3 entregó la asignación de vehículos y la programación de rutas con patrones de diseño reutilizables (Strategy y Observer), además del primer despliegue continuo a Staging. Las pérdidas de tiempo vinieron de detalles evitables: excepciones no previstas, ramas desincronizadas, configuración del despliegue y un esquema de base de datos que no se actualizó junto con las entidades.