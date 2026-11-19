# Especificación Técnica de Reportes y Comprobantes

## 1. Visión General del Módulo

El subsistema de reportes de TRAVELINK (`com.travelink.reportes`) proporciona la generación dinámica de documentos formales en formato PDF y hojas de cálculo tabulares en formato Microsoft Excel (OOXML `.xlsx`). Su objetivo es respaldar las operaciones comerciales de los turistas, la conciliación financiera de las agencias de viajes y la auditoría global por parte de los administradores.

### Tecnologías Empleadas
- **OpenPDF 1.3.39**: Generación de documentos PDF vectoriales (comprobantes de reserva y liquidaciones).
- **Apache POI 5.2.5 (`poi-ooxml`)**: Creación de libros Excel con estilos tipográficos, cabeceras coloreadas y autoajuste de columnas.
- **Controlador Orquestador**: `ReportesController` expone endpoints REST protegidos por Spring Security.

---

## 2. Convención de Nomenclatura

Todos los documentos descargados siguen una convención estandarizada en la cabecera HTTP `Content-Disposition`:

```text
reporte_[modulo]_[rol]_[yyyyMMdd].[extension]
```

Ejemplos:
- `reporte_reserva_turista_20261118.pdf`
- `reporte_reservas_agencia_20261118.xlsx`
- `reporte_comisiones_admin_20261118.pdf`

---

## 3. Catálogo de Reportes

### 3.1 Documentos en Formato PDF (OpenPDF)

| Código | Documento | Endpoint REST | Rol Autorizado | Parámetros | Descripción |
|---|---|---|---|---|---|
| **REP-PDF-01** | Comprobante de Reserva | `GET /api/turista/reservas/{id}/comprobante.pdf` | Turista (propietario), Agencia, Admin | `id` (path) o `codigo` (query) | Emite el voucher de viaje con desglose de pasajeros, código QR/referencia, datos del tour, fecha y monto pagado. |
| **REP-PDF-02** | Liquidación de Comisiones | `GET /api/admin/reportes/comisiones.pdf` | Administrador | Ninguno | Resumen consolidado del porcentaje de comisión recaudado por la plataforma por cada agencia. |

### 3.2 Hojas de Cálculo en Formato Excel (Apache POI)

| Código | Documento | Endpoint REST | Rol Autorizado | Parámetros de Filtrado | Columnas Incluidas |
|---|---|---|---|---|---|
| **REP-XLS-01** | Reservas de Agencia | `GET /api/agencia/reportes/reservas.xlsx` | Agencia | `fechaInicio`, `fechaFin`, `estado` | ID, Código Reserva, Tour, Fecha Salida, Turista, Contacto, N° Pasajeros, Monto Total, Estado, Fecha Reserva |
| **REP-XLS-02** | Pagos de Agencia | `GET /api/agencia/reportes/pagos.xlsx` | Agencia | `fechaInicio`, `fechaFin`, `estado` | ID Pago, Código Reserva, Método, Monto, Referencia Transacción, Fecha Pago, Estado |
| **REP-XLS-03** | Reservas Globales | `GET /api/admin/reportes/reservas.xlsx` | Administrador | `fechaInicio`, `fechaFin`, `estado` | ID, Código, Agencia, Turista, Tour, Fecha, Monto, Estado |
| **REP-XLS-04** | Comisiones Globales | `GET /api/admin/reportes/comisiones.xlsx` | Administrador | Ninguno | ID Comisión, Agencia, Monto Reserva, % Comisión, Monto Comisión, Estado Liquidación, Fecha |
| **REP-XLS-05** | Padrón de Usuarios | `GET /api/admin/reportes/usuarios.xlsx` | Administrador | Ninguno | ID Usuario, Nombre Completo, Email, Teléfono, Rol, Estado, Fecha Registro |
| **REP-XLS-06** | Directorio de Agencias | `GET /api/admin/reportes/agencias.xlsx` | Administrador | Ninguno | ID Agencia, Razón Social, RUC/NIT, Correo Contacto, Teléfono, Estado Verificación, Fecha Afiliación |

---

## 4. Control de Acceso y Seguridad de Datos

1. **Aislamiento Multitenant por Agencia**: Al invocar endpoints bajo `/api/agencia/reportes/*`, la identidad del usuario autenticado se extrae del `SecurityContextHolder`. Se resuelve el `idAgencia` asociado a través de `AgenciaServicio.buscarPorIdUsuario()` o la sesión activa. Una agencia nunca puede consultar transacciones de agencias competidoras.
2. **Propiedad de Comprobantes**: Al descargar un comprobante PDF, se verifica mediante consulta directa que `authUserId == resIdUsuario` o que la agencia corresponda a `resIdAgencia`. Si un usuario intenta forzar la descarga del voucher de otro cliente, se responde con código HTTP `403 Forbidden`.
3. **Prevención de Caché Sensible**: Todas las respuestas incorporan la cabecera `Cache-Control: no-cache, no-store, must-revalidate` para evitar que información financiera quede almacenada en proxies intermedios o cachés de navegador.
