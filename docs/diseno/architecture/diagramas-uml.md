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

### Colaboración 3 · Confirmación de recepción y publicación del evento (HU-09)

```mermaid
flowchart LR
    C([Comprador])
    API[RecepcionController]
    S[RecepcionService]
    PE[PedidoRepository]
    IR[InconformidadRepository]
    E[ApplicationEventPublisher]
    L[RecepcionConfirmadaListener]

    C -->|"1. POST /api/v1/pedidos/{id}/recepcion"| API
    API -->|"2. confirmarRecepcion(pedidoId, usuarioId)"| S
    S -->|"3. findByIdParaActualizar(pedidoId) y verificar que pertenezca al comprador"| PE
    S -->|"4. Si el pedido está EN_TRANSPORTE, comprobar que no tenga una inconformidad EN_REVISION"| IR
    S -->|"5. Guardar el pedido ENTREGADO con su fecha_entrega"| PE
    S -->|"6. publishEvent(RecepcionConfirmadaEvent)"| E
    E -->|"7. alConfirmarRecepcion(evento) después del commit"| L
    API -.->|"8. HTTP 200 OK · recepción confirmada"| C
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
## Diagramas de secuencia

### Login y generacion del token

```mermaid
sequenceDiagram
    actor U as Usuario
    participant W as Interfaz Web<br/>(Navegador)<br/>La pantalla del usuario
    participant C as Controlador REST<br/>«RestController»<br/>Recibe la petición
    participant S as Servicio<br/>«AuthService»<br/>Aplica las reglas del negocio
    participant R as Repositorio JPA<br/>«Repository»<br/>Consulta y guarda datos
    participant DB as PostgreSQL<br/>agrovalle_db<br/>Guarda la información
    participant B as Cifrador<br/>«BCrypt»<br/>Compara contraseñas
    participant J as Servicio de token<br/>«JwtService»<br/>Crea y firma el token

    U->>W: 1.1 Escribe identificación y contraseña y pulsa «Ingresar»
    W->>C: 1.2 Envía los datos (POST /api/v1/auth/login)
    C->>C: 1.3 Revisa que los datos estén completos
    C->>S: 1.4 Pide iniciar sesión
    S->>R: 1.5 ¿Existe un usuario con esa identificación?
    R->>DB: 1.6 Busca en la tabla usuarios
    DB-->>R: 1.7 Devuelve la fila del usuario (o ninguna)
    R-->>S: 1.8 Devuelve el usuario encontrado (o vacío)

    alt El usuario no existe
        S-->>C: 1.9 Lanza error de credenciales inválidas
        C-->>W: 1.10 401 No autorizado
        W-->>U: 1.11 «Credenciales inválidas»
    else El usuario existe
        S->>B: 1.12 Compara la contraseña escrita con la contraseña cifrada guardada
        B-->>S: 1.13 Responde si coinciden (true / false)

        alt La cuenta está inactiva o la contraseña es incorrecta
            S-->>C: 1.14 Lanza error de credenciales inválidas
            C-->>W: 1.15 401 No autorizado
            W-->>U: 1.16 «Credenciales inválidas»
        else Las credenciales son correctas
            S->>J: 1.17 Pide crear el token con el id del usuario y sus roles
            J-->>S: 1.19 Devuelve el token JWT
            S-->>C: 1.20 Devuelve token, tipo Bearer y tiempo de expiración
            C-->>W: 1.21 200 OK con el token
            W->>W: 1.22 Guarda el token (localStorage o variable)
            W-->>U: 1.23 «Bienvenido»
        end
    end
```
### HU-08: Seguimiento del envío

```mermaid
sequenceDiagram
    actor U as Comprador
    participant W as Interfaz Web<br/>(Navegador)<br/>La pantalla del usuario
    participant C as Controlador REST<br/>«RestController»<br/>Recibe la petición
    participant S as Servicio de pedidos<br/>«PedidoService»<br/>Aplica las reglas del negocio
    participant R as Repositorio JPA<br/>«Repository»<br/>Consulta y guarda datos
    participant DB as PostgreSQL<br/>agrovalle_db<br/>Guarda la información

    U->>W: 8.1 Entra al detalle de su pedido activo
    W->>C: 8.2 Pide el seguimiento (GET /api/v1/pedidos/{id}/seguimiento)
    C->>C: 8.3 Valida el token JWT y el rol COMPRADOR
    C->>S: 8.4 Pide consultar el seguimiento del pedido
    S->>R: 8.5 Busca el pedido por id
    R->>DB: 8.6 Busca en la tabla pedidos
    DB-->>R: 8.7 Devuelve la fila del pedido (o ninguna)
    R-->>S: 8.8 Devuelve el pedido encontrado (o vacío)

    alt El pedido no existe o es de otro comprador
        S-->>C: 8.9 Lanza error de recurso no encontrado
        C-->>W: 8.10 404 No encontrado
        W-->>U: 8.11 «Pedido no encontrado»
    else El pedido es del comprador
        S->>S: 8.12 Arma el mensaje según el estado (PENDIENTE, CONFIRMADO, EN_TRANSPORTE, ENTREGADO o CANCELADO)

        alt El pedido está activo y no tiene fecha estimada de llegada
            S->>S: 8.13 Agrega «Aún no hay fecha estimada de llegada» al mensaje
        else Ya tiene fecha estimada o el pedido está ENTREGADO / CANCELADO
            S->>S: 8.14 Deja el mensaje sin cambios
        end

        S-->>C: 8.15 Devuelve estado, fechas (creación, estimada de llegada y entrega) y mensaje
        C-->>W: 8.16 200 OK con el seguimiento
        W-->>U: 8.17 Muestra el estado actualizado y la hora estimada de llegada (si existe)
    end
```

### HU-09: Confirmación de recepción de la mercancía

```mermaid
sequenceDiagram
    actor U as Comprador
    participant W as Interfaz Web<br/>(Navegador)<br/>La pantalla del usuario
    participant C as Controlador REST<br/>«RestController»<br/>Recibe la petición
    participant S as Servicio de recepción<br/>«RecepcionService»<br/>Aplica las reglas del negocio
    participant R as Repositorio JPA<br/>«Repository»<br/>Consulta y guarda datos
    participant DB as PostgreSQL<br/>agrovalle_db<br/>Guarda la información
    participant E as Publicador de eventos<br/>«ApplicationEventPublisher»<br/>Avisa a los interesados
    participant L as Observador<br/>«RecepcionConfirmadaListener»<br/>Reacciona al evento

    U->>W: 9.1 Revisa la carga y pulsa «Confirmar Recepción»
    W->>C: 9.2 Envía la confirmación (POST /api/v1/pedidos/{id}/recepcion)
    C->>C: 9.3 Valida el token JWT y el rol COMPRADOR
    C->>S: 9.4 Pide confirmar la recepción del pedido
    S->>R: 9.5 Busca el pedido por id y lo bloquea para actualizar
    R->>DB: 9.6 Busca en la tabla pedidos (bloqueo de escritura)
    DB-->>R: 9.7 Devuelve la fila del pedido (o ninguna)
    R-->>S: 9.8 Devuelve el pedido encontrado (o vacío)

    alt El pedido no existe o es de otro comprador
        S-->>C: 9.9 Lanza error de recurso no encontrado
        C-->>W: 9.10 404 No encontrado
        W-->>U: 9.11 «Pedido no encontrado»
    else El pedido ya estaba ENTREGADO
        S-->>C: 9.12 Lanza error de conflicto
        C-->>W: 9.13 409 Conflicto
        W-->>U: 9.14 «El pedido ya fue confirmado como entregado»
    else El pedido no está EN_TRANSPORTE
        S-->>C: 9.15 Lanza error de pedido inválido
        C-->>W: 9.16 400 Solicitud incorrecta
        W-->>U: 9.17 «Solo se puede confirmar un pedido en transporte»
    else El pedido está EN_TRANSPORTE
        S->>R: 9.18 ¿Existe una inconformidad EN_REVISION para este pedido?
        R->>DB: 9.19 Busca en la tabla inconformidades
        DB-->>R: 9.20 Devuelve si existe (true / false)
        R-->>S: 9.21 Devuelve true / false

        alt Hay una inconformidad en revisión
            S-->>C: 9.22 Lanza error de conflicto
            C-->>W: 9.23 409 Conflicto
            W-->>U: 9.24 «El pedido tiene una inconformidad en revisión»
        else No hay inconformidades en revisión
            S->>S: 9.25 Cambia el estado a ENTREGADO y guarda la fecha de entrega
            S->>R: 9.26 Guarda el pedido actualizado
            R->>DB: 9.27 Actualiza la tabla pedidos
            DB-->>R: 9.28 Confirma
            R-->>S: 9.29 Quedó guardado
            S->>E: 9.30 Publica RecepcionConfirmadaEvent (pedido, comprador, agricultor y fecha)
            S-->>C: 9.31 Devuelve id del pedido, estado ENTREGADO y fecha de recepción
            C-->>W: 9.32 200 OK con la recepción
            W-->>U: 9.33 «Recepción confirmada»
            E-)L: 9.34 Entrega el evento al observador
            L->>L: 9.35 Deja traza en el log (HU-11 la reemplazará por la liberación del pago retenido)
        end
    end
```

### HU-15: Reporte de inconformidad o producto dañado

```mermaid
sequenceDiagram
    actor U as Comprador
    participant W as Interfaz Web<br/>(Navegador)<br/>La pantalla del usuario
    participant C as Controlador REST<br/>«RestController»<br/>Recibe la petición
    participant S as Servicio de inconformidades<br/>«InconformidadService»<br/>Aplica las reglas del negocio
    participant R as Repositorio JPA<br/>«Repository»<br/>Consulta y guarda datos
    participant DB as PostgreSQL<br/>agrovalle_db<br/>Guarda la información
    participant E as Publicador de eventos<br/>«ApplicationEventPublisher»<br/>Avisa a los interesados

    U->>W: 15.1 Escribe la descripción, adjunta la evidencia y pulsa «Reportar inconformidad»
    W->>C: 15.2 Envía el reporte (POST /api/v1/pedidos/{id}/inconformidad)
    C->>C: 15.3 Valida el token JWT y el rol COMPRADOR
    C->>C: 15.4 Revisa que la descripción y la URL de la evidencia (HTTP/HTTPS) sean válidas

    alt Los datos están incompletos o son inválidos
        C-->>W: 15.5 400 Solicitud incorrecta
        W-->>U: 15.6 «Revisa la descripción y la evidencia»
    else Los datos son válidos
        C->>S: 15.7 Pide registrar la inconformidad del pedido
        S->>R: 15.8 Busca el pedido por id y lo bloquea para actualizar
        R->>DB: 15.9 Busca en la tabla pedidos (bloqueo de escritura)
        DB-->>R: 15.10 Devuelve la fila del pedido (o ninguna)
        R-->>S: 15.11 Devuelve el pedido encontrado (o vacío)
        S->>S: 15.12 Verifica que el pedido exista y sea del comprador (si no, 404)

        alt El pedido no existe o es de otro comprador
            S-->>C: 15.13 Lanza error de recurso no encontrado
            C-->>W: 15.14 404 No encontrado
            W-->>U: 15.15 «Pedido no encontrado»
        else El pedido no está EN_TRANSPORTE
            S-->>C: 15.16 Lanza error de pedido inválido
            C-->>W: 15.17 400 Solicitud incorrecta
            W-->>U: 15.18 «El pedido debe estar en transporte para reportar»
        else El pedido está EN_TRANSPORTE
            S->>R: 15.19 ¿Ya existe una inconformidad EN_REVISION para este pedido?
            R->>DB: 15.20 Busca en la tabla inconformidades
            DB-->>R: 15.21 Devuelve si existe (true / false)
            R-->>S: 15.22 Devuelve true / false

            alt Ya hay una inconformidad en revisión
                S-->>C: 15.23 Lanza error de conflicto
                C-->>W: 15.24 409 Conflicto
                W-->>U: 15.25 «El pedido ya tiene una inconformidad en revisión»
            else No hay inconformidades en revisión
                S->>S: 15.26 Crea la inconformidad (descripción, evidencia, estado EN_REVISION y fecha del reporte)
                S->>R: 15.27 Guarda la inconformidad
                R->>DB: 15.28 Inserta en la tabla inconformidades
                DB-->>R: 15.29 Confirma
                R-->>S: 15.30 Quedó guardada
                S->>E: 15.31 Publica InconformidadRegistradaEvent para avisar al agricultor
                S-->>C: 15.32 Devuelve la inconformidad (id, pedido, evidencia, estado y fecha)
                C-->>W: 15.33 201 Creado
                W-->>U: 15.34 «Inconformidad registrada, en revisión»
            end
        end
    end
```

