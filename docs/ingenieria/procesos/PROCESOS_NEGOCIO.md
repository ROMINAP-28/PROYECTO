# Especificación de Procesos de Negocio — TRAVELINK 🔄

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
