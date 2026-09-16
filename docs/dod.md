# Contrato Técnico: Definition of Done (DoD) — AgroValle Connect

El DoD es el **contrato innegociable** que garantiza la integridad de cada incremento funcional entregado por el equipo. Ninguna Historia de Usuario se considera terminada, y ningún Pull Request se fusiona a `develop` o `main`, si no cumple todos los puntos de este checklist.

## Checklist de Cumplimiento Obligatorio

- [✔] **Build Local:** El proyecto compila o transpila sin errores en el entorno local.
- [✔] **Linter Pass (Modularity/Style):** El código cumple con las reglas estáticas de Checkstyle; cero advertencias, usando `checkstyle.xml` basado en Google Java Style. Esto garantiza el atributo de Mantenibilidad de la norma ISO/IEC 25010.
- [✔] **Functional Correctness:** El 100% de las pruebas unitarias existentes pasan con éxito.
- [✔] **Peer Review:** Todo Pull Request ha sido revisado y aprobado por al menos un compañero de equipo después de una revisión de código.
- [✔] **Documentation:** El `README.md` y la documentación técnica de la carpeta `/docs` están actualizados.
- [✔] **Commits:** El historial sigue estrictamente la convención de Conventional Commits (`feat:`, `fix:`, `docs:`, `style:`,    `refactor:`, `test:`, `chore:`).
- [✔] **Automatización:** Los hooks de Husky están activos y bloquean el commit si falla el linter o las pruebas.

## ISO/IEC 25010

| Característica ISO/IEC 25010 | Cómo la protege este DoD |
|---|---|
| **Mantenibilidad** | Checkstyle con cero advertencias y estructura modular en `src/main/java`. |
| **Adecuación Funcional** | Functional Correctness: 100% de pruebas unitarias pasando. |
| **Fiabilidad** | Build Local sin errores + hooks de Husky que impiden código roto. |
| **Compatibilidad** | Contratos de API consistentes (JSON, códigos HTTP estándar) verificados en el Peer Review. |

## Firma del equipo

Al aprobar este documento, cada integrante se compromete a no fusionar código propio a `develop` o `main` que incumpla los puntos anteriores.

| Integrante | Aceptación | | Fecha    |
|---         |---         |---         |
| _[Josen Julian Mina Carabali]_ |✔|| 15/09/2026 |
| _[Yenni Liseth Obando Obando]_ |✔|| 15/09/2026 |
| _[Lesly Camila Quintero Popo]_ |✔||15/09/2026  |
| _[Josue Mindineros Castillo]_ |✔| | 15/09/2026 |
