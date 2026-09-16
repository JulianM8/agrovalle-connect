# Product Backlog — AgroValle Connect
proyecto: AgroValle Connect
Priorización: **MoSCoW** (Must / Should / Could / Won't have this time)
Estimación: **Story Points** en escala de Fibonacci, acordados por el equipo mediante Planning Poker.

---

### HU-01: Registro de Agricultores
**Como** Agricultor, **quiero** registrarme en la plataforma **para** ofrecer mis productos.

- **Priorización:** Must have
- **Estimación:** 3 puntos

**Escenario BDD:**
- **Given** el usuario cuenta con información personal válida y desea registrarse como agricultor.
- **When** completa el formulario con su nombre, número de identificación, municipio y datos de contacto, y selecciona la opción "Registrarse".
- **Then** el sistema guarda la información del agricultor y muestra un mensaje confirmando que el registro se realizó correctamente.

---

### HU-02: Publicación de Productos
**Como** Agricultor, **quiero** publicar mis cosechas **para** que sean visibles.

- **Priorización:** Must have
- **Estimación:** 5 puntos

**Escenario BDD:**
- **Given** un agricultor está registrado en la aplicación y desea ofrecer un producto de su cosecha.
- **When** ingresa el nombre del producto, la categoría, la cantidad disponible, el precio y la fecha estimada de recolección y selecciona la opción "Publicar producto".
- **Then** el sistema valida que la fecha no sea anterior a hoy y muestra el producto en el catálogo para que los compradores puedan consultarlo.

---

### HU-03: Visualización de Precios Regionales
**Como** Usuario, **quiero** ver los precios promedio del Valle **para** negociar mejor.

- **Priorización:** Should have
- **Estimación:** 3 puntos

**Escenario BDD:**
- **Given** existen registros de ventas recientes de un producto agrícola en diferentes municipios del Valle del Cauca.
- **When** el usuario busca un producto, por ejemplo "café", y selecciona la opción para consultar su precio promedio.
- **Then** el sistema calcula la media aritmética y despliega el valor exacto en pesos colombianos.

---

### HU-04: Filtro de Categorías
**Como** usuario, **quiero** filtrar los productos agrícolas por categoría y municipio de origen **para** encontrar rápidamente las ofertas que necesito comprar.

- **Priorización:** Must have
- **Estimación:** 3 puntos

**Escenario BDD:**
- **Given** existen productos agrícolas publicados en el catálogo, clasificados por categorías como frutas, verduras y tubérculos.
- **When** el usuario selecciona la categoría y el municipio en los filtros de búsqueda.
- **Then** el sistema consulta el catálogo correspondiente y muestra únicamente los productos disponibles que pertenezcan a la categoría y el municipio seleccionados; si no hay productos disponibles, el sistema muestra un mensaje diciendo que no hay proveedores del producto en ese municipio.

---

### HU-05: Contacto Directo
**Como** usuario, **quiero** contactar directamente al agricultor dueño de un producto que estoy viendo en el catálogo **para** negociar condiciones específicas antes de generar una orden de compra.

- **Priorización:** Must have
- **Estimación:** 3 puntos

**Escenario BDD:**
- **Given** un comprador encuentra un producto de su interés dentro de la aplicación.
- **When** selecciona la opción "Contactar agricultor" y envía un mensaje válido relacionado con la oferta.
- **Then** el sistema registra la solicitud de contacto asociada a ese producto, notifica al agricultor el mensaje, y responde.

---

### HU-06: Asignación de Vehículo de Transporte
**Como** agricultor, **quiero** asignar un vehículo adecuado a mi pedido confirmado **para** asegurar que la mercancía sea transportada de forma segura hasta el usuario.

- **Priorización:** Must have
- **Estimación:** 5 puntos

**Escenario BDD:**
- **Given** el agricultor tiene un pedido confirmado y está listo para despacharlo.
- **When** selecciona el tipo de transporte necesario para la carga y confirma la asignación.
- **Then** el sistema guarda la información del vehículo asignado y notifica al comprador que el transporte ha sido asignado.

---

### HU-07: Programación de Rutas de Despacho
**Como** transportista, **quiero** consultar una ruta organizada para mis entregas **para** realizar los despachos de manera eficiente y reducir los tiempos de recorrido.

- **Priorización:** Must have
- **Estimación:** 5 puntos

**Escenario BDD:**
- **Given** el transportista tiene varios pedidos asignados para entregar en cierto municipio.
- **When** ingresa a la opción de rutas de entregas del día.
- **Then** el sistema muestra la secuencia sugerida de entregas ordenada según la ubicación de cada comprador.

---

### HU-08: Seguimiento del Envío
**Como** usuario, **quiero** consultar el estado actualizado de mi pedido y la hora estimada de llegada **para** saber cuándo recibiré la mercancía.

- **Priorización:** Should have
- **Estimación:** 3 puntos

**Escenario BDD:**
- **Given** el usuario ha realizado un pedido que ya se encuentra en proceso de despacho o en camino.
- **When** entra a la sección de detalles de la orden activa.
- **Then** el sistema le muestra el estado actualizado del pedido y, cuando exista información disponible, la hora estimada de llegada.

---

### HU-09: Confirmación de Recepción de la Mercancía
**Como** usuario, **quiero** confirmar que recibí la mercancía en buenas condiciones **para** informar que el pedido fue entregado correctamente y permitir la finalización del pago al agricultor.

- **Priorización:** Must have
- **Estimación:** 3 puntos

**Escenario BDD:**
- **Given** el transportista acaba de entregar el pedido en el establecimiento del comprador.
- **When** el usuario revisa la carga y presiona el botón de "Confirmar Recepción".
- **Then** el sistema marca la orden como entregada exitosamente y, si el pago se encuentra retenido, cambia su estado para permitir su liberación al productor según las condiciones establecidas.

---

### HU-10: Pagos en Línea
**Como** usuario, **quiero** pagar mi pedido mediante un medio electrónico **para** completar la compra de forma rápida, segura y sin utilizar dinero en efectivo.

- **Priorización:** Must have
- **Estimación:** 5 puntos

**Escenario BDD:**
- **Given** el usuario tiene artículos agregados en su carrito de compras y va a finalizar el pedido.
- **When** selecciona un medio de pago y completa la transacción mediante el proveedor o portal bancario correspondiente.
- **Then** el sistema verifica la confirmación del pago y, cuando la transacción sea aprobada, genera la orden de compra con el estado de pago correspondiente.

---

### HU-11: Retención y Liberación del Pago
**Como** usuario, **quiero** que mi pago permanezca protegido hasta recibir y revisar el pedido **para** asegurar que el dinero solo sea entregado al agricultor cuando la compra se complete correctamente.

- **Priorización:** Should have
- **Estimación:** 5 puntos

**Escenario BDD:**
- **Given** el usuario realizó y aprobó el pago de un pedido de cosechas.
- **When** la compra es procesada por el sistema y el pedido queda pendiente de entrega.
- **Then** el pago permanece en estado retenido y solo se habilita su liberación al productor cuando el agricultor confirma la recepción o se resuelve una eventual inconformidad según las condiciones establecidas.

---

### HU-12: Generación de Factura Digital
**Como** usuario, **quiero** descargar una factura digital de mi pedido **para** conservar un comprobante de los productos adquiridos y del pago realizado.

- **Priorización:** Could have
- **Estimación:** 2 puntos

**Escenario BDD:**
- **Given** un pedido ha sido pagado y completado exitosamente.
- **When** el comprador selecciona la opción "Descargar Factura".
- **Then** el sistema genera y permite descargar la factura digital en formato PDF con la información detallada de los productos, precios, impuestos y datos de la transacción.

---

### HU-13: Cancelación del Pedido por el Comprador
**Como** usuario, **quiero** cancelar un pedido antes de que sea despachado **para** corregir errores en la selección de productos, cantidades o dirección de entrega.

- **Priorización:** Must have
- **Estimación:** 3 puntos

**Escenario BDD:**
- **Given** el usuario tiene un pedido confirmado que aún se encuentra pendiente de preparación o despacho.
- **When** selecciona la opción "Cancelar pedido", pone la razón de su cancelación y confirma.
- **Then** el sistema cambia el estado del pedido a "Cancelado", informa al productor y mantiene o inicia el proceso de devolución del pago según el medio utilizado.

---

### HU-14: Calificación y Opinión del Usuario
**Como** agricultor, **quiero** calificar al usuario después de finalizar el pedido **para** registrar su comportamiento y contribuir a la confianza entre los usuarios de la plataforma.

- **Priorización:** Should have
- **Estimación:** 3 puntos

**Escenario BDD:**
- **Given** el usuario ha recibido satisfactoriamente una orden de compra.
- **When** asigna una puntuación de 1 a 5 estrellas y escribe un comentario sobre la calidad de los productos y la experiencia con el productor.
- **Then** el sistema guarda la reseña y actualiza la calificación promedio visible en el perfil del productor.

---

### HU-15: Reporte de Inconformidad o Producto Dañado
**Como** usuario, **quiero** reportar un producto dañado o diferente a lo acordado y adjuntar evidencias **para** solicitar una solución, revisión o reembolso cuando corresponda.

- **Priorización:** Must have
- **Estimación:** 5 puntos

**Escenario BDD:**
- **Given** el usuario recibe un producto que no cumple con los estándares o condiciones acordadas.
- **When** adjunta una fotografía como evidencia y envía el reporte de inconformidad antes de confirmar la recepción.
- **Then** el sistema registra la inconformidad, mantiene el pago retenido cuando corresponda e inicia un proceso de revisión para determinar la solución o el reembolso correspondiente.

---

## Resumen de priorización (MoSCoW)

| Prioridad      | Historias |
|---             |---        |
| **Must have**  | HU-01, HU-02, HU-04, HU-05, HU-06, HU-07, HU-09, HU-10, HU-13, HU-15 |
| **Should have**| HU-03, HU-08, HU-11, HU-14 |
| **Could have** | HU-12 |
| **Won't have (este sprint)** | — |

**Total Story Points del backlog:** 56
