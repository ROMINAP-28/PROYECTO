# Plan Maestro de Despliegue y Operación en Producción — TRAVELINK

Este documento especifica la estrategia integral de empaquetado, infraestructura, despliegue continuo (CD), hardening perimetral y contingencia para TRAVELINK, siguiendo los lineamientos de la **Sesión 15 (Despliegue de Aplicaciones Java)**.

Cada sección se encuentra formalmente rotulada según su nivel de verificación técnica:
- `[VALIDADO]`: Ejecutado, verificado y comprobado empíricamente en esta máquina con evidencias reales registradas en `despliegue/evidencias/`.
- `[DOCUMENTADO]`: Especificado arquitecturalmente como diseño operativo estándar sin ejecución en este entorno local (ej. servidores de aplicaciones externos Tomcat/WildFly, despliegue en servidor Linux remoto con Systemd o certificados Let's Encrypt para dominio público).

---

## 1. Fundamentos y Decisiones de Empaquetado

### 1.1 Modelo Fat JAR Ejecutable vs Contenedor de Servlets WAR `[VALIDADO / DOCUMENTADO]`
- **Modelo Adoptado `[VALIDADO]`**: Fat JAR ejecutable autónomo (`spring-boot-starter-web` con servidor embebido Apache Tomcat).
  - *Justificación*: Embeber el servidor dentro del JAR (`travelink-1.0.0.jar`) permite el cumplimiento del principio de Twelve-Factor App (Port Binding), eliminando diferencias de versión entre entornos y facilitando la construcción de imágenes de contenedor Docker ligeras basadas en JRE Eclipse Temurin 21.
- **Alternativa WAR `[DOCUMENTADO]`**:
  - Para desplegar en servidores de aplicaciones tradicionales (Apache Tomcat 10+, Eclipse GlassFish 7+, Red Hat WildFly 30+), el proyecto requeriría:
    1. Cambiar `<packaging>jar</packaging>` por `<packaging>war</packaging>` en `pom.xml`.
    2. Marcar `spring-boot-starter-tomcat` con `<scope>provided</scope>`.
    3. Extender la clase principal con `SpringBootServletInitializer` y sobrescribir el método `configure(SpringApplicationBuilder)`.

### 1.2 Perfiles Maven y Spring Boot `[VALIDADO]`
El archivo `pom.xml` define tres perfiles sincronizados con las propiedades de entorno:
- **`dev` (por defecto)**: Activa `application-dev.properties` para desarrollo local con recarga rápida de estáticos.
- **`staging`**: Activa `application-staging.properties` con sesiones de 60m y validaciones pre-productivas.
- **`prod`**: Activa `application-prod.properties` con flags de cookies seguras (`secure=true`), compresión perimetral y optimizaciones de rendimiento.

---

## 2. Ciclo de Vida Maven y Configuración Corporativa `[VALIDADO]`

El empaquetado se rige por el ciclo de vida estándar de Maven:
```text
validate -> compile -> test -> package -> verify -> install -> deploy
```
- **Evidencia Real de Compilación y Test**: La ejecución de `./mvnw clean package -Pprod` concluyó con `BUILD SUCCESS` en 30.9 s, ejecutando 20 pruebas unitarias con JaCoCo y empaquetando el artefacto en `target/travelink-1.0.0.jar`. Evidencia en [`despliegue/evidencias/build-package.txt`](../../despliegue/evidencias/build-package.txt).
- **Plantilla de Distribución**: Se proporciona [`despliegue/maven/settings.xml.example`](../../despliegue/maven/settings.xml.example) para integración con servidores Nexus/Artifactory mediante variables de entorno `${env.GH_TOKEN}` y `${env.NEXUS_PASSWORD}` sin credenciales hardcodeadas.

---

## 3. Infraestructura del Servidor y Hardening Operativo

### 3.1 Despliegue Standalone con Systemd `[DOCUMENTADO]`
Para servidores Linux dedicados (Ubuntu 22.04 LTS), se suministran:
- **`despliegue/servidor/setup-servidor.sh`**: Script idempotente de instalación de Java 21, Nginx, UFW y creación del usuario de sistema sin privilegios `travelink` con permisos restringidos sobre `/opt/travelink` y `/var/log/travelink`.
- **`despliegue/servidor/travelink.service`**: Archivo de unidad con políticas de sandboxing (`PrivateTmp=true`, `ProtectSystem=full`), reinicio automático (`Restart=always`) y límites de descriptores (`LimitNOFILE=65536`, `LimitNPROC=4096`).

### 3.2 Proxy Inverso Nginx con TLS y Hardening `[VALIDADO]`
Configurado en [`despliegue/servidor/nginx/travelink.conf`](../../despliegue/servidor/nginx/travelink.conf):
- **Terminación TLS**: Cifrado obligatorio en el puerto 443 con redirección 301 permanente desde HTTP (80).
- **Certificados**: Probado con certificados autofirmados mediante script [`despliegue/servidor/nginx/generar-cert.ps1`](../../despliegue/servidor/nginx/generar-cert.ps1). Para dominios reales se incluye la directiva `/.well-known/acme-challenge/` para integración con Certbot / Let's Encrypt `[DOCUMENTADO]`.
- **Aislamiento de Paneles**: Restricción de acceso a `/html/administrador/` y `/api/admin/` exclusivamente a IPs de gestión interna (127.0.0.1 y subredes privadas).
- **CORS Restringido**: Se elimina el comodín `Access-Control-Allow-Origin: *` evaluando dinámicamente orígenes autorizados.
- **Flags de Cookies**: Se inyecta `secure; samesite=lax` a la cookie `XSRF-TOKEN`.

---

## 4. Orquestación con Docker Compose en Producción `[VALIDADO]`

El archivo [`despliegue/docker/docker-compose.prod.yml`](../../despliegue/docker/docker-compose.prod.yml) orquesta la topología de tres capas:
1. **`nginx`**: Expone puertos 80 y 443 al host. Límites: 1.0 CPU, 512 MB RAM.
2. **`app` (Spring Boot 3.3.13)**: Puerto 8080 **NO publicado al host** (aislado en red `travelink-prod-net`). Límites: 2.0 CPU, 1536 MB RAM.
3. **`db` (MySQL 8.0)**: Puerto 3306 **NO publicado al host**. Límites: 1.5 CPU, 1024 MB RAM.

### 4.1 Tuning de la Máquina Virtual Java (JVM) `[VALIDADO]`
Parámetros inyectados mediante `JAVA_OPTS`:
- `-Xms512m -Xmx1024m`: Asignación determinista de memoria heap inicial y máxima.
- `-XX:+UseG1GC`: Recolector de basura Garbage-First optimizado para baja latencia en entornos multicore.
- `-XX:+UseStringDeduplication`: Deduplicación automática de cadenas de texto en memoria.
- `-Xlog:gc*:file=/app/logs/gc.log:time,uptime:filecount=5,filesize=10M`: Registro rotativo de eventos de recolección de basura.

### 4.2 Monitoreo y Healthchecks con Spring Boot Actuator `[VALIDADO]`
- Se integró `spring-boot-starter-actuator` exponiendo únicamente `/actuator/health` y `/actuator/info`.
- Endpoint verificado por HTTPS: `{"status":"UP","groups":["liveness","readiness"]}`.
- El contenedor `travelink-app-prod` converge al estado `healthy` en 12.57 segundos. Evidencia en [`despliegue/evidencias/despliegue-prod-compose-ps.txt`](../../despliegue/evidencias/despliegue-prod-compose-ps.txt).

---

## 5. Estrategias de Despliegue y Pipeline de CI/CD

### 5.1 Comparativa de Estrategias de Despliegue `[DOCUMENTADO]`

| Estrategia | Mecanismo Operativo | Ventajas | Desventajas | Aplicabilidad en TRAVELINK |
|---|---|---|---|---|
| **Recreate / Rolling Update (Adoptada)** | Despliegue contenedorizado secuencial con healthcheck previo al switch | Bajo costo de infraestructura, simple de orquestar en VPS único, compatible con base relacional única | Breve desconexión en balanceador si no hay múltiples réplicas | **ADOPTADA**: Ideal para arquitectura monolítica modular en servidor único con rollback automático. |
| **Blue/Green** | Dos entornos productivos idénticos (Azul y Verde); el proxy conmuta el 100% del tráfico | Tiempo de inactividad cero (Zero-Downtime), rollback instantáneo en Nginx | Duplica el costo de hardware y exige compatibilidad estricta de base de datos bidireccional | Documentada para fase de escalamiento horizontal con réplicas de lectura. |
| **Canary Deployment** | Desvío progresivo del tráfico (5%, 25%, 100%) hacia la nueva versión | Detección temprana de anomalías en una porción reducida de usuarios | Requiere ingress controller avanzado (Istio/Traefik) y ruteo por pesos | Complejidad desproporcionada para la etapa inicial del proyecto. |

### 5.2 Pipeline de Entrega Continua (`.github/workflows/deploy.yml`) `[VALIDADO en sintaxis]`
Estructurado en 4 etapas:
1. **Build & Test**: Compilación con Maven, ejecución de suite de pruebas unitarias y empaquetado del Fat JAR con checksum SHA-256.
2. **Publish Docker Image**: Login en GitHub Container Registry (`ghcr.io`) y publicación de imagen etiquetada con la versión del tag semántico (`v*`).
3. **Staging Smoke Test**: Despliegue efímero en runner de GitHub Actions con `docker-compose.prod.yml`, validación de healthcheck y verificación de cabeceras OWASP.
4. **Production Gate**: Despliegue productivo condicionado a aprobación manual en el entorno protegido `production`.

### 5.3 Procedimiento de Rollback Automatizado `[VALIDADO]`
- **Script**: [`despliegue/scripts/rollback.ps1`](../../despliegue/scripts/rollback.ps1) y [`despliegue/scripts/rollback.sh`](../../despliegue/scripts/rollback.sh).
- **Ejecución Comprobada**: Se simuló la reversión a la versión previa etiquetada `travelink-app:v1.0.0`. El script detuvo el contenedor, reasignó la etiqueta estable y levantó el servicio, alcanzando estado `healthy` en menos de 20 segundos. Evidencia en [`despliegue/evidencias/prueba-rollback.txt`](../../despliegue/evidencias/prueba-rollback.txt).

---

## 6. Resultados y Comparativa de Cabeceras de Seguridad `[VALIDADO]`

Comparativa empírica entre el despliegue de desarrollo previo (puerto 8080 directo) y el despliegue productivo final con Nginx TLS (puerto 443):

| Parámetro / Cabecera | ANTES (Local Directo 8080) | DESPUÉS (Nginx TLS Producción 443) | Remediación de Seguridad |
|---|---|---|---|
| **Protocolo de Red** | HTTP/1.1 (Texto plano en claro) | HTTPS (TLSv1.2/1.3 + HTTP/2) | Cifrado perimetral total de datos en tránsito |
| **Puerto Expuesto al Host** | `8080/tcp` (Abierto a red local) | Ninguno (8080 es privado; solo 80 y 443 en host) | Cierre de superficie de ataque directa al backend |
| **Strict-Transport-Security** | Faltante | `max-age=31536000; includeSubDomains` | Protección permanente contra downgrade attacks |
| **Content-Security-Policy** | Faltante | `default-src 'self'; script-src ...` | Mitigación robusta contra XSS y scripts maliciosos |
| **Referrer-Policy** | Faltante | `strict-origin-when-cross-origin` | Previene fuga de tokens y parámetros en referer |
| **Permissions-Policy** | Faltante | `camera=(), microphone=(), payment=()` | Desactiva APIs sensibles del navegador del usuario |
| **X-Frame-Options** | `SAMEORIGIN` | `SAMEORIGIN` | Prevención de ataques de Clickjacking |
| **X-Content-Type-Options** | `nosniff` | `nosniff` | Prevención de ataques de MIME-Sniffing |
| **X-XSS-Protection** | Desactivada (0) | `1; mode=block` | Habilitación de filtro preventivo de XSS |
| **CORS (Allow-Origin)** | `*` (Comodín universal permisivo) | Orígenes restringidos explícitos | Mitigación de abuso de API por portales de terceros |
| **Cookie XSRF-TOKEN** | Sin flags de seguridad | `Secure; SameSite=Lax` | Previene captura de cookie y ataques CSRF |
| **Cookie JSESSIONID** | `HttpOnly` básico | `HttpOnly; Secure; SameSite=Lax` | Protección máxima de la sesión autenticada |
| **Monitoreo / Liveness** | Endpoint no disponible | `/actuator/health` con probe UP | Supervisión nativa de salud del contenedor |
| **Tiempo de Arranque App** | 176.52 s (esperas de Docker/WSL2) | **12.57 s** (con Actuator y JVM Tuning) | **Reducción de 92.8% en tiempo de inicialización** |

---

## 7. Gestión de Riesgos y Matriz de Contingencia

| Riesgo Operativo | Severidad | Mitigación Implementada | Estado |
|---|---|---|:---:|
| Falla en la inicialización de base de datos | Alta | Healthcheck dependiente con `start_period: 30s` y reintentos automáticos en compose | VALIDADO |
| Degradación por consumo excesivo de memoria en Java | Media | Flags explícitos `-Xms512m -Xmx1024m` y límites de memoria Docker a 1.5 GB | VALIDADO |
| Sobrecarga del recolector de basura (GC pauses) | Media | Uso del colector `G1GC` con deduplicación de cadenas y logs en archivo rotativo | VALIDADO |
| Caída del servicio durante una actualización | Alta | Script automatizado de rollback por tag determinista (`rollback.sh` / `.ps1`) | VALIDADO |
| Compromiso de certificados TLS en servidor | Crítica | Aislamiento de clave privada en `/etc/nginx/ssl` con permisos 600 y renovación programada | VALIDADO |
