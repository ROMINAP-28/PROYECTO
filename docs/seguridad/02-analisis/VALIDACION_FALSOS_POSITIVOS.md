# Validación y Discriminación de Falsos Positivos — Reporte OWASP ZAP (DAST)

**Proyecto:** TRAVELINK — Plataforma de Gestión y Reservas Turísticas  
**Herramienta Base:** OWASP ZAP Baseline Scan (`zaproxy/zap-stable:latest`)  
**Archivo de Origen:** [`pruebas/evidencias/zap/zap-parsed.json`](../../pruebas/evidencias/zap/zap-parsed.json)  
**Total de Tipos de Alerta Evaluados:** 16 Alertas (62 instancias de URL analizadas)  

---

## 1. Criterios de Clasificación Metodológica

Para auditar con rigor técnico los resultados arrojados por el escáner dinámico DAST, cada hallazgo se somete a verificación contra el código fuente (`Springboot/src`), la configuración de Spring Security (`SecurityConfig.java`) y pruebas manuales reproducibles con `curl`:

- **Verdadero Positivo (VP / True Positive):** La vulnerabilidad o debilidad de configuración existe fácticamente en el sistema y representa un riesgo de seguridad comprobable.
- **Falso Positivo (FP / False Positive):** El escáner interpretó incorrectamente un comportamiento estándar del framework, un parámetro inerte o una heurística genérica sin viabilidad de explotación.
- **Informativo (INFO / Informational):** Hallazgos de telemetría, descubrimiento arquitectónico o buenas prácticas operativas que no constituyen vulnerabilidad por sí mismos.

---

## 2. Matriz General de Validación de Alertas ZAP (16 Hallazgos)

| ID Plugin | Nombre de la Alerta ZAP | Severidad Reportada | Clasificación Final | Justificación Técnica y Análisis de Riesgo | PoC / Hallazgo Asociado |
|:---:|---|:---:|:---:|---|:---:|
| **10038** | Content Security Policy (CSP) Header Not Set | Medium | **Verdadero Positivo (VP)** | Ninguna respuesta HTTP incluye cabecera CSP. Facilita la ejecución de scripts no confiables tras inyecciones XSS. | [`PoC-06-Headers.md`](../04-poc/PoC-06-Headers.md) / [`H-08.md`](../03-hallazgos/H-08.md) |
| **90003** | Sub Resource Integrity Attribute Missing | Medium | **Verdadero Positivo (VP)** | Los recursos CDN (Bootstrap, FontAwesome) en plantillas HTML carecen de `integrity` y `crossorigin`, exponiendo a ataques en cadena de suministro. | [`H-08.md`](../03-hallazgos/H-08.md) |
| **10010** | Cookie No HttpOnly Flag | Low | **Verdadero Positivo con Matiz (VP)** | La cookie `XSRF-TOKEN` se emite deliberadamente sin `HttpOnly` para que Angular/JS la lea; sin embargo, al coexistir con XSS Almacenado, es extraíble por atacantes. | [`PoC-02-XSS.md`](../04-poc/PoC-02-XSS.md) / [`PoC-06-Headers.md`](../04-poc/PoC-06-Headers.md) |
| **10054** | Cookie without SameSite Attribute | Low | **Falso Positivo Parcial (FP)** | `JSESSIONID` sí cuenta con `SameSite=Lax` en Tomcat 10; solo el token `XSRF-TOKEN` omite el atributo explícito en la primera petición GET anónima. | [`PoC-03-CSRF.md`](../04-poc/PoC-03-CSRF.md) |
| **10017** | Cross-Domain JavaScript Source File Inclusion | Low | **Informativo (INFO)** | Inclusión de librerías legítimas de terceros (CDN cdnjs/jsdelivr). No hay control de origen malicioso por el usuario. | N/A |
| **90004** | Cross-Origin-Embedder-Policy (COEP) Missing | Low | **Verdadero Positivo (VP)** | Ausencia de cabecera `Cross-Origin-Embedder-Policy: require-corp`. Falta de endurecimiento contra ataques side-channel (Spectre). | [`PoC-06-Headers.md`](../04-poc/PoC-06-Headers.md) |
| **90004** | Cross-Origin-Opener-Policy (COOP) Missing | Low | **Verdadero Positivo (VP)** | Ausencia de cabecera `Cross-Origin-Opener-Policy: same-origin`. El contexto de navegación puede ser compartido por ventanas emergentes. | [`PoC-06-Headers.md`](../04-poc/PoC-06-Headers.md) |
| **90004** | Cross-Origin-Resource-Policy (CORP) Missing | Low | **Verdadero Positivo (VP)** | Ausencia de cabecera `Cross-Origin-Resource-Policy: same-origin` en hojas de estilo y recursos estáticos. | [`PoC-06-Headers.md`](../04-poc/PoC-06-Headers.md) |
| **10063** | Permissions Policy Header Not Set | Low | **Verdadero Positivo (VP)** | No se restringe el acceso a APIs del navegador (geolocalización, cámara, micrófono) mediante cabecera `Permissions-Policy`. | [`PoC-06-Headers.md`](../04-poc/PoC-06-Headers.md) |
| **10024** | Information Disclosure - Sensitive Information in URL | Informational | **Falso Positivo (FP)** | ZAP inyectó credenciales sintéticas en query string simulando un formulario GET contra `R_agencia_registro.html`, pero la aplicación procesa el registro vía `POST /api/registro`. | N/A |
| **10027** | Information Disclosure - Suspicious Comments | Informational | **Informativo (INFO)** | Comentarios benignos en HTML y JavaScript generados por plantillas de maquetación (e.g. `<!-- slide -->`, autores de CSS). | N/A |
| **10109** | Modern Web Application | Informational | **Informativo (INFO)** | Detección arquitectónica de Single Page App / consumo dinámico mediante `Fetch API`. | N/A |
| **10049** | Non-Storable Content | Informational | **Informativo (INFO)** | Comportamiento defensivo correcto: las vistas autenticadas de administración devuelven directivas para no persistir en caché proxy. | N/A |
| **10112** | Session Management Response Identified | Informational | **Informativo (INFO)** | Telemetría del escáner reconociendo el patrón de cabecera `Set-Cookie: JSESSIONID=...`. | N/A |
| **10049** | Storable and Cacheable Content | Informational | **Informativo (INFO)** | Recursos estáticos CSS e imágenes cacheados por el navegador para optimización de ancho de banda. | N/A |
| **10031** | User Controllable HTML Element Attribute | Informational | **Verdadero Positivo (VP)** | Parámetros de consulta reflejados en el DOM sin sanitizar, confirmando la superficie explotable del vector XSS. | [`PoC-02-XSS.md`](../04-poc/PoC-02-XSS.md) / [`H-04.md`](../03-hallazgos/H-04.md) |

---

## 3. Verificación Manual y Confirmación con Tráfico HTTP Real

A continuación se presentan 3 validaciones manuales independientes con peticiones y respuestas capturadas directamente sobre el entorno local:

---

### Caso 1: Cabeceras Defensivas Faltantes en Respuestas REST (VP Confirmado)
- **Alerta ZAP:** `10038 - Content Security Policy (CSP) Header Not Set` y `90004 - Missing COOP/COEP/CORP`
- **Diagnóstico:** La aplicación responde a las peticiones con datos sensibles sin configurar directivas de aislamiento de contexto ni políticas de restricción de orígenes de scripts.
- **Petición HTTP Real:**
  ```http
  GET /api/obtener_reservas?idUsuario=101 HTTP/1.1
  Host: 127.0.0.1:8080
  Accept: application/json
  Connection: close
  ```
- **Respuesta HTTP Real del Servidor:**
  ```http
  HTTP/1.1 200 OK
  Vary: Origin, Access-Control-Request-Method, Access-Control-Request-Headers
  Set-Cookie: XSRF-TOKEN=c3ad7e8c-a982-411b-8be3-0a9604e9c224; Path=/
  Access-control-allow-headers: Content-Type, Authorization, X-Requested-With
  Access-control-allow-methods: GET, POST, PUT, DELETE, OPTIONS
  Access-control-allow-origin: *
  X-Content-Type-Options: nosniff
  X-XSS-Protection: 0
  X-Frame-Options: SAMEORIGIN
  Content-Type: application/json;charset=UTF-8
  Content-Length: 955

  {"status":"success","reservas":[...]}
  ```
- **Análisis de Evidencia:**
  1. No existe cabecera `Content-Security-Policy`.
  2. No existen cabeceras `Cross-Origin-Opener-Policy`, `Cross-Origin-Embedder-Policy` ni `Permissions-Policy`.
  3. No se define `Cache-Control: no-store` a pesar de que el endpoint devuelve nombres, apellidos y documentos de identidad de pasajeros.
- **PoC Vinculada:** [`docs/seguridad/04-poc/PoC-06-Headers.md`](../04-poc/PoC-06-Headers.md)

---

### Caso 2: Configuración CORS Excesivamente Permisiva con Credenciales (VP Confirmado)
- **Alerta ZAP / Hallazgo:** `H-07 - Configuración Permisiva de CORS con Exposición de Credenciales`
- **Diagnóstico:** `SecurityConfig.java:108-118` refleja dinámicamente cualquier origen enviado en la cabecera `Origin` y simultáneamente activa `.allowCredentials(true)`, permitiendo a aplicaciones web de terceros leer respuestas autenticadas mediante peticiones cross-origin con credenciales de sesión activas.
- **Petición HTTP Real (Preflight OPTIONS):**
  ```http
  OPTIONS /api/auth/me HTTP/1.1
  Host: 127.0.0.1:8080
  Origin: http://sitio-malicioso.local
  Access-Control-Request-Method: GET
  Access-Control-Request-Headers: Authorization, Content-Type
  Connection: close
  ```
- **Respuesta HTTP Real del Servidor:**
  ```http
  HTTP/1.1 200 OK
  Access-Control-Allow-Origin: http://sitio-malicioso.local
  Access-Control-Allow-Credentials: true
  Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
  Access-Control-Allow-Headers: Authorization, Content-Type
  Vary: Origin, Access-Control-Request-Method, Access-Control-Request-Headers
  Content-Length: 0
  ```
- **Análisis de Evidencia:**
  El servidor responde con `Access-Control-Allow-Origin: http://sitio-malicioso.local` y `Access-Control-Allow-Credentials: true`. Esto viola la especificación W3C / MDN y las mejores prácticas de OWASP (A01:2021 - Broken Access Control), posibilitando ataques de filtración de datos de perfil mediante JavaScript malicioso alojado en dominios no autorizados.
- **PoC Vinculada:** [`docs/seguridad/04-poc/PoC-04-CORS.md`](../04-poc/PoC-04-CORS.md)

---

### Caso 3: Emisión de Cookie de Protección `XSRF-TOKEN` sin Flag `HttpOnly` (VP con Impacto Combinado)
- **Alerta ZAP:** `10010 - Cookie No HttpOnly Flag`
- **Diagnóstico:** La directiva CSRF de Spring Security (`CookieCsrfTokenRepository.withHttpOnlyFalse()`) emite el token anti-falsificación en una cookie legible por scripts de cliente. Aunque este mecanismo fue concebido originalmente para facilitar frameworks SPA, su convivencia con vulnerabilidades XSS (PoC-02) anula la protección CSRF en los endpoints que sí tienen activada la validación de token.
- **Petición HTTP Real:**
  ```http
  GET /api/tours?soloPublicados=true HTTP/1.1
  Host: 127.0.0.1:8080
  Accept: application/json
  Connection: close
  ```
- **Respuesta HTTP Real del Servidor:**
  ```http
  HTTP/1.1 200 OK
  Set-Cookie: XSRF-TOKEN=76889e92-b5f6-4a43-9908-9f4972856a5d; Path=/
  Content-Type: application/json;charset=UTF-8
  Connection: close

  {"status":"success","data":[...]}
  ```
- **Análisis de Evidencia:**
  La directiva `Set-Cookie` omite el flag `HttpOnly;` y `SameSite;`. Por consiguiente:
  `document.cookie` expone el valor del token CSRF ante cualquier script inyectado (como el demostrado en `PoC-02-XSS`). El script malicioso puede leer el token y adjuntarlo en cabeceras `X-XSRF-TOKEN` automatizadas para forzar operaciones privilegiadas.
- **PoCs Vinculadas:** [`docs/seguridad/04-poc/PoC-02-XSS.md`](../04-poc/PoC-02-XSS.md) y [`docs/seguridad/04-poc/PoC-03-CSRF.md`](../04-poc/PoC-03-CSRF.md)

