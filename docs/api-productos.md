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
