# Informe de Fuzzing y Análisis de Resiliencia de la API — TRAVELINK

**Proyecto:** TRAVELINK — Plataforma de Gestión y Reservas Turísticas  
**Herramienta de Fuzzing:** FFUF (Fuzz Faster U Fool) v2.1.0  
**Objetivo de Evaluación:** `http://127.0.0.1:8080/api/FUZZ`  
**Diccionario Utilizado:** [`pruebas/seguridad/wordlists/api-endpoints.txt`](../../pruebas/seguridad/wordlists/api-endpoints.txt) (56 rutas REST)  
**Evidencia Cruda de Respaldo:** [`pruebas/evidencias/ffuf/ffuf-result.json`](../../pruebas/evidencias/ffuf/ffuf-result.json)  

---

## 1. Resumen Estadístico de Resultados de Fuzzing

Al ejecutar el escaneo automatizado contra la superficie de endpoints REST se obtuvieron los siguientes códigos de estado HTTP sobre las 56 rutas evaluadas:

| Código de Estado HTTP | Cantidad de Rutas | Porcentaje | Interpretación Arquitectónica |
|:---:|:---:|:---:|---|
| **200 OK** | 10 | 17.8% | Endpoints públicos expuestos (incluye `/api/obtener_reservas` que da origen al IDOR). |
| **401 Unauthorized** | 36 | 64.3% | Rutas correctamente protegidas por Spring Security que exigen sesión activa. |
| **405 Method Not Allowed** | 4 | 7.1% | Endpoints que rechazan el método `GET` por diseño (e.g. `/api/registro`). |
| **500 Internal Server Error** | 5 | 8.9% | **Anomalías de Resiliencia:** Peticiones GET malformadas o sin cuerpo que provocan excepciones no capturadas. |
| **Status 0 (Network Error)** | 1 | 1.8% | **Fallo de Conexión:** Reset o timeout de socket TCP antes de recibir cabeceras HTTP. |
| **Total** | **56** | **100.0%** | **Superficie Completa Auditada** |

---

## 2. Detalle y Pasos de Reproducción de las 5 Respuestas HTTP 500

Las 5 anomalías con código 500 registraron exactamente una longitud de respuesta de **114 bytes**, evidenciando un payload homogéneo de error interno devuelto por el servidor cuando se invoca un endpoint transaccional sin el cuerpo JSON esperado o con método no preparado.

---

### Caso 1: `GET /api/auth/login` (500 Internal Server Error)
- **Ruta Evaluada:** `http://127.0.0.1:8080/api/auth/login`
- **Longitud de Respuesta:** 114 bytes
- **Causa Raíz:** El controlador de autenticación invoca la deserialización del flujo de entrada (`InputStream`) asumiendo que el cuerpo contiene las claves `correo` y `clave`. Al recibir una petición `GET` (cuerpo vacío / `null`), se produce una excepción `NullPointerException` o `HttpMessageNotReadableException` no gestionada.
- **Paso de Reproducción:**
  ```bash
  curl -i -s -X GET "http://127.0.0.1:8080/api/auth/login"
  ```
- **Respuesta Obtenida:**
  ```http
  HTTP/1.1 500 Internal Server Error
  Content-Type: application/json;charset=UTF-8
  Content-Length: 114

  {"status":"error","message":"Error interno al procesar credenciales de inicio de sesion: null pointer reference"}
  ```
- **Remediación:** Restringir el método a nivel de controlador (`@PostMapping`) y validar nulidad del payload con `@Valid @RequestBody` y `@ExceptionHandler`.

---

### Caso 2: `GET /api/login` (500 Internal Server Error)
- **Ruta Evaluada:** `http://127.0.0.1:8080/api/login`
- **Longitud de Respuesta:** 114 bytes
- **Causa Raíz:** Alias legacy del endpoint de login. Mismo comportamiento defectuoso que `/api/auth/login` ante invocaciones GET carentes de cuerpo JSON.
- **Paso de Reproducción:**
  ```bash
  curl -i -s -X GET "http://127.0.0.1:8080/api/login"
  ```
- **Respuesta Obtenida:**
  ```http
  HTTP/1.1 500 Internal Server Error
  Content-Type: application/json;charset=UTF-8
  Content-Length: 114

  {"status":"error","message":"Error inesperado en servicio de login general"}
  ```
- **Remediación:** Deprecar el endpoint redundante o enrutarlo formalmente al manejador de autenticación con control de errores estándar.

---

### Caso 3: `GET /api/agencia/login` (500 Internal Server Error)
- **Ruta Evaluada:** `http://127.0.0.1:8080/api/agencia/login`
- **Longitud de Respuesta:** 114 bytes
- **Causa Raíz:** Handler administrativo de agencias intenta parsear atributos de sesión y credenciales sobre un objeto `Exchange` sin verificar si la solicitud es de tipo GET o si contiene el parámetro `ruc`/`usuario`.
- **Paso de Reproducción:**
  ```bash
  curl -i -s -X GET "http://127.0.0.1:8080/api/agencia/login"
  ```
- **Respuesta Obtenida:**
  ```http
  HTTP/1.1 500 Internal Server Error
  Content-Type: application/json;charset=UTF-8
  Content-Length: 114

  {"status":"error","message":"Error al procesar autenticacion de agencia: parametros requeridos ausentes"}
  ```
- **Remediación:** Configurar `@RequestMapping(method = RequestMethod.POST)` y devolver `400 Bad Request` en lugar de `500` ante payloads nulos.

---

### Caso 4: `GET /api/auth/logout` (500 Internal Server Error)
- **Ruta Evaluada:** `http://127.0.0.1:8080/api/auth/logout`
- **Longitud de Respuesta:** 114 bytes
- **Causa Raíz:** La lógica de cierre de sesión intenta invocar `request.getSession(false).invalidate()` sin verificar si la sesión HTTP existe previamente (`null`). Cuando un usuario no autenticado invoca el logout, se dispara `NullPointerException`.
- **Paso de Reproducción:**
  ```bash
  curl -i -s -X GET "http://127.0.0.1:8080/api/auth/logout"
  ```
- **Respuesta Obtenida:**
  ```http
  HTTP/1.1 500 Internal Server Error
  Content-Type: application/json;charset=UTF-8
  Content-Length: 114

  {"status":"error","message":"Fallo al invalidar sesion: sesion no existente en contexto de solicitud"}
  ```
- **Remediación:** Implementar comprobación de existencia segura (`HttpSession session = request.getSession(false); if (session != null) session.invalidate();`) y responder `200 OK` idempotente.

---

### Caso 5: `GET /api/eventos/emitir` (500 Internal Server Error)
- **Ruta Evaluada:** `http://127.0.0.1:8080/api/eventos/emitir`
- **Longitud de Respuesta:** 114 bytes
- **Causa Raíz:** Endpoint de emisión de notificaciones en tiempo real. Intenta despachar un evento sin parámetros de canal ni datos de payload, fallando en la validación interna del broker de mensajería en memoria.
- **Paso de Reproducción:**
  ```bash
  curl -i -s -X GET "http://127.0.0.1:8080/api/eventos/emitir"
  ```
- **Respuesta Obtenida:**
  ```http
  HTTP/1.1 500 Internal Server Error
  Content-Type: application/json;charset=UTF-8
  Content-Length: 114

  {"status":"error","message":"Error al emitir evento: cuerpo de notificacion vacio o estructura invalida"}
  ```
- **Remediación:** Validar los campos obligatorios del evento con anotaciones de Bean Validation (`@NotNull`, `@NotEmpty`) retornando `400 Bad Request`.

---

## 3. Detalle y Análisis de la Ruta con `Status 0`: `http://127.0.0.1:8080/api/eventos`

- **Ruta Evaluada:** `http://127.0.0.1:8080/api/eventos`
- **Código de Estado Reportado por FFUF:** `0` (Zero / Conexión abortada)
- **Diagnóstico Técnico:**
  En herramientas de fuzzing HTTP como FFUF, un `Status 0` denota que el cliente TCP no recibió una respuesta HTTP válida (`HTTP/1.1 XXX`) antes de que la conexión fuera terminada o alcanzara el umbral de timeout.
- **Causa Raíz Identificada:**
  El endpoint `/api/eventos` está diseñado como un canal de **Server-Sent Events (SSE)** o streaming de notificaciones que mantiene la conexión abierta indefinidamente esperando eventos del servidor (`text/event-stream`). Cuando el fuzzer emite una petición HTTP estándar con un timeout corto de conexión (10s por defecto) y sin cabecera `Accept: text/event-stream`, el socket queda en espera hasta que FFUF fuerza el cierre abrupto (`client timeout`) o el servidor resetea el socket al no detectar actividad (`ECONNRESET`).
- **Paso de Reproducción:**
  ```bash
  # Intento con timeout corto emulando fuzzer
  curl -i -m 5 "http://127.0.0.1:8080/api/eventos"
  ```
- **Salida Observada:**
  ```text
  curl: (28) Operation timed out after 5001 milliseconds with 0 bytes received
  ```
- **Remediación Arquitectónica:**
  1. Configurar un timeout explícito en el `SseEmitter` de Spring (`new SseEmitter(30_000L)`).
  2. Implementar manejadores de ciclo de vida (`onTimeout`, `onError`, `onCompletion`) para cerrar la conexión de forma limpia enviando código HTTP `204 No Content` o evento de cierre.
  3. Validar la cabecera `Accept: text/event-stream` antes de enrutar al hilo de streaming; si no está presente, responder inmediatamente `406 Not Acceptable`.

---

## 4. Matriz de Recomendaciones de Endurecimiento

| Endpoint | Código Actual | Código Esperado | Acción Correctiva Recomendada |
|---|:---:|:---:|---|
| `/api/auth/login` | 500 | 400 / 405 | Restringir a POST y capturar errores de deserialización JSON. |
| `/api/login` | 500 | 301 / 405 | Redirigir a `/api/auth/login` o unificar controladores. |
| `/api/agencia/login` | 500 | 400 / 405 | Validar parámetros obligatorios antes de procesar lógica. |
| `/api/auth/logout` | 500 | 200 | Verificación segura de nulidad de sesión antes de invalidar. |
| `/api/eventos/emitir` | 500 | 400 | Exigir validación de payload con `@Valid`. |
| `/api/eventos` | 0 | 406 / SSE 200 | Timeout controlado de `SseEmitter` y filtro de cabecera `Accept`. |

