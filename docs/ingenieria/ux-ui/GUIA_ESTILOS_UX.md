# Guía de Estilos y Experiencia de Usuario (UX/UI) — TRAVELINK 🎨

El diseño de interfaz de usuario de **TRAVELINK** prioriza un enfoque minimalista, responsivo y de alto rendimiento, evitando frameworks pesados de frontend para garantizar tiempos de carga sub-segundo y compatibilidad multiplataforma.

---

## 1. Arquitectura de Interfaz Desacoplada

El frontend se organiza en `Springboot/resources/static/` mediante tres pilares:

```text
Springboot/resources/static/
├── css/                   # Hojas de estilo modulares (variables CSS, layout, componentes)
├── js/                    # Módulos JavaScript Vanilla (asíncronos con Fetch API)
├── img/                   # Fotografías de destinos peruanos y recursos gráficos
└── html/                  # Vistas HTML5 semánticas particionadas por actor:
    ├── turista/           # Catálogo, detalle de tour, reserva y comprobantes
    ├── agencia/           # Panel operacional, publicación de tours y cupos
    └── administrador/     # Dashboard directivo, aprobación de agencias y comisiones
```

---

## 2. Paleta Cromática y Tipografía

| Propósito | Variable / Nombre | Código Hexadecimal | Uso en la Interfaz |
|---|---|:---:|---|
| **Color Primario** | Azul Corporativo | `#1E3A8A` | Barras de navegación, botones de acción principal (CTA), encabezados |
| **Color Secundario** | Ámbar Turístico | `#D97706` | Badges de destacados, calificaciones con estrellas, alertas moderadas |
| **Fondo Principal** | Blanco Ceniza | `#F8FAFC` | Fondo de página, tarjetas de tours y contenedores modulares |
| **Superficie** | Blanco Puro | `#FFFFFF` | Paneles de formulario, tablas de datos y diálogos modales |
| **Texto Principal** | Gris Carbón | `#1E293B` | Tipografía de lectura, títulos y etiquetas de campos |
| **Texto Atenuado** | Gris Pizarra | `#64748B` | Metadatos, fechas, notas al pie y textos explicativos |
| **Estado Éxito** | Verde Esmeralda | `#10B981` | Confirmación de reserva, cupos disponibles, estado 'Activo' |
| **Estado Error** | Rojo Carmesí | `#EF4444` | Alertas de validación, cupos agotados, cancelación de tours |

**Tipografía Base:** Sistema sans-serif moderno (`Inter`, `system-ui`, `-apple-system`, `Roboto`, `Segoe UI`, `sans-serif`) priorizando legibilidad a 16px para cuerpo de texto y jerarquía proporcional (`1.25rem`, `1.5rem`, `2rem`) para títulos.

---

## 3. Vistas y Experiencia por Actor

### 3.1. Experiencia del Turista (`html/turista/`)
- **Exploración Visual:** Grid responsivo de destinos con fotografía de portada, precio base desde y cantidad de tours activos.
- **Detalle de Experiencia (`detalle.html`):** Itinerario detallado día a día, galería de fotos, lista de inclusiones/exclusiones y widget interactivo de cálculo de tarifa en vivo según número de adultos, niños e infantes.
- **Flujo de Checkout y Pasajeros:** Formulario asistido para ingreso de manifiesto de pasajeros con validación de DNI en tiempo real.
- **Voucher y Feedback:** Acceso al historial de reservas con botón directo para descargar comprobante PDF y modal para calificar con estrellas y reseña.

### 3.2. Experiencia de la Agencia (`html/agencia/`)
- **Panel de Control:** Resumen de cupos ocupados vs disponibles en el mes en curso.
- **Gestión de Salidas:** Calendario interactivo para habilitar fechas y cupos máximos por salida.
- **Carga Masiva Excel/CSV:** Zona de arrastrar y soltar (*drag & drop*) para subir planillas de cupos con feedback visual del progreso y reporte de errores fila por fila.

### 3.3. Experiencia del Administrador (`html/administrador/`)
- **Dashboard Analítico:** Tarjetas de métricas (volumen transaccionado, comisión total 15%, agencias activas).
- **Mesa de Partes Virtual:** Bandeja de solicitudes de afiliación con visor de datos SUNAT para aprobación o rechazo en un clic.
- **Módulo de Liquidación:** Tabla filtrable por agencia y periodo para aplicar el corte de liquidación y exportar la planilla en formato Excel.
