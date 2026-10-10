# API de productos

> **Actualización:** a partir de la migración a autenticación JWT, publicar un producto exige
> haber iniciado sesión. Consultar el catálogo (este documento, sección siguiente) sigue siendo
> público y no requiere token.

## Iniciar sesión 

`POST /api/v1/auth/login`

| Campo | Tipo | Descripción |
|---|---|---|
| `identificacion` | string | Identificación de 10 dígitos del agricultor ya registrado. |
| `contrasena` | string | Contraseña definida al registrarse (mínimo 8 caracteres). |

```json
{
  "identificacion": "1234567890",
  "contrasena": "clave12345"
}
```

Respuesta exitosa (`200 OK`):

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "expiraEnMs": 3600000
}
```

Si la identificación no existe o la contraseña no coincide, responde `401 Unauthorized`.

## Publicar un producto 

`POST /api/v1/productos`

El agricultor que publica el producto ya **no** se indica con un campo `agricultorId` en el
cuerpo de la petición: se obtiene del token. Si el encabezado falta o el token es inválido o
expiró, el endpoint responde `403 Forbidden` (comportamiento por defecto de Spring Security
para peticiones no autenticadas sobre una ruta protegida).

```json
{
  "nombre": "Aguacate",
  "categoria": "Frutas",
  "cantidad": 50,
  "precio": 3500,
  "fechaCosecha": "2026-10-15"
}
```

## Consultar productos por categoría y municipio

`GET /api/v1/productos`

| Parámetro | Requerido | Descripción |
|---|---|---|
| `categoria` | Sí | Categoría que se desea consultar, por ejemplo `Frutas`. |
| `municipio` | Sí | Municipio de origen del agricultor, por ejemplo `Palmira`. |

El endpoint devuelve únicamente productos que coinciden con ambos filtros y tienen una
cantidad disponible mayor que cero. Responde `200 OK` tanto si encuentra productos como si
la lista queda vacía. Si falta alguno de los parámetros, responde `400 Bad Request`.

### Cuando hay resultados

```json
{
  "productos": [
    {
      "id": 10,
      "nombre": "Aguacate",
      "categoria": "Frutas",
      "cantidad": 50,
      "precio": 3500,
      "fechaCosecha": "2026-10-04",
      "agricultorId": 1
    }
  ],
  "mensaje": null
}
```

### Cuando no hay resultados

```json
{
  "productos": [],
  "mensaje": "No hay proveedores del producto en ese municipio."
}
```

Con `DB_PASSWORD` configurada para la base PostgreSQL local, inicia la aplicación así:

```powershell
.\mvnw.cmd spring-boot:run
```

El conjunto incluye Aguacate Hass disponible en Palmira, Banano Cavendish disponible en Cali,
Yuca Amarilla disponible en Tuluá y Tomate Chonto agotado en Palmira. Por ejemplo,
`GET /api/v1/productos?categoria=Frutas&municipio=Palmira` devuelve Aguacate Hass, mientras que
`GET /api/v1/productos?categoria=Verduras&municipio=Palmira` devuelve la lista vacía y el mensaje
de ausencia de proveedores.

