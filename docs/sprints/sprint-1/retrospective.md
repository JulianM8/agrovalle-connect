# Retrospectiva — Sprint 1 — AgroValle Connect

**Fecha:** [30 de septiembre de 2026]
**Participantes:** Julian Mina (Scrum Master), Josué Mindineros (Backend/DevOps), Lesly Quintero (Product Owner), Yenni Obando (QA/Documentador)
**Formato:** Qué funcionó bien / Qué se puede mejorar / Acciones para el próximo sprint

## 1. Contexto

El equipo cerró el Sprint 1 con tres historias comprometidas (HU-01, HU-02 y HU-04, 13 Story Points en total) y un tablero Kanban de cinco columnas con límite de trabajo en progreso. A lo largo del sprint, el equipo enfrentó dificultades principalmente en la configuración del entorno de desarrollo y en el cumplimiento de las reglas automatizadas de calidad, más que en el diseño de la lógica de negocio en sí.

## 2. Qué funcionó bien

- El equipo estableció desde el inicio una separación clara por capas (`model`, `repository`, `service`, `controller`, `dto`, `exception`), lo que permitió avanzar en paralelo sin choques significativos de código entre integrantes.
- La protección de la rama `develop`, configurada para exigir que el pipeline de GitHub Actions pase antes de fusionar, evitó que código sin pruebas o con errores de estilo llegara a la rama principal.
- Ante una ambigüedad sobre el alcance de HU-01 (si debía cubrir solo agricultores o también compradores y transportistas), el equipo consultó directamente con la docente antes de continuar el desarrollo, evitando construir sobre un supuesto incorrecto.
- La decisión sobre el mecanismo de identificación del agricultor en HU-02 (T2.6) se tomó de forma consciente, evaluando el costo de implementar JWT frente al alcance ya comprometido del sprint, y quedó documentada para revisarse en un sprint futuro en lugar de improvisarse a mitad de una tarea.

## 3. Qué se puede mejorar

- **Configuración de entorno:** el equipo perdió tiempo resolviendo errores de conexión a PostgreSQL causados por una variable de entorno (`DB_PASSWORD`) definida en una terminal distinta a la que ejecutaba la aplicación. 
- **Separación de configuraciones de prueba y de aplicación real:** en más de una ocasión, la configuración de la base de datos de pruebas (H2) y la de la base de datos real (PostgreSQL) se mezclaron en un mismo archivo, lo que habría hecho que las pruebas dependieran de una base de datos externa en lugar de ser autocontenidas.
- **Cobertura de pruebas como parte del flujo, no como paso final:** el equipo experimentó fallas repetidas del pipeline por cobertura insuficiente (JaCoCo) al intentar subir código de una historia sin sus pruebas correspondientes. Esto ocurrió porque las pruebas se escribieron después de completar toda la lógica, en lugar de en paralelo con cada tarea.
- **Convenciones de Git:** se presentaron errores derivados de comandos mal formados (fusión accidental de instrucciones al copiar varias líneas a la vez) y de una identidad de Git sin configurar en al menos una máquina del equipo, lo que retrasó la primera contribución de esa persona.
- **Disponibilidad de equipo de cómputo:** una integrante (Yenni Obando, responsable de QA) no pudo participar en la escritura de código durante el sprint por no contar con un computador disponible. Esto concentró la responsabilidad de pruebas en menos personas de las planeadas y evidenció que el equipo no tenía un plan de respaldo ante la falta de un recurso básico de un integrante.
- **Reparto de tareas ajustado sobre la marcha:** el reparto original de HU-02 y HU-04 se invirtió durante la ejecución (Josué terminó implementando HU-04 en lugar de HU-02, y Lesly HU-02 en lugar de HU-04), sin que quedara documentado el motivo del cambio en el momento en que ocurrió, lo que dificulta rastrear después por qué se tomó esa decisión.


## 4. Acciones para el próximo sprint

| Acción | Responsable | Criterio de éxito |
|---|---|---|
| Escribir la prueba unitaria de cada tarea en el mismo Pull Request que su lógica, no en un PR posterior | Todo el equipo | Ningún Pull Request de una historia se abre sin su cobertura correspondiente |
| Confirmar la identidad de Git (`user.name`, `user.email`) de cada integrante antes del inicio del siguiente sprint | Julian (Scrum Master) | Los 4 integrantes tienen commits propios visibles en el historial desde el primer día del sprint |
| Validar la capacidad del equipo (Story Points por sprint) contra cualquier restricción dada por la docente antes del Sprint Planning, no durante la ejecución | Lesly (Product Owner) | El Sprint Goal y los puntos comprometidos quedan confirmados en la planificación, sin ajustes retroactivos |
| Definir un plan de respaldo para cuando un integrante no cuente con equipo de cómputo (por ejemplo, acceso remoto a la máquina de otro integrante o uso de equipos de la universidad) | Julian (Scrum Master) | Ningún integrante queda fuera del desarrollo por falta de equipo durante un sprint completo |
| Registrar en el tablero (Issue o comentario) cualquier cambio de responsable de una tarea en el momento en que ocurre, junto con el motivo | Todo el equipo | Cada tarea del tablero refleja quién la ejecutó realmente y por qué cambió, sin depender de la memoria del equipo al cierre del sprint |

## 5. Conclusión

El equipo concluyó que la mayor parte del tiempo perdido durante el Sprint 1 no provino de la complejidad de las historias de usuario en sí, sino de la configuración del entorno y del cumplimiento tardío de las reglas de calidad automatizadas. Para el Sprint 2, el equipo se propone anticipar estos puntos desde el Sprint Planning, de forma que el tiempo de desarrollo se concentre en la lógica de negocio.






