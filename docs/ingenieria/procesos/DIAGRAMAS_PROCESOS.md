# Diagramas de Procesos de Negocio — TRAVELINK

Este documento especifica los flujos de procesos implementados en el código fuente de TRAVELINK (`Springboot/src/com/travelink/controlador/*` y `servicio/*`), modelados mediante diagramas de flujo (`flowchart`) y diagramas de secuencia (`sequenceDiagram`) en sintaxis Mermaid.

---

## 1. Proceso de Registro y Autenticación (Login / Logout)

### 1.1 Diagrama de Flujo (Flowchart)
```mermaid
flowchart TD
    Inicio([Inicio]) --> DecisionRol{"¿Qué rol desea registrar?"}
    
    DecisionRol -->|Turista| RegTurista["Llenar formulario de turista<br/>(Nombre, Correo, Password, Teléfono)"]
    RegTurista --> PostTurista["POST /api/auth/register o /api/turista/registro<br/>(Payload: RegistroTuristaRequest)"]
    PostTurista --> ValTurista{"¿Correo ya existe?"}
    ValTurista -->|Sí| ErrTurista["HTTP 409 Conflict<br/>Mensaje: Correo ya registrado"]
    ValTurista -->|No| CreateTurista["Cifrar contraseña con BCrypt<br/>Crear Persona, Usuario (rol: TURISTA)<br/>HTTP 201 Created"]
    
    DecisionRol -->|Agencia| RegAgencia["Llenar formulario corporativo<br/>(Razón Social, RUC, Dirección, Teléfono)"]
    RegAgencia --> PostAgencia["POST /api/agencia/registro<br/>(Payload: RegistroAgenciaRequest)"]
    PostAgencia --> ValAgencia{"¿RUC / Correo válido?"}
    ValAgencia -->|No| ErrAgencia["HTTP 400 Bad Request"]
    ValAgencia -->|Sí| SolAgencia["Crear Usuario, Agencia y SolicitudAgencia<br/>Estado inicial: PENDIENTE<br/>HTTP 201 Created"]
    
    CreateTurista --> Login["Inicio de Sesión: POST /api/auth/login<br/>(Payload: LoginRequest)"]
    Login --> ValCreds{"¿Credenciales correctas?<br/>BCrypt.matches"}
    ValCreds -->|No| ErrLogin["HTTP 401 Unauthorized"]
    ValCreds -->|Sí| GenSession["Establecer SecurityContext<br/>Crear sesión HTTP con ID_USUARIO y ROL<br/>HTTP 200 OK con AuthResponse"]
    GenSession --> Fin([Fin])
```

### 1.2 Diagrama de Secuencia: Login de Usuario
```mermaid
sequenceDiagram
    autonumber
    actor Usuario
    participant Frontend as Cliente Web (HTML/JS)
    participant AuthCtrl as AuthApiController / AuthController
    participant SecMgr as Spring Security / UserDetailsService
    participant UserSvc as UsuarioServicio
    participant UserRepo as UsuarioRepositorio
    participant DB as MySQL 8.0

    Usuario->>Frontend: Ingresa correo y contraseña
    Frontend->>AuthCtrl: POST /api/auth/login {correo, password}
    AuthCtrl->>SecMgr: authenticate(UsernamePasswordAuthenticationToken)
    SecMgr->>UserSvc: loadUserByUsername(correo)
    UserSvc->>UserRepo: findByCorreo(correo)
    UserRepo->>DB: SELECT * FROM Usuario WHERE correo = ?
    DB-->>UserRepo: Registro de usuario y hash BCrypt
    UserRepo-->>UserSvc: Usuario entity
    UserSvc-->>SecMgr: UserDetails (rol, passwordHash)
    SecMgr->>SecMgr: BCryptPasswordEncoder.matches(raw, hash)
    alt Contraseña correcta
        SecMgr-->>AuthCtrl: Authentication exitosa
        AuthCtrl->>Frontend: HTTP 200 OK {status: "ok", token/user: UsuarioDto}
        Frontend-->>Usuario: Redirección al panel correspondiente
    else Credenciales inválidas
        SecMgr-->>AuthCtrl: BadCredentialsException
        AuthCtrl->>Frontend: HTTP 401 Unauthorized {status: "error"}
        Frontend-->>Usuario: Muestra alerta de error
    end
```

---

## 2. Proceso de Creación y Confirmación de Reserva

### 2.1 Diagrama de Flujo (Flowchart)
```mermaid
flowchart TD
    InicioRes([Inicio de Reserva]) --> SelecTour["Turista selecciona Tour y Fecha de Salida<br/>GET /api/tours/{id}"]
    SelecTour --> IngresoPasajeros["Ingresa lista de pasajeros<br/>(Nombres, Documento, Edad)"]
    IngresoPasajeros --> PostRes["POST /api/turista/reservas o /api/reservas<br/>(Handler: GuardarReservaHandler)"]
    PostRes --> VerifCupos{"ReservaServicio:<br/>¿Cupos disponibles en TourFecha?"}
    
    VerifCupos -->|No| ErrCupos["HTTP 400 Bad Request<br/>Sin cupos disponibles"]
    VerifCupos -->|Sí| TxReserva["Iniciar Transacción:<br/>1. Descontar cupos en TourFecha<br/>2. Insertar en tabla Reserva (Estado: PENDIENTE)<br/>3. Insertar DetalleReserva y Pasajero<br/>4. Generar código único de reserva"]
    
    TxReserva --> Pagar["Turista procede al pago simulado:<br/>POST /api/pagos"]
    Pagar --> ValPago{"PagoServicio:<br/>¿Monto coincide con total reserva?"}
    ValPago -->|No| ErrPago["HTTP 400 Monto inválido"]
    ValPago -->|Sí| ConfPago["1. Registrar en tabla Pago (Estado: CONFIRMADO)<br/>2. Actualizar Reserva a CONFIRMADA<br/>3. Calcular Comisión 15% para Admin<br/>4. Generar comprobante PDF<br/>HTTP 200 OK"]
    ConfPago --> FinRes([Fin de Reserva])
```

### 2.2 Diagrama de Secuencia: Guardar Reserva y Pago
```mermaid
sequenceDiagram
    autonumber
    actor Turista
    participant UI as static/html/turista/reservas.html
    participant ResHandler as GuardarReservaHandler / ReservaController
    participant ResSvc as ReservaServicio
    participant FechaRepo as TourFechaRepositorio
    participant ResRepo as ReservaRepositorio
    participant PagoSvc as PagoServicio
    participant DB as MySQL 8.0

    Turista->>UI: Completa datos de pasajeros y confirma
    UI->>ResHandler: POST /api/turista/reservas (ReservaRequest)
    ResHandler->>ResSvc: crearReserva(request, idUsuario)
    ResSvc->>FechaRepo: findById(idTourFecha)
    FechaRepo->>DB: SELECT cuposDisponibles FROM TourFecha WHERE id = ?
    DB-->>FechaRepo: Cupos
    alt Cupos insuficientes
        ResSvc-->>ResHandler: Excepción: Sin cupos disponibles
        ResHandler-->>UI: HTTP 400 Bad Request
    else Cupos disponibles
        ResSvc->>DB: UPDATE TourFecha SET cuposDisponibles = cuposDisponibles - N
        ResSvc->>ResRepo: save(Reserva: PENDIENTE)
        ResSvc->>DB: INSERT INTO Reserva, DetalleReserva, Pasajero
        DB-->>ResRepo: idReserva generado
        ResSvc-->>ResHandler: ReservaDto (codigoReserva)
        ResHandler-->>UI: HTTP 201 Created {codigoReserva}
        
        Turista->>UI: Efectúa pago del voucher
        UI->>PagoSvc: POST /api/pagos {idReserva, monto, metodo}
        PagoSvc->>DB: INSERT INTO Pago, UPDATE Reserva SET estado = 'CONFIRMADA'
        PagoSvc->>DB: INSERT INTO Comision (15% de plataforma)
        PagoSvc-->>UI: HTTP 200 OK (Pago confirmado)
        UI-->>Turista: Muestra confirmación y enlace a voucher PDF
    end
```

---

## 3. Proceso de Cancelación de Reserva

### 3.1 Diagrama de Flujo (Flowchart)
```mermaid
flowchart TD
    InicioCanc([Solicitud de Cancelación]) --> PetCanc["Turista solicita cancelar:<br/>POST /api/turista/reservas/{id}/cancelar<br/>(Handler: CancelarReservaHandler)"]
    PetCanc --> CheckAuth{"¿Usuario autenticado es dueño<br/>de la reserva?"}
    CheckAuth -->|No| ErrAuth["HTTP 403 Forbidden"]
    CheckAuth -->|Sí| CheckEstado{"¿Estado permite cancelación?<br/>(PENDIENTE o CONFIRMADA)"}
    CheckEstado -->|No| ErrEstado["HTTP 400 Bad Request<br/>Reserva ya ejecutada o cancelada"]
    CheckEstado -->|Sí| ProcesoCanc["1. Actualizar Reserva SET estado = 'CANCELADA'<br/>2. Revertir cupos en TourFecha (+N pasajeros)<br/>3. Notificar a la Agencia asociada<br/>HTTP 200 OK"]
    ProcesoCanc --> FinCanc([Fin Cancelación])
```

---

## 4. Proceso de Calificación y Moderación

### 4.1 Diagrama de Secuencia: Calificación por Turista y Moderación Admin
```mermaid
sequenceDiagram
    autonumber
    actor Turista
    actor Admin
    participant UI as static/html/turista/mis-viajes.html
    participant CalHandler as CalificarHandler / CalificacionControlador
    participant CalidadAdmin as CalidadAdminHandler / AdminApiController
    participant DB as MySQL 8.0

    Turista->>UI: Envía puntuación (1-5 estrellas) y comentario
    UI->>CalHandler: POST /api/turista/calificar (CalificacionRequest)
    CalHandler->>DB: SELECT * FROM Reserva WHERE idUsuario = ? AND idTour = ? AND estado = 'COMPLETADA'
    alt No ha completado el tour
        CalHandler-->>UI: HTTP 400 (Solo tours completados pueden calificarse)
    else Verificación correcta
        CalHandler->>DB: INSERT INTO Calificacion (idTour, idUsuario, puntuacion, comentario, fecha)
        CalHandler-->>UI: HTTP 201 Created {status: "ok"}
    end

    Note over Admin,CalidadAdmin: Moderación de Contenido Inapropiado
    Admin->>CalidadAdmin: GET /api/admin/calidad (Listar reseñas reportadas)
    CalidadAdmin->>DB: SELECT * FROM Calificacion WHERE estado = 'REPORTADA'
    DB-->>CalidadAdmin: Lista de calificaciones
    CalidadAdmin-->>Admin: Muestra listado en panel de moderación
    Admin->>CalidadAdmin: DELETE /api/turista/resenas/{id} o PUT estado = 'MODERADA'
    CalidadAdmin->>DB: UPDATE / DELETE Calificacion
    CalidadAdmin-->>Admin: HTTP 200 OK (Reseña moderada)
```

---

## 5. Proceso de Liquidación de Comisiones

### 5.1 Diagrama de Flujo (Flowchart)
```mermaid
flowchart TD
    InicioCom([Confirmación de Pago]) --> CalcCom["PagoServicio calcula comisión:<br/>montoComision = montoTotal * 0.15"]
    CalcCom --> RegCom["INSERT INTO Comision<br/>(idReserva, idAgencia, porcentaje: 15, estado: 'PENDIENTE')"]
    RegCom --> ConsultaAdmin["Admin consulta comisiones acumuladas:<br/>GET /api/admin/comisiones<br/>(Handler: ComisionesAdminHandler)"]
    ConsultaAdmin --> Liquidar["Admin ejecuta corte mensual:<br/>POST /api/admin/comisiones/{id}/liquidar<br/>(Handler: LiquidarComisionHandler)"]
    Liquidar --> UpdCom["UPDATE Comision SET estado = 'LIQUIDADA', fechaLiquidacion = NOW()<br/>HTTP 200 OK"]
    UpdCom --> FinCom([Fin Liquidación])
```

---

## 6. Proceso de Importación de Ficheros (Carga Masiva)

### 6.1 Diagrama de Secuencia: Importación de Ficheros de Catálogo
```mermaid
sequenceDiagram
    autonumber
    actor Agencia
    participant UI as static/html/agencia/paquetes.html
    participant ApiCtrl as AgenciaApiController / PaquetesAgenciaHandler
    participant Dto as ImportacionResultadoDto
    participant POI as Apache POI Parser
    participant DB as MySQL 8.0

    Agencia->>UI: Selecciona archivo Excel (.xlsx) y presiona "Importar"
    UI->>ApiCtrl: POST /api/agencia/tours/importar (MultipartFile file)
    Note over ApiCtrl: [Pendiente de confirmar integración completa de endpoint en controlador;<br/>DTO ImportacionResultadoDto implementado]
    ApiCtrl->>POI: Abrir Workbook(file.getInputStream())
    loop Por cada fila del Excel
        POI->>ApiCtrl: Leer celdas (Título, Destino, Precio, Cupo)
        alt Fila con datos inválidos o vacíos
            ApiCtrl->>Dto: agregarError(numFila, motivo)
        else Fila válida
            ApiCtrl->>DB: INSERT INTO Tour, TourFecha
            ApiCtrl->>Dto: incrementarImportadas()
        end
    end
    ApiCtrl-->>UI: HTTP 200 OK (ImportacionResultadoDto: totalFilas, importadas, errores)
    UI-->>Agencia: Despliega modal con resumen de filas importadas y errores
```

---

## 7. Proceso de Generación y Descarga de Reportes

### 7.1 Diagrama de Secuencia: Emisión de Documentos (PDF / Excel)
```mermaid
sequenceDiagram
    autonumber
    actor Solicitante as Turista / Agencia / Admin
    participant UI as Navegador Web
    participant RepCtrl as ReportesController
    participant SecUtils as SecurityUtils (RBAC)
    participant PdfSvc as PdfReporteService (OpenPDF)
    participant XlsSvc as ExcelReporteService (Apache POI)
    participant DB as MySQL 8.0

    Solicitante->>UI: Clic en "Descargar Comprobante PDF" o "Exportar Excel"
    alt Reporte PDF: GET /api/turista/reservas/{id}/comprobante.pdf
        UI->>RepCtrl: GET /api/turista/reservas/{id}/comprobante.pdf
        RepCtrl->>SecUtils: getAuthenticatedIdUsuario()
        RepCtrl->>DB: Validar propiedad (authUserId == resIdUsuario o isAdmin)
        RepCtrl->>PdfSvc: generarComprobanteReservaPdf(idReserva, outStream)
        PdfSvc->>DB: Consultar reserva, pasajeros y tour
        PdfSvc->>RepCtrl: Escribir bytes OpenPDF
        RepCtrl-->>UI: HTTP 200 OK [Content-Type: application/pdf]<br/>Content-Disposition: attachment; filename="reporte_reserva_turista_YYYYMMDD.pdf"
    else Reporte Excel: GET /api/agencia/reportes/reservas.xlsx
        UI->>RepCtrl: GET /api/agencia/reportes/reservas.xlsx?fechaInicio=...
        RepCtrl->>SecUtils: resolverIdAgencia()
        RepCtrl->>XlsSvc: generarReservasAgenciaExcel(agencyId, outStream)
        XlsSvc->>DB: SELECT * FROM Reserva WHERE idAgencia = ?
        XlsSvc->>RepCtrl: Escribir hojas de cálculo Apache POI
        RepCtrl-->>UI: HTTP 200 OK [Content-Type: application/vnd.openxmlformats-officedocument...]<br/>Content-Disposition: attachment; filename="reporte_reservas_agencia_YYYYMMDD.xlsx"
    end
    UI-->>Solicitante: Inicia descarga del archivo en el navegador
```


---

## 5. Especificación de Procesos de Negocio Consolidados (Plataforma Bilateral)

El sistema **TRAVELINK** opera como una plataforma bilateral de intermediación turística orientada al mercado peruano. Conecta a tres actores clave mediante flujos de negocio estructurados:

1. **Turistas (Rol 1):** Búsqueda de destinos/tours, reserva en línea, registro de pasajeros, pago simulado y emisión de comprobante PDF.
2. **Agencias de Viaje (Rol 2):** Registro formal con RUC (11 dígitos), publicación de tours, programación de fechas y cupos, carga masiva Excel/CSV y consulta de liquidaciones.
3. **Administradores (Rol 3):** Aprobación de agencias, monitoreo de métricas operacionales, cálculo y liquidación de comisiones de plataforma (15%) y moderación de contenido.

---

## 1. Proceso de Registro y Validación Formal de Agencias

```mermaid
flowchart TD
    Inicio([Representante de Agencia]) --> FormReg[Completar Formulario de Registro<br/>Razón Social, RUC 11 dígitos, Dirección, Teléfono]
    FormReg --> ValidarRUC{¿RUC válido y único?}
    ValidarRUC -- No --> ErrorRUC[Notificar error de formato o duplicidad]
    ErrorRUC --> FormReg
    ValidarRUC -- Sí --> CrearPendiente[Crear registro en estado 'Pendiente'<br/>Insertar en SolicitudAgencia]
    CrearPendiente --> AdminRevisa[Administrador evalúa solicitud en Dashboard]
    AdminRevisa --> Decision{¿Aprobada?}
    Decision -- No --> Rechazar[Marcar 'Rechazada' en SolicitudAgencia<br/>Registrar motivo en HistorialAgencia]
    Decision -- Sí --> Aprobar[Activar Agencia en tabla Agencia<br/>Asignar ROLE_AGENCIA a Usuario]
    Aprobar --> FinOk([Agencia habilitada para publicar tours])
    Rechazar --> FinRechazo([Notificación de rechazo emitida])
```

---

## 2. Proceso de Creación, Disponibilidad y Carga Masiva de Tours

Las agencias publican tours y gestionan cupos de forma individual o mediante libros Excel/CSV (`TourFecha`):

```mermaid
flowchart TD
    AgenciaAutenticada([Agencia Autenticada]) --> ElegirModo{Modalidad de Carga}
    ElegirModo -- Manual --> FormTour[Ingresar datos: Destino, Título, Itinerario, Precio Adulto/Niño]
    FormTour --> GuardarTour[Persistir en tabla Tour]
    GuardarTour --> ProgramarFechas[Asignar fechas específicas y cupos máximos en TourFecha]
    
    ElegirModo -- Carga Masiva --> SubirArchivo[Subir archivo .xlsx / .csv con fechas y cupos]
    SubirArchivo --> ParsearPOI[ExcelReporteService procesa filas mediante Apache POI]
    ParsearPOI --> ValidarFilas{¿Estructura y tipos válidos?}
    ValidarFilas -- Error --> ReportarLog[Retornar reporte de errores por fila]
    ValidarFilas -- Correcto --> BatchInsert[Inserción transaccional de registros en TourFecha]
    ProgramarFechas --> FinDisponibilidad([Tours disponibles para reserva pública])
    BatchInsert --> FinDisponibilidad
```

---

## 3. Proceso Integral de Reserva, Pasajeros y Liquidación

```mermaid
sequenceDiagram
    autonumber
    actor T as Turista
    participant UI as Frontend (Vanilla JS)
    participant API as Backend (Spring Boot REST)
    participant DB as Base de Datos (MySQL)
    participant PDF as Generador OpenPDF

    T->>UI: Selecciona Tour, fecha y cantidad de pasajeros
    UI->>API: POST /api/guardar_reserva (DetalleReserva + Pasajeros)
    API->>DB: Verificar cupos disponibles en TourFecha
    alt Sin cupos suficientes
        API-->>UI: Error 400 (Cupos agotados)
    else Cupos disponibles
        API->>DB: Crear Reserva (Estado: 'Pendiente'), insertar Pasajeros
        API->>DB: Descontar cupos en TourFecha
        API->>DB: Generar registro en Comision (15% plataforma, 85% agencia)
        API-->>UI: Reserva creada (ID Reserva)
        T->>UI: Confirma simulación de pago
        UI->>API: POST /api/procesar_pago (ID Reserva, MetodoPago)
        API->>DB: Actualizar Reserva a 'Confirmada' e insertar en Pago
        API->>PDF: Generar comprobante PDF (PdfReporteService)
        PDF-->>UI: Retornar stream binario PDF para descarga
    end
```

---

## 4. Proceso de Liquidación de Comisiones Administrativas (15%)

De acuerdo con las reglas de negocio de TRAVELINK:
- La plataforma retiene una comisión fija del **15%** sobre el subtotal de cada reserva confirmada.
- El saldo (**85%**) se acredita a la agencia organizadora.
- El administrador ejecuta liquidaciones periódicas mediante el endpoint `/api/comision/liquidar`, generando el corte contable en la tabla `Comision` y emitiendo reportes de pago vía Apache POI Excel.
