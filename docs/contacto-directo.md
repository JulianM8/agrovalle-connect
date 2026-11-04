# HU-05: Contacto directo

## T5.1 — Persistencia del contacto

La entidad `Contacto` corresponde a `contactos` en
`docs/diseno/modelo-datos-der.jpg`. Cada contacto guarda las referencias
`comprador_id` y `producto_id`, un mensaje de hasta 1000 caracteres y la
fecha de envío. La respuesta, también de hasta 1000 caracteres, y su fecha
son opcionales mientras la solicitud está pendiente.

El agricultor destinatario se obtiene a través de
`contacto.producto.agricultor`; la tabla no duplica esa relación. El esquema
se crea o actualiza mediante JPA, según la configuración actual
`spring.jpa.hibernate.ddl-auto=update`. La prueba de T5.1 comprueba el
mapeo sobre H2; no ejecuta una migración ni una instancia PostgreSQL.
