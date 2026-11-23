# Retrospectiva — Sprint 2 — AgroValle Connect

**Fecha:** [4 de noviembre de 2026]
**Participantes:** Julian Mina (Scrum Master), Josué Mindineros (Backend/DevOps), Lesly Quintero (Product Owner), Yenni Obando (QA/Documentador)
**Formato:** Qué funcionó bien / Qué se puede mejorar / Acciones para el próximo sprint

## 1. Contexto

El equipo trabajó en el Sprint 2 los prerrequisitos de registro de comprador y roles (T0.1 a T0.5), HU-10 (Pagos en línea), HU-13 (Cancelación del pedido) y HU-05 (Contacto directo), con un integrante a cargo de cada bloque. A lo largo del sprint, las dificultades vinieron principalmente de la integración entre ramas y tareas dependientes, del manejo de errores HTTP y de la verificación tardía del build, más que de la lógica de negocio en sí.

## 2. Qué funcionó bien

- Las historias se dividieron en tareas pequeñas, con estimación, responsable y Definition of Done claro, lo que permitió avanzar en un orden lógico (entidades, repositorios, servicios, endpoints y pruebas) y integrar cada pieza por separado.
- El código nuevo siguió el estilo y la estructura de capas de las clases existentes (por ejemplo, `Comprador` se construyó sobre la base de `Agricultor`), lo que facilitó la revisión y evitó impedimentos al arrancar.
- Checkstyle, Husky y JaCoCo detectaron problemas antes de que el código llegara a `develop`, y se mantuvo la convención del equipo: ramas por historia, commits semánticos y Pull Request hacia `develop`.
- Al escribir el registro de comprador se encontró y corrigió un hueco de seguridad del Sprint 1: `AgricultorService` no validaba la contraseña al reutilizar una cuenta existente. La corrección se hizo en un commit separado, más fácil de revisar y de revertir. Además, la contraseña se valida antes de revisar duplicados, por lo que quien no la tiene siempre recibe 401 y no puede averiguar si una identificación ya es comprador o agricultor.
- Se aplicaron acciones de la retrospectiva anterior: la mayoría de las pruebas se escribieron junto con el código de cada tarea y se agruparon en el mismo commit, y los commits de HU-13 se ordenaron para que cada uno compile por separado.
- El patrón Strategy en los medios de pago permite agregar nuevos medios sin modificar `PagoService`, y la autorización con `@PreAuthorize` queda lista para reutilizarse por rol en otros endpoints.
- Las reglas de propiedad se aplicaron de forma consistente: la cancelación reutiliza la validación de que el pedido pertenezca al comprador autenticado, y el listado de contactos filtra por el agricultor autenticado.
- La integrante de QA participó en el desarrollo de código (HU-10), cerrando la preocupación del Sprint 1 sobre la disponibilidad de equipo de cómputo.

## 3. Qué se puede mejorar

- **Seguridad no detectada a tiempo:** el registro de agricultor del Sprint 1 permitía agregar un rol a la cuenta de otra persona conociendo solo su identificación, y la revisión del Pull Request no lo detectó.
- **Manejo de errores incompleto, repetido en dos historias:** `PedidoInvalidoException` y `RecursoNoEncontradoException` se crearon sin registrar su manejador en `GlobalExceptionHandler`, por lo que los errores 400 y 404 habrían salido como 500. Ocurrió en HU-10 y de nuevo en HU-13, lo que indica que la acción equivalente no quedó cerrada en `develop`.
- **Pruebas y cobertura al final:** en HU-10 las pruebas se dejaron para el cierre y JaCoCo bloqueó el build al abrir el Pull Request, el mismo problema de cobertura del Sprint 1.


## 4. Acciones para el próximo sprint

| Acción | Responsable | Criterio de éxito |
|---|---|---|
| Hacer `git pull origin develop` al empezar cada tarea y antes de cada push | Todo el equipo | Cero push rechazados |
| Correr `mvnw verify` completo (sin saltar JaCoCo ni Checkstyle) y revisar `git status` antes de abrir cada Pull Request | Todo el equipo | CI en verde al abrir el PR, sin archivos ajenos a la tarea |
| Escribir las pruebas de cada tarea en el mismo commit que el código | Quien implementa | Cobertura estable sobre 60 % |
| Al crear una excepción nueva, registrar su manejador en `GlobalExceptionHandler` en el mismo commit y probar el código HTTP | Quien implementa | Prueba de 400 y 404 en cada endpoint nuevo |
| Aclarar con el Product Owner las reglas de negocio faltantes (stock, estados repetidos, pagos pendientes) antes de empezar la historia | Lesly (Product Owner) | Criterios de aceptación completos al iniciar el sprint |
| Separar los cambios correctivos en commits propios y limitar cada PR a los archivos de su tarea (leer la subissue, el DER y las clases relacionadas antes de editar) | Quien implementa | Un commit por propósito (`feat`, `fix`) y PR sin archivos ajenos |


## 5. Conclusión

El equipo concluyó que la mayor parte del tiempo perdido durante el Sprint 2 no provino de la complejidad de las historias en sí, sino de la integración entre tareas y ramas, del manejo de errores HTTP que se repitió en dos historias y de verificar el build y la seguridad demasiado tarde. Para el Sprint 3, el equipo se propone correr el build completo antes de cada PR, acordar el orden de integración y las reglas de negocio desde el Sprint Planning, y revisar con una lista de control los flujos de seguridad, de forma que el tiempo de desarrollo se concentre en la lógica de negocio.
