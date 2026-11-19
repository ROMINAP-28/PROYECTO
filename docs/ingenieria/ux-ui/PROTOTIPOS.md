# Inventario de Vistas, Prototipos y Navegación — TRAVELINK

Este documento cataloga la totalidad de las vistas web desarrolladas en `Springboot/resources/static/`, organizadas por rol de usuario (Público/Turista, Agencia y Administrador), detallando su propósito funcional, componentes clave de interfaz, endpoints REST que consumen y la relación de capturas de pantalla de la interfaz gráfica.

---

## 1. Módulo Turista y Vistas Públicas (`html/turista/` y raíz)

| N° | Archivo / Ruta | Rol / Acceso | Propósito de la Vista | Componentes Clave de UI | Endpoints REST Consumidos |
|---|---|---|---|---|---|
| 1 | `index.html` | Público | Portal de aterrizaje y vitrina comercial principal | Barra de navegación global, buscador con autocompletado, carrusel de tours destacados, pie de página. | `GET /api/destinos`<br>`GET /api/tours?destacados=true` |
| 2 | `html/turista/registro.html` | Público | Formulario de alta para nuevos turistas | Inputs validados para DNI, nombre, apellidos, correo, contraseña con medidor de fortaleza, términos. | `POST /api/auth/register`<br>`POST /api/turista/registro` |
| 3 | `html/turista/login.html` | Público | Autenticación y acceso de turistas | Formulario de login, botón de visualización de contraseña, enlace de recuperación y redirección RBAC. | `POST /api/auth/login`<br>`GET /api/auth/me` |
| 4 | `html/turista/experiencias.html` | Público / Turista | Catálogo interactivo de tours y filtros | Barra lateral con filtros por destino, rango de precios, duración y estrellas; cuadrícula responsiva de tarjetas. | `GET /api/destinos`<br>`GET /api/tours` |
| 5 | `html/turista/ofertas.html` | Público / Turista | Sección de paquetes con descuento promocional | Listado de tours filtrados por bandera de descuento, cronómetros de oferta y etiquetas de ahorro. | `GET /api/tours?ofertas=true` |
| 6 | `html/turista/detalle.html` | Público / Turista | Ficha técnica completa del tour | Galería de fotos con lightbox, itinerario día por día, selector de fecha/hora de salida, políticas. | `GET /api/tours/{id}`<br>`GET /api/tours/{id}/fechas`<br>`GET /api/calificaciones/tour/{id}` |
| 7 | `html/turista/form_reserva.html` | Turista autenticado | Proceso de reserva y datos de viajeros | Selector de cupos por edad (adulto/niño/bebé), formularios dinámicos para cada pasajero, desglose de subtotal. | `POST /api/turista/reservas`<br>`GET /api/tours/{id}/fechas/{idFecha}` |
| 8 | `html/turista/reservas.html` | Turista autenticado | Panel de gestión y seguimiento de viajes | Pestañas de reservas (Activas, Pasadas, Canceladas), modal de cancelación, botón de descarga de voucher PDF. | `GET /api/turista/reservas`<br>`POST /api/turista/reservas/{id}/cancelar`<br>`GET /api/turista/reservas/{id}/comprobante.pdf` |
| 9 | `html/turista/agencias.html` | Público | Directorio público de agencias autorizadas | Tarjetas de presentación de agencias con calificación media, número de tours y enlace a perfil. | `GET /api/agencias?estado=APROBADA` |
| 10 | `html/turista/detalle-agencia.html` | Público | Perfil público de una agencia específica | Portada, reseña comercial, contacto, tours vigentes de la agencia y reseñas acumuladas. | `GET /api/agencias/{id}`<br>`GET /api/tours?idAgencia={id}` |
| 11 | `html/turista/nosotros.html` | Público | Información institucional de TRAVELINK | Misión, visión, políticas de sostenibilidad, formalización del turismo y canales de soporte. | Estático / Sin llamadas API |

---

## 2. Módulo Agencia de Viajes (`html/agencia/`)

| N° | Archivo / Ruta | Rol / Acceso | Propósito de la Vista | Componentes Clave de UI | Endpoints REST Consumidos |
|---|---|---|---|---|---|
| 12 | `html/agencia/R_agencia_login.html` | Agencia | Inicio de sesión exclusivo para operadores | Login con validación de estado de cuenta (alerta si la solicitud aún está en revisión). | `POST /api/auth/login` |
| 13 | `html/agencia/R_agencia_registro.html` | Público (Empresa) | Inicio del asistente de registro de agencia | Formulario de datos básicos de la empresa y credenciales iniciales. | `POST /api/agencia/registro` |
| 14 | `html/agencia/R_agencia_empresa.html` | Agencia (Wizard) | Paso 2: Razón social, RUC y domicilio fiscal | Inputs validados para longitud de RUC (11 dígitos), validación de formato tributario. | `PUT /api/agencia/perfil/empresa` |
| 15 | `html/agencia/R_agencia_responsable.html` | Agencia (Wizard) | Paso 3: Datos del representante legal | Nombre completo, DNI y facultades de representación del titular legal. | `PUT /api/agencia/perfil/representante` |
| 16 | `html/agencia/R_agencia_turismo.html` | Agencia (Wizard) | Paso 4: Licencias y especialidades turísticas | Selección de modalidades (Aventura, Cultural, Ecoturismo) y número de registro MINCETUR. | `PUT /api/agencia/perfil/turismo` |
| 17 | `html/agencia/R_agencia_documentos.html` | Agencia (Wizard) | Paso 5: Carga digital de archivos probatorios | Input de subida de PDF para Ficha RUC, Licencia de Funcionamiento y DNI del titular. | `POST /api/agencia/documentos` |
| 18 | `html/agencia/R_agencia_revision.html` | Agencia (Wizard) | Paso 6: Resumen final y envío de solicitud | Vista previa de todos los datos antes del bloqueo por revisión administrativa. | `POST /api/agencia/solicitud/enviar` |
| 19 | `html/agencia/inicio.html` | Agencia activa | Tablero principal (Dashboard operativo) | Métricas rápidas: reservas del mes, cupos vendidos, ingresos acumulados, próximas salidas. | `GET /api/agencia/dashboard`<br>`GET /api/agencia/metricas` |
| 20 | `html/agencia/paquetes.html` | Agencia activa | Gestión y creación de catálogo de tours | Formulario modal para crear nuevo tour, tabla de tours vigentes con switch para activar/pausar. | `GET /api/agencia/tours`<br>`POST /api/agencia/tours`<br>`PUT /api/agencia/tours/{id}` |
| 21 | `html/agencia/disponibilidad.html` | Agencia activa | Programación de calendario y cupos | Calendario interactivo de salidas, configuración de horas de partida y aforo máximo por salida. | `GET /api/agencia/tours/{id}/fechas`<br>`POST /api/agencia/tours/{id}/fechas` |
| 22 | `html/agencia/reservas.html` | Agencia activa | Monitoreo y manifiesto de reservas | Bandeja de órdenes recibidas, lista de pasajeros por tour, botón de confirmación y exportación Excel. | `GET /api/agencia/reservas`<br>`GET /api/agencia/reportes/reservas.xlsx` |
| 23 | `html/agencia/pagos.html` | Agencia activa | Liquidaciones, balance y transferencias | Tabla de reservas pagadas, porcentaje retenido de comisión (15%) y exportación en Excel. | `GET /api/agencia/pagos`<br>`GET /api/agencia/reportes/pagos.xlsx` |
| 24 | `html/agencia/servicios.html` | Agencia activa | Catálogo de servicios adicionales | Configuración de traslados al hotel, guías bilingües y equipo de excursión incluido. | `GET /api/agencia/servicios` |
| 25 | `html/agencia/configuracion.html` | Agencia activa | Ajustes de cuenta y perfil institucional | Modificación de logotipos, horarios de atención, teléfonos de emergencia y claves. | `GET /api/agencia/perfil`<br>`PUT /api/agencia/perfil` |

---

## 3. Módulo Administrador (`html/administrador/`)

| N° | Archivo / Ruta | Rol / Acceso | Propósito de la Vista | Componentes Clave de UI | Endpoints REST Consumidos |
|---|---|---|---|---|---|
| 26 | `html/administrador/login.html` | Administrador | Portal de acceso administrativo | Interfaz de autenticación restringida para personal de plataforma. | `POST /api/auth/login` |
| 27 | `html/administrador/dashboard.html` / `index.html` | Administrador | Centro de control global de TRAVELINK | Indicadores clave (KPIs): volumen bruto de ventas (GMV), comisiones generadas, usuarios registrados. | `GET /api/admin/dashboard`<br>`GET /api/admin/estadisticas` |
| 28 | `html/administrador/agencias.html` | Administrador | Padrón de agencias y bandeja de aprobación | Listado de solicitudes de afiliación con visor de documentos PDF, botones "Aprobar" y "Rechazar con motivo". | `GET /api/admin/agencias`<br>`POST /api/admin/agencias/{id}/aprobar`<br>`POST /api/admin/agencias/{id}/rechazar`<br>`GET /api/admin/reportes/agencias.xlsx` |
| 29 | `html/administrador/usuarios.html` | Administrador | Gestión de cuentas de usuario | Tabla de usuarios (turistas, operadores), filtros por rol y estado, opciones para bloquear o restablecer contraseña. | `GET /api/admin/usuarios`<br>`PUT /api/admin/usuarios/{id}/estado`<br>`GET /api/admin/reportes/usuarios.xlsx` |
| 30 | `html/administrador/tours.html` | Administrador | Auditoría de catálogo general de tours | Visor de todos los tours publicados en la plataforma por cualquier agencia, con filtro por destino. | `GET /api/admin/tours`<br>`PUT /api/admin/tours/{id}/estado` |
| 31 | `html/administrador/catalogo.html` | Administrador | Mantenimiento de destinos y categorías | Formularios CRUD para añadir nuevas ciudades turísticas, regiones y categorías globales. | `GET /api/admin/destinos`<br>`POST /api/admin/destinos`<br>`DELETE /api/admin/destinos/{id}` |
| 32 | `html/administrador/reservas.html` | Administrador | Registro histórico central de reservas | Auditoría de todas las compras realizadas en la plataforma y descarga consolidada en Excel. | `GET /api/admin/reservas`<br>`GET /api/admin/reportes/reservas.xlsx` |
| 33 | `html/administrador/comisiones.html` | Administrador | Módulo financiero de cobro de comisiones | Conciliación de comisiones retenidas por plataforma (15%), botón para liquidación masiva y exportación PDF/Excel. | `GET /api/admin/comisiones`<br>`POST /api/admin/comisiones/{id}/liquidar`<br>`GET /api/admin/reportes/comisiones.pdf`<br>`GET /api/admin/reportes/comisiones.xlsx` |
| 34 | `html/administrador/calidad.html` | Administrador | Moderación de reseñas y satisfacción | Bandeja de opiniones y calificaciones reportadas, visor de quejas y eliminación de reseñas infractoras. | `GET /api/admin/calidad`<br>`DELETE /api/turista/resenas/{id}` |

---

## 4. Registro de Capturas de Pantalla Pendientes (`docs/ingenieria/ux-ui/capturas/`)

Debido a que el entorno de ejecución actual es un contenedor de terminal sin servidor gráfico Chrome activo en depuración remota, se formaliza la lista exacta de capturas pendientes por página principal de cada rol, a realizarse en la verificación de interfaz con las cuentas de [`docs/seguridad/05-informe/CUENTAS_PRUEBA.md`](../../seguridad/05-informe/CUENTAS_PRUEBA.md):

| Archivo de Destino Previsto | Vista | Rol / Cuenta de Prueba | Elemento Crítico a Capturar |
|---|---|---|---|
| `capturas/01-publico-home.png` | `index.html` | Público (Anónimo) | Vitrina principal con buscador y carrusel de tours destacados. |
| `capturas/02-turista-catalogo.png` | `html/turista/experiencias.html` | Turista (`turista1@ejemplo.com`) | Cuadrícula de tours filtrada con selector de precios y destinos. |
| `capturas/03-turista-mis-viajes.png`| `html/turista/reservas.html` | Turista (`turista1@ejemplo.com`) | Tabla de reservas activas con botón de descarga de voucher PDF. |
| `capturas/04-agencia-dashboard.png` | `html/agencia/inicio.html` | Agencia (`contacto@andes.com`) | Tablero principal con gráficos de reservas e ingresos del mes. |
| `capturas/05-agencia-paquetes.png` | `html/agencia/paquetes.html` | Agencia (`contacto@andes.com`) | Listado de tours propios y modal de creación de nuevo paquete. |
| `capturas/06-admin-dashboard.png` | `html/administrador/dashboard.html`| Admin (`admin@travelink.com`) | KPIs de plataforma (ventas totales, comisiones, agencias activas). |
| `capturas/07-admin-solicitudes.png` | `html/administrador/agencias.html` | Admin (`admin@travelink.com`) | Bandeja de solicitudes de afiliación con botones de aprobación. |
| `capturas/08-admin-comisiones.png` | `html/administrador/comisiones.html`| Admin (`admin@travelink.com`) | Tabla de comisiones retenidas del 15% y botón de liquidación. |
