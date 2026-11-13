# ADR-001: Adopción de Plataforma PaaS y GitHub Actions para Despliegue Continuo (CD) en Staging

* **Estado:** Aprobado
* **Fecha:** 2026-11-12
* **Autores:** Célula de Desarrollo AgroValle Connect
* **Contexto Técnico:** Asignatura Ingeniería de Software II - UNIAJC

## Contexto y Problemática

El proyecto integrador AgroValle Connect (backend en Java 17 / Spring Boot) se ha ejecutado hasta ahora únicamente en las máquinas locales de desarrollo. Con la Integración Continua (CI) ya consolidada (compilación con Maven Wrapper, Checkstyle, pruebas unitarias con JUnit 5 y Mockito, y cobertura JaCoCo mayor o igual al 60%), el equipo requiere transicionar a un ambiente de **Staging** (preproducción) ejecutable en la nube.

Se necesita una solución que:

* Se integre con el pipeline de GitHub Actions y publique incrementos funcionales de manera continua.
* No genere costos de infraestructura ni exija la administración manual de servidores.
* Mantenga las credenciales fuera del código fuente y del historial del repositorio.
* Ofrezca una URL pública para la validación del Product Owner y las pruebas de integración.

## Opciones Consideradas

1. **Despliegue manual en servidor VPS**: ofrece control completo del sistema operativo, pero exige configuraciones manuales complejas, gestión de parches de seguridad y scripts de despliegue propensos al error humano. Además, implica un costo mensual.
2. **Despliegue automatizado en plataforma PaaS (Render) con base de datos PostgreSQL gestionada (Neon)**: permite integración mediante Webhooks de despliegue, ejecución del contenedor Spring Boot en Java 17 sin administrar el sistema operativo y una base de datos PostgreSQL gestionada, con capa gratuita disponible para ambos servicios.

## Decisión Adoptada

Se aprueba la **Opción 2: Plataforma PaaS junto a GitHub Actions**, con la siguiente arquitectura:

| Componente | Tecnología | Función |
|---|---|---|
| Control de versiones y CI/CD | GitHub y GitHub Actions | Ejecuta las verificaciones de calidad y dispara el despliegue |
| Aplicación (Staging) | Render (Web Service, plan Free) | Ejecuta el artefacto Spring Boot empaquetado en un contenedor Docker |
| Base de datos (Staging) | Neon (PostgreSQL), base `agrovalle_staging` | Persistencia del ambiente de pruebas, con el esquema de `docs/diseno/schema.sql` |
| Gestión de secretos | GitHub Secrets y variables de entorno de Render | Inyección de credenciales sin exponerlas en el código |

**Flujo de despliegue:**

1. Cada integración a la rama `main` activa el workflow `.github/workflows/cd.yml`.
2. El job `verify-and-test` compila el proyecto y ejecuta `./mvnw clean verify` (Checkstyle, pruebas unitarias y cobertura JaCoCo >= 60%).
3. El job `deploy-to-staging`, condicionado con `needs: verify-and-test`, solo se ejecuta si el job anterior fue exitoso e invoca el Deploy Hook de Render mediante una petición HTTP POST.
4. Render reconstruye la imagen con el `Dockerfile` del repositorio y reinicia el servicio ejecutándolo con el perfil `application-staging.yml`.

**Decisiones complementarias:**

* **Perfil de configuración:** el archivo `src/main/resources/application-staging.yml` no contiene valores fijos; toda la configuración sensible (URL, usuario y contraseña de la base de datos, llave JWT) se lee desde variables de entorno (`DB_URL_VAR`, `DB_USER_VAR`, `DB_PASS_VAR`, `JWT_SECRET_VAR`), y el puerto desde `PORT` (por defecto 8080). Si una variable crítica no está definida, la aplicación interrumpe el arranque de forma preventiva.
* **Gestión de secretos:** se registran en GitHub Secrets `STAGING_DB_URL`, `STAGING_DB_USER`, `STAGING_DB_PASSWORD`, `JWT_SECRET_KEY` y `DEPLOY_HOOK_URL`. Las credenciales nunca se versionan en el repositorio.
* **Empaquetado con Docker:** Render no ofrece un entorno de ejecución nativo para Java, por lo que el servicio se despliega a partir de un `Dockerfile` multietapa (compilación con JDK 17 y ejecución con JRE 17). Esta decisión es una consecuencia de la plataforma elegida y no un requisito del despliegue en sí.
* **Despliegue controlado por el pipeline:** el despliegue automático nativo de Render se desactiva (Auto-Deploy en *Off*) para que el único camino de publicación sea el pipeline, evitando despliegues duplicados o que omitan las verificaciones de calidad.
* **Política de esquema:** la estructura de tablas se crea con el script `schema.sql` ejecutado sobre `agrovalle_staging`. La propiedad `ddl-auto` de Hibernate se mantiene en `update` para el ambiente de Staging.

## Consecuencias y Mantenibilidad

* **Consecuencias Positivas**:
  * Automatización completa del ciclo de entrega (*Continuous Deployment*): un cambio validado en `main` llega a Staging sin intervención manual.
  * Seguridad al inyectar contraseñas y llaves JWT desde GitHub Secrets y variables de entorno, previniendo la fuga de credenciales en el historial del repositorio.
  * Flujo de calidad no negociable: ningún despliegue ocurre si falla la compilación, el linter, las pruebas unitarias o el umbral de cobertura.
  * Disponibilidad de una URL pública para la validación del Product Owner y las pruebas de integración End-to-End.
  * Separación clara entre configuración y código, lo que facilita crear otros ambientes (por ejemplo, producción) con el mismo artefacto.
  * Sin costos de infraestructura ni administración de servidores durante el curso.
* **Consecuencias Negativas / Limitaciones**:
  * La capa gratuita de la PaaS apaga el servicio tras periodos de inactividad, por lo que la primera petición posterior puede presentar un tiempo de latencia inicial (*Cold Start*) de entre 30 segundos y un par de minutos. Del mismo modo, el cómputo de la base de datos en Neon puede suspenderse por inactividad y añadir demora a la primera conexión.
  * El plan gratuito de Render tiene recursos limitados (memoria reducida), por lo que se limita la memoria de la JVM mediante la variable `JAVA_TOOL_OPTIONS`.
  * Los servicios gratuitos pueden reiniciarse en cualquier momento y no cuentan con acceso SSH, escalado ni discos persistentes; la persistencia debe residir siempre en la base de datos externa.
  * Dependencia de plataformas de terceros (Render y Neon): un cambio en sus condiciones de capa gratuita podría obligar a migrar.
  * El Deploy Hook actúa como credencial: quien conozca la URL puede disparar despliegues, por lo que debe permanecer únicamente en GitHub Secrets.

