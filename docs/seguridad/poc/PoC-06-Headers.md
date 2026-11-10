# Guía de Prueba de Concepto: PoC-06 — Ausencia de Cabeceras Defensivas HTTP y Cache-Control Desactivado

**Vulnerabilidad:** H-07 — Falta de Cabeceras de Seguridad y Desactivación de Cache-Control  
**CWE:** CWE-693 (Protection Mechanism Failure) / CWE-525 (Use of Web Browser Cache Containing Sensitive Information)  
**OWASP Top 10:2021:** A05:2021 - Security Misconfiguration  
**Severidad:** Media  
**Clasificación:** CONFIRMADO EN CÓDIGO  

---

## 1. Descripción Técnica del Fallo
En `SecurityConfig.java:191-196`, la configuración de cabeceras HTTP de Spring Security desactiva deliberadamente el control de caché mediante `.cacheControl(cache -> cache.disable())`. Esto suprime las cabeceras `Cache-Control: no-cache, no-store, max-age=0, must-revalidate` y `Pragma: no-cache` en respuestas JSON confidenciales, permitiendo que navegadores y proxies intermedios almacenen en caché datos privados de reservas y perfiles de usuarios. Asimismo, la aplicación carece de directivas `Content-Security-Policy` (CSP), `Referrer-Policy` y `Permissions-Policy`.

---

## 2. Precondiciones
1. App local levantada en `http://127.0.0.1:8080`.

---

## 3. Procedimiento de Ejecución Manual (Request Exacta)

### Petición `curl` para inspeccionar cabeceras de respuesta en un endpoint sensible:
```bash
curl -i -s -X GET "http://127.0.0.1:8080/api/obtener_reservas?idUsuario=101" \
     -o /dev/null -D -
```
*(En Windows PowerShell se puede usar: `curl.exe -i -s "http://127.0.0.1:8080/api/obtener_reservas?idUsuario=101" | Select-String -Pattern '^(HTTP|Content|Cache|X-|Pragma)'`)*

### Formato para Burp Suite:
```http
GET /api/obtener_reservas?idUsuario=101 HTTP/1.1
Host: 127.0.0.1:8080
Accept: application/json
Connection: close

```

---

## 4. Respuesta Esperada (Cabeceras Analizadas)
Cabeceras devueltas por el servidor:
```http
HTTP/1.1 200 OK
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
X-Frame-Options: SAMEORIGIN
Content-Type: application/json;charset=UTF-8
```

### Análisis de Deficiencias Observables:
1. **`Cache-Control`:** **TOTALMENTE AUSENTE** (o no restrictivo). No existe `no-store, no-cache, must-revalidate`.
2. **`Pragma`:** **AUSENTE** (`no-cache` omitido).
3. **`Content-Security-Policy`:** **AUSENTE**.
4. **`Referrer-Policy`:** **AUSENTE**.
5. **`Strict-Transport-Security`:** **AUSENTE** *(Nota explicativa: en HTTP plano local es ignorado por navegadores según RFC 6797, pero debe estar contemplado para despliegues TLS)*.

---

## 5. Qué Capturar para el Informe Académico
1. **Terminal / Burp Suite:** Captura de la respuesta HTTP completa mostrando la ausencia de las cabeceras `Cache-Control` y `Content-Security-Policy`.
2. **Editor de Código:** Captura de `SecurityConfig.java` en las líneas 191-196 mostrando `.cacheControl(cache -> cache.disable())`.

---

## 6. Resultado Real (Completar tras la prueba)
*(Espacio reservado para el evaluador)*

```text

```

