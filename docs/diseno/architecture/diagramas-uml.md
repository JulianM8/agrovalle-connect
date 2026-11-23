## Diagramas de colaboración

### Colaboración 1 · Confirmación del pedido y validación de inventario (HU-10)

```mermaid
flowchart LR
    C([Comprador])
    API[PedidoController]
    S[PedidoService]
    PR[ProductoRepository]
    PE[PedidoRepository]
    DE[DetallePedidoRepository]
    PS[PagoService]
    M[MedioPagoStrategy]
    PA[PagoRepository]

    C -->|"1. POST /api/v1/pedidos"| API
    API -->|"2. crear(pedido, usuarioId)"| S
    S -->|"3. findById(productoId) para comprobar la cantidad disponible"| PR
    S -->|"4. save(pedido PENDIENTE)"| PE
    S -->|"5. saveAll(detalles)"| DE
    API -.->|"6. HTTP 201 Created · pedido creado"| C
    C -->|"7. POST /api/v1/pedidos/{id}/pago"| API
    API -->|"8. pagar(pedidoId, solicitud, usuarioId)"| PS
    PS -->|"9. obtenerPedidoDelComprador() y calcularTotal()"| S
    PS -->|"10. procesar(pedido, monto)"| M
    PS -->|"11. Si el pago se completó, guardar pedido CONFIRMADO"| PE
    PS -->|"12. save(pago)"| PA
    API -.->|"13. HTTP 201 Created · resultado del pago"| C
```

### Colaboración 2 · Despacho y publicación del evento de asignación (HU-06)

```mermaid
flowchart LR
    A([Agricultor])
    API[DespachoController]
    S[DespachoService]
    PE[PedidoRepository]
    AT[AsignacionTransporteRepository]
    V[VehiculoRepository]
    R[RutaRepository]
    E[ApplicationEventPublisher]

    A -->|"1. POST /api/v1/pedidos/{id}/asignacion"| API
    API -->|"2. asignarVehiculo(pedidoId, vehiculoId, usuarioId)"| S
    S -->|"3. Buscar el pedido y verificar que pertenezca al agricultor"| PE
    S -->|"4. Comprobar que el pedido aún no tenga asignación"| AT
    S -->|"5. Si el pedido está CONFIRMADO, buscar el vehículo"| V
    S -->|"6. Buscar la ruta por vehículo y fecha o crearla"| R
    S -->|"7. Consultar las paradas y guardar la asignación con el siguiente orden"| AT
    S -->|"8. Guardar el pedido EN_TRANSPORTE"| PE
    S -->|"9. publishEvent(AsignacionRegistradaEvent)"| E
    API -.->|"10. HTTP 201 Created · transporte asignado"| A
```

## Diagrama de despliegue V2

```mermaid
flowchart LR
    subgraph CLI["«device» Cliente: Navegador / Web App"]
        WEB["Web App<br/>«aplicación»"]
    end

    subgraph GH["«node» GitHub Repository"]
        CI["«artifact»<br/>ci.yml"]
        CD["«artifact»<br/>cd.yml"]
        SEC["«secrets»<br/>GitHub Secrets"]
        HOOK["«trigger»<br/>Disparador por Webhook"]
    end

    subgraph PAAS["«device» Render / Railway PaaS - Staging"]
        subgraph CONT["«execution environment» Contenedor Spring Boot"]
            subgraph JVM["«execution environment» Java 17 Runtime (JVM)"]
                subgraph TOM["«execution environment» Tomcat embebido - 8080 interno / TLS 443"]
                    SPEC["«deployment spec»<br/>application-staging.yml"]
                    JAR["«artifact»<br/>agrovalle-connect.jar"]
                    APP["«component»<br/>AgroValle Connect"]
                    API(("API REST"))
                end
            end
        end
    end

    subgraph DB["«device» PostgreSQL Cloud Database - Neon / Supabase"]
        PG[("PostgreSQL<br/>«base de datos»<br/>Schema en 3FN")]
    end

    CI --> HOOK
    CD --> HOOK
    SEC -.-> CD
    HOOK -- "«webhook / deploy» HTTPS" --> SPEC

    SPEC -.-> JAR
    JAR -- "«manifest»" --> APP
    APP --- API

    WEB -. "«HTTPS» TCP 443" .-> API
    APP -. "«JDBC sobre SSL» TCP 5432" .-> PG
```