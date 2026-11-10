# Guía de Prueba de Concepto: PoC-04 — Configuración CORS Insegura con Orígenes Arbitrarios y Credenciales

**Vulnerabilidad:** H-04 — CORS Permisivo con `allowedOriginPatterns("*")` y `allowCredentials(true)`  
**CWE:** CWE-942 (Permissive Cross-domain Policy with Untrusted Domains)  
**OWASP Top 10:2021:** A05:2021 - Security Misconfiguration  
**Severidad:** Alta  
**Clasificación:** CONFIRMADO EN CÓDIGO  

---

## 1. Descripción Técnica del Fallo
En `SecurityConfig.java:267-278`, el bean `corsConfigurationSource()` configura la política de recursos de origen cruzado (CORS) estableciendo:
- `configuration.setAllowedOriginPatterns(List.of("*"));`
- `configuration.setAllowCredentials(true);`

De acuerdo con el estándar de Fetch del W3C, cuando `allowCredentials` es `true`, los navegadores no permiten el uso del comodín literal `*`. Sin embargo, el método `setAllowedOriginPatterns("*")` de Spring Framework elude esta restricción reflejando dinámicamente cualquier cabecera `Origin` suministrada por el cliente en la cabecera `Access-Control-Allow-Origin`, acompañada de `Access-Control-Allow-Credentials: true`. Esto permite a cualquier sitio web externo malicioso realizar peticiones autenticadas y leer datos privados de los usuarios.

---

## 2. Precondiciones
1. App local levantada en `http://127.0.0.1:8080`.

---

## 3. Procedimiento de Ejecución Manual (Request Exacta)

### Petición `curl` simulando un origen no confiable (`http://malicious-attacker.local`):
```bash
curl -i -s -X OPTIONS "http://127.0.0.1:8080/api/auth/me" \
     -H "Origin: http://malicious-attacker.local" \
     -H "Access-Control-Request-Method: GET" \
     -H "Access-Control-Request-Headers: Authorization, X-XSRF-TOKEN"
```

### Petición directa GET con origen arbitrario:
```bash
curl -i -s -X GET "http://127.0.0.1:8080/api/destinos" \
     -H "Origin: http://malicious-attacker.local"
```

### Formato para Burp Suite Repeater:
```http
OPTIONS /api/auth/me HTTP/1.1
Host: 127.0.0.1:8080
Origin: http://malicious-attacker.local
Access-Control-Request-Method: GET
Access-Control-Request-Headers: Authorization, X-XSRF-TOKEN
Connection: close

```

---

## 4. Respuesta Esperada
- **Código de Estado:** `200 OK` (o `204 No Content`)
- **Cabeceras de Respuesta Clave:**
  ```http
  Access-Control-Allow-Origin: http://malicious-attacker.local
  Access-Control-Allow-Credentials: true
  Access-Control-Allow-Methods: GET,POST,PUT,DELETE,OPTIONS,PATCH
  Vary: Origin
  Vary: Access-Control-Request-Method
  Vary: Access-Control-Request-Headers
  ```
- **Análisis de Seguridad:** El servidor refleja incondicionalmente el origen hostil `http://malicious-attacker.local` y confirma `Access-Control-Allow-Credentials: true`. Esto comprueba que el navegador entregará las respuestas con cookies de sesión a scripts que se ejecuten bajo dicho origen externo.

---

## 5. Qué Capturar para el Informe Académico
1. **Burp Suite / Terminal:** Captura de las cabeceras HTTP de respuesta donde se aprecie claramente la combinación peligrosa de:
   - `Access-Control-Allow-Origin: http://malicious-attacker.local`
   - `Access-Control-Allow-Credentials: true`

---

## 6. Resultado Real (Completar tras la prueba)
*(Espacio reservado para el evaluador)*

```text

```

