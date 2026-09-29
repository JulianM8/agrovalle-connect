# API de productos

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

## Datos de demostración

El archivo `src/main/resources/data.sql` contiene datos ficticios para verificar el filtro en
PostgreSQL. Se cargan al iniciar la aplicación con el perfil `demo`; las inserciones evitan
duplicar agricultores y productos si se reinicia la aplicación.

Con `DB_PASSWORD` configurada para la base PostgreSQL local, inicia la aplicación así:

```powershell
$env:SPRING_PROFILES_ACTIVE = "demo"
.\mvnw.cmd spring-boot:run
```

El conjunto incluye Aguacate Hass disponible en Palmira, Banano Cavendish disponible en Cali,
Yuca Amarilla disponible en Tuluá y Tomate Chonto agotado en Palmira. Por ejemplo,
`GET /api/v1/productos?categoria=Frutas&municipio=Palmira` devuelve Aguacate Hass, mientras que
`GET /api/v1/productos?categoria=Verduras&municipio=Palmira` devuelve la lista vacía y el mensaje
de ausencia de proveedores.

Las pruebas usan H2 y desactivan la carga de este conjunto para conservar fixtures aisladas.
