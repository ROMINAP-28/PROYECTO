# TRAVELINK 🌍✈️
> Plataforma Integral de Gestión Turística, Catálogo de Tours, Reservas y Liquidación de Comisiones.

TRAVELINK es una solución web empresarial desarrollada en **Java 21 LTS** y **Spring Boot 3.3.13**, orientada al sector turístico formal del Perú. Integra a tres actores principales:
1. **Turistas:** Exploración de destinos y paquetes, reserva interactiva en línea, cálculo automático de tarifas por pasajero (adultos, niños, infantes), generación de comprobantes en PDF y calificaciones con reseñas.
2. **Agencias de Viaje:** Registro formal validado con RUC, publicación de paquetes turísticos, programación de cupos y fechas de salida, carga masiva de disponibilidad vía Excel/CSV y liquidación de ventas.
3. **Administradores:** Aprobación de agencias solicitantes, auditoría operativa, liquidación de comisiones de plataforma (15%), moderación de calidad y monitoreo de métricas en dashboard.

---

## 🛠️ Stack Tecnológico

| Capa / Componente | Tecnologías y Versiones |
|---|---|
| **Lenguaje y Runtime** | Java 21 (Eclipse Temurin LTS) |
| **Framework Backend** | Spring Boot 3.3.13 (actualizado desde línea base 3.3.4; CVEs auditados con `.trivyignore`) |
| **Seguridad de Aplicación** | Spring Security 6.3 (BCrypt, CSRF con tokens `XSRF-TOKEN`, RBAC por rol, bloqueo tras 5 intentos fallidos) |
| **Persistencia Principal** | Spring Data JPA / Hibernate 6 (CRUD transaccional e integridad referencial) |
| **Persistencia Especializada** | JDBC Nativo con `PreparedStatement` (Consultas analíticas, reportes y dashboards) |
| **Motor de Base de Datos** | MySQL 8.0 (Instancia local Dockerizada o Aiven Cloud) con HikariCP |
| **Reportes y Streaming** | OpenPDF 1.3.39 (Comprobantes PDF) y Apache POI 5.2.5 con `SXSSFWorkbook` (Excel streaming) |
| **Frontend Desacoplado** | HTML5 semántico (34 vistas), CSS3 modular y Vanilla JavaScript moderno (Fetch API asíncrona) |
| **Monitoreo y Salud** | Spring Boot Actuator (`/actuator/health` y `/actuator/info`) |
| **Infraestructura Perimetral** | Nginx Reverse Proxy (Terminación TLS, HSTS, CSP, cookies `Secure; SameSite=Lax`) |
| **Pruebas Automatizadas** | JUnit 5, Mockito, JaCoCo (cobertura), Testcontainers (MySQL 8.0), Apache JMeter y OWASP ZAP |

---

## 📁 Estructura del Repositorio

La organización del proyecto sigue una separación estricta de responsabilidades entre código de producción, documentación técnica, pruebas y despliegue:

```text
PROYECTO/
├── README.md                          # Presentación general, instalación y accesos
├── pom.xml                            # Configuración Maven, dependencias, perfiles (dev, staging, prod)
├── Dockerfile                         # Build multi-stage (Maven Temurin 21 + JRE jammy ligero)
├── docker-compose.yml                 # Orquestación de desarrollo local (travelink-app + travelink-db)
├── .env.example                       # Plantilla de variables de entorno requeridas
├── .trivyignore                       # 9 excepciones justificadas de vulnerabilidades críticas
├── .github/workflows/
│   ├── ci.yml                         # Pipeline CI: Compilación, Tests, SCA Trivy y DAST ZAP
│   └── deploy.yml                     # Pipeline CD: Build, GHCR Docker Publish, Staging Smoke Test y Prod Gate
├── Springboot/
│   ├── src/com/travelink/             # Código fuente Java por capas (controlador, servicio, modelo, repositorio, dao)
│   ├── resources/                     # Configuración application.properties, logback.xml y vistas static (html, css, js)
│   └── test/com/travelink/            # Suite de pruebas unitarias (20 casos) e integración (Testcontainers)
├── sql/
│   ├── travelink_schema.sql           # DDL de las 18 tablas relacionales del sistema
│   ├── datos_minimos.sql              # Roles del sistema y administrador inicial
│   └── seed_pruebas.sql               # Datos sintéticos para pruebas locales (usuarios, tours, reservas)
├── docs/                              # SOLO documentación técnica (.md) e imágenes
│   ├── INVENTARIO_DOCUMENTACION.md    # Inventario maestro y catálogo de todos los activos del proyecto
│   ├── arquitectura/                  # Arquitectura MVC, capas, dualidad JPA/JDBC y catálogo pom.xml
│   ├── ingenieria/                    # Especificaciones de ingeniería de software
│   │   ├── procesos/                  # Procesos de negocio y diagramas de flujo/secuencia Mermaid
│   │   ├── diseno/                    # Patrones (DTO, DAO/Repository, MVC), SOLID y diagramas de clases
│   │   ├── base-datos/                # Modelo relacional, ERD lógico-físico y diccionario de datos de 18 tablas
│   │   ├── ux-ui/                     # Arquitectura frontend, guía de estilos y prototipos de 34 vistas
│   │   ├── reportes/                  # Modelo de reportes PDF/Excel, streaming SXSSFWorkbook y formatos
│   │   └── codigo-fuente/             # Estructura de paquetes, dependencias, perfiles y logback
│   ├── seguridad/                     # Auditoría de seguridad del software
│   │   ├── 01-plan/                   # Plan maestro de auditoría, alcance, cronograma S1-S6 y RACI
│   │   ├── 02-analisis/               # SAST, seguridad Java, mapa de endpoints, tabla CVE y falsos positivos ZAP
│   │   ├── 03-hallazgos/              # Fichas técnicas de hallazgos H-01 a H-09 con severidades CVSS
│   │   ├── 04-poc/                    # Guías de explotación técnica y pruebas de concepto reproducibles
│   │   └── 05-informe/                # Informe ejecutivo final, rúbrica (105 pts) y cuentas de prueba
│   ├── pruebas-software/              # Plan maestro de pruebas de software, 20 unitarias, JMeter y JaCoCo
│   ├── control-versiones/             # Modelo de ramas, trazabilidad de PRs (#1, #3, #4, #6), grafo y rollback
│   ├── despliegue/                    # Plan maestro de despliegue Sesión 15 (JAR vs WAR, Nginx TLS, tuning JVM)
│   └── exposicion/                    # Guion de exposición (14 diapositivas) y guion de demo en vivo vinculando código
├── pruebas/                           # Scripts, configuración, wordlists y evidencias de pruebas
│   ├── entorno/                       # Scripts de reinicio controlado (reset-db) y siembra sintética (seed)
│   ├── seguridad/                     # Scripts de escaneo DAST, SAST, SCA, fuzzing y SQLMap
│   ├── rendimiento/                   # Scripts de pruebas de carga Apache JMeter (.jmx)
│   └── evidencias/                    # Reportes de auditoría generados (Trivy, ZAP, JMeter, FFUF, SQLMap)
└── despliegue/                        # Infraestructura ejecutable de servidor y producción
    ├── docker/                        # Orquestación multicontenedor prod (docker-compose.prod.yml con Nginx TLS)
    ├── servidor/                      # travelink.service (systemd), setup-servidor.sh y travelink.conf (Nginx)
    ├── maven/                         # settings.xml.example y scripts de empaquetado con checksum SHA-256
    ├── scripts/                       # Scripts ejecutables de deploy y rollback por tag
    └── evidencias/                    # Evidencias de empaquetado, compose ps, cabeceras TLS, healthcheck y rollback
```

---

## 🌐 Guía de Accesos y Redes (`localhost` vs `app`)

> [!IMPORTANT]
> **Aclaración Crítica de Acceso según el Origen de Ejecución:**
> - **Desde el Navegador Web o Consola del Anfitrión (Host):** Se accede exclusivamente mediante **`http://localhost:8080`** (o `http://127.0.0.1:8080`) en desarrollo, o **`https://localhost`** en el entorno de producción con Nginx TLS. La aplicación base sirve HTTP plano nativamente; el protocolo `https://` en el puerto 8080 no responderá hasta que intervenga el proxy Nginx con certificados.
> - **Dentro de la Red Docker (`travelink-net` / `travelink-prod-net`):** El host **`http://app:8080`** es un alias de resolución DNS interno exclusivo para contenedores (OWASP ZAP, SQLMap, Apache JMeter o CI runner). Si se ingresa `http://app:8080` directamente en la barra de direcciones del navegador del anfitrión, el sistema arrojará el error **`DNS_PROBE_FINISHED_NXDOMAIN`**.

| Entorno / Modo | URL de Acceso en Navegador | Protocolo | Puerto Expuesto | Propósito |
|---|---|:---:|:---:|---|
| **Desarrollo Local** | `http://localhost:8080` | HTTP | `8080` (Local) | Navegación de usuarios, pruebas manuales y depuración |
| **Producción Hardened** | `https://localhost` | HTTPS (TLS 1.2/1.3) | `443` (Nginx) | Tráfico público cifrado con cabeceras HSTS y CSP |
| **Monitoreo de Salud** | `https://localhost/actuator/health` | HTTPS | `443` | Healthcheck y sondas liveness/readiness |
| **Red Interna Docker** | `http://app:8080` | HTTP | Red interna | Escaneos automatizados ZAP, JMeter y runner CI |

---

## 🚀 Cómo Ejecutar la Aplicación

### Opción 1: Ejecución Local Directa con Maven Wrapper (Recomendado para Desarrollo)
1. **Configurar Variables de Entorno:**
   ```powershell
   # En Windows PowerShell:
   Copy-Item .env.example .env
   # En Linux / macOS:
   cp .env.example .env
   ```
   Ajuste las credenciales de MySQL en el archivo `.env`.

2. **Compilar y Ejecutar:**
   ```powershell
   # En Windows:
   .\mvnw.cmd spring-boot:run
   # En Linux / macOS:
   ./mvnw spring-boot:run
   ```
   La aplicación estará disponible en: **`http://localhost:8080`**.

---

### Opción 2: Ejecución Local Contenedorizada con Docker Compose
Aprovisiona automáticamente la base de datos MySQL 8.0 y la aplicación Spring Boot en contenedores enlazados:
```bash
docker compose up -d --build
```
- Base de datos MySQL inicializada secuencialmente con esquema y datos mínimos en `3307/tcp`.
- Aplicación disponible en: **`http://localhost:8080`**.
- Verificación del estado de salud:
  ```bash
  docker compose ps
  ```

---

### Opción 3: Entorno de Producción con Nginx Reverse Proxy y TLS Hardening
Aprovisiona el clúster seguro de 3 niveles (`nginx` perimetral con TLS + `app` en red privada con G1GC tuning + `db` MySQL aislada):
```bash
docker compose --env-file .env -f despliegue/docker/docker-compose.prod.yml up -d --build
```
- Aplicación accesible en: **`https://localhost`** (acepte el certificado autofirmado en desarrollo).
- Verificación de cabeceras de seguridad perimetral:
  ```bash
  curl -k -I https://localhost/api/destinos
  ```
- Verificación del healthcheck de Actuator:
  ```bash
  curl -k -i https://localhost/actuator/health
  ```

---

### Opción 4: Despliegue en la Nube con Render (Docker Blueprint)
El repositorio incluye el archivo de infraestructura como código [`render.yaml`](render.yaml) para desplegar el contenedor automáticamente en Render:
1. Conecta el repositorio de GitHub en [Render Dashboard](https://dashboard.render.com/).
2. Selecciona **Blueprints** ➡️ **New Blueprint Instance** y elige este repositorio.
3. Render detectará automáticamente `render.yaml` y construirá la imagen con el `Dockerfile`.
4. Configura las variables de base de datos (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) en el panel de Render apuntando a tu instancia MySQL (ej. Aiven Cloud).
5. La aplicación iniciará con HTTPS público automático y verificación de salud en `/actuator/health`.

---

```bash
# Ejecutar la suite completa de 20 pruebas unitarias con reporte de cobertura JaCoCo:
./mvnw -B clean test

# Empaquetar el artefacto de producción (Fat JAR con Tomcat embebido):
./mvnw -B clean package -DskipTests
```

---

## 📚 Catálogo Central de Documentación

Consulte los índices detallados de cada área del proyecto:
- 📖 [**Inventario Maestro Consolidado**](docs/INVENTARIO_DOCUMENTACION.md): Catálogo exhaustivo de todos los documentos y evidencias de talleres 1 a 12 y despliegue.
- 🏛️ [**Arquitectura del Sistema**](docs/arquitectura/ARQUITECTURA.md): Modelo MVC, capas, dualidad JPA/JDBC y catálogo pom.xml.
- ⚙️ [**Ingeniería de Software**](docs/ingenieria/README.md): Procesos, diseño orientado a objetos, base de datos de 18 tablas, prototipos UX/UI y reportes.
- 🛡️ [**Auditoría de Seguridad**](docs/seguridad/README.md): Plan, análisis SAST/SCA, 9 hallazgos H-01 a H-09, PoCs e informe final.
- 🧪 [**Pruebas de Software**](docs/pruebas-software/PLAN_PRUEBAS_SOFTWARE.md): Pirámide de pruebas, 20 unitarias, Testcontainers y JaCoCo.
- 🌿 [**Control de Versiones**](docs/control-versiones/CONTROL_VERSIONES.md): Modelo de ramas, trazabilidad de PRs, grafo y rollback.
- 🚀 [**Plan Maestro de Despliegue**](docs/despliegue/PLAN_DESPLIEGUE.md): Sesión 15, empaquetado JAR, Nginx TLS, Docker prod, JVM tuning y rollback.
- 🎤 [**Guion de Exposición**](docs/exposicion/GUION_EXPOSICION.md): 14 diapositivas y guion de demostración en vivo vinculando código real.
