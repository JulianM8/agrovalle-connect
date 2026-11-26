# ADR-002: Estrategia de Despliegue Rolling con Promoción Controlada a Producción

* **Estado:** Propuesto
* **Fecha:** 19 de noviembre de 2026
* **Autores:** Julian Mina, Josué Mindineros, Lesly Quintero, Yenni Obando
* **Contexto Técnico:** Asignatura Ingeniería de Software II - UNIAJC
* **Relacionado con:** ADR-001 (Adopción de Plataforma PaaS y GitHub Actions para Despliegue Continuo en Staging)

## Contexto y Problemática

El ADR-001 resolvió el despliegue automático de cada integración en `main` hacia un ambiente de Staging en la nube (Render y Neon, con el perfil `application-staging.yml` y las credenciales en GitHub Secrets). Staging sirve para validar, pero no está pensado para usuarios reales.

Antes de entregar AgroValle Connect hace falta definir cómo se publica una versión en **Producción**, de modo que:

* una versión defectuosa no llegue a los usuarios solo por haberse integrado en `main`;
* se pueda volver a la versión anterior de forma rápida y documentada;
* los datos y las credenciales de producción estén aislados de los de pruebas;
* la actualización cause la menor interrupción posible del servicio.

Restricciones: equipo de cuatro personas sin personal de operaciones, presupuesto mínimo, una plataforma (Render) que no permite repartir el tráfico por porcentaje entre versiones, y un esquema de base de datos que hoy se mantiene a mano con `src/main/resources/db/migration/schema.sql`, sin herramienta de migraciones.

## Opciones Consideradas

1. **Blue-Green Deployment**: mantiene dos entornos idénticos (uno activo y otro pasivo) y conmuta el tráfico al instante, con cero tiempo de inactividad y reversión inmediata. Exige duplicar la infraestructura completa y un enrutador que cambie el tráfico, algo que la PaaS elegida no ofrece sin servicios adicionales. Además, las dos versiones seguirían compartiendo una sola base de datos, por lo que el riesgo de los cambios de esquema no desaparece. Su costo es excesivo para el alcance del proyecto.
2. **Canary Deployment**: entrega la versión nueva a un porcentaje reducido de usuarios (por ejemplo 5 % o 10 %) antes del despliegue total. Requiere un balanceador inteligente y monitoreo continuo por versión, y el volumen de usuarios del proyecto no permite sacar conclusiones de una muestra tan pequeña.
3. **Rolling Deployment con promoción controlada**: la plataforma reemplaza la instancia de forma progresiva (arranca la versión nueva, espera a que pase la verificación de salud y solo entonces retira la anterior), sin entornos duplicados. La liberación se controla con una etiqueta de versión y una aprobación manual. Durante el reemplazo coexisten brevemente dos versiones de la aplicación, por lo que los cambios de esquema deben ser compatibles hacia atrás.
4. **Despliegue continuo directo a Producción desde `main`**: es el flujo más simple, pero no hay punto de control humano ni validación previa en un ambiente real, y no separa "integrar" de "liberar".

## Decisión Adoptada

Se aprueba la **Opción 3: Rolling Deployment con promoción controlada**, con las siguientes reglas.

1. **Flujo de promoción.** `feature/*` → Pull Request a `develop` (CI) → `main` → despliegue automático a Staging (ADR-001) → etiqueta `vX.Y.Z` sobre el commit ya validado en Staging → pipeline de producción → aprobación manual → Producción.
2. **Pipeline `.github/workflows/cd.yml`.** Se dispara con las etiquetas `v*.*.*`. Tiene dos jobs encadenados con `needs`: `verify-and-test` (`./mvnw clean verify`: compilación, Checkstyle, pruebas JUnit y cobertura JaCoCo ≥ 60 %) y `deploy-to-production`, asociado a un GitHub Environment `production` con revisor obligatorio. Este último invoca el Deploy Hook del servicio de producción y consulta `GET /api/v1/estado` hasta recibir 200 OK; si no lo recibe en el plazo definido, el job falla.
3. **Aislamiento de ambientes.** Producción tiene su propio servicio en Render, su propia base de datos (`agrovalle_prod` en Neon), el perfil `application-prod.yml` (activado con `SPRING_PROFILES_ACTIVE=prod`) y secretos propios en el Environment `production`: `PROD_DB_URL`, `PROD_DB_USER`, `PROD_DB_PASSWORD`, `PROD_JWT_SECRET_KEY` y `PROD_DEPLOY_HOOK_URL`. La llave JWT de producción es distinta de la de Staging. Ninguna credencial se escribe en el código ni en archivos `application*.yml`.
4. **Base de datos.** En producción Hibernate usa `ddl-auto=validate`. Los cambios de esquema se aplican con un script versionado **antes** del despliegue y deben ser compatibles hacia atrás (primero se agregan tablas o columnas; el borrado o renombrado se hace en una versión posterior), de modo que las dos versiones que coexisten durante el reemplazo y el retroceso funcionen con el mismo esquema.
5. **Retroceso.** Si la verificación posterior falla o se detecta un error grave, se redespliega la versión anterior desde el historial de despliegues de Render, o se vuelve a ejecutar el pipeline con la etiqueta anterior. Criterios de retroceso: `/api/v1/estado` distinto de 200, errores 5xx en los registros o fallo de los flujos críticos (registro, login y creación de pedido).
6. **Verificación posterior al despliegue.** Prueba de humo con la colección de Postman del proyecto (estado, registro y login, consulta de productos) sobre la URL de producción, con datos nuevos. El resultado se anota en la descripción de la etiqueta de versión.

## Consecuencias y Mantenibilidad

* **Consecuencias Positivas**:
  * Hay un punto de control humano antes de llegar a los usuarios, y a Producción solo llegan versiones ya probadas en Staging.
  * No se duplica infraestructura: se reutilizan el Dockerfile, el pipeline y las convenciones del ADR-001, con un costo bajo.
  * Cada versión queda identificada por una etiqueta, lo que facilita saber qué hay en producción y volver a una anterior.
  * Los datos y las credenciales de producción quedan aislados de los de pruebas.
  
* **Consecuencias Negativas / Limitaciones**:
  * El proceso depende de personas: una aprobación apresurada o una prueba de humo incompleta puede dejar pasar errores. Se mitiga con una lista de verificación en cada etiqueta.
  * El retroceso toma minutos, no es instantáneo como en Blue-Green, y solo es seguro si los cambios de esquema respetan la compatibilidad hacia atrás.
  * Producción requiere un plan sin suspensión por inactividad, con costo mensual, para evitar el arranque en frío que se aceptó en Staging.
  * Mientras no exista una herramienta de migraciones (por ejemplo Flyway), el script de esquema manual sigue siendo un punto de error humano.
  * No hay monitoreo ni alertas automáticas; la detección de fallos depende de la verificación de salud y de los registros de la plataforma.
