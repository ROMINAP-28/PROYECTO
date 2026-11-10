# Análisis Estático de Seguridad de Código Fuente — TRAVELINK

**Proyecto:** TRAVELINK — Plataforma de Gestión y Reservas Turísticas  
**Fecha de Evaluación:** Noviembre 2026  
**Evaluador:** Equipo DevSecOps / Auditoría de Seguridad Académica (UTP Integrador I)  
**Alcance:** Código fuente Java (`Springboot/src`), Recursos y Frontend Web (`Springboot/resources`), Scripts de Base de Datos (`sql/`) y Despliegue (`Dockerfile`, `docker-compose.yml`)  
**Metodología:** OWASP Top 10:2021, ASVS (Application Security Verification Standard) v4.0.3 y CWE/SANS Top 25  

---

## 1. Resumen Ejecutivo del Análisis Estático

El análisis estático de seguridad (SAST) de TRAVELINK se efectuó mediante inspección manual exhaustiva y revisión guiada por flujo de datos del código fuente. Se identificaron **7 hallazgos principales** (H-01 a H-07) más 4 debilidades secundarias en la arquitectura de control de acceso, frontend y manejo de errores.

### Cuadro de Clasificación de Hallazgos

| ID | Vulnerabilidad / Debilidad | Área OWASP 2021 | Archivo(s) y Líneas Clave | Clasificación | Severidad Estimada |
|---|---|---|---|---|---|
| **H-01** | Puerta Trasera (Backdoor) y Comparación Débil en Verificación de Contraseñas | A07:2021 - Identification & Authentication Failures | `Springboot/src/com/travelink/servicio/impl/UsuarioServicioImpl.java` (L119-127) | **CONFIRMADO EN CÓDIGO** | **CRÍTICO** |
| **H-02** | Credencial de Administrador en Texto Plano en Seed Inicial de Base de Datos | A07:2021 - Identification & Authentication Failures | `sql/datos_minimos.sql` (L22-24) | **CONFIRMADO EN CÓDIGO** | **ALTO** |
| **H-03** | Fuga Masiva de Datos e IDOR / BOLA en Consulta y Cancelación de Reservas | A01:2021 - Broken Access Control | `Springboot/src/com/travelink/controlador/turista/ObtenerReservasHandler.java` (L20-35)<br>`Springboot/src/com/travelink/controlador/turista/CancelarReservaHandler.java` (L66-85)<br>`Springboot/src/com/travelink/config/SecurityConfig.java` (L152-153) | **CONFIRMADO EN CÓDIGO** | **CRÍTICO** |
| **H-04** | Configuración CORS Excesivamente Permisiva con Credenciales Habilitadas | A05:2021 - Security Misconfiguration | `Springboot/src/com/travelink/config/SecurityConfig.java` (L267-278) | **CONFIRMADO EN CÓDIGO** | **ALTO** |
| **H-05** | Desactivación Masiva de Protección CSRF en Endpoints Transaccionales | A01:2021 - Broken Access Control | `Springboot/src/com/travelink/config/SecurityConfig.java` (L66-91) | **CONFIRMADO EN CÓDIGO** | **MEDIO** |
| **H-06** | Cross-Site Scripting Almacenado (Stored XSS) en Reseñas y Calificaciones | A03:2021 - Injection | Backend: `Springboot/src/com/travelink/controlador/turista/CalificarHandler.java` (L220-223)<br>Frontend: `Springboot/resources/static/js/turista/detalle.js` (L343-355) | **CONFIRMADO EN CÓDIGO** | **ALTO** |
| **H-07** | Ausencia de Cabeceras Defensivas HTTP y Desactivación Explícita de Cache-Control | A05:2021 - Security Misconfiguration | `Springboot/src/com/travelink/config/SecurityConfig.java` (L191-196) | **CONFIRMADO EN CÓDIGO** | **MEDIO** |
| **H-08** | Escalamiento de Privilegios por Rutas Duales sin Prefijo `/api/admin/` | A01:2021 - Broken Access Control | `Springboot/src/com/travelink/controlador/AdminApiController.java` (L67, 72, 77, 82, 87, 92) | **CONFIRMADO EN CÓDIGO** | **CRÍTICO** |
| **H-09** | Broken Object Level Authorization (BOLA) en Edición/Eliminación de Reseñas | A01:2021 - Broken Access Control | `Springboot/src/com/travelink/controlador/turista/CalificarHandler.java` (L53-126) | **CONFIRMADO EN CÓDIGO** | **ALTO** |
| **H-10** | Inyección de Eventos SSE Anónima no Autorizada | A01:2021 - Broken Access Control | `Springboot/src/com/travelink/controlador/EventosController.java` (L44-50) | **CONFIRMADO EN CÓDIGO** | **ALTO** |
| **H-11** | Exposición de Información Técnica y Stack Traces en Respuestas de Error | A05:2021 - Security Misconfiguration | `UsuariosAdminHandler.java` (L100-101), `CalificarHandler.java` (L87-88, L123-124) | **CONFIRMADO EN CÓDIGO** | **BAJO** |

---

## 2. Análisis Detallado por Área Técnica

---

### 2.1. Control de Acceso e Insecure Direct Object References (IDOR / BOLA)

#### [H-03] IDOR / BOLA en Consulta de Reservas (`/api/obtener_reservas`)
- **Clasificación:** **CONFIRMADO EN CÓDIGO**
- **CWE:** CWE-639 (Authorization Bypass Through User-Controlled Key), CWE-284 (Improper Access Control).
- **Archivos y Líneas:**
  - `Springboot/src/com/travelink/controlador/turista/ObtenerReservasHandler.java`, líneas 20–35.
  - `Springboot/src/com/travelink/config/SecurityConfig.java`, línea 152.
- **Evidencia en Código:**
  ```java
  // ObtenerReservasHandler.java: líneas 20-33
  Map<String, String> queryParams = JavaApiServer.parseQueryParams(exchange.getRequestURI().getQuery());
  String correo = queryParams.getOrDefault("correo", queryParams.getOrDefault("email", ""));
  int idUsuario = 0;
  try { idUsuario = Integer.parseInt(queryParams.getOrDefault("idUsuario", "0")); } catch (Exception ignored) {}

  // Si no vino idUsuario explícito, intentar tomar de la sesión autenticada
  if (idUsuario <= 0) {
      Object sessUserObj = exchange.getAttribute("ID_USUARIO");
      if (sessUserObj instanceof Integer) {
          idUsuario = (Integer) sessUserObj;
      } else if (SecurityUtils.getAuthenticatedIdUsuario().isPresent()) {
          idUsuario = SecurityUtils.getAuthenticatedIdUsuario().get();
      }
  }

  Map<String, Object> res = JavaApiServer.reservaRepositorio.obtenerReservasPorUsuario(correo, idUsuario);
  ```
  ```java
  // SecurityConfig.java: líneas 137-154
  .requestMatchers(
      "/api/auth/**",
      ...
      "/api/obtener_reservas",
      "/api/cancelar_reserva"
  ).permitAll()
  ```
- **Impacto y Vector de Explotación:**
  1. El endpoint se encuentra declarado como público (`permitAll()`), sin exigir sesión iniciada.
  2. Si el cliente suministra el parámetro `idUsuario` o `correo` en la URL (por ejemplo: `GET /api/obtener_reservas?idUsuario=101`), el handler **ignora por completo** la sesión autenticada y consulta directamente la base de datos con el identificador foráneo provisto.
  3. La respuesta serializa el listado completo de reservas, itinerarios, costos totales, datos personales de todos los pasajeros (nombre, apellido paterno, apellido materno, número de documento/DNI, titularidad) e información financiera sensible del método de pago (`marcaTarjeta`, `ultimosDigitos`).
  4. Cualquier atacante no autenticado puede enumerar secuencialmente usuarios (`idUsuario=1, 2, 3...`) extrayendo toda la base de clientes y reservas de la plataforma.

---

#### [H-03b] BOLA / IDOR en Cancelación de Reservas (`/api/cancelar_reserva`)
- **Clasificación:** **CONFIRMADO EN CÓDIGO**
- **CWE:** CWE-285 (Improper Authorization).
- **Archivos y Líneas:**
  - `Springboot/src/com/travelink/controlador/turista/CancelarReservaHandler.java`, líneas 66–85.
  - `Springboot/src/com/travelink/config/SecurityConfig.java`, línea 153 (`permitAll()`).
- **Evidencia en Código:**
  ```java
  // CancelarReservaHandler.java: líneas 66-85
  // Validación estricta de propiedad si hay sesión autenticada
  Integer sessionUserId = null;
  Object sessUserObj = exchange.getAttribute("ID_USUARIO");
  if (sessUserObj instanceof Integer) {
      sessionUserId = (Integer) sessUserObj;
  } else if (SecurityUtils.getAuthenticatedIdUsuario().isPresent()) {
      sessionUserId = SecurityUtils.getAuthenticatedIdUsuario().get();
  }

  if (sessionUserId != null) {
      // Verificar si pertenece al usuario o si es rol agencia/admin
      boolean esAdmin = SecurityUtils.hasRole("ADMIN");
      boolean esAgencia = SecurityUtils.hasRole("AGENCIA");
      if (!esAdmin && !esAgencia && !JavaApiServer.reservaRepositorio.perteneceAUsuario(idReserva, sessionUserId)) {
          JavaApiServer.sendJsonResponse(exchange, 403, "{\"status\":\"error\",\"message\":\"Acceso denegado: esta reserva no pertenece a su usuario\"}");
          return;
      }
  }

  ReservaPersistencia.CancelarResultado resultado = JavaApiServer.reservaRepositorio.cancelarReservaConDetalle(idReserva, motivo);
  ```
- **Impacto y Vector de Explotación:**
  1. Si un atacante envía una petición `POST /api/cancelar_reserva?idReserva=500&motivo=Ataque` **sin incluir cookies de sesión**, la variable `sessionUserId` permanece en `null`.
  2. Como la verificación de pertenencia está anidada dentro del bloque condicional `if (sessionUserId != null)`, la comprobación **se omite completamente**.
  3. El sistema procede a ejecutar `cancelarReservaConDetalle()`, cancelando la reserva de cualquier cliente arbitrario sin requerir autenticación ni autorización.

---

#### [H-08] Escalamiento Vertical de Privilegios por Rutas Alternativas (`/api/usuarios`)
- **Clasificación:** **CONFIRMADO EN CÓDIGO**
- **CWE:** CWE-284 (Improper Access Control).
- **Archivos y Líneas:**
  - `Springboot/src/com/travelink/controlador/AdminApiController.java`, línea 67.
  - `Springboot/src/com/travelink/config/SecurityConfig.java`, líneas 168 y 185.
- **Evidencia en Código:**
  ```java
  // AdminApiController.java: línea 67
  @RequestMapping({"/api/admin/usuarios", "/api/usuarios", "/api/admin/usuario/guardar", "/api/admin/usuario/eliminar", "/api/usuario/guardar", "/api/usuario/eliminar"})
  public void adminUsuarios(HttpServletRequest req, HttpServletResponse res) throws IOException {
      delegate(usuariosHandler, req, res);
  }
  ```
  ```java
  // SecurityConfig.java: líneas 168 y 185
  .requestMatchers("/api/admin/**").hasRole("ADMIN")
  ...
  .anyRequest().authenticated()
  ```
- **Impacto:**
  1. `SecurityConfig` protege exclusivamente el patrón de ruta `/api/admin/**` con el rol `ADMIN`.
  2. Sin embargo, `AdminApiController` mapea rutas alias sin ese prefijo: `/api/usuarios`, `/api/usuario/guardar`, `/api/usuario/eliminar`.
  3. Estas rutas alias caen en la regla genérica `.anyRequest().authenticated()`, que solo requiere una sesión válida sin distinción de rol.
  4. Por ende, un usuario con rol `ROLE_TURISTA` puede enviar una petición `POST /api/usuario/eliminar?idUsuario=1` y desactivar la cuenta del Administrador general o volcar la lista completa de usuarios con `GET /api/usuarios`.

---

### 2.2. Autenticación, Sesiones y Almacenamiento de Contraseñas

#### [H-01] Puerta Trasera (Backdoor) en Comparación de Contraseñas
- **Clasificación:** **CONFIRMADO EN CÓDIGO**
- **CWE:** CWE-547 (Use of Hard-coded Credentials), CWE-305 (Authentication Bypass by Primary Weakness).
- **Archivos y Líneas:**
  - `Springboot/src/com/travelink/servicio/impl/UsuarioServicioImpl.java`, líneas 119–127.
  - `Springboot/src/com/travelink/controlador/UsuarioControlador.java`, líneas 32–37.
- **Evidencia en Código:**
  ```java
  // UsuarioServicioImpl.java: líneas 110-130
  @Override
  public boolean verificarContrasena(String rawPassword, String encodedPassword) {
      if (rawPassword == null || encodedPassword == null) return false;
      String rawTrimmed = rawPassword.trim();
      String encTrimmed = encodedPassword.trim();

      if (encTrimmed.startsWith("$2")) {
          if (passwordEncoder.matches(rawPassword, encTrimmed) || passwordEncoder.matches(rawTrimmed, encTrimmed)) {
              return true;
          }
      } else if (encTrimmed.equals(rawPassword) || encTrimmed.equalsIgnoreCase(rawTrimmed)) {
          return true;
      }

      // Respaldo para entorno de desarrollo y pruebas locales
      if ("123456".equals(rawTrimmed) || "12345678".equals(rawTrimmed) || "pass123".equals(rawTrimmed)
              || "admin".equalsIgnoreCase(rawTrimmed) || "admin123".equalsIgnoreCase(rawTrimmed)) {
          return true;
      }

      return false;
  }
  ```
- **Impacto:**
  1. **Aceptación de texto plano (L119-121):** Si una contraseña en la base de datos no está hasheada con BCrypt (`encTrimmed` no empieza con `$2`), la función realiza una comparación por igualdad de cadenas en texto claro.
  2. **Bypass universal (L124-127):** Si se suministra cualquiera de las contraseñas hardcodeadas (`"123456"`, `"12345678"`, `"pass123"`, `"admin"`, `"admin123"`), la función devuelve `true` incondicionalmente, validando el inicio de sesión para **CUALQUIER usuario de la base de datos**, con independencia de cuál sea su contraseña real.
  3. Esto anula por completo la fortaleza criptográfica de BCrypt y permite la toma de control de cuentas de turistas, agencias y administradores con contraseñas triviales predecibles.

---

#### [H-02] Contraseña de Administrador por Defecto en Texto Plano
- **Clasificación:** **CONFIRMADO EN CÓDIGO**
- **CWE:** CWE-259 (Use of Hard-coded Password), CWE-521 (Weak Password Requirements).
- **Archivos y Líneas:**
  - `sql/datos_minimos.sql`, líneas 22–24.
- **Evidencia en Código:**
  ```sql
  -- sql/datos_minimos.sql: líneas 22-24
  INSERT INTO Usuario (idUsuario, idPersona, idRol, nombreUsuario, contrasena, estado) VALUES
  (1, 1, 3, 'admin', '123456', 'ACTIVO')
  ON DUPLICATE KEY UPDATE idRol = 3, contrasena = VALUES(contrasena), estado = 'ACTIVO';
  ```
- **Impacto:**
  - El script semilla fundamental del proyecto crea el usuario superadministrador con la contraseña `'123456'` en texto plano.
  - Al iniciar la aplicación, la cuenta es inmediatamente vulnerable a inicio de sesión con dicha credencial, combinándose con la debilidad de H-01.

---

#### Mecanismos de Autenticación Analizados Positivamente
- **BCrypt:** Se encuentra correctamente instanciado como bean en `SecurityConfig.java:263` (`new BCryptPasswordEncoder()`).
- **Rehasheo Automático:** En `AuthApiController.java:109-112`, cuando un usuario inicia sesión con contraseña plana o formato antiguo, el sistema genera automáticamente un hash BCrypt y actualiza la base de datos.
- **Protección contra Fuerza Bruta:** Implementada en `LoginAttemptService.java` e integrada en `AuthApiController.java:80-87`. Bloquea peticiones por 15 minutos tras 5 intentos fallidos consecutivos por combinación `IP:login`.
- **Fijación de Sesión:** Protegida mediante `.sessionFixation().migrateSession()` en `SecurityConfig.java:97`.

---

### 2.3. Cross-Site Scripting (XSS) y Validación de Frontend

#### [H-06] Stored XSS en Reseñas y Calificaciones
- **Clasificación:** **CONFIRMADO EN CÓDIGO**
- **CWE:** CWE-79 (Improper Neutralization of Input During Web Page Generation).
- **Archivos y Líneas:**
  - Backend: `Springboot/src/com/travelink/controlador/turista/CalificarHandler.java`, líneas 218–225.
  - Frontend: `Springboot/resources/static/js/turista/detalle.js`, líneas 343–355.
- **Evidencia en Código:**
  ```java
  // CalificarHandler.java: líneas 220-224 (solo escapa caracteres de control JSON, NO entidades HTML)
  json.append("{")
      .append("\"id\":").append(rsL.getInt("idCalificacion")).append(",")
      .append("\"turista\":\"").append(JavaApiServer.jsonEscape(rsL.getString("turista"))).append("\",")
      .append("\"tour\":\"").append(JavaApiServer.jsonEscape(rsL.getString("tour"))).append("\",")
      .append("\"estrellas\":").append(rsL.getInt("estrellas")).append(",")
      .append("\"comentario\":\"").append(JavaApiServer.jsonEscape(rsL.getString("comentario"))).append("\",")
  ```
  ```javascript
  // detalle.js: líneas 343-355 (concatenación e inserción directa en innerHTML)
  contComentarios.innerHTML = lista.map(c => {
      const stars = '★'.repeat(c.estrellas) + '☆'.repeat(Math.max(0, 5 - c.estrellas));
      return `
          <div style="border-bottom: 1px solid #f1f5f9; padding: 12px 0;">
              <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:4px;">
                  <strong style="color:#0b1f38; font-size:14px;"><i class="fa-solid fa-user-circle" style="color:#64748b;"></i> ${c.turista || 'Viajero Travelink'}</strong>
                  <span style="font-size:12px; color:#94a3b8;">${c.fecha || ''}</span>
              </div>
              <div style="color:#f59e0b; font-size:13px; margin-bottom:4px;">${stars}</div>
              <p style="margin:0; color:#475569; font-size:13.5px; line-height:1.5;">${c.comentario || 'Excelente servicio y puntualidad.'}</p>
          </div>
      `;
  }).join('');
  ```
- **Impacto y Vector de Explotación:**
  1. El endpoint `POST /api/calificar` almacena en la tabla `Calificacion` el campo `comentario` sin sanitizar etiquetas HTML (`<script>`, `<img>`, `<iframe>`).
  2. La función `JavaApiServer.jsonEscape()` solo sustituye comillas dobles y saltos de línea para mantener la validez sintáctica de JSON, pero preserva caracteres como `<` y `>`.
  3. Al consultar la vista de detalle del tour (`detalle.html`), `detalle.js` lee la respuesta JSON e inyecta la cadena directamente en `innerHTML`.
  4. Un payload como `<img src=x onerror="alert(document.cookie)">` o un script que robe el `XSRF-TOKEN` o secuestre la sesión se ejecutará en el contexto del navegador de cualquier turista o administrador que inspeccione dicho tour o agencia.

#### [Uso Generalizado de `innerHTML` en el Frontend]
- **Clasificación:** **A VALIDAR DINÁMICAMENTE**
- **Archivos:**
  - `Springboot/resources/static/js/admin/admin.js`, líneas 172, 281, 426, 556.
  - `Springboot/resources/static/js/turista/index.js`, líneas 107, 116.
  - `Springboot/resources/static/js/agencia/paquetes.js`.
- **Observación:** Se detectó el uso recurrente de `innerHTML` interpolando nombres de tours, descripciones y datos de agencias. Aunque la mayoría proviene de la base de datos local, si un atacante con rol de agencia registra un tour con carga útil XSS en su título, el panel del administrador (`admin.js:426`) resultará afectado.

---

### 2.4. Cross-Site Request Forgery (CSRF)

#### [H-05] Exclusiones Masivas de Protección CSRF
- **Clasificación:** **CONFIRMADO EN CÓDIGO**
- **CWE:** CWE-352 (Cross-Site Request Forgery).
- **Archivos y Líneas:**
  - `Springboot/src/com/travelink/config/SecurityConfig.java`, líneas 66–91.
- **Evidencia en Código:**
  ```java
  // SecurityConfig.java: líneas 63-91
  .csrf(csrf -> csrf
      .csrfTokenRepository(tokenRepository)
      .csrfTokenRequestHandler(requestHandler)
      .ignoringRequestMatchers(
          "/api/auth/login",
          "/api/auth/registro",
          "/api/login",
          "/api/registro",
          "/api/agencia/login",
          "/api/agencia/registro",
          "/api/admin/login",
          "/api/solicitudes/**",
          "/api/eventos/**",
          "/api/guardar_reserva",
          "/api/turista/reservas",
          "/api/reservas",
          "/api/reservas/**",
          "/api/obtener_reservas",
          "/api/cancelar_reserva",
          "/api/destinos",
          "/api/destinos/**",
          "/api/tours",
          "/api/tours/**",
          "/api/calificar",
          "/api/calificar/**",
          "/api/admin/**",
          "/api/agencia/**"
      )
  )
  ```
- **Impacto:**
  1. A pesar de que la aplicación configura la emisión del token CSRF mediante `CookieCsrfTokenRepository` (L54) y su propagación con `CsrfCookieFilter` (L281), la cláusula `ignoringRequestMatchers` desactiva la validación en **prácticamente todas las rutas con efectos secundarios del sistema**.
  2. Si un usuario autenticado como administrador o agencia navega por un sitio malicioso mientras tiene su sesión abierta, una página externa puede inducir a su navegador a enviar peticiones automáticas `POST /api/admin/agencias` (para aprobar solicitudes maliciosas) o `POST /api/cancelar_reserva` sin que el token CSRF sea exigido ni verificado.

---

### 2.5. Cross-Origin Resource Sharing (CORS)

#### [H-04] Política CORS Insegura con Orígenes Comodín y Credenciales Habilitadas
- **Clasificación:** **CONFIRMADO EN CÓDIGO**
- **CWE:** CWE-942 (Permissive Cross-domain Policy with Untrusted Domains).
- **Archivos y Líneas:**
  - `Springboot/src/com/travelink/config/SecurityConfig.java`, líneas 267–278.
- **Evidencia en Código:**
  ```java
  // SecurityConfig.java: líneas 267-278
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
      CorsConfiguration configuration = new CorsConfiguration();
      configuration.setAllowedOriginPatterns(List.of("*"));
      configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
      configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "X-XSRF-TOKEN"));
      configuration.setExposedHeaders(Arrays.asList("Authorization", "X-XSRF-TOKEN"));
      configuration.setAllowCredentials(true);

      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      source.registerCorsConfiguration("/**", configuration);
      return source;
  }
  ```
- **Impacto:**
  1. Spring Security resuelve `setAllowedOriginPatterns(List.of("*"))` reflejando de vuelta en la cabecera de respuesta `Access-Control-Allow-Origin` exactamente el valor de la cabecera `Origin` enviada por el navegador del cliente.
  2. Al combinarse con `setAllowCredentials(true)` (`Access-Control-Allow-Credentials: true`), cualquier sitio web malicioso de terceros (por ejemplo: `http://malicious-site.local`) puede formular peticiones `fetch()` con `{ credentials: 'include' }` hacia `http://127.0.0.1:8080/api/auth/me` o `/api/obtener_reservas`.
  3. El navegador del usuario incluirá automáticamente la cookie de sesión `JSESSIONID`, y el navegador permitirá al sitio atacante leer las respuestas JSON que contienen PII y datos de reservas.

---

### 2.6. Cabeceras Defensivas HTTP y Políticas de Caché

#### [H-07] Ausencia de Cabeceras Defensivas y Desactivación de Cache-Control
- **Clasificación:** **CONFIRMADO EN CÓDIGO**
- **CWE:** CWE-693 (Protection Mechanism Failure), CWE-525 (Use of Web Browser Cache Containing Sensitive Information).
- **Archivos y Líneas:**
  - `Springboot/src/com/travelink/config/SecurityConfig.java`, líneas 191–196.
- **Evidencia en Código:**
  ```java
  // SecurityConfig.java: líneas 191-196
  .headers(headers -> headers
      .frameOptions(frame -> frame.sameOrigin())
      .contentTypeOptions(content -> {})
      .xssProtection(xss -> {})
      .cacheControl(cache -> cache.disable())
  );
  ```
- **Análisis de Impacto:**
  1. **Cache-Control Desactivado (L195):** La instrucción explícita `.disable()` suprime los encabezados estándar `Cache-Control: no-cache, no-store, max-age=0, must-revalidate` y `Pragma: no-cache`. En consecuencia, navegadores web y proxies intermedios almacenarán en disco copias en caché de respuestas JSON sensibles que contienen datos personales, documentos de identidad y reservas.
  2. **Bloques Vacíos (L193-194):** `contentTypeOptions(content -> {})` y `xssProtection(xss -> {})` no configuran directivas explícitas adicionales.
  3. **Cabeceras Ausentes:**
     - `Content-Security-Policy` (CSP): Inexistente, lo que agrava sustancialmente el impacto de Stored XSS ([H-06]).
     - `Referrer-Policy`: Inexistente.
     - `Permissions-Policy`: Inexistente.
  4. **Nota sobre HSTS (`Strict-Transport-Security`):** La aplicación no incluye la cabecera HSTS. Es menester enfatizar que **en el entorno de pruebas local sobre HTTP plano (`http://127.0.0.1:8080`), la ausencia de HSTS no constituye una vulnerabilidad operativa en sí misma**, dado que la especificación RFC 6797 exige que los navegadores ignoren HSTS sobre conexiones no cifradas (HTTP). No obstante, se califica como debilidad de configuración para entornos de producción en caso de no desplegarse tras un reverse proxy con terminación TLS.

---

### 2.7. Deserialización, Validación de Entradas e Inyección SQL

#### Evaluación de Inyección SQL (SQLi)
- **Clasificación:** **A VALIDAR DINÁMICAMENTE CON SQLMAP**
- **Análisis de Código:**
  - Se examinaron las clases DAO y Handlers: `ConexionDB`, `TourPersistencia`, `ReservaPersistencia`, `UsuarioPersistencia`, `CalificacionPersistencia`, `AgenciasAdminHandler`, `UsuariosAdminHandler`, `CalificarHandler`, `PaquetesAgenciaHandler`.
  - **Patrón Observado:** Las consultas dinámicas utilizan de manera consistente `java.sql.PreparedStatement` con comodines posicionales (`?`) para todos los parámetros de entrada (`setInt`, `setString`, `setBigDecimal`).
  - No se detectaron patrones de concatenación de cadenas directas (`"SELECT ... WHERE id = " + userInput`) en sentencias ejecutadas con `Statement.executeQuery()`.
  - En la Fase 6 se ejecutará `sqlmap` contra los endpoints parametrizados para verificar dinámicamente que no existan comportamientos anómalos en parámetros numéricos o de ordenamiento.

#### Validación de Entradas y Manejo de Tipos
- **Clasificación:** **CONFIRMADO EN CÓDIGO**
- **Observación:** La mayoría de los handlers capturan genéricamente `try { ... } catch (Exception ignored) {}` al realizar conversiones numéricas (`Integer.parseInt`). Si bien esto evita caídas no controladas, provoca que valores malformados o negativos se interpreten como `0`, desencadenando fallos lógicos en los filtros de base de datos (`? = 0 OR c.idAgencia = ?`).

---

### 2.8. Manejo de Errores y Exposición de Stack Traces

#### [H-11] Fuga de Detalles de Implementación en Mensajes de Error
- **Clasificación:** **CONFIRMADO EN CÓDIGO**
- **CWE:** CWE-209 (Generation of Error Message Containing Sensitive Information).
- **Archivos y Líneas:**
  - `Springboot/src/com/travelink/controlador/admin/UsuariosAdminHandler.java`, líneas 100–101.
  - `Springboot/src/com/travelink/controlador/turista/CalificarHandler.java`, líneas 87–88, 123–124.
  - `Springboot/src/com/travelink/controlador/agencia/PaquetesAgenciaHandler.java`, líneas 274–275.
- **Evidencia en Código:**
  ```java
  // UsuariosAdminHandler.java: líneas 100-101
  } catch (Exception e) {
      e.printStackTrace();
      JavaApiServer.sendJsonResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Error interno: " + JavaApiServer.jsonEscape(e.getMessage()) + "\"}");
      return;
  }
  ```
- **Impacto:**
  - Las excepciones de base de datos (como `SQLException` de claves foráneas, sintaxis o nombres de tablas) se devuelven en el cuerpo JSON al cliente, facilitando el reconocimiento y fingerprinting de la estructura de base de datos a potenciales atacantes.

---

### 2.9. Gestión de Secretos y Configuración de Cookies

#### Gestión de Secretos en Repositorio
- **Clasificación:** **CONFIRMADO EN CÓDIGO**
- **Evaluación:**
  - `application.properties` está configurado correctamente para no almacenar secretos en texto plano, delegando en `${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}`.
  - El archivo `.env` se encuentra efectivamente excluido por `.gitignore` y `.dockerignore`.
  - La única exposición de credencial hardcodeada identificada es la contraseña admin en `sql/datos_minimos.sql` ([H-02]) y las cadenas de bypass en `UsuarioServicioImpl.java` ([H-01]).

#### Configuración de Cookies
- **Clasificación:** **CONFIRMADO EN CÓDIGO**
- **Cookie `JSESSIONID`:**
  - Gestionada por Tomcat embebido. No tiene el atributo `SameSite` forzado a `Strict` en `application.properties`, incrementando la viabilidad de ataques CSRF en navegadores antiguos o configuraciones no predeterminadas.
- **Cookie `XSRF-TOKEN`:**
  - En `SecurityConfig.java:54`, `CookieCsrfTokenRepository.withHttpOnlyFalse()` establece `HttpOnly=false`. Esto es un compromiso arquitectónico común para permitir la lectura del token desde JavaScript (`api.js`), pero significa que cualquier explotación exitosa de XSS ([H-06]) puede leer directamente el token antifalsificación.
